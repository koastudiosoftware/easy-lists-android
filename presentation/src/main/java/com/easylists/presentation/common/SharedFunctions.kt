package com.easylists.presentation.common

import android.content.Context
import android.graphics.Bitmap
import android.net.Uri
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.gestures.calculateCentroid
import androidx.compose.foundation.gestures.calculatePan
import androidx.compose.foundation.gestures.calculateZoom
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.isSpecified
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.PointerEventPass
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.input.pointer.positionChanged
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import coil3.ImageLoader
import coil3.compose.AsyncImage
import coil3.request.ImageRequest
import coil3.request.SuccessResult
import coil3.toBitmap
import com.easylists.presentation.models.ZoomState
import com.toxicbakery.logging.Arbor
import dagger.hilt.android.qualifiers.ApplicationContext
import java.io.File
import javax.inject.Inject
import kotlin.math.pow
import androidx.core.net.toUri


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
) {
    var containerSize by remember { mutableStateOf(IntSize.Zero) }

    var liveScale by remember(scale) { mutableFloatStateOf(scale) }
    var livePxOffset by remember(normalizedOffsetX, normalizedOffsetY, containerSize) {
        mutableStateOf(
            Offset(
                normalizedOffsetX * containerSize.width,
                normalizedOffsetY * containerSize.height,
            )
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
            .clipToBounds()
            .then(
                if (zoomEnabled) {
                    Modifier
                        .pointerInput(Unit) {
                            detectTapGestures(
                                onDoubleTap = {
                                    if (liveScale > 1f) {
                                        liveScale = 1f
                                        livePxOffset = Offset.Zero
                                    } else {
                                        liveScale = doubleTapScale
                                        // offset left at current value; recenter here if desired
                                    }
                                    reportTransform()
                                }
                            )
                        }
                        .pointerInput(Unit) {
                            detectTransformGestures { _, pan, zoom, _ ->
                                liveScale = (liveScale * zoom).coerceIn(1f, 5f)
                                livePxOffset += pan
                                reportTransform()
                            }
                        }
                } else modifier
            )
    ) {
        AsyncImage(
            model = ImageRequest.Builder(LocalContext.current)
                .data(resolvePhotoModel(photoUri))
                .build(),
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier = modifier
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


//region Modifier.pinchToZoom, used for pinch to zoom on Add/Edit Item bottom sheet
fun Modifier.pinchToZoom(
    state: ZoomState,
    maxScale: Float = 5f,
    doubleTapScale: Float = 2.5f,
    onTap: (() -> Unit)? = null,
): Modifier = this
    .pointerInput(state) {
        awaitEachGesture {
            // Initial pass: runs before the bottom sheet / LazyColumn see the events
            awaitFirstDown(requireUnconsumed = false, pass = PointerEventPass.Initial)
            do {
                val event = awaitPointerEvent(PointerEventPass.Initial)
                val fingers = event.changes.count { it.pressed }

                if (fingers >= 2 || state.isZoomed) {
                    val zoom = if (fingers >= 2) event.calculateZoom() else 1f
                    val pan = event.calculatePan()
                    val centroid = event.calculateCentroid()

                    if (centroid.isSpecified) {
                        val center = Offset(size.width / 2f, size.height / 2f)
                        val newScale = (state.scale * zoom).coerceIn(1f, maxScale)
                        val z = newScale / state.scale

                        // Keep the content point under the fingers stationary
                        val raw = (centroid - center) * (1f - z) + state.offset * z + pan

                        val maxX = size.width * (newScale - 1f) / 2f
                        val maxY = size.height * (newScale - 1f) / 2f

                        state.scale = newScale
                        state.offset = Offset(
                            raw.x.coerceIn(-maxX, maxX) / maxX,
                            raw.y.coerceIn(-maxY, maxY) / maxY,
                        )

                        event.changes.forEach { if (it.positionChanged()) it.consume() }
                    }
                }
            } while (event.changes.any { it.pressed })
            // No reset here: the image stays zoomed after the fingers lift.
        }
    }
    .pointerInput(state, onTap) {
        detectTapGestures(
            onTap = { onTap?.invoke() },
            onDoubleTap = { tap ->
                if (state.isZoomed) {
                    state.reset()
                    // reset view model values to defaults
                } else {
                    val center = Offset(size.width / 2f, size.height / 2f)
                    val maxX = size.width * (doubleTapScale - 1f) / 2f
                    val maxY = size.height * (doubleTapScale - 1f) / 2f
                    val raw = (tap - center) * (1f - doubleTapScale)
                    state.scale = doubleTapScale
                    state.offset = Offset(
                        raw.x.coerceIn(-maxX, maxX) / maxX,
                        raw.y.coerceIn(-maxY, maxY) / maxY,
                    )
                }
            },
        )
    }
    // Clip to the Box's original bounds (the sheet clips anyway), OUTSIDE the layer
    .clipToBounds()
    .graphicsLayer {
        scaleX = state.scale
        scaleY = state.scale
        translationX = state.offset.x * size.width
        translationY = state.offset.y * size.height
    }
//endregion


//region isNumeric
fun isNumeric(str: String): Boolean = str
    .removePrefix("-")
    .removePrefix("+")
    .all { it in '0'..'9' }
//endregion


//region Color.toHexCodeWithAlpha()
fun Color.toHexCodeWithAlpha(): String {
    val alpha = this.alpha * 255
    val red = this.red * 255
    val green = this.green * 255
    val blue = this.blue * 255
    return String.format(
        "#%02x%02x%02x%02x",
        alpha.toInt(),
        red.toInt(),
        green.toInt(),
        blue.toInt()
    )
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
        MaterialTheme.colorScheme.onPrimaryContainer
    } else {
        MaterialTheme.colorScheme.onSurface
    }
}
//endregion
