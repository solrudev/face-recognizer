package ru.solrudev.facerecognizer

import android.graphics.Bitmap
import android.graphics.Rect

data class FaceRecognition(val face: RecognizedFace, val boundingBox: Rect, val bitmap: Bitmap)