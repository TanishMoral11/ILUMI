# Copilot Instructions
## AR Jewelry Try-On System

This document provides strict instructions for AI coding assistants (GitHub Copilot, Copilot Chat, AI agents) to build this project.

The AI must follow the development process exactly as described.

Do NOT skip steps.

Do NOT introduce new frameworks.

Always build incrementally and ensure the app compiles before moving to the next stage.

---

# 1 Project Goal

The goal of this project is to build an Android application that allows users to try jewelry virtually using augmented reality.

However, the development must start with a **minimal working MVP**.

The MVP will simply load and display a **GLB necklace model in AR space**.

The MVP verifies that:

- AR rendering works
- GLB models load correctly
- the AR scene is functional
- models can be anchored in the environment

Only after the MVP works should more advanced features (face tracking, landmark detection, jewelry placement) be implemented.

---

# 2 MVP Requirements (FIRST PRIORITY)

The AI must implement the MVP first.

The MVP must do the following:

1. Open the device camera.
2. Start an AR scene.
3. Load a `.glb` necklace model.
4. Display the model in AR space.
5. Allow the user to anchor the model by pressing a button.

The model does NOT need to follow the user's body yet.

It only needs to appear in AR space.

---

# 3 Technology Stack

The AI must use only the following technologies.

Programming Language  
Kotlin

UI Framework  
Jetpack Compose or Android XML

AR Framework  
SceneView

Rendering Engine  
Filament (handled internally by SceneView)

Build Tool  
Gradle

Do NOT introduce other frameworks.

Forbidden tools for the MVP:

Do NOT use:
- MediaPipe
- OpenCV
- Python
- Unity
- Flutter

Those will be added later in future phases.

---

# 4 Dependencies

The following dependencies must be added to the project.

SceneView AR library:

implementation "io.github.sceneview:arsceneview:1.0.0"

Core Android libraries:

implementation "androidx.core:core-ktx:1.12.0"

implementation "androidx.appcompat:appcompat:1.6.1"

Material components:

implementation "com.google.android.material:material:1.11.0"

---

# 5 Project Structure

The project must follow this structure.


app
├ models
│ └ necklace.glb
│
├ ui
│ └ activity_main.xml
│
├ MainActivity.kt
│
├ AndroidManifest.xml


The GLB model must be placed inside:

models/necklace.glb

---

# 6 UI Layout

The MVP screen should be very simple.

It must contain:

- AR scene view
- a button to place the model

Layout concept:


| |
| AR Camera View |
| |
| |


PLACE MODEL

---

# 7 MVP Application Flow

The application must follow this flow.

App Launch  
↓  
AR Scene starts  
↓  
Camera opens  
↓  
Necklace GLB loads  
↓  
User taps "Place Model"  
↓  
Model becomes anchored in the real world

---

# 8 MVP Implementation Steps

The AI must follow these steps strictly.

---

## Step 1 Create Android Project

Create a new Android project with the following settings:

Language: Kotlin  
Minimum SDK: 26  
Target SDK: Latest  

---

## Step 2 Add SceneView Dependency

Add SceneView AR dependency to the Gradle file.

Sync the project.

Ensure the project builds successfully.

---

## Step 3 Create AR Layout

Create layout file:

activity_main.xml

Add an AR SceneView component.

Also add a button at the bottom labeled:

"Place Model"

---

## Step 4 Implement MainActivity

Create MainActivity.kt.

Responsibilities:

- initialize the AR scene
- load the necklace GLB model
- add the model to the scene
- allow anchoring via button click

---

## Step 5 Load GLB Model

The GLB model must be loaded asynchronously.

Example concept:

Load model  
Add model node to scene  
Keep model floating until anchored

---

## Step 6 Place Model

When the user presses the button:

Anchor the model to the AR environment.

After anchoring:

- disable the placement button
- hide plane indicators

---

# 9 Expected MVP Result

The final MVP should behave like this:

1. User opens the app.
2. Camera view appears.
3. Necklace model loads.
4. Model floats in AR view.
5. User presses "Place Model".
6. Necklace remains fixed in the environment.

This confirms that the AR pipeline works correctly.

---

# 10 After MVP is Completed

Only after the MVP works correctly should the AI start implementing advanced features.

Future features include:

Face tracking  
Hand tracking  
Necklace placement on neck  
Ring placement on fingers  
Bracelet placement on wrist  

These features require MediaPipe and will be implemented in later development phases.

---

# 11 AI Development Rules

The AI must follow these rules.

Never skip steps.

Never hallucinate APIs.

Never introduce new frameworks.

Always ensure the project builds after each step.

Only move to the next step after the current one works.

---

# 12 Future Architecture (Do NOT implement yet)

The final system architecture will eventually become:

Camera  
↓  
Frame Capture  
↓  
MediaPipe Landmark Detection  
↓  
Landmark Processing  
↓  
Jewelry Placement Engine  
↓  
Rendering Engine  
↓  
Augmented Camera Output

However, this architecture will only be implemented after the MVP is finished.

---

# End of Instructions