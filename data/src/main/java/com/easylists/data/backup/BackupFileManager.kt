package com.easylists.data.backup

import android.content.ContentResolver
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.provider.DocumentsContract
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import java.io.BufferedInputStream
import java.io.BufferedOutputStream
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileNotFoundException
import java.io.FileOutputStream
import java.io.IOException
import java.io.InputStream
import java.io.OutputStream
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.Locale
import java.util.zip.Deflater
import java.util.zip.ZipEntry
import java.util.zip.ZipInputStream
import java.util.zip.ZipOutputStream
import javax.inject.Inject
import javax.inject.Singleton

// Thrown when the chosen backup folder isn't usable: no folder set, permission lost, or the folder is gone.
// The UI should respond by asking the user to pick a folder again.
class BackupDestinationException(message: String) : Exception(message)

data class CreatedBackup(
    val uri: Uri,
    val name: String,
    val summary: BackupSummary,
    val missingPhotos: Int = 0,
)

data class BackupEntry(
    val uri: Uri,
    val name: String,
    val lastModified: Long,
    val size: Long,
)

/**
 * Everything that touches files: the backup folder the user picked, the zip, and the photos.
 *
 * Zip layout:
 *   backup.json          the BackupFile (first entry, so it can be read without scanning the whole zip)
 *   photos/<file name>   one entry per photo, only when the Photos section is included
 *
 * Failures are reported as exceptions: [BackupDestinationException] when the folder can't be used,
 * [BackupFormatException] for a file that can't be restored, and IOException for everything else.
 */
