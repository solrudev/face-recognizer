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
	suspend fun detect(
		image: ImageProxy,
		rotationDegrees: Int,
		previewTransform: CoordinateTransform
	): List<DetectedFace>
}

class MlKitFaceDetector @Inject constructor(private val faceDetector: MlKitVisionFaceDetector) : FaceDetector {

	@OptIn(ExperimentalGetImage::class)
	override suspend fun detect(
		image: ImageProxy,
		rotationDegrees: Int,
		previewTransform: CoordinateTransform
	): List<DetectedFace> {
		val inputImage = InputImage.fromMediaImage(image.image!!, rotationDegrees)
		return faceDetector
			.process(inputImage)
			.await()
			.mapNotNull { face ->
				val rotatedFaceBoundingBox = face.boundingBox
				val faceBoundingBox = rotatedFaceBoundingBox.rotateCounterclockwise(
					image.width, image.height, rotationDegrees
				)
				val faceBitmap = image.toBitmap().crop(faceBoundingBox, rotationDegrees) ?: return@mapNotNull null
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