package ru.solrudev.facerecognizer.di

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import ru.solrudev.facerecognizer.FaceEmbeddingsRepository
import ru.solrudev.facerecognizer.InMemoryFaceEmbeddingsRepository

@InstallIn(SingletonComponent::class)
@Module
interface RepositoryBindModule {

	@Binds
	fun bindFaceEmbeddingsRepository(faceEmbeddingsRepository: InMemoryFaceEmbeddingsRepository): FaceEmbeddingsRepository
}