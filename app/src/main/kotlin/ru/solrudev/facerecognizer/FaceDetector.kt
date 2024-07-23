@file:OptIn(TransformExperimental::class)

package ru.solrudev.facerecognizer

import android.graphics.Bitmap
import android.graphics.RectF
import androidx.annotation.OptIn
import androidx.camera.core.ExperimentalGetImage
import androidx.camera.core.ImageProxy
import androidx.camera.view.TransformExperimental
import androidx.camera.view.transform.CoordinateTransform
import com.google.mlkit.vision.common.InputImage
import ru.solrudev.facerecognizer.util.await
import ru.solrudev.facerecognizer.util.crop
import ru.solrudev.facerecognizer.util.rotateCounterclockwise
import javax.inject.Inject
import com.google.mlkit.vision.face.FaceDetector as MlKitVisionFaceDetector

interface FaceDetector : AutoCloseable {
	suspend fun detect(image: Image, previewTransform: CoordinateTransform): List<DetectedFace>
}

class MlKitFaceDetector @Inject constructor(private val faceDetector: MlKitVisionFaceDetector) : FaceDetector {

	override suspend fun detect(image: Image, previewTransform: CoordinateTransform): List<DetectedFace> {
		return faceDetector
			.process(image.toInputImage())
			.await()
			.mapNotNull { face ->
				val rotatedFaceBoundingBox = face.boundingBox
				val faceBoundingBox = rotatedFaceBoundingBox.rotateCounterclockwise(
					image.width, image.height, image.rotationDegrees
				)
				val faceBitmap = image.toBitmap().crop(faceBoundingBox, image.rotationDegrees) ?: return@mapNotNull null
				previewTransform.mapRect(faceBoundingBox)
				val faceId = face.trackingId ?: error("Face tracking is disabled")
				DetectedFace(faceId, faceBitmap, faceBoundingBox)
			}
	}

	override fun close() {
		faceDetector.close()
	}
}

data class DetectedFace(val id: Int, val bitmap: Bitmap, val boundingBox: RectF)

class Image(private val image: ImageProxy) {

	val width: Int
		get() = image.width

	val height: Int
		get() = image.height

	val rotationDegrees: Int
		get() = image.imageInfo.rotationDegrees

	@OptIn(ExperimentalGetImage::class)
	fun toInputImage() = InputImage.fromMediaImage(image.image!!, rotationDegrees)

	fun toBitmap() = image.toBitmap()
}