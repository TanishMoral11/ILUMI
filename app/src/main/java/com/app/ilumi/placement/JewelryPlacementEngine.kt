package com.app.ilumi.placement

import com.app.ilumi.detection.PoseDetector
import com.google.mediapipe.tasks.components.containers.NormalizedLandmark
import dev.romainguy.kotlin.math.Float3
import kotlin.math.abs
import kotlin.math.max

object JewelryPlacementEngine {

    // State for Low-Pass Filter (Exponential Smoothing)
    private var lastPosition: Float3? = null
    private var lastScale: Float = 0f
    
    // Smoothing factor: Lower = smoother but lags, Higher = faster but jittery
    private const val ALPHA = 0.3f

    fun calculateNecklacePositionAndScale(
        landmarks: List<NormalizedLandmark>,
        isFrontCamera: Boolean,
        sceneWidth: Int,
        sceneHeight: Int
    ): Pair<Float3, Float>? {
        // Need basic landmarks
        if (landmarks.size <= PoseDetector.RIGHT_SHOULDER) {
            lastPosition = null
            lastScale = 0f
            return null
        }
        
        // --- Fetch Landmarks ---
        val nose = landmarks[0]
        val leftEye = landmarks[2]
        val rightEye = landmarks[5]
        val leftEar = landmarks[7]
        val rightEar = landmarks[8]
        val mouthLeft = landmarks[9]
        val mouthRight = landmarks[10]
        val leftShoulder = landmarks[PoseDetector.LEFT_SHOULDER]
        val rightShoulder = landmarks[PoseDetector.RIGHT_SHOULDER]

        // --- Extrapolate Additional Features ---
        
        // 1. Lips (Center)
        val lipsX = (mouthLeft.x() + mouthRight.x()) / 2f
        val lipsY = (mouthLeft.y() + mouthRight.y()) / 2f

        // 2. Eyes (Center)
        val eyesCenterX = (leftEye.x() + rightEye.x()) / 2f
        val eyesCenterY = (leftEye.y() + rightEye.y()) / 2f

        // Face scale based on distance between ears & eyes
        val faceWidth = abs(leftEar.x() - rightEar.x())
        val eyeToMouthDist = abs(eyesCenterY - lipsY)

        // 3. Chin (Estimated)
        // Chin is typically below the lips by about the distance from nose to lips, or a fraction of eye-to-mouth
        val chinX = lipsX
        val chinY = lipsY + (eyeToMouthDist * 0.6f)

        // Shoulders (Center)
        val shoulderMidX = (leftShoulder.x() + rightShoulder.x()) / 2f
        val shoulderMidY = (leftShoulder.y() + rightShoulder.y()) / 2f

        // 4. Neck (Estimated)
        // Neck is between chin and shoulders
        // Weight it depending on how the head moves. Let's make it more stable by anchoring between the chin and the shoulders.
        val neckX = (chinX + shoulderMidX) / 2f
        // The neck is closer to the shoulders in terms of Y for a necklace to rest properly
        val neckY = (chinY * 0.4f + shoulderMidY * 0.6f)

        // --- Determine Final Position ---
        // For a necklace, we want the placement to rest exactly on the neck,
        // draping over the chest.
        // We use the calculated neck values.
        val targetX = neckX
        val targetY = neckY

        // Convert normalized coordinates (0..1) to SceneView screen space [-1, 1]
        var mappedX = (targetX - 0.5f) * 2f
        if (isFrontCamera) {
            mappedX = -mappedX // Mirror X for front camera
        }
        val mappedY = -(targetY - 0.5f) * 2f
        
        // --- Calculate Dynamic Scale ---
        // The width of shoulders determines depth. Face width helps refine it.
        val shoulderDist = abs(leftShoulder.x() - rightShoulder.x())
        // Combine them to get a robust scale metric (shoulders can be sideways).
        // If faceWidth is 0.2, maybe head is close.
        val scaleMetric = max(shoulderDist, faceWidth * 2f)
        
        // Magic multiplier for standard GLB size
        val targetScale = scaleMetric * 2.2f

        val targetPosition = Float3(mappedX, mappedY, 0f)
        
        // --- Apply Low-Pass Filter ---
        val finalPosition = if (lastPosition == null) {
            targetPosition
        } else {
            Float3(
                (targetPosition.x * ALPHA) + (lastPosition!!.x * (1 - ALPHA)),
                (targetPosition.y * ALPHA) + (lastPosition!!.y * (1 - ALPHA)),
                (targetPosition.z * ALPHA) + (lastPosition!!.z * (1 - ALPHA))
            )
        }
        
        val finalScale = if (lastScale == 0f) {
            targetScale
        } else {
            (targetScale * ALPHA) + (lastScale * (1 - ALPHA))
        }
        
        lastPosition = finalPosition
        lastScale = finalScale

        return Pair(finalPosition, finalScale)
    }
}
