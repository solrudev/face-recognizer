package ru.solrudev.facerecognizer.ui.widget

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Path
import android.graphics.Rect
import android.util.AttributeSet
import android.view.View
import androidx.core.content.ContextCompat
import androidx.core.graphics.withClip
import ru.solrudev.facerecognizer.FaceRecognition
import ru.solrudev.facerecognizer.R
import ru.solrudev.facerecognizer.util.roundedPath

private const val BOUNDING_BOX_TEXT_PADDING = 16
private const val BOUNDING_BOX_CORNER_SIZE = 50f
private const val BOUNDING_BOX_TEXT_MARGIN = 25f

class OverlayView(context: Context, attrs: AttributeSet) : View(context, attrs) {

	private var results = listOf<FaceRecognition>()
	private val boxPaint = Paint()
	private val textBackgroundPaint = Paint()
	private val textPaint = Paint().apply { isAntiAlias = true }
	private val bounds = Rect()

	init {
		initPaints()
	}

	fun drawResults(detectionResults: List<FaceRecognition>) {
		results = detectionResults
		invalidate()
	}

	fun clear() {
		textPaint.reset()
		textBackgroundPaint.reset()
		boxPaint.reset()
		invalidate()
		initPaints()
	}

	override fun draw(canvas: Canvas) {
		super.draw(canvas)
		for (result in results) {
			val roundedBoundingBox = result.boundingBox.roundedPath(BOUNDING_BOX_CORNER_SIZE)
			if (result.face.isUnknown) {
				canvas.drawPath(roundedBoundingBox, boxPaint)
				return
			}
			canvas.drawFaceRecognition(result, roundedBoundingBox)
		}
	}

	private fun Canvas.drawFaceRecognition(faceRecognition: FaceRecognition, roundedBoundingBox: Path) {
		val text = "${faceRecognition.face.name} ${String.format("%.2f", faceRecognition.face.distance)}"
		textBackgroundPaint.getTextBounds(text, 0, text.length, bounds)
		val textWidth = bounds.width()
		val textHeight = bounds.height()
		val top = faceRecognition.boundingBox.top.toFloat()
		val left = faceRecognition.boundingBox.left.toFloat()
		withClip(roundedBoundingBox) {
			drawRect(
				left, top,
				left + textWidth + BOUNDING_BOX_TEXT_PADDING + BOUNDING_BOX_TEXT_MARGIN,
				top + textHeight + BOUNDING_BOX_TEXT_PADDING + BOUNDING_BOX_TEXT_MARGIN,
				textBackgroundPaint
			)
			drawText(
				text,
				left + BOUNDING_BOX_TEXT_MARGIN,
				top + textHeight + BOUNDING_BOX_TEXT_MARGIN,
				textPaint
			)
		}
		drawPath(roundedBoundingBox, boxPaint)
	}

	private fun initPaints() {
		textBackgroundPaint.apply {
			color = Color.BLACK
			style = Paint.Style.FILL
			textSize = 50f
		}
		textPaint.apply {
			color = Color.WHITE
			style = Paint.Style.FILL
			textSize = 50f
		}
		boxPaint.apply {
			color = ContextCompat.getColor(context, R.color.bounding_box_color)
			strokeWidth = 8f
			style = Paint.Style.STROKE
		}
	}
}