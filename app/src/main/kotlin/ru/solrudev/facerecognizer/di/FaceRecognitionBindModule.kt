package ru.solrudev.facerecognizer.di

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import ru.solrudev.facerecognizer.FaceRecognitionCoroutineScope
import ru.solrudev.facerecognizer.FaceRecognitionCoroutineScopeImpl
import ru.solrudev.facerecognizer.FaceRecognitionProcessor
import ru.solrudev.facerecognizer.FaceRecognitionProcessorImpl
import ru.solrudev.facerecognizer.FaceRecognizer
import ru.solrudev.facerecognizer.TfLiteFaceRecognizer

@InstallIn(SingletonComponent::class)
@Module
interface FaceRecognitionBindModule {

	@Binds
	fun bindFaceRecognizer(faceRecognizer: TfLiteFaceRecognizer): FaceRecognizer

	@Binds
	fun bindFaceRecognitionProcessor(faceRecognitionProcessor: FaceRecognitionProcessorImpl): FaceRecognitionProcessor

	@Binds
	fun bindFaceRecognitionCoroutineScope(
		faceRecognitionCoroutineScope: FaceRecognitionCoroutineScopeImpl
	): FaceRecognitionCoroutineScope
}