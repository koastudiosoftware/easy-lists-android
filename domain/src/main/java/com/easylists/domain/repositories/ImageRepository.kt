package com.easylists.domain.repositories

//region ImageRepository
interface ImageRepository {
    // Loads the image at sourceUri, stores a scaled and compressed copy in app-private storage,
    // and returns the location to keep in the item's photo_uri
    suspend fun savePhoto(sourceUri: String): Result<String>

    // Deletes the stored photo files with the given names; missing files are ignored
    suspend fun deletePhotos(fileNames: List<String>)
}
//endregion
