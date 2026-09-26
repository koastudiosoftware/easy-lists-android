package com.easylists.presentation.models

import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.listSaver
import androidx.compose.runtime.setValue
import androidx.compose.ui.geometry.Offset

//region ZoomState class, used for pinch to zoom
@Stable
class ZoomState(
    initialScale: Float = 1f,
    initialOffset: Offset = Offset.Zero,
) {
    var scale by mutableFloatStateOf(initialScale)
    var offset by mutableStateOf(initialOffset)

    val isZoomed: Boolean get() = scale > 1f

    fun reset() {
        scale = 1f
        offset = Offset.Zero
    }

    companion object {
        val Saver = listSaver<ZoomState, Float>(
            save = { listOf(it.scale, it.offset.x, it.offset.y) },
            restore = { ZoomState(it[0], Offset(it[1], it[2])) },
        )
    }
}
//endregion
