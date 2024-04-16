package ru.solrudev.facerecognizer.util

import android.graphics.Bitmap
import android.graphics.Matrix
import android.graphics.RectF
import androidx.core.graphics.toRect

fun Bitmap.crop(rect: RectF, rotationDegrees: Int): Bitmap? {
	val cropRect = rect.toRect()
	val x = cropRect.left.coerceAtLeast(0)
	val y = cropRect.top.coerceAtLeast(0)
	val width = cropRect.width()
	val height = cropRect.height()
	val croppedWidth = if (x + width > this.width) this.width - x else width
	val croppedHeight = if (y + height > this.height) this.height - y else height
	if (croppedWidth <= 0 || croppedHeight <= 0) {
		return null
	}
	val needToRotate = rotationDegrees % 360 != 0
	if (needToRotate) {
		val rotationMatrix = Matrix().apply { postRotate(rotationDegrees.toFloat()) }
		return Bitmap.createBitmap(this, x, y, croppedWidth, croppedHeight, rotationMatrix, true)
	}
	return Bitmap.createBitmap(this, x, y, croppedWidth, croppedHeight)
}