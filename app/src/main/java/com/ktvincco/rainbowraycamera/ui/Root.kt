package com.ktvincco.rainbowraycamera.ui

import com.ktvincco.rainbowraycamera.presentation.ModelData
import com.ktvincco.rainbowraycamera.presentation.UiEventHandler

import android.app.Activity
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.core.snap
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import com.ktvincco.rainbowraycamera.ui.components.DialogWindows
import com.ktvincco.rainbowraycamera.ui.components.GeneralComponents


class Root (
    private val mainActivity: Activity,
    private val modelData: ModelData,
    private val uiEventHandler: UiEventHandler,
    private val uiScale: Float,
    private val screenResolution: Pair<Int, Int>,
    private val screenResolutionInDp: Pair<Float, Float>,
){


    // Variables


    // ui components
    private val generalComponents = GeneralComponents(mainActivity, modelData, uiEventHandler,
        uiScale, screenResolution, screenResolutionInDp)
    private val dialogWindows = DialogWindows(mainActivity, modelData, uiEventHandler, uiScale,
        generalComponents)
    private val cameraControl = CameraControl(mainActivity, modelData, uiEventHandler, uiScale,
        screenResolution, generalComponents)
    private val cameraPreview = CameraPreview(mainActivity, modelData, uiEventHandler, uiScale,
        screenResolution, generalComponents)
    private val gallery = Gallery(mainActivity, modelData, uiEventHandler, uiScale,
        screenResolution, generalComponents)
    private val aboutApp = AboutApp(mainActivity, modelData, uiEventHandler, uiScale,
        screenResolution, generalComponents)
    private val enableFlashlightOption = EnableFlashlightOption(mainActivity, modelData,
        uiEventHandler, uiScale, screenResolution, generalComponents)
    private val startupScreen = StartupScreen(mainActivity, modelData, uiEventHandler, uiScale,
        screenResolution, generalComponents)
    private val accessDeniedScreen = AccessDeniedScreen(mainActivity, modelData, uiEventHandler,
        uiScale, screenResolution, generalComponents)
    private val updateRequired = UpdateRequired(mainActivity, modelData, uiEventHandler,
        uiScale, screenResolution, generalComponents)
    private val benchmarkScreen = BenchmarkScreen(mainActivity, modelData, uiEventHandler,
        uiScale, screenResolution, generalComponents)


    // Public


    // Provide callbacks
    fun onNewActivityState(newState: String) {
    }


    @Composable
    fun UserInterfaceRoot() {
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = Color.Red
        )
        {
            PageSelector()
        }
    }


    // Private


    @OptIn(ExperimentalAnimationApi::class)
    @Composable
    fun PageSelector() {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black)
        ) {
            // Get active pages
            val activePages = modelData.activePages.collectAsState().value

            // Page controls

            if (activePages.contains("CameraPreview")) {
                cameraPreview.CameraPreviewPage()
            }
            if (activePages.contains("CameraControl")) {
                cameraControl.CameraControl()
            }
            if (activePages.contains("Gallery")) {
                gallery.GalleryPage()
            }
            AnimatedVisibility(
                visible = activePages.contains("Selector"),
                enter = fadeIn(animationSpec = tween(330)),
                exit = fadeOut(animationSpec = snap())
            ) {
                dialogWindows.Selector()
            }
            AnimatedVisibility(
                visible = activePages.contains("Selector2"),
                enter = fadeIn(animationSpec = tween(330)),
                exit = fadeOut(animationSpec = snap())
            ) {
                dialogWindows.Selector2()
            }
            AnimatedVisibility(
                visible = activePages.contains("CameraModeSelector"),
                enter = fadeIn(animationSpec = tween(330)),
                exit = fadeOut(animationSpec = snap())
            ) {
                dialogWindows.CameraModeSelector()
            }
            AnimatedVisibility(
                visible = activePages.contains("SelectorVideoSize"),
                enter = fadeIn(animationSpec = tween(330)),
                exit = fadeOut(animationSpec = snap())
            ) {
                dialogWindows.SelectorVideoSize()
            }
            AnimatedVisibility(
                visible = activePages.contains("Popup"),
                enter = fadeIn(animationSpec = tween(250)),
                exit = fadeOut(animationSpec = snap())
            ) {
                dialogWindows.PopupPage()
            }
            AnimatedVisibility(
                visible = activePages.contains("AboutApp"),
                enter = scaleIn(animationSpec = tween(333)),
                exit = scaleOut(animationSpec = tween(250))
            ) {
                aboutApp.AboutAppPage()
            }
            if (activePages.contains("EnableFlashlightOption")) {
                enableFlashlightOption.EnableFlashlightOptionPage()
            }
            AnimatedVisibility(
                visible = activePages.contains("StartupScreen"),
                enter = fadeIn(animationSpec = tween(500)),
                exit = fadeOut(animationSpec = tween(333))
            ) {
                startupScreen.StartupScreenPage()
            }
            AnimatedVisibility(
                visible = activePages.contains("AccessDenied"),
                enter = scaleIn(animationSpec = tween(500)),
                exit = scaleOut(animationSpec = tween(500))
            ) {
                accessDeniedScreen.AccessDeniedPage()
            }
            AnimatedVisibility(
                visible = activePages.contains("UpdateRequired"),
                enter = scaleIn(animationSpec = tween(500)),
                exit = scaleOut(animationSpec = tween(500))
            ) {
                updateRequired.UpdateRequired()
            }
            AnimatedVisibility(
                visible = activePages.contains("BenchmarkScreen"),
                enter = scaleIn(animationSpec = tween(500)),
                exit = fadeOut(snap())
            ) {
                benchmarkScreen.BenchmarkScreen()
            }
        }
    }
}