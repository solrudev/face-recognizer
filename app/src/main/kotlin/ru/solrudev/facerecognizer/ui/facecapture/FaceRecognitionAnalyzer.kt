package ru.solrudev.facerecognizer.ui.facecapture

import android.util.Size
import androidx.annotation.OptIn
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageProxy
import androidx.camera.view.TransformExperimental
import androidx.camera.view.transform.CoordinateTransform
import androidx.camera.view.transform.ImageProxyTransformFactory
import androidx.camera.view.transform.OutputTransform
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.runBlocking
import ru.solrudev.facerecognizer.FaceRecognitionProcessor
import ru.solrudev.facerecognizer.di.MainDispatcher

@OptIn(TransformExperimental::class)
class FaceRecognitionAnalyzer @AssistedInject constructor(
	private val faceRecognitionProcessor: FaceRecognitionProcessor,
	@MainDispatcher private val mainDispatcher: CoroutineDispatcher,
	@Assisted private val previewOutputTransformProvider: () -> OutputTransform,
) : ImageAnalysis.Analyzer, AutoCloseable {

	val results = faceRecognitionProcessor.results

	@Volatile
	private var isClosed = false

	private val previewOutputTransform by lazy {
		runBlocking(mainDispatcher) {
			previewOutputTransformProvider()
		}
	}

	private val imageProxyTransformFactory = ImageProxyTransformFactory()
	private val size = Size(480, 360)

	override fun analyze(image: ImageProxy) {
		if (isClosed) {
			return
		}
		val sourceTransform = imageProxyTransformFactory.getOutputTransform(image)
		val previewTransform = CoordinateTransform(sourceTransform, previewOutputTransform)
		faceRecognitionProcessor.processFrame(image, previewTransform)
	}

	override fun getDefaultTargetResolution(): Size {
		return size
	}

	override fun close() {
		isClosed = true
		faceRecognitionProcessor.close()
	}
}