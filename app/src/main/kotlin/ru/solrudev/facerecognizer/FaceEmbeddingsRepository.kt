package ru.solrudev.facerecognizer

import javax.inject.Inject
import javax.inject.Singleton

interface FaceEmbeddingsRepository {
	suspend fun addFaceEmbeddings(faceEmbeddings: FaceEmbeddings)
	suspend fun getAllFaceEmbeddings(): List<FaceEmbeddings>
}

@Singleton
class InMemoryFaceEmbeddingsRepository @Inject constructor() : FaceEmbeddingsRepository {

	private val faceEmbeddingsList = mutableListOf<FaceEmbeddings>()

	override suspend fun addFaceEmbeddings(faceEmbeddings: FaceEmbeddings) {
		faceEmbeddingsList += faceEmbeddings
	}

	override suspend fun getAllFaceEmbeddings(): List<FaceEmbeddings> {
		return faceEmbeddingsList
	}
}

data class FaceEmbeddings(val name: String, val embeddings: FloatArray) {

	override fun equals(other: Any?): Boolean {
		if (this === other) return true
		if (javaClass != other?.javaClass) return false
		other as FaceEmbeddings
		if (name != other.name) return false
		return embeddings.contentEquals(other.embeddings)
	}

	override fun hashCode(): Int {
		var result = name.hashCode()
		result = 31 * result + embeddings.contentHashCode()
		return result
	}
}