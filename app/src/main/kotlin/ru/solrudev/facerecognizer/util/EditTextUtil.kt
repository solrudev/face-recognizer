package ru.solrudev.facerecognizer.util

import android.widget.EditText

fun EditText.setString(string: String) {
	setText(string)
	text?.let { text ->
		setSelection(text.length)
	}
}