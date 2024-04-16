package ru.solrudev.facerecognizer.ui.facecapture

import androidx.annotation.OptIn
import androidx.camera.view.TransformExperimental
import androidx.camera.view.transform.OutputTransform
import dagger.assisted.AssistedFactory

@AssistedFactory
interface FaceRecognitionAnalyzerFactory {
	@OptIn(TransformExperimental::class)
	fun create(previewOutputTransformProvider: () -> OutputTransform): FaceRecognitionAnalyzer
}