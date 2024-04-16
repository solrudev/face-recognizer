package ru.solrudev.facerecognizer.util

import android.Manifest
import android.content.pm.PackageManager
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import ru.solrudev.facerecognizer.R

const val CAMERA_PERMISSION = Manifest.permission.CAMERA

inline fun Fragment.registerForCameraPermissionResult(crossinline onGranted: () -> Unit) =
	registerForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
		if (granted) {
			onGranted()
		} else {
			Toast.makeText(
				requireContext(),
				R.string.camera_permission_not_granted,
				Toast.LENGTH_SHORT
			).show()
			findNavController().popBackStack()
		}
	}

fun Fragment.checkOrRequestCameraPermission(launcher: ActivityResultLauncher<String>) {
	if (!isCameraPermissionGranted()) {
		launcher.launch(CAMERA_PERMISSION)
	}
}

fun Fragment.isCameraPermissionGranted() = ContextCompat.checkSelfPermission(
	requireContext(), CAMERA_PERMISSION
) == PackageManager.PERMISSION_GRANTED

inline fun Fragment.launchRepeatOnViewLifecycle(
	crossinline action: suspend CoroutineScope.() -> Unit
) = viewLifecycleOwner.lifecycleScope.launch {
	viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
		action()
	}
}