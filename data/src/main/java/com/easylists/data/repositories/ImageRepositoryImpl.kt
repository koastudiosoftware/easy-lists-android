package com.easylists.data.repositories

import android.content.Context
import com.easylists.domain.repositories.ImageRepository
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import javax.inject.Inject

//region ImageRepositoryImpl
class ImageRepositoryImpl @Inject constructor(
    @ApplicationContext private val context: Context
) : ImageRepository {

    override suspend fun saveImage(imageData: ByteArray, fileName: String): Result<String> =
        withContext(Dispatchers.IO) {
            try {
                val file = File(context.filesDir, fileName)
                FileOutputStream(file).use { out ->
                    out.write(imageData)
                }
                Result.success(file.absolutePath)
            } catch (e: IOException) {
                Result.failure(e)
            }
        }
}
//endregion
