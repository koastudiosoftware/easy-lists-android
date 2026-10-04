package com.easylists.domain.use_cases

import com.easylists.domain.repositories.ImageRepository
import javax.inject.Inject

//region SaveListItemPhotoUseCase
class SaveListItemPhotoUseCase @Inject constructor(private val repository: ImageRepository) {
    suspend operator fun invoke(imageData: ByteArray, fileName: String): Result<String> =
        repository.saveImage(imageData, fileName)
}
//endregion
