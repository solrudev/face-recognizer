package ru.solrudev.facerecognizer.di

import com.google.mlkit.vision.face.FaceDetection
import com.google.mlkit.vision.face.FaceDetector
import com.google.mlkit.vision.face.FaceDetectorOptions
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.asExecutor

@InstallIn(SingletonComponent::class)
@Module(includes = [FaceDetectorBindModule::class])
object FaceDetectorModule {

	@Provides
	fun provideFaceDetector(@DefaultDispatcher defaultDispatcher: CoroutineDispatcher): FaceDetector {
		val detectorOptions = FaceDetectorOptions.Builder()
			.setPerformanceMode(FaceDetectorOptions.PERFORMANCE_MODE_FAST)
			.setContourMode(FaceDetectorOptions.CONTOUR_MODE_NONE)
			.setLandmarkMode(FaceDetectorOptions.LANDMARK_MODE_NONE)
			.setClassificationMode(FaceDetectorOptions.CLASSIFICATION_MODE_NONE)
			.setMinFaceSize(0.5f)
			.enableTracking()
			.setExecutor(defaultDispatcher.asExecutor())
			.build()
		return FaceDetection.getClient(detectorOptions)
	}
}