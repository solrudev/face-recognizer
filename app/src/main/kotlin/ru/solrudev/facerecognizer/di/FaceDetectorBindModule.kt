package ru.solrudev.facerecognizer.di

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import ru.solrudev.facerecognizer.FaceDetector
import ru.solrudev.facerecognizer.MlKitFaceDetector

@InstallIn(SingletonComponent::class)
@Module
interface FaceDetectorBindModule {

	@Binds
	fun bindFaceDetector(faceDetector: MlKitFaceDetector): FaceDetector
}