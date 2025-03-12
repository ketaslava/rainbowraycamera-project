package com.ktvincco.rainbowraycamera.ui


import android.annotation.SuppressLint
import com.ktvincco.rainbowraycamera.domain.util.StandardImage
import com.ktvincco.rainbowraycamera.presentation.ModelData
import com.ktvincco.rainbowraycamera.presentation.UiEventHandler

import android.app.Activity
import android.view.SurfaceView
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.snap
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Divider
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import com.ktvincco.rainbowraycamera.R
import com.ktvincco.rainbowraycamera.ui.components.GeneralComponents


class CameraPreview (
    private var mainActivity: Activity,
    private var modelData: ModelData,
    private var uiEventHandler: UiEventHandler,
    private val uiScale: Float,
    private val screenResolution: Pair<Int, Int>,
    private var generalComponents: GeneralComponents
){


    @Composable
    fun BlackScreen() {
        Box(modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
        )
    }


    @Composable
    fun StandardImageViewer(image: StandardImage) {

        val imageBitmap = image.getInSpaceNormalizedImage().getImageAsBitmap().asImageBitmap()

        Column (
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
            ) {
                Image(
                    bitmap = imageBitmap,
                    contentDescription = null,
                    modifier = Modifier
                        .fillMaxSize()
                )
            }
        }
    }


    @SuppressLint("NotConstructor")
    @Composable
    fun SurfacePreview(
        modifier: Modifier = Modifier,
    ) {
        val context = LocalContext.current
        val surfaceView = remember { SurfaceView(context) }

        val previewSurfaceAspectRatio = modelData.previewSurfaceAspectRatio.collectAsState().value

        // Send SurfaceView to uiEventHandler
        DisposableEffect(surfaceView) {
            uiEventHandler.setMainPreviewSurfaceView(surfaceView)
            onDispose {
                uiEventHandler.setMainPreviewSurfaceView(null)
            }
        }
        LaunchedEffect(surfaceView) {
            uiEventHandler.setMainPreviewSurfaceView(surfaceView)
        }

        // Show SurfaceView
        Column (
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height((100.dp * uiScale) * previewSurfaceAspectRatio)
            ) {
                AndroidView(
                    factory = {
                        surfaceView
                    },
                    modifier = modifier.matchParentSize()
                )
            }
        }
    }


    private fun calcFocusPoint(touchPositionX: Float, touchPositionY: Float,
                                  viewAspRatio: Float): Pair<Float, Float> {
        val newSizeX = generalComponents.mapFloatToNormalizedRange(touchPositionX, 0.0F,
            screenResolution.first.toFloat())
        val newSizeY = generalComponents.mapFloatToNormalizedRange(touchPositionY, 0.0F,
            screenResolution.first.toFloat() * viewAspRatio)
        return Pair(newSizeX, newSizeY)
    }
    private fun calcFocusPointBoxSize(touchPosition: Pair<Float, Float>,
                                      viewAspRatio: Float): Pair<Dp, Dp> {
        var newWidth = (100.dp * uiScale * touchPosition.first) + (4.5.dp * uiScale)
        var newHeight = (100.dp * uiScale * viewAspRatio *
                touchPosition.second) + (4.5.dp * uiScale)
        if (newWidth < 9.dp * uiScale) { newWidth = 9.dp * uiScale}
        if (newHeight < 9.dp * uiScale) { newHeight = 9.dp * uiScale}
        return Pair(newWidth, newHeight)
    }
    @Composable
    fun FocusPoint() {

        // Get aspect ratio
        val previewSurfaceAspectRatio = modelData.previewSurfaceAspectRatio.collectAsState().value
        var viewAspRatio by remember { mutableStateOf(previewSurfaceAspectRatio) }
        viewAspRatio = previewSurfaceAspectRatio

        // Input raw
        var isWasInput by remember { mutableStateOf(false) }
        var touchPositionX by remember { mutableStateOf(0.0F) }
        var touchPositionY by remember { mutableStateOf(0.0F) }

        // Touch position
        var focusPoint = calcFocusPoint(touchPositionX, touchPositionY, viewAspRatio)

        // Use center position before first input
        if (!isWasInput) { focusPoint = Pair(0.5F, 0.5F) }

        // Flip (rotate) dimensions before update point position
        val focusPointOut = Pair(focusPoint.second, 1.0F - focusPoint.first)
        // Callback
        if (isWasInput) { uiEventHandler.focusPointUpdate(focusPointOut) }

        // Calculate pointer position
        val focusPointBoxSize = calcFocusPointBoxSize(focusPoint, viewAspRatio)


        Column (
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally,
        ){
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height((100.dp * uiScale) * previewSurfaceAspectRatio)
                    .pointerInput(Unit) {
                        detectTapGestures { offset ->
                            touchPositionX = offset.x
                            touchPositionY = offset.y
                            isWasInput = true
                        }
                    }
                    .pointerInput(Unit) {
                        detectDragGestures { change, _ ->
                            touchPositionX = change.position.x
                            touchPositionY = change.position.y
                            isWasInput = true
                        }
                    }
            ) {
                Box(modifier = Modifier
                    .width(focusPointBoxSize.first)
                    .height(focusPointBoxSize.second)
                ) {
                    Column (
                        modifier = Modifier.fillMaxSize(),
                        verticalArrangement = Arrangement.Bottom,
                        horizontalAlignment = Alignment.End,
                    ) {
                        generalComponents.ImageIcon1(
                            imageRes = R.drawable.icon_focus_2,
                            modifier = Modifier
                                .width(9.dp * uiScale)
                                .height(9.dp * uiScale)
                        )
                    }
                }
            }
        }
    }


    @Composable
    fun Grid() {

        val isEnableGrid = modelData.getCameraOptionStateBoolean("isEnableGrid") == true
        val previewSurfaceAspectRatio = modelData.previewSurfaceAspectRatio.collectAsState().value

        AnimatedVisibility(
            visible = isEnableGrid,
            enter = fadeIn(animationSpec = tween(250)),
            exit = fadeOut(animationSpec = tween(250))
        ) {
            Column(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height((100.dp * uiScale) * previewSurfaceAspectRatio)
                ) {
                    Column(
                        modifier = Modifier.fillMaxSize(),
                        verticalArrangement = Arrangement.SpaceEvenly,
                        horizontalAlignment = Alignment.CenterHorizontally,
                    ) {
                        Divider(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(uiScale.dp * 0.1F),
                            color = Color.White
                        )
                        Divider(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(uiScale.dp * 0.1F),
                            color = Color.White
                        )
                    }
                    Row(
                        modifier = Modifier.fillMaxSize(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceEvenly,
                    ) {
                        Divider(
                            modifier = Modifier
                                .fillMaxHeight()
                                .width(uiScale.dp * 0.1F),
                            color = Color.White
                        )
                        Divider(
                            modifier = Modifier
                                .fillMaxHeight()
                                .width(uiScale.dp * 0.1F),
                            color = Color.White
                        )
                    }
                }
            }
        }
    }


    @Composable
    fun GridCross() {
        Box(modifier = Modifier
            .width(uiScale.dp * 5)
            .height(uiScale.dp * 5)
        ) {
            Column(
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.fillMaxSize()
            ) {
                Box(
                    modifier = Modifier
                        .width(uiScale.dp * 5)
                        .height(uiScale.dp * 0.2F)
                        .background(Color.White)
                )
            }
            Column(
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.fillMaxSize()
            ) {
                Box(
                    modifier = Modifier
                        .width(uiScale.dp * 0.2F)
                        .height(uiScale.dp * 5)
                        .background(Color.White)
                )
            }
        }
    }


    @Composable
    fun Grid2() {

        val isEnableGrid = modelData.getCameraOptionStateBoolean("isEnableGrid") == true
        val previewSurfaceAspectRatio = modelData.previewSurfaceAspectRatio.collectAsState().value

        AnimatedVisibility(
            visible = isEnableGrid,
            enter = fadeIn(animationSpec = tween(250)),
            exit = fadeOut(animationSpec = tween(250))
        ) {
            Column(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height((100.dp * uiScale) * previewSurfaceAspectRatio)
                ) {
                    Column(
                        modifier = Modifier.fillMaxSize(),
                        verticalArrangement = Arrangement.SpaceEvenly,
                        horizontalAlignment = Alignment.CenterHorizontally,
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceEvenly,
                        ) {
                            GridCross()
                            GridCross()
                        }
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceEvenly,
                        ) {
                            GridCross()
                            GridCross()
                        }
                    }
                }
            }
        }
    }


    @Composable
    fun AlignmentCursor() {

        val isEnableAlignmentCursor = modelData.isEnableAlignmentCursor.collectAsState().value
        val previewSurfaceAspectRatio = modelData.previewSurfaceAspectRatio.collectAsState().value

        AnimatedVisibility(
            visible = isEnableAlignmentCursor,
            enter = fadeIn(animationSpec = snap()),
            exit = fadeOut(animationSpec = tween(333))
        ) {
            Column(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height((100.dp * uiScale) * previewSurfaceAspectRatio)
                ) {

                    // Alignment cursor pointer
                    Column(
                        modifier = Modifier.fillMaxSize(),
                        verticalArrangement = Arrangement.Center,
                        horizontalAlignment = Alignment.CenterHorizontally,
                    ) {
                        Image(
                            painter = painterResource(id = R.drawable.alignment_cursor_2),
                            contentDescription = "",
                            modifier = Modifier
                                .width(uiScale.dp * 16)
                                .height(uiScale.dp * 16))
                    }

                    // Alignment cursor text
                    Column(
                        modifier = Modifier.fillMaxSize(),
                        verticalArrangement = Arrangement.SpaceEvenly,
                        horizontalAlignment = Alignment.CenterHorizontally,
                    ) {
                        generalComponents.TextMain(text = "Hold your phone steady")
                        Spacer(modifier = Modifier.height(uiScale.dp * 16))
                        generalComponents.TextMain(text = "while capturing a photo")
                    }

                    val alignmentCursorPosition =
                        modelData.alignmentCursorPosition.collectAsState().value

                    val workZone = 75F
                    var boxXStartSize = 0F
                    var boxXEndSize = 0F
                    var boxYTopSize = 0F
                    var boxYBottomSize = 0F
                    if (alignmentCursorPosition.first > 0) {
                        boxXStartSize = workZone * alignmentCursorPosition.first
                    } else {
                        boxXEndSize = workZone * -alignmentCursorPosition.first
                    }
                    if (alignmentCursorPosition.second > 0) {
                        boxYTopSize = workZone * alignmentCursorPosition.second
                    } else {
                        boxYBottomSize = workZone * -alignmentCursorPosition.second
                    }

                    // Alignment cursor target
                    Column(
                        modifier = Modifier.fillMaxSize(),
                        verticalArrangement = Arrangement.Center,
                        horizontalAlignment = Alignment.CenterHorizontally,
                    ) {
                        Box(modifier = Modifier
                            .height(uiScale.dp * boxYTopSize)
                            .width(uiScale.dp * 2))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center,
                        ) {
                            Box(modifier = Modifier
                                .width(uiScale.dp * boxXStartSize)
                                .height(uiScale.dp * 2))
                            Image(
                                painter = painterResource(id = R.drawable.alignment_cursor),
                                contentDescription = "",
                                modifier = Modifier
                                    .width(uiScale.dp * 16)
                                    .height(uiScale.dp * 16)
                                    .rotate(alignmentCursorPosition.third * 180F)
                            )
                            Box(modifier = Modifier
                                .width(uiScale.dp * boxXEndSize)
                                .height(uiScale.dp * 2))
                        }
                        Box(modifier = Modifier
                            .height(uiScale.dp * boxYBottomSize)
                            .width(uiScale.dp * 2))
                    }
                }
            }
        }
    }


    @SuppressLint("NotConstructor")
    @Composable
    fun BlinkScreen(
        modifier: Modifier = Modifier,
    ) {
        val previewSurfaceAspectRatio =
            modelData.previewSurfaceAspectRatio.collectAsState().value
        val blinkScreenWhilePhotoCaptureState =
            modelData.blinkScreenWhileCapturePhotoState.collectAsState().value

        // Show SurfaceView
        Column (
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            AnimatedVisibility(
                visible = blinkScreenWhilePhotoCaptureState,
                enter = fadeIn(animationSpec = tween(64)),
                exit = fadeOut(animationSpec = tween(256))
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height((100.dp * uiScale) * previewSurfaceAspectRatio)
                        .background(Color.Black)
                )
            }
        }
    }


    @Composable
    fun OnPreviewSpace() {
        FocusPoint()
        Grid2()
        AlignmentCursor()
    }


    @Composable
    fun CameraPreviewPage() {

        // Preview content

        SurfacePreview()

        val previewImage = modelData.cameraPreviewImage.collectAsState().value
        val isEnableSurfacePreview = modelData.isEnableSurfacePreview.collectAsState().value

        if (previewImage != null || !isEnableSurfacePreview) { BlackScreen() }
        if (previewImage != null) { StandardImageViewer(previewImage) }

        // On preview controls

        OnPreviewSpace()

        // On top of the preview

        BlinkScreen()
    }
}

