package ru.solrudev.facerecognizer.util

import com.google.android.gms.tasks.Task
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

suspend fun <TResult> Task<TResult>.await(): TResult = suspendCancellableCoroutine { continuation ->
	addOnCanceledListener { continuation.cancel() }
	addOnSuccessListener { continuation.resume(it) }
	addOnFailureListener { continuation.resumeWithException(it) }
}