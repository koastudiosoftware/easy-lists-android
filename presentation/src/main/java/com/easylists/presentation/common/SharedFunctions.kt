package com.easylists.presentation.common

import android.content.Context
import android.graphics.Bitmap
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.core.net.toUri
import coil3.ImageLoader
import coil3.compose.AsyncImage
import coil3.request.ImageRequest
import coil3.request.SuccessResult
import coil3.size.Size
import coil3.toBitmap
import dagger.hilt.android.qualifiers.ApplicationContext
import java.io.File
import javax.inject.Inject
import kotlin.math.pow


//region ImageBitmapLoader
// Take in a string URI and return a bitmap of that file's content
class ImageBitmapLoader @Inject constructor(
    @ApplicationContext private val context: Context,
    private val imageLoader: ImageLoader
) {
    suspend fun load(imageUrl: String): Bitmap? {
        val request = ImageRequest.Builder(context)
            .data(imageUrl)
            .build()

        val result = imageLoader.execute(request)
        return (result as? SuccessResult)?.image?.toBitmap()
    }
}
//endregion


//region resolvePhotoModel
fun resolvePhotoModel(photoUri: String): Any {
    val scheme = photoUri.toUri().scheme
    return if (scheme != null) photoUri.toUri() else File(photoUri)
}
//endregion


private fun Offset.clampToBounds(scale: Float, size: IntSize): Offset {
    val maxX = (size.width * (scale - 1f) / 2f).coerceAtLeast(0f)
    val maxY = (size.height * (scale - 1f) / 2f).coerceAtLeast(0f)
    return Offset(x.coerceIn(-maxX, maxX), y.coerceIn(-maxY, maxY))
}


@Composable
fun FramedPhoto(
    photoUri: String,
    scale: Float,
    normalizedOffsetX: Float,
    normalizedOffsetY: Float,
    modifier: Modifier = Modifier,
    onTransformChanged: ((scale: Float, normalizedOffsetX: Float, normalizedOffsetY: Float) -> Unit)? = null,
    zoomEnabled: Boolean = false,
    doubleTapScale: Float = 2.5f,
    targetSize: Size = Size.ORIGINAL,
    shape: Shape = RectangleShape,
) {
    var containerSize by remember { mutableStateOf(IntSize.Zero) }

    var liveScale by remember(scale) { mutableFloatStateOf(scale) }
    var livePxOffset by remember(normalizedOffsetX, normalizedOffsetY, containerSize) {
        mutableStateOf(
            Offset(
                normalizedOffsetX * containerSize.width,
                normalizedOffsetY * containerSize.height,
            ).clampToBounds(scale, containerSize)
        )
    }

    fun reportTransform() {
        if (containerSize.width > 0 && containerSize.height > 0) {
            onTransformChanged?.invoke(
                liveScale,
                livePxOffset.x / containerSize.width,
                livePxOffset.y / containerSize.height,
            )
        }
    }

    Box(
        modifier = modifier
            .onGloballyPositioned { containerSize = it.size }
            .size(44.dp)
            .clip(shape)
            .then(
                if (zoomEnabled) {
                    Modifier
                        // zoom with double tap
                        .pointerInput(Unit) {
                            detectTapGestures(
                                onDoubleTap = { tap ->
                                    if (liveScale > 1f) {
                                        liveScale = 1f
                                        livePxOffset = Offset.Zero
                                    } else {
                                        val center = Offset(size.width / 2f, size.height / 2f)
                                        val z = doubleTapScale / liveScale
                                        val raw = (tap - center) * (1f - z) + livePxOffset * z
                                        liveScale = doubleTapScale
                                        livePxOffset = raw.clampToBounds(doubleTapScale, containerSize)
                                    }
                                    reportTransform()
                                }
                            )
                        }

                        // zoom with pinch-to-zoom
                        .pointerInput(Unit) {
                            detectTransformGestures { centroid, pan, zoom, _ ->
                                val oldScale = liveScale
                                val newScale = (oldScale * zoom).coerceIn(1f, 5f)
                                val z = newScale / oldScale
                                val center = Offset(size.width / 2f, size.height / 2f)

                                // keep the content point under the fingers stationary
                                val raw = (centroid - center) * (1f - z) + livePxOffset * z + pan

                                liveScale = newScale
                                livePxOffset = raw.clampToBounds(newScale, containerSize)
                                reportTransform()
                            }
                        }
                } else Modifier
            )
    ) {
        AsyncImage(
            model = ImageRequest.Builder(LocalContext.current)
                .data(resolvePhotoModel(photoUri))
                .build(),
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier = Modifier
                .fillMaxSize()
                .graphicsLayer {
                    scaleX = liveScale
                    scaleY = liveScale
                    translationX = livePxOffset.x
                    translationY = livePxOffset.y
                },
        )
    }
}
//endregion


//region Color.getContrastColor()
@Composable
fun Color.getContrastColor(): Color {
    val flipRelativeLuminance = 0.342  // based on APCA™ 0.98G middle contrast BG color

    val trc = 2.4

    val redCoefficient = 0.2126729
    val greenCoefficient = 0.7151522
    val blueCoefficient = 0.0721750

    val red = this.red * 255
    val green = this.green * 255
    val blue = this.blue * 255

    val relativeLuminance = (red / 255.0).pow(trc) * redCoefficient +
            (green / 255.0).pow(trc) * greenCoefficient +
            (blue / 255.0).pow(trc) * blueCoefficient

    return if (relativeLuminance < flipRelativeLuminance) {
        MaterialTheme.colorScheme.onSecondary
    } else {
        MaterialTheme.colorScheme.onPrimaryContainer
    }
}
//endregion
