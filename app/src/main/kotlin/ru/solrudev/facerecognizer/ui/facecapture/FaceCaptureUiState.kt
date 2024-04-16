package ru.solrudev.facerecognizer.ui.facecapture

import android.graphics.Bitmap
import ru.solrudev.facerecognizer.ui.DialogUiState

data class FaceCaptureUiState(
	val cameraLens: CameraLens = CameraLens.FRONT,
	val addFaceDialog: DialogUiState<AddFaceDialog> = DialogUiState()
)

data class AddFaceDialog(val name: String = "", val bitmap: Bitmap?)