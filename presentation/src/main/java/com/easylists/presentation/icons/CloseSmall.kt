package com.easylists.presentation.icons

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathFillType
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.unit.dp

public val Close_small: ImageVector
    get() {
        if (_Close_small != null) {
            return _Close_small!!
        }
        _Close_small = ImageVector.Builder(
            name = "Close_small",
            defaultWidth = 24.dp,
            defaultHeight = 24.dp,
            viewportWidth = 960f,
            viewportHeight = 960f
        ).apply {
            path(
                fill = SolidColor(Color.Black),
                fillAlpha = 1.0f,
                stroke = null,
                strokeAlpha = 1.0f,
                strokeLineWidth = 1.0f,
                strokeLineCap = StrokeCap.Butt,
                strokeLineJoin = StrokeJoin.Miter,
                strokeLineMiter = 1.0f,
                pathFillType = PathFillType.NonZero
            ) {
                moveTo(336f, 680f)
                lineToRelative(-56f, -56f)
                lineToRelative(144f, -144f)
                lineToRelative(-144f, -143f)
                lineToRelative(56f, -56f)
                lineToRelative(144f, 144f)
                lineToRelative(143f, -144f)
                lineToRelative(56f, 56f)
                lineToRelative(-144f, 143f)
                lineToRelative(144f, 144f)
                lineToRelative(-56f, 56f)
                lineToRelative(-143f, -144f)
                close()
            }
        }.build()
        return _Close_small!!
    }

private var _Close_small: ImageVector? = null
