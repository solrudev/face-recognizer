package ru.solrudev.facerecognizer.ui.facecapture

import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import ru.solrudev.facerecognizer.FaceEmbeddings
import ru.solrudev.facerecognizer.FaceEmbeddingsRepository
import ru.solrudev.facerecognizer.FaceRecognition
import ru.solrudev.facerecognizer.ui.DialogUiState
import javax.inject.Inject

@HiltViewModel
class FaceCaptureViewModel @Inject constructor(
	private val faceEmbeddingsRepository: FaceEmbeddingsRepository
) : ViewModel(), DefaultLifecycleObserver {

	private var lastResult: FaceRecognition? = null
	private val _uiState = MutableStateFlow(FaceCaptureUiState())
	val uiState = _uiState.asStateFlow()

	override fun onStop(owner: LifecycleOwner) = _uiState.update {
		val addFaceDialog = it.addFaceDialog.copy(isVisible = false)
		it.copy(addFaceDialog = addFaceDialog)
	}

	fun onAddedNameChanged(name: CharSequence?) = _uiState.update {
		val addFaceDialog = it.addFaceDialog.dialog?.copy(name = name?.toString().orEmpty())
		it.copy(addFaceDialog = it.addFaceDialog.copy(dialog = addFaceDialog))
	}

	fun showAddFaceDialog() {
		if (lastResult == null) {
			return
		}
		_uiState.update {
			val addFaceDialog = AddFaceDialog(bitmap = lastResult?.bitmap)
			it.copy(addFaceDialog = it.addFaceDialog.copy(dialog = addFaceDialog))
		}
	}

	fun onAddFaceDialogShowed() = _uiState.update {
		val addFaceDialog = it.addFaceDialog.copy(isVisible = true)
		it.copy(addFaceDialog = addFaceDialog)
	}

	fun onFaceAdded(name: String) = viewModelScope.launch {
		val faceEmbeddings = FaceEmbeddings(name, lastResult?.face?.embeddings ?: floatArrayOf())
		faceEmbeddingsRepository.addFaceEmbeddings(faceEmbeddings)
	}

	fun onAddFaceDialogDismissed() = _uiState.update {
		it.copy(addFaceDialog = DialogUiState())
	}

	fun switchCameraLens() = _uiState.update {
		val cameraLens = it.cameraLens.switch()
		it.copy(cameraLens = cameraLens)
	}

	fun onFaceRecognitionResults(results: List<FaceRecognition>) {
		val result = results.firstOrNull()
		if (_uiState.value.addFaceDialog.dialog == null) {
			lastResult = result
		}
	}
}