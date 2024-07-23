@file:OptIn(TransformExperimental::class)

package ru.solrudev.facerecognizer

import androidx.annotation.OptIn
import androidx.camera.core.ImageProxy
import androidx.camera.view.TransformExperimental
import androidx.camera.view.transform.CoordinateTransform
import androidx.core.graphics.toRect
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.cancel
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.transform
import kotlinx.coroutines.launch
import javax.inject.Inject
import kotlin.time.Duration.Companion.milliseconds

interface FaceRecognitionProcessor : AutoCloseable {
	val results: Flow<List<FaceRecognition>>
	fun processFrame(image: ImageProxy, rotationDegrees: Int, previewTransform: CoordinateTransform)
}

class FaceRecognitionProcessorImpl @Inject constructor(
	private val faceDetector: FaceDetector,
	private val faceRecognizer: FaceRecognizer,
	private val coroutineScope: FaceRecognitionCoroutineScope
) : FaceRecognitionProcessor {

	private val detectedFaces = MutableSharedFlow<List<DetectedFace>>(
		replay = 1,
		onBufferOverflow = BufferOverflow.DROP_OLDEST
	)

	private val recognizedFaces = detectedFaces
		.transform { detectedFaces ->
			emit(recognizeFaces(detectedFaces))
			delay(500.milliseconds)
		}
		.onStart { emit(emptyList()) }

	override val results = combine(detectedFaces, recognizedFaces) { detectedFaces, recognizedFaces ->
		detectedFaces.map { detectedFace ->
			val recognizedFace = recognizedFaces
				.firstOrNull { it.id == detectedFace.id }
				?.recognizedFace ?: RecognizedFace()
			FaceRecognition(recognizedFace, detectedFace.boundingBox.toRect(), detectedFace.bitmap)
		}
	}

	override fun processFrame(image: ImageProxy, rotationDegrees: Int, previewTransform: CoordinateTransform) {
		coroutineScope.launch {
			val faces = image.use { imageProxy ->
				faceDetector.detect(Image(imageProxy), previewTransform)
			}
			detectedFaces.emit(faces)
		}
	}

	override fun close() {
		faceDetector.close()
		faceRecognizer.close()
		coroutineScope.cancel()
	}

	private suspend inline fun recognizeFaces(detectedFaces: List<DetectedFace>) = coroutineScope {
		detectedFaces
			.map { face ->
				async { RecognizedFaceWithId(face.id, faceRecognizer.recognize(face.bitmap)) }
			}
			.awaitAll()
	}
}

private data class RecognizedFaceWithId(val id: Int, val recognizedFace: RecognizedFace)