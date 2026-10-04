package com.easylists.domain.repositories

//region ImageRepository
interface ImageRepository {
    suspend fun saveImage(imageData: ByteArray, fileName: String): Result<String>
}
//endregion
