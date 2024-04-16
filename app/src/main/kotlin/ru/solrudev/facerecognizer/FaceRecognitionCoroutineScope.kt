package ru.solrudev.facerecognizer

import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import ru.solrudev.facerecognizer.di.DefaultDispatcher
import javax.inject.Inject

interface FaceRecognitionCoroutineScope : CoroutineScope

class FaceRecognitionCoroutineScopeImpl @Inject constructor(
	@DefaultDispatcher dispatcher: CoroutineDispatcher
) : FaceRecognitionCoroutineScope, CoroutineScope by CoroutineScope(dispatcher)