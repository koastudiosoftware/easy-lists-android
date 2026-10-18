package com.easylists.data.repositories

import android.content.Context
import android.graphics.Bitmap
import android.os.Build
import androidx.core.graphics.scale
import coil3.ImageLoader
import coil3.request.ImageRequest
import coil3.request.SuccessResult
import coil3.request.allowHardware
import coil3.toBitmap
import com.easylists.domain.common.PhotoStorage
import com.easylists.domain.repositories.ImageRepository
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.IOException
import javax.inject.Inject
import kotlin.math.roundToInt
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid


//region ImageRepositoryImpl
class ImageRepositoryImpl @Inject constructor(
    @ApplicationContext private val context: Context,
    private val imageLoader: ImageLoader,
) : ImageRepository {

    // Loads the image at sourceUri, scales it down, encodes it as WEBP and stores it in the photos folder.
    // Returns just the file name, which is what gets kept in the item's photo_uri.
    @OptIn(ExperimentalUuidApi::class)
    override suspend fun savePhoto(sourceUri: String): Result<String> =
        withContext(Dispatchers.IO) {
            try {
                val bitmap = loadBitmap(sourceUri)
                    ?: throw IOException("The image at $sourceUri couldn't be loaded")
                val bytes = withContext(Dispatchers.Default) { scaleAndCompress(bitmap) }

                val directory = PhotoStorage.directory(context.filesDir).apply { mkdirs() }
                val fileName = "${Uuid.generateV7()}.webp"
                File(directory, fileName).writeBytes(bytes)
                Result.success(fileName)
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                Result.failure(e)
            }
        }

    private suspend fun loadBitmap(sourceUri: String): Bitmap? {
        val request = ImageRequest.Builder(context)
            .data(sourceUri)
            // A software bitmap, so it can be scaled and compressed
            .allowHardware(false)
            .build()
        val result = imageLoader.execute(request)
        return (result as? SuccessResult)?.image?.toBitmap()
    }

    private fun scaleAndCompress(source: Bitmap): ByteArray {
        val width = source.width
        val height = source.height
        val longEdge = maxOf(width, height)

        // Scale down if the long edge is over the limit, keeping the aspect ratio
        val scaled = if (longEdge > MAX_LONG_EDGE) {
            val ratio = MAX_LONG_EDGE.toFloat() / longEdge
            source.scale(
                (width * ratio).roundToInt().coerceAtLeast(1),
                (height * ratio).roundToInt().coerceAtLeast(1)
            )
        } else {
            source
        }

        val output = ByteArrayOutputStream()
        scaled.compress(WEBP_FORMAT, QUALITY, output)

        // Only recycle the copy made here, never the bitmap that was handed in
        if (scaled !== source) scaled.recycle()

        return output.toByteArray()
    }

    private companion object {
        const val PHOTOS_DIR = "photos"
        const val MAX_LONG_EDGE = 1600
        const val QUALITY = 85

        // WEBP_LOSSY only exists on Android 11 and up; on older versions the plain WEBP constant is lossy
        @Suppress("DEPRECATION")
        val WEBP_FORMAT: Bitmap.CompressFormat = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            Bitmap.CompressFormat.WEBP_LOSSY
        } else {
            Bitmap.CompressFormat.WEBP
        }
    }


    override suspend fun deletePhotos(fileNames: List<String>) {
        withContext(Dispatchers.IO) {
            val directory = PhotoStorage.directory(context.filesDir)
            fileNames.forEach { File(directory, File(it).name).delete() }
        }
    }

}
//endregion