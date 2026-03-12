package com.app.ilumi

import android.Manifest
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.os.Bundle
import android.util.Size
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageProxy
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import com.app.ilumi.detection.PoseDetector
import com.app.ilumi.placement.JewelryPlacementEngine
import com.app.ilumi.ui.theme.ILUMITheme
import com.google.android.filament.Camera
import dev.romainguy.kotlin.math.Float3
import io.github.sceneview.Scene
import io.github.sceneview.math.Position
import io.github.sceneview.node.ModelNode
import io.github.sceneview.rememberCameraNode
import io.github.sceneview.rememberEngine
import io.github.sceneview.rememberEnvironmentLoader
import io.github.sceneview.rememberMaterialLoader
import io.github.sceneview.rememberModelLoader
import io.github.sceneview.rememberNodes

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            ILUMITheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    CameraPermissionsAndARScene(modifier = Modifier.padding(innerPadding))
                }
            }
        }
    }
}

@Composable
fun CameraPermissionsAndARScene(modifier: Modifier = Modifier) {
    val context = LocalContext.current
    var hasCameraPermission by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.CAMERA
            ) == PackageManager.PERMISSION_GRANTED
        )
    }

    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission(),
        onResult = { granted ->
            hasCameraPermission = granted
        }
    )

    LaunchedEffect(Unit) {
        if (!hasCameraPermission) {
            launcher.launch(Manifest.permission.CAMERA)
        }
    }

    if (hasCameraPermission) {
        JewelryARScreen(modifier = modifier)
    } else {
        Box(modifier = modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text("Camera permission is required for AR Try-on.")
        }
    }
}

@Composable
fun JewelryARScreen(modifier: Modifier = Modifier) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current

    var lensFacing by remember { mutableStateOf(CameraSelector.LENS_FACING_FRONT) }
    var necklacePositionAndScale by remember { mutableStateOf<Pair<Float3, Float>?>(null) }
    
    // SceneView components
    val engine = rememberEngine()
    val modelLoader = rememberModelLoader(engine)
    val materialLoader = rememberMaterialLoader(engine)
    val environmentLoader = rememberEnvironmentLoader(engine)
    val cameraNode = rememberCameraNode(engine)
    val childNodes = rememberNodes()

    // Load the model once
    var necklaceModelNode by remember { mutableStateOf<ModelNode?>(null) }
    LaunchedEffect(Unit) {
        val modelInstance = modelLoader.createModelInstance("models/necklace.glb")
        if (modelInstance != null) {
            val node = ModelNode(modelInstance = modelInstance, scaleToUnits = null)
            childNodes.add(node)
            necklaceModelNode = node
        }
    }

    // MediaPipe Pose Detector
    val poseDetector = remember {
        PoseDetector(context, object : PoseDetector.PoseLandmarkerListener {
            override fun onError(error: String) {}

            override fun onResults(resultBundle: PoseDetector.ResultBundle) {
                if (resultBundle.results.isNotEmpty() && resultBundle.results[0].landmarks().isNotEmpty()) {
                    val landmarks = resultBundle.results[0].landmarks()[0]
                    
                    // The camera preview resolution helps scale SceneView coords if needed
                    val pos = JewelryPlacementEngine.calculateNecklacePositionAndScale(
                        landmarks = landmarks,
                        isFrontCamera = lensFacing == CameraSelector.LENS_FACING_FRONT,
                        sceneWidth = resultBundle.inputImageWidth,
                        sceneHeight = resultBundle.inputImageHeight
                    )
                    
                    necklacePositionAndScale = pos
                }
            }
        })
    }

    DisposableEffect(Unit) {
        onDispose {
            poseDetector.clear()
        }
    }

    // Update ModelNode position and scale
    LaunchedEffect(necklacePositionAndScale) {
        necklacePositionAndScale?.let { (pos, scale) ->
            necklaceModelNode?.position = Position(x = pos.x, y = pos.y, z = pos.z)
            necklaceModelNode?.scale = Position(x = scale, y = scale, z = scale)
        }
    }

    Box(modifier = modifier.fillMaxSize()) {
        // First Layer: CameraX Preview
        androidx.compose.runtime.key(lensFacing) {
            AndroidView(
                modifier = Modifier.fillMaxSize(),
            factory = { ctx ->
                val previewView = PreviewView(ctx)
                val cameraProviderFuture = ProcessCameraProvider.getInstance(ctx)

                cameraProviderFuture.addListener({
                    val cameraProvider = cameraProviderFuture.get()

                    val preview = Preview.Builder().build().also {
                        it.setSurfaceProvider(previewView.surfaceProvider)
                    }

                    val imageAnalyzer = ImageAnalysis.Builder()
                        .setTargetResolution(Size(previewView.width, previewView.height))
                        .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                        .setOutputImageFormat(ImageAnalysis.OUTPUT_IMAGE_FORMAT_RGBA_8888)
                        .build()
                        .also { analysis ->
                            analysis.setAnalyzer(ContextCompat.getMainExecutor(ctx)) { imageProxy ->
                                val bitmap = imageProxy.toBitmap()
                                poseDetector.detectLiveStream(bitmap, lensFacing == CameraSelector.LENS_FACING_FRONT)
                                imageProxy.close()
                            }
                        }

                    val cameraSelector = CameraSelector.Builder().requireLensFacing(lensFacing).build()

                    try {
                        cameraProvider.unbindAll()
                        cameraProvider.bindToLifecycle(
                            lifecycleOwner,
                            cameraSelector,
                            preview,
                            imageAnalyzer
                        )
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }, ContextCompat.getMainExecutor(ctx))

                previewView
            },
            update = { previewView ->
                // Bind lifecycle again if lensFacing changes
                val cameraProviderFuture = ProcessCameraProvider.getInstance(context)
                cameraProviderFuture.addListener({
                    val cameraProvider = cameraProviderFuture.get()
                    val preview = Preview.Builder().build().also {
                        it.setSurfaceProvider(previewView.surfaceProvider)
                    }

                    val imageAnalyzer = ImageAnalysis.Builder()
                        .setTargetResolution(Size(previewView.width, previewView.height))
                        .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                        .setOutputImageFormat(ImageAnalysis.OUTPUT_IMAGE_FORMAT_RGBA_8888)
                        .build()
                        .also { analysis ->
                            analysis.setAnalyzer(ContextCompat.getMainExecutor(context)) { imageProxy ->
                                val bitmap = imageProxy.toBitmap()
                                poseDetector.detectLiveStream(bitmap, lensFacing == CameraSelector.LENS_FACING_FRONT)
                                imageProxy.close()
                            }
                        }

                    val cameraSelector = CameraSelector.Builder().requireLensFacing(lensFacing).build()

                    try {
                        cameraProvider.unbindAll()
                        cameraProvider.bindToLifecycle(
                            lifecycleOwner,
                            cameraSelector,
                            preview,
                            imageAnalyzer
                        )
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }, ContextCompat.getMainExecutor(context))
            }
        )
        }

        // Second Layer: Transparent SceneView for 3D overlay
        Scene(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Transparent),
            isOpaque = false,
            engine = engine,
            modelLoader = modelLoader,
            materialLoader = materialLoader,
            environmentLoader = environmentLoader,
            cameraNode = cameraNode,
            childNodes = childNodes,
            cameraManipulator = null,
            onFrame = { _ ->
                // Enforce orthographic projection every frame to prevent
                // onResized from resetting to the default perspective lens projection.
                // Ortho bounds [-1,1] on both axes map directly to the full viewport.
                cameraNode.setProjection(
                    Camera.Projection.ORTHO,
                    -1.0, 1.0,
                    -1.0, 1.0,
                    0.0, 100.0
                )
            },
            onViewUpdated = {
                // Make the SceneView background completely transparent
                setZOrderOnTop(true)
                holder.setFormat(android.graphics.PixelFormat.TRANSLUCENT)
                setBackgroundColor(android.graphics.Color.TRANSPARENT)
                
                // Keep lighting environment without the visual skybox
                scene.skybox = null
            }
        )

        // Camera Switch Button
        Button(
            onClick = {
                lensFacing = if (lensFacing == CameraSelector.LENS_FACING_FRONT) {
                    CameraSelector.LENS_FACING_BACK
                } else {
                    CameraSelector.LENS_FACING_FRONT
                }
            },
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 32.dp)
        ) {
            Text("Switch Camera")
        }
    }
}