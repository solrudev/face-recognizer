package ru.solrudev.facerecognizer.util

import android.graphics.Path
import android.graphics.Rect
import android.graphics.RectF
import androidx.core.graphics.toRectF
import com.google.android.material.shape.ShapeAppearanceModel
import com.google.android.material.shape.ShapeAppearancePathProvider

fun Rect.rotateCounterclockwise(containerWidth: Int, containerHeight: Int, rotationDegrees: Int): RectF {
	if (rotationDegrees % 360 == 0) {
		return toRectF()
	}
	var left = left
	var top = top
	var right = right
	var bottom = bottom
	val normalizedDegrees = (rotationDegrees % 360 + 360) % 360
	val previousLeft = left
	val previousTop = top
	val previousRight = right
	val previousBottom = bottom
	when (normalizedDegrees) {
		90 -> {
			left = previousTop
			top = containerHeight - previousRight
			right = previousBottom
			bottom = containerHeight - previousLeft
		}

		180 -> {
			left = containerWidth - previousRight
			top = containerHeight - previousBottom
			right = containerWidth - previousLeft
			bottom = containerHeight - previousTop
		}

		270 -> {
			left = containerWidth - previousBottom
			top = previousLeft
			right = containerWidth - previousTop
			bottom = previousRight
		}
	}
	return RectF(left.toFloat(), top.toFloat(), right.toFloat(), bottom.toFloat())
}

fun Rect.roundedPath(cornerSize: Float): Path {
	val roundPath = Path()
	val appearanceModel = ShapeAppearanceModel().withCornerSize(cornerSize)
	ShapeAppearancePathProvider().calculatePath(appearanceModel, 1f, toRectF(), roundPath)
	return roundPath
}