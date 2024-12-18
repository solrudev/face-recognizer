package ru.solrudev.facerecognizer.ui.facecapture

import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
import android.os.Bundle
import android.view.View
import androidx.annotation.OptIn
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.lifecycle.awaitInstance
import androidx.camera.view.TransformExperimental
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import by.kirich1409.viewbindingdelegate.viewBinding
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.asExecutor
import kotlinx.coroutines.flow.distinctUntilChangedBy
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import ru.solrudev.facerecognizer.R
import ru.solrudev.facerecognizer.databinding.DialogAddFaceBinding
import ru.solrudev.facerecognizer.databinding.FragmentCaptureFaceBinding
import ru.solrudev.facerecognizer.di.DefaultDispatcher
import ru.solrudev.facerecognizer.util.checkOrRequestCameraPermission
import ru.solrudev.facerecognizer.util.isCameraPermissionGranted
import ru.solrudev.facerecognizer.util.launchRepeatOnViewLifecycle
import ru.solrudev.facerecognizer.util.registerForCameraPermissionResult
import ru.solrudev.facerecognizer.util.setString
import ru.solrudev.facerecognizer.util.showWithLifecycle
import java.util.concurrent.Executor
import javax.inject.Inject

@AndroidEntryPoint
class FaceCaptureFragment : Fragment(R.layout.fragment_capture_face) {

	private val binding by viewBinding(FragmentCaptureFaceBinding::bind)
	private val viewModel: FaceCaptureViewModel by viewModels()
	private val requestPermissionLauncher = registerForCameraPermissionResult { startCamera() }
	private var faceRecognitionAnalyzer: FaceRecognitionAnalyzer? = null
	private lateinit var cameraExecutor: Executor
	private var faceRecognitionResultsRenderJob: Job? = null

	@Inject
	lateinit var faceRecognitionAnalyzerFactory: FaceRecognitionAnalyzerFactory

	@Inject
	@DefaultDispatcher
	lateinit var defaultDispatcher: CoroutineDispatcher

	@SuppressLint("SourceLockedOrientationActivity")
	override fun onAttach(context: Context) {
		super.onAttach(context)
		cameraExecutor = defaultDispatcher.asExecutor()
		requireActivity().requestedOrientation = SCREEN_ORIENTATION_PORTRAIT
	}

	override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
		checkOrRequestCameraPermission(requestPermissionLauncher)
		binding.buttonCaptureFaceAdd.setOnClickListener {
			viewModel.showAddFaceDialog()
		}
		binding.buttonCaptureFaceCameraLens.setOnClickListener {
			viewModel.switchCameraLens()
		}
		viewLifecycleOwner.lifecycle.addObserver(viewModel)
		startRender()
	}

	override fun onDestroy() {
		faceRecognitionAnalyzer?.close()
		super.onDestroy()
	}

	private fun startRender() = launchRepeatOnViewLifecycle {
		renderAddFaceDialog()
		renderCameraLens()
	}

	private fun CoroutineScope.renderAddFaceDialog() {
		viewModel.uiState
			.distinctUntilChangedBy { it.addFaceDialog }
			.filter { it.addFaceDialog.shouldShow }
			.onEach { showAddFaceDialog(it.addFaceDialog.dialog!!) }
			.launchIn(this)
	}

	private fun CoroutineScope.renderCameraLens() {
		viewModel.uiState
			.distinctUntilChangedBy { it.cameraLens }
			.filter { isCameraPermissionGranted() }
			.onEach { startCamera(it.cameraLens.toCameraSelectorLens()) }
			.launchIn(this)
	}

	private fun FaceRecognitionAnalyzer.startResultsRender() = launchRepeatOnViewLifecycle {
		results.collect { results ->
			viewModel.onFaceRecognitionResults(results)
			binding.overlayFaceCaptureResults.drawResults(results)
		}
	}

	@OptIn(TransformExperimental::class)
	private fun startCamera(cameraLens: Int = viewModel.uiState.value.cameraLens.toCameraSelectorLens()) {
		val faceRecognitionAnalyzer = faceRecognitionAnalyzerFactory.create(
			previewOutputTransformProvider = { binding.previewViewFaceCapture.outputTransform!! }
		)
		this.faceRecognitionAnalyzer?.close()
		this.faceRecognitionAnalyzer = faceRecognitionAnalyzer
		faceRecognitionResultsRenderJob?.cancel()
		faceRecognitionResultsRenderJob = faceRecognitionAnalyzer.startResultsRender()
		lifecycleScope.launch {
			startCamera(faceRecognitionAnalyzer, cameraLens)
		}
	}

	private suspend inline fun startCamera(analyzer: FaceRecognitionAnalyzer, cameraLens: Int) {
		val cameraProvider = ProcessCameraProvider.awaitInstance(requireContext())
		if (binding.previewViewFaceCapture.display == null) {
			return
		}
		val preview = Preview.Builder()
			.build()
			.also { it.surfaceProvider = binding.previewViewFaceCapture.surfaceProvider }
		val imageAnalysis = ImageAnalysis.Builder()
			.setTargetRotation(binding.previewViewFaceCapture.display.rotation)
			.setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
			.build()
			.also { it.setAnalyzer(cameraExecutor, analyzer) }
		val cameraSelector = CameraSelector.Builder()
			.requireLensFacing(cameraLens)
			.build()
		cameraProvider.unbindAll()
		cameraProvider.bindToLifecycle(this, cameraSelector, preview, imageAnalysis)
	}

	private fun showAddFaceDialog(dialog: AddFaceDialog) {
		val dialogBinding = DialogAddFaceBinding.inflate(layoutInflater).apply {
			imageViewAddFacePhoto.setImageBitmap(dialog.bitmap)
			editTextAddFaceName.setString(dialog.name)
			editTextAddFaceName.addTextChangedListener(onTextChanged = { text, _, _, _ ->
				viewModel.onAddedNameChanged(text)
			})
		}
		MaterialAlertDialogBuilder(requireContext())
			.setView(dialogBinding.root)
			.setPositiveButton(R.string.add) { _, _ ->
				val name = dialogBinding.editTextAddFaceName.text.toString()
				viewModel.onFaceAdded(name)
			}
			.setOnDismissListener {
				viewModel.onAddFaceDialogDismissed()
			}
			.showWithLifecycle(viewLifecycleOwner.lifecycle)
		viewModel.onAddFaceDialogShowed()
	}
}