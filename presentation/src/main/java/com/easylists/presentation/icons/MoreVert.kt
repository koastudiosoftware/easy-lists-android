package com.easylists.presentation.icons

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathFillType
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.unit.dp
import kotlin.apply

public val More_vert: ImageVector
    get() {
        if (_More_vert != null) {
            return _More_vert!!
        }
        _More_vert = ImageVector.Builder(
            name = "More_vert",
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
                moveTo(480f, 800f)
                quadToRelative(-33f, 0f, -56.5f, -23.5f)
                reflectiveQuadTo(400f, 720f)
                reflectiveQuadToRelative(23.5f, -56.5f)
                reflectiveQuadTo(480f, 640f)
                reflectiveQuadToRelative(56.5f, 23.5f)
                reflectiveQuadTo(560f, 720f)
                reflectiveQuadToRelative(-23.5f, 56.5f)
                reflectiveQuadTo(480f, 800f)
                moveToRelative(0f, -240f)
                quadToRelative(-33f, 0f, -56.5f, -23.5f)
                reflectiveQuadTo(400f, 480f)
                reflectiveQuadToRelative(23.5f, -56.5f)
                reflectiveQuadTo(480f, 400f)
                reflectiveQuadToRelative(56.5f, 23.5f)
                reflectiveQuadTo(560f, 480f)
                reflectiveQuadToRelative(-23.5f, 56.5f)
                reflectiveQuadTo(480f, 560f)
                moveToRelative(0f, -240f)
                quadToRelative(-33f, 0f, -56.5f, -23.5f)
                reflectiveQuadTo(400f, 240f)
                reflectiveQuadToRelative(23.5f, -56.5f)
                reflectiveQuadTo(480f, 160f)
                reflectiveQuadToRelative(56.5f, 23.5f)
                reflectiveQuadTo(560f, 240f)
                reflectiveQuadToRelative(-23.5f, 56.5f)
                reflectiveQuadTo(480f, 320f)
            }
        }.build()
        return _More_vert!!
    }

private var _More_vert: ImageVector? = null
