package com.easylists.presentation.icons

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathFillType
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.unit.dp

public val Category: ImageVector
    get() {
        if (_Category != null) {
            return _Category!!
        }
        _Category = ImageVector.Builder(
            name = "Category",
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
                moveTo(260f, 440f)
                lineToRelative(220f, -360f)
                lineToRelative(220f, 360f)
                close()
                moveTo(700f, 880f)
                quadToRelative(-75f, 0f, -127.5f, -52.5f)
                reflectiveQuadTo(520f, 700f)
                reflectiveQuadToRelative(52.5f, -127.5f)
                reflectiveQuadTo(700f, 520f)
                reflectiveQuadToRelative(127.5f, 52.5f)
                reflectiveQuadTo(880f, 700f)
                reflectiveQuadToRelative(-52.5f, 127.5f)
                reflectiveQuadTo(700f, 880f)
                moveToRelative(-580f, -20f)
                verticalLineToRelative(-320f)
                horizontalLineToRelative(320f)
                verticalLineToRelative(320f)
                close()
                moveToRelative(580f, -60f)
                quadToRelative(42f, 0f, 71f, -29f)
                reflectiveQuadToRelative(29f, -71f)
                reflectiveQuadToRelative(-29f, -71f)
                reflectiveQuadToRelative(-71f, -29f)
                reflectiveQuadToRelative(-71f, 29f)
                reflectiveQuadToRelative(-29f, 71f)
                reflectiveQuadToRelative(29f, 71f)
                reflectiveQuadToRelative(71f, 29f)
                moveToRelative(-500f, -20f)
                horizontalLineToRelative(160f)
                verticalLineToRelative(-160f)
                horizontalLineTo(200f)
                close()
                moveToRelative(202f, -420f)
                horizontalLineToRelative(156f)
                lineToRelative(-78f, -126f)
                close()
                moveToRelative(298f, 340f)
            }
        }.build()
        return _Category!!
    }

private var _Category: ImageVector? = null
