package com.easylists.presentation.common

import android.graphics.Bitmap
import android.os.Build
import java.io.ByteArrayOutputStream
import androidx.core.graphics.scale

// Determine the correct WebP format based on the device's Android version
val compressFormat = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
    Bitmap.CompressFormat.WEBP_LOSSY
} else {
    // TODO remove this part when we have a minSdk of 30+
    @Suppress("DEPRECATION")
    Bitmap.CompressFormat.WEBP
}

fun scaleAndCompressBitmap(
    sourceBitmap: Bitmap,
    maxLongEdge: Int = 1600,
    quality: Int = 80
): ByteArray {
    val width = sourceBitmap.width
    val height = sourceBitmap.height
    val originalLongEdge = maxOf(width, height)

    // Scale down if the long edge exceeds the 1600px limit
    val bitmapToCompress = if (originalLongEdge > maxLongEdge) {
        val targetWidth: Int
        val targetHeight: Int
        if (width > height) {
            targetWidth = maxLongEdge
            targetHeight = (height.toFloat() * maxLongEdge / width).toInt()
        } else {
            targetHeight = maxLongEdge
            targetWidth = (width.toFloat() * maxLongEdge / height).toInt()
        }

        // Create a scaled version respecting the aspect ratio
        sourceBitmap.scale(targetWidth, targetHeight)
    } else {
        sourceBitmap
    }

    // Compress into ByteArrayOutputStream
    val outputStream = ByteArrayOutputStream()
    bitmapToCompress.compress(compressFormat, quality, outputStream)

    // Clean up memory if a new scaled bitmap instance was created
    if (bitmapToCompress != sourceBitmap) {
        bitmapToCompress.recycle()
    }

    return outputStream.toByteArray()
}
