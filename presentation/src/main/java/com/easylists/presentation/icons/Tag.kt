package com.easylists.presentation.icons

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathFillType
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.unit.dp

public val Tag: ImageVector
    get() {
        if (_Tag != null) {
            return _Tag!!
        }
        _Tag = ImageVector.Builder(
            name = "Tag",
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
                moveTo(240f, 800f)
                lineToRelative(40f, -160f)
                horizontalLineTo(120f)
                lineToRelative(20f, -80f)
                horizontalLineToRelative(160f)
                lineToRelative(40f, -160f)
                horizontalLineTo(180f)
                lineToRelative(20f, -80f)
                horizontalLineToRelative(160f)
                lineToRelative(40f, -160f)
                horizontalLineToRelative(80f)
                lineToRelative(-40f, 160f)
                horizontalLineToRelative(160f)
                lineToRelative(40f, -160f)
                horizontalLineToRelative(80f)
                lineToRelative(-40f, 160f)
                horizontalLineToRelative(160f)
                lineToRelative(-20f, 80f)
                horizontalLineTo(660f)
                lineToRelative(-40f, 160f)
                horizontalLineToRelative(160f)
                lineToRelative(-20f, 80f)
                horizontalLineTo(600f)
                lineToRelative(-40f, 160f)
                horizontalLineToRelative(-80f)
                lineToRelative(40f, -160f)
                horizontalLineTo(360f)
                lineToRelative(-40f, 160f)
                close()
                moveToRelative(140f, -240f)
                horizontalLineToRelative(160f)
                lineToRelative(40f, -160f)
                horizontalLineTo(420f)
                close()
            }
        }.build()
        return _Tag!!
    }

private var _Tag: ImageVector? = null