@Singleton
class BackupFileManager @Inject constructor(
    @ApplicationContext private val context: Context,
    private val backupDataSource: BackupDataSource,
    private val destinationStore: BackupDestinationStore,
) {

    private val resolver: ContentResolver get() = context.contentResolver

    // Only one backup or restore at a time
    private val mutex = Mutex()

    //region Backup folder

    // The folder the user picked, or null when none is set or the permission to use it is gone.
    suspend fun destination(): Uri? {
        val uri = destinationStore.get() ?: return null
        val hasPermission = resolver.persistedUriPermissions.any {
            it.uri == uri && it.isReadPermission && it.isWritePermission
        }
        return if (hasPermission) uri else null
    }

    // Call with the URI returned by the folder picker (ActivityResultContracts.OpenDocumentTree).
    suspend fun setDestination(treeUri: Uri) {
        val flags = Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_GRANT_WRITE_URI_PERMISSION
        resolver.takePersistableUriPermission(treeUri, flags)

        // Android limits how many persisted permissions an app can hold, so give up the old folder's
        val previous = destinationStore.get()
        if (previous != null && previous != treeUri) {
            runCatching { resolver.releasePersistableUriPermission(previous, flags) }
        }
        destinationStore.set(treeUri)
    }

    //endregion

    //region Create

    suspend fun createBackup(sections: Set<BackupSection>): CreatedBackup =
        withContext(Dispatchers.IO) {
            mutex.withLock {
                val tree = destination()
                    ?: throw BackupDestinationException("No backup folder is set, or access to it was lost.")

                // Export first, so a failure here doesn't leave an empty file in the user's folder
                val backup = backupDataSource.export(sections)

                val name = FILE_PREFIX + LocalDateTime.now().format(NAME_FORMAT) + ".zip"
                val folder = DocumentsContract.buildDocumentUriUsingTree(
                    tree,
                    DocumentsContract.getTreeDocumentId(tree)
                )
                val created: Uri? = try {
                    DocumentsContract.createDocument(resolver, folder, "application/zip", name)
                } catch (e: SecurityException) {
                    throw BackupDestinationException("Access to the backup folder was lost.")
                } catch (e: FileNotFoundException) {
                    throw BackupDestinationException("The backup folder no longer exists.")
                }
                val document = created
                    ?: throw BackupDestinationException("Couldn't create a file in the backup folder.")

                var missingPhotos = 0
                try {
                    val output = resolver.openOutputStream(document, "w")
                        ?: throw IOException("Couldn't open the backup file for writing.")
                    ZipOutputStream(BufferedOutputStream(output)).use { zip ->
                        missingPhotos = writeZip(zip, backup)
                    }
                } catch (e: Exception) {
                    // Don't leave a half-written backup behind
                    runCatching { DocumentsContract.deleteDocument(resolver, document) }
                    throw e
                }

                CreatedBackup(
                    uri = document,
                    name = name,
                    summary = backup.summary(),
                    missingPhotos = missingPhotos,
                )
            }
        }

    // Returns how many photos the backup refers to but couldn't be found on disk
    private fun writeZip(zip: ZipOutputStream, backup: BackupFile): Int {
        zip.putNextEntry(ZipEntry(JSON_ENTRY))
        zip.write(BackupJson.encode(backup).toByteArray(Charsets.UTF_8))
        zip.closeEntry()

        if (BackupSection.Photos !in backup.sectionSet()) return 0

        // Photos are already compressed, so storing them without more compression is much faster
        zip.setLevel(Deflater.NO_COMPRESSION)
        var missing = 0
        for (name in backup.photoFileNames()) {
            val source = photoFile(name)
            if (!source.isFile) {
                missing++
                continue
            }
            zip.putNextEntry(ZipEntry(PHOTO_PREFIX + name))
            source.inputStream().use { it.copyTo(zip) }
            zip.closeEntry()
        }
        return missing
    }

    //endregion

    //region Find

    // The backups in the chosen folder, newest first
    suspend fun listBackups(): List<BackupEntry> = withContext(Dispatchers.IO) {
        val tree = destination() ?: return@withContext emptyList()
        val children = DocumentsContract.buildChildDocumentsUriUsingTree(
            tree,
            DocumentsContract.getTreeDocumentId(tree)
        )
        val projection = arrayOf(
            DocumentsContract.Document.COLUMN_DOCUMENT_ID,
            DocumentsContract.Document.COLUMN_DISPLAY_NAME,
            DocumentsContract.Document.COLUMN_LAST_MODIFIED,
            DocumentsContract.Document.COLUMN_SIZE,
        )

        val entries = ArrayList<BackupEntry>()
        try {
            resolver.query(children, projection, null, null, null)?.use { cursor ->
                while (cursor.moveToNext()) {
                    val name = cursor.getString(1) ?: continue
                    if (!name.startsWith(FILE_PREFIX) || !name.endsWith(".zip", ignoreCase = true)) continue
                    entries += BackupEntry(
                        uri = DocumentsContract.buildDocumentUriUsingTree(tree, cursor.getString(0)),
                        name = name,
                        lastModified = cursor.getLong(2),
                        size = cursor.getLong(3),
                    )
                }
            }
        } catch (e: SecurityException) {
            throw BackupDestinationException("Access to the backup folder was lost.")
        } catch (e: IllegalArgumentException) {
            throw BackupDestinationException("The backup folder no longer exists.")
        }
        entries.sortedByDescending { it.lastModified }
    }

    //endregion

    //region Restore

    // Reads only backup.json, so the UI can show "12 lists, 340 items..." before anything changes.
    // Works for any zip the user has read access to, not just ones in the backup folder.
    suspend fun readBackup(uri: Uri): BackupFile = withContext(Dispatchers.IO) { scan(uri) }

    /**
     * Restores the zip at [uri]. [sections] defaults to everything in the backup.
     *
     * Photos are first unpacked into a staging folder, then moved into place only after the database
     * restore has succeeded, so a failed restore leaves no stray files. A photo whose file name already
     * exists in the app is kept as it is.
     */
    suspend fun restore(uri: Uri, sections: Set<BackupSection>? = null): RestoreSummary =
        withContext(Dispatchers.IO) {
            mutex.withLock {
                val backup = scan(uri)
                val toRestore = (sections ?: backup.sectionSet()) intersect backup.sectionSet()

                val staging = File(context.cacheDir, "restore-staging")
                staging.deleteRecursively()
                staging.mkdirs()
                try {
                    val staged = if (BackupSection.Photos in toRestore) {
                        stagePhotos(uri, backup.photoFileNames(), staging)
                    } else {
                        emptySet()
                    }

                    // A photo is available if it was just unpacked or the app already has that file.
                    // The name check also keeps a crafted file from pointing photo_uri at other files.
                    val photoPathFor = { name: String ->
                        if (SAFE_NAME.matches(name) && (name in staged || photoFile(name).isFile)) {
                            photoFile(name).absolutePath
                        } else {
                            null
                        }
                    }

                    val summary = backupDataSource.restore(backup, photoPathFor, toRestore)
                    movePhotos(staging, staged)
                    summary
                } finally {
                    staging.deleteRecursively()
                }
            }
        }

    private fun scan(uri: Uri): BackupFile {
        var json: String? = null
        openZip(uri).use { zip ->
            while (true) {
                val entry = zip.nextEntry ?: break
                if (entry.name == JSON_ENTRY) {
                    json = readLimited(zip, MAX_JSON_BYTES).toString(Charsets.UTF_8)
                    break
                }
            }
        }
        val text = json ?: throw BackupFormatException("This zip file isn't an Easy Lists backup.")
        return BackupJson.decode(text)
    }

    // Unpacks the photos the backup refers to. Entries with unsafe names or that nothing refers to are skipped.
    private fun stagePhotos(uri: Uri, wanted: Set<String>, staging: File): Set<String> {
        val staged = HashSet<String>()
        var total = 0L
        openZip(uri).use { zip ->
            while (true) {
                val entry = zip.nextEntry ?: break
                if (!entry.name.startsWith(PHOTO_PREFIX)) continue
                val name = entry.name.removePrefix(PHOTO_PREFIX)
                if (!SAFE_NAME.matches(name) || name !in wanted) continue
                if (photoFile(name).isFile) continue

                FileOutputStream(File(staging, name)).use { out ->
                    total += copyLimited(zip, out, MAX_TOTAL_PHOTO_BYTES - total)
                }
                staged += name
            }
        }
        return staged
    }

    private fun movePhotos(staging: File, staged: Set<String>) {
        for (name in staged) {
            val source = File(staging, name)
            val target = photoFile(name)
            if (target.exists()) continue
            // The photos folder doesn't exist on a fresh install until a photo has been saved
            target.parentFile?.mkdirs()
            if (!source.renameTo(target)) source.copyTo(target)
        }
    }

    private fun openZip(uri: Uri): ZipInputStream {
        val input = resolver.openInputStream(uri) ?: throw IOException("Couldn't open the backup file.")
        return ZipInputStream(BufferedInputStream(input))
    }

    private fun readLimited(input: InputStream, limit: Int): ByteArray {
        val out = ByteArrayOutputStream()
        val buffer = ByteArray(8 * 1024)
        while (true) {
            val read = input.read(buffer)
            if (read < 0) break
            if (out.size() + read > limit) {
                throw BackupFormatException("This backup file is too large to be valid.")
            }
            out.write(buffer, 0, read)
        }
        return out.toByteArray()
    }

    private fun copyLimited(input: InputStream, output: OutputStream, limit: Long): Long {
        val buffer = ByteArray(16 * 1024)
        var copied = 0L
        while (true) {
            val read = input.read(buffer)
            if (read < 0) break
            copied += read
            if (copied > limit) {
                throw BackupFormatException("This backup is larger than expected and can't be restored.")
            }
            output.write(buffer, 0, read)
        }
        return copied
    }

    //endregion

    // The one place that knows where photos live: filesDir/photos, where ListItemsViewModel saves them.
    // When photo saving moves into the repository, this should call it instead.
    private fun photoFile(name: String) = File(File(context.filesDir, "photos"), name)

    private companion object {
        const val JSON_ENTRY = "backup.json"
        const val PHOTO_PREFIX = "photos/"
        const val FILE_PREFIX = "easy-lists-backup-"
        const val MAX_JSON_BYTES = 64 * 1024 * 1024
        const val MAX_TOTAL_PHOTO_BYTES = 2L * 1024 * 1024 * 1024

        // File names we're willing to read or write: no separators, and no leading dot
        val SAFE_NAME = Regex("[A-Za-z0-9_][A-Za-z0-9._-]{0,99}")

        val NAME_FORMAT: DateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd-HHmmss", Locale.ROOT)
    }

}