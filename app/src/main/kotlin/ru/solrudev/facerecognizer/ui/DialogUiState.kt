package ru.solrudev.facerecognizer.ui

data class DialogUiState<T>(val isVisible: Boolean = false, val dialog: T? = null) {
	val shouldShow: Boolean
		get() = !isVisible && dialog != null
}