# AR Jewelry Try-On System
## Complete Development Plan

---

# 1. Project Overview

## 1.1 Project Name
AR Jewelry Try-On System

## 1.2 Description
The AR Jewelry Try-On System is an Android application that allows users to virtually try jewelry such as earrings, necklaces, rings, and bracelets using the device camera.

The application detects facial landmarks, hand landmarks, and body pose using machine learning and overlays jewelry assets on the detected positions in real time.

The system uses modern Android technologies including:

- Kotlin
- Jetpack Compose
- CameraX
- MediaPipe
- Android Canvas Rendering

This application demonstrates Augmented Reality capabilities for online jewelry shopping experiences.

---

# 2. Main Use Cases

## 2.1 Virtual Jewelry Try-On
Users can see how jewelry looks on them before purchasing.

## 2.2 Online Shopping Visualization
E-commerce platforms can integrate this system to preview jewelry on users.

## 2.3 AR Technology Demonstration
Demonstrates real-time computer vision and AR capabilities.

---

# 3. Core Features

### Feature 1 — Live Camera Preview
Display real-time camera feed using CameraX.

### Feature 2 — Face Landmark Detection
Detect facial landmarks using MediaPipe Face Mesh.

Used for:
- Earrings
- Necklaces

### Feature 3 — Hand Landmark Detection
Detect finger and wrist landmarks using MediaPipe Hands.

Used for:
- Rings
- Bracelets

### Feature 4 — Jewelry Overlay Rendering
Overlay PNG jewelry assets on detected landmarks.

### Feature 5 — Jewelry Selection
Allow users to switch between jewelry types.

### Feature 6 — Screenshot Capture
Allow users to capture the AR try-on result.

---

# 4. Target Platform

| Component | Value |
|----------|------|
| Platform | Android |
| Language | Kotlin |
| UI Framework | Jetpack Compose |
| Minimum SDK | 26 |
| Target SDK | Latest |
| Camera Framework | CameraX |
| ML Framework | MediaPipe |

---

# 5. Technology Stack

## Programming Language
Kotlin

## UI Framework
Jetpack Compose

## Camera
CameraX

## Computer Vision
MediaPipe Tasks Vision

## Rendering
Compose Canvas

## Dependency Management
Gradle

---

# 6. Dependencies

Add the following dependencies to `build.gradle`.

## Compose


implementation "androidx.compose.ui:ui"
implementation "androidx.compose.material3:material3"
implementation "androidx.activity:activity-compose"
implementation "androidx.lifecycle:lifecycle-runtime-compose"


## CameraX


implementation "androidx.camera:camera-core"
implementation "androidx.camera:camera-camera2"
implementation "androidx.camera:camera-lifecycle"
implementation "androidx.camera:camera-view"


## MediaPipe


implementation "com.google.mediapipe:tasks-vision"


## Kotlin Coroutines


implementation "org.jetbrains.kotlinx:kotlinx-coroutines-android"


---

# 7. High Level Architecture


Jetpack Compose UI
│
▼
CameraX Preview
│
▼
Frame Stream
│
▼
MediaPipe Detection
│
▼
Landmark Coordinates
│
▼
Jewelry Placement Engine
│
▼
Overlay Renderer
│
▼
Augmented Camera View


---

# 8. Project Folder Structure


app
├── camera
│ └── CameraController.kt
│
├── detection
│ ├── FaceDetector.kt
│ ├── HandDetector.kt
│ └── PoseDetector.kt
│
├── placement
│ └── JewelryPlacementEngine.kt
│
├── renderer
│ └── OverlayRenderer.kt
│
├── model
│ └── JewelryItem.kt
│
├── ui
│ ├── CameraScreen.kt
│ ├── JewelrySelector.kt
│ └── ScreenshotButton.kt
│
└── MainActivity.kt


---

# 9. Data Models

## JewelryItem

Represents a jewelry asset.

Fields:


id
name
type
imagePath
scaleFactor
anchorType


### Jewelry Types


EARRING
NECKLACE
RING
BRACELET


---

# 10. System Modules

## 10.1 Camera Module

### Responsibilities

- Start camera preview
- Stream frames
- Provide frames to detection pipeline

### Implementation

Use CameraX `ImageAnalysis`.

---

## 10.2 Landmark Detection Module

Uses MediaPipe models.

### Face Mesh
Used for:

- earrings
- necklaces

### Hands
Used for:

- rings
- bracelets

### Pose
Used for:

- neck
- shoulders

Output:


Landmark coordinates
(x,y)


---

## 10.3 Jewelry Placement Engine

Responsible for calculating where jewelry should appear.

### Earrings

Anchor:


Left ear landmark
Right ear landmark


Position:


x = ear landmark x
y = ear landmark y


Scale:


distance between ear and jaw


---

### Necklace

Anchor:


midpoint(left shoulder, right shoulder)


---

### Rings

Anchor:


finger joint


---

### Bracelets

Anchor:


wrist landmark


---

# 11. Rendering System

Rendering will use Compose Canvas.

Flow:


CameraPreview
+
Canvas Overlay


Canvas draws jewelry images using coordinates from placement engine.

Example concept:


Box
├ CameraPreview
└ Canvas
drawImage(jewelry)


---

# 12. UI Screens

## Screen 1 — Camera Screen

Main screen.

Components:


CameraPreview
CanvasOverlay
JewelrySelector
CaptureButton


Layout:


| |
| Camera Preview |
| |
| Jewelry Overlay Layer |


Jewelry Selector

---

## Screen 2 — Jewelry Selector

Allows user to switch jewelry.

Options:


Earrings
Necklace
Ring
Bracelet


Example UI:


[ Earrings ] [ Necklace ] [ Ring ] [ Bracelet ]


---

## Screen 3 — Screenshot Preview

Displays captured AR image.

Options:


Save
Share
Discard


---

# 13. App Flow


App Launch
↓
Camera Permission Request
↓
Camera Screen Opens
↓
CameraX Starts Preview
↓
Frames Sent To MediaPipe
↓
Landmarks Detected
↓
Placement Engine Calculates Positions
↓
Renderer Draws Jewelry
↓
User Selects Jewelry
↓
Overlay Updates
↓
User Captures Screenshot


---

# 14. Performance Requirements

Minimum FPS


20 FPS


Latency


<100ms per frame


Optimization strategies:

- Use background thread for detection
- Avoid frame copying
- Process only latest frame

---

# 15. Jewelry Assets

Assets folder:


assets/jewelry/


Example files:


earring_gold.png
earring_diamond.png
necklace_pearl.png
ring_gold.png
bracelet_chain.png


Images must have transparent background.

---

# 16. Step-by-Step Development Plan

This section is designed for AI agents.

Follow these steps exactly.

---

## Step 1 — Create Android Project

Create a new Android Studio project.

Configuration:


Language: Kotlin
UI: Jetpack Compose
Min SDK: 26


---

## Step 2 — Add Dependencies

Add CameraX, MediaPipe, and Compose dependencies.

Sync Gradle.

---

## Step 3 — Implement Camera Preview

Create:


CameraController.kt


Responsibilities:

- start camera
- stream frames

Display preview in Compose.

---

## Step 4 — Add Frame Analyzer

Use:


ImageAnalysis.Analyzer


Extract frames and send to detection module.

---

## Step 5 — Implement Face Detection

Create:


FaceDetector.kt


Use MediaPipe FaceMesh.

Output:


ear landmarks
jaw landmarks


---

## Step 6 — Implement Hand Detection

Create:


HandDetector.kt


Output:


finger landmarks
wrist landmarks


---

## Step 7 — Implement Placement Engine

Create:


JewelryPlacementEngine.kt


Input:


landmark coordinates


Output:


jewelry screen position
scale
rotation


---

## Step 8 — Implement Renderer

Create:


OverlayRenderer.kt


Draw jewelry images on Compose Canvas.

---

## Step 9 — Implement Jewelry Selector

Create:


JewelrySelector.kt


User can switch jewelry type.

---

## Step 10 — Implement Screenshot Feature

Capture Compose UI.

Save image to storage.

---

# 17. Future Improvements

Possible upgrades:

### 3D Jewelry Models

Use:


Sceneform
Filament


---

### Online Catalog

Load jewelry from server.

---

### AI Size Recommendation

Recommend jewelry size.

---

# 18. Summary

This project is a real-time AR jewelry try-on system using:

- Kotlin
- Jetpack Compose
- CameraX
- MediaPipe

The application detects facial and hand landmarks and overlays jewelry assets on the detected positions.

The architecture separates the system into modules:

- camera
- detection
- placement
- rendering
- UI

This modular design ensures maintainability and scalability for future features such as 3D models and online catalogs.

---