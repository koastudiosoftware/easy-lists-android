package com.easylists.domain.use_cases

import com.easylists.domain.repositories.ImageRepository
import javax.inject.Inject

//region SaveListItemPhotoUseCase
class SaveListItemPhotoUseCase @Inject constructor(private val repository: ImageRepository) {
    suspend operator fun invoke(sourceUri: String): Result<String> = repository.savePhoto(sourceUri)
}
//endregion
