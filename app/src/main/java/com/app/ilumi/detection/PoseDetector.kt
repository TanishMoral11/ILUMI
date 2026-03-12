package com.app.ilumi.detection

import android.content.Context
import android.graphics.Bitmap
import android.os.SystemClock
import android.util.Log
import com.google.mediapipe.framework.image.BitmapImageBuilder
import com.google.mediapipe.framework.image.MPImage
import com.google.mediapipe.tasks.core.BaseOptions
import com.google.mediapipe.tasks.core.Delegate
import com.google.mediapipe.tasks.vision.core.RunningMode
import com.google.mediapipe.tasks.vision.poselandmarker.PoseLandmarker
import com.google.mediapipe.tasks.vision.poselandmarker.PoseLandmarkerResult

class PoseDetector(
    private val context: Context,
    private val poseLandmarkerResultListener: PoseLandmarkerListener? = null
) {
    private var poseLandmarker: PoseLandmarker? = null

    init {
        setupPoseLandmarker()
    }

    private fun setupPoseLandmarker() {
        val baseOptionsBuilder = BaseOptions.builder()
            .setModelAssetPath("models/pose_landmarker_lite.task")
            .setDelegate(Delegate.CPU)

        try {
            val baseOptions = baseOptionsBuilder.build()
            val optionsBuilder = PoseLandmarker.PoseLandmarkerOptions.builder()
                .setBaseOptions(baseOptions)
                .setMinPoseDetectionConfidence(0.5f)
                .setMinTrackingConfidence(0.5f)
                .setMinPosePresenceConfidence(0.5f)
                .setRunningMode(RunningMode.LIVE_STREAM)
                .setResultListener(this::returnLivestreamResult)
                .setErrorListener(this::returnLivestreamError)

            val options = optionsBuilder.build()
            poseLandmarker = PoseLandmarker.createFromOptions(context, options)
        } catch (e: IllegalStateException) {
            Log.e(TAG, "MediaPipe failed to initialize.", e)
        } catch (e: RuntimeException) {
            Log.e(TAG, "Pose landmarker model not found.", e)
        }
    }

    fun detectLiveStream(bitmap: Bitmap, isFrontCamera: Boolean) {
        if (poseLandmarker == null) return

        val frameTime = SystemClock.uptimeMillis()
        val mpImage = BitmapImageBuilder(bitmap).build()
        
        try {
            // Note: MediaPipe tasks handle camera rotation implicitly if configured, 
            // but for simple bitmaps we pass them directly.
            // If the image is mirrored (front camera), we should be aware for coordinate mapping.
            poseLandmarker?.detectAsync(mpImage, frameTime)
        } catch (e: Exception) {
            Log.e(TAG, "Error running detection: \${e.message}")
        }
    }

    private fun returnLivestreamResult(
        result: PoseLandmarkerResult,
        input: MPImage
    ) {
        val finishTimeMs = SystemClock.uptimeMillis()
        val inferenceTime = finishTimeMs - result.timestampMs()

        poseLandmarkerResultListener?.onResults(
            ResultBundle(
                listOf(result),
                inferenceTime,
                input.height,
                input.width
            )
        )
    }

    private fun returnLivestreamError(error: RuntimeException) {
        poseLandmarkerResultListener?.onError(error.message ?: "An unknown error has occurred")
    }

    fun clear() {
        poseLandmarker?.close()
        poseLandmarker = null
    }

    companion object {
        const val TAG = "PoseDetector"
        
        // Key landmark indices for necklace placement
        const val LEFT_SHOULDER = 11
        const val RIGHT_SHOULDER = 12
    }

    interface PoseLandmarkerListener {
        fun onError(error: String)
        fun onResults(resultBundle: ResultBundle)
    }

    data class ResultBundle(
        val results: List<PoseLandmarkerResult>,
        val inferenceTime: Long,
        val inputImageHeight: Int,
        val inputImageWidth: Int
    )
}
