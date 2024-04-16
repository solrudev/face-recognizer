package ru.solrudev.facerecognizer.ui.facecapture

import androidx.camera.core.CameraSelector

enum class CameraLens {
	FRONT, BACK
}

fun CameraLens.switch() = when (this) {
	CameraLens.BACK -> CameraLens.FRONT
	CameraLens.FRONT -> CameraLens.BACK
}

fun CameraLens.toCameraSelectorLens() = when (this) {
	CameraLens.FRONT -> CameraSelector.LENS_FACING_FRONT
	CameraLens.BACK -> CameraSelector.LENS_FACING_BACK
}