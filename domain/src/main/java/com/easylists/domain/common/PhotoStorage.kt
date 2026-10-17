package com.easylists.domain.common

import java.io.File

// The one definition of where list item photos live. photo_uri holds only the file name.
object PhotoStorage {
    const val DIRECTORY = "photos"

    fun directory(filesDir: File): File = File(filesDir, DIRECTORY)

    fun file(filesDir: File, fileName: String): File = File(directory(filesDir), fileName)
}
