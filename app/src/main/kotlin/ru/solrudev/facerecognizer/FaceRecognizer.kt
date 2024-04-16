package ru.solrudev.facerecognizer

import android.graphics.Bitmap
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.runInterruptible
import org.tensorflow.lite.support.common.ops.NormalizeOp
import org.tensorflow.lite.support.image.ImageProcessor
import org.tensorflow.lite.support.image.TensorImage
import org.tensorflow.lite.support.image.ops.ResizeOp
import ru.solrudev.facerecognizer.di.DefaultDispatcher
import ru.solrudev.facerecognizer.ml.MobileFaceNet
import javax.inject.Inject
import kotlin.coroutines.coroutineContext
import kotlin.math.sqrt

private const val FACE_DISTANCE_THRESHOLD = 1f

interface FaceRecognizer : AutoCloseable {
	suspend fun recognize(bitmap: Bitmap): RecognizedFace
}

class TfLiteFaceRecognizer @Inject constructor(
	private val faceRecognizer: MobileFaceNet,
	private val faceEmbeddingsRepository: FaceEmbeddingsRepository,
	@DefaultDispatcher private val defaultDispatcher: CoroutineDispatcher
) : FaceRecognizer {

	private val facePreprocessor = ImageProcessor.Builder()
		.add(ResizeOp(112, 112, ResizeOp.ResizeMethod.BILINEAR))
		.add(NormalizeOp(128f, 128f))
		.build()

	override suspend fun recognize(bitmap: Bitmap): RecognizedFace {
		val faceTensorImage = TensorImage.fromBitmap(bitmap)
		val embeddings = runInterruptible(defaultDispatcher) {
			val tensorImage = facePreprocessor.process(faceTensorImage)
			val outputs = faceRecognizer.process(tensorImage.tensorBuffer)
			outputs.outputFeature0AsTensorBuffer.floatArray
		}
		return findNearestRegisteredFace(embeddings)
	}

	override fun close() {
		faceRecognizer.close()
	}

	private suspend inline fun findNearestRegisteredFace(embeddings: FloatArray) = coroutineScope {
		val knownEmbeddings = faceEmbeddingsRepository.getAllFaceEmbeddings()
		if (knownEmbeddings.isEmpty()) {
			return@coroutineScope RecognizedFace(embeddings = embeddings)
		}
		val nearestFace = knownEmbeddings
			.map { faceEmbeddings ->
				async(defaultDispatcher) {
					val distance = euclideanDistance(embeddings, faceEmbeddings.embeddings)
					RecognizedFace(faceEmbeddings.name, distance, embeddings)
				}
			}
			.awaitAll()
			.minBy { it.distance }
		if (nearestFace.distance > FACE_DISTANCE_THRESHOLD) {
			return@coroutineScope RecognizedFace(embeddings = embeddings)
		}
		return@coroutineScope nearestFace
	}

	private suspend inline fun euclideanDistance(embeddings1: FloatArray, embeddings2: FloatArray) = sqrt(
		embeddings1.foldIndexed(initial = 0f) { index, acc, value ->
			coroutineContext.ensureActive()
			val diff = value - embeddings2[index]
			acc + (diff * diff)
		}
	)
}

data class RecognizedFace(
	val name: String = "",
	val distance: Float = 0f,
	val embeddings: FloatArray = floatArrayOf()
) {

	val isUnknown: Boolean
		get() = name == "" && distance == 0f

	override fun equals(other: Any?): Boolean {
		if (this === other) return true
		if (javaClass != other?.javaClass) return false
		other as RecognizedFace
		if (name != other.name) return false
		if (distance != other.distance) return false
		return embeddings.contentEquals(other.embeddings)
	}

	override fun hashCode(): Int {
		var result = name.hashCode()
		result = 31 * result + distance.hashCode()
		result = 31 * result + embeddings.contentHashCode()
		return result
	}
}