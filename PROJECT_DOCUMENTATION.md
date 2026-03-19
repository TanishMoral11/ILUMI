# ILUMI: AR Jewelry Try-On Application

## Project Overview

**ILUMI** is a cutting-edge Augmented Reality (AR) mobile application built for Android. The project aims to revolutionize the jewelry shopping experience by allowing users to virtually "try on" jewelry pieces using their smartphone camera.

### Problem it Solves
Online jewelry shopping often suffers from a lack of physical interaction, making it difficult for customers to visualize how a piece would look on them. ILUMI bridges this gap by providing a real-time, 3D visualization of jewelry accurately placed on the user's body, reducing the uncertainty in purchasing decisions.

### Key Features
- **Real-time Pose Detection**: Accurately identifies body landmarks (shoulders, neck, etc.) using machine learning.
- **3D Augmented Reality Rendering**: Overlays high-quality 3D models (GLB format) onto the live camera feed.
- **Dynamic Placement**: Automatically adjusts the jewelry's position and rotation based on the user's movements.
- **Smooth Interaction**: Fast processing ensuring a low-latency AR experience.

---

## Technologies & Tools Used

| Technology | Purpose | Why Chosen | How it is used |
| :--- | :--- | :--- | :--- |
| **Kotlin** | Core Language | Robust, expressive, and the industry standard for modern Android development. | Used for all application logic, from detection to rendering orchestration. |
| **Jetpack Compose** | UI Framework | Modern declarative UI toolkit that simplifies UI development and state management. | Used in `MainActivity` to create the camera overlay, buttons, and overall screen layout. |
| **Google MediaPipe** | Machine Learning | High-performance, cross-platform ML solution for real-time vision tasks. | `PoseLandmarker` is used in `PoseDetector.kt` to extract body landmarks from camera frames. |
| **ARSceneView** | AR/3D Rendering | A powerful, user-friendly library built on top of Sceneform/Filament for AR and 3D rendering. | Used in `JewelryPlacementEngine.kt` to load the `necklace.glb` model and manage its position in the 3D scene. |
| **CameraX** | Camera Operations | Lifecycle-aware camera library that simplifies camera integration across different Android devices. | Used in `MainActivity.kt` to manage the camera lifecycle, preview, and image analysis for MediaPipe. |
| **Gradle** | Build System | Flexible and powerful build automation tool. | Manages dependencies and build configurations (KTS scripts). |

### Example: MediaPipe Implementation
In `PoseDetector.kt`, MediaPipe is initialized as follows:
```kotlin
val options = PoseLandmarkerOptions.builder()
    .setBaseOptions(BaseOptions.builder().setModelAssetPath("models/pose_landmarker_lite.task").build())
    .setRunningMode(RunningMode.LIVE_STREAM)
    .build()
poseLandmarker = PoseLandmarker.createFromOptions(context, options)
```

---

## Architecture / System Design

The project follows a modular architecture designed for performance and clarity.

### Modules & Interactions
1.  **Detection Module (`PoseDetector`)**: Consumes raw image frames from `CameraX`. It processes these frames using MediaPipe to detect pose landmarks. These landmarks are passed to the UI layer via flows or callbacks.
2.  **Placement Module (`JewelryPlacementEngine`)**: Acts as the bridge between ML detections and AR rendering. It calculates the 3D coordinates (x, y, z) and rotation based on the 2D landmarks received from the detector.
3.  **UI/Orchestration (`MainActivity`)**: Coordinates the camera stream, detection lifecycle, and the AR scene lifecycle. It ensures that frames are passed to the detector and the resulting landmarks are used by the placement engine to update the 3D model in real-time.

---

## Implementation Details

### Key Logic: Pose-to-3D Mapping
The most critical part of the application is mapping 2D image coordinates (landmarks) to 3D AR space.
- **Landmark Extraction**: The `PoseDetector` retrieves landmarks for the left and right shoulders (indices 11 and 12).
- **Position Calculation**: The necklace is placed at the midpoint between the two shoulders, adjusted slightly upwards to sit at the base of the neck.
- **Rotation Logic**: The engine calculates the angle between the shoulders to ensure the necklace tilts appropriately as the user moves.

### Workflow
1.  `CameraX` captures a frame.
2.  Frame is passed to `PoseDetector` (MediaPipe).
3.  `PoseDetector` identifies landmarks and returns them.
4.  `JewelryPlacementEngine` processes landmarks and updates the `ModelNode`'s `worldPosition` and `worldRotation`.
5.  `ARSceneView` renders the updated 3D model over the camera feed.

---

## Folder Structure / Code Organization

```text
com.app.ilumi/
├── detection/
│   └── PoseDetector.kt         # Logic for MediaPipe Pose Landmarker
├── placement/
│   └── JewelryPlacementEngine.kt # Logic for AR model placement and updates
├── ui/
│   └── theme/                  # Theme, Colors, and Typography definitions
└── MainActivity.kt             # Main entry point and UI controller
---
assets/
└── models/
    ├── necklace.glb            # 3D necklace asset
    └── pose_landmarker_lite.task # MediaPipe ML model
```

---

## Challenges Faced & Solutions

### Challenge 1: Coordinate System Mismatch
**Problem**: MediaPipe landmarks are in normalized image coordinates (0 to 1), while ARSceneView uses world coordinates in meters.
**Solution**: Implemented a transformation utility inside `JewelryPlacementEngine` that maps the normalized landmarks to the AR scene's coordinate system, taking into account the camera's field of view and aspect ratio.

### Challenge 2: Jittery Model Movement
**Problem**: Small variations in detected landmarks caused the 3D model to "jitter."
**Solution**: Applied a Low-Pass Filter (Simple Exponential Smoothing) to the landmark positions before updating the model's position, resulting in a much smoother visual experience.

---

## Conclusion & Future Improvements

ILUMI successfully demonstrates a functional AR jewelry try-on prototype. The integration of MediaPipe and ARSceneView provides a solid foundation for more advanced features.

### Future Improvements
- **Multi-Jewelry Support**: Add support for earrings, rings, and bracelets using specialized MediaPipe tasks (Hand Landmarker, Face Landmarker).
- **Occlusion Handling**: Implement depth sensing and segmentation to allow the user's hair or clothes to realistically hide parts of the jewelry.
- **Lighting Estimation**: Use ARCore's lighting estimation to match the 3D model's shadows and highlights with the real-world environment.
- **E-commerce Integration**: Connect to a backend API to fetch real jewelry data and prices.
