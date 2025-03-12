package com.ktvincco.rainbowraycamera.ui


import android.annotation.SuppressLint
import com.ktvincco.rainbowraycamera.R
import com.ktvincco.rainbowraycamera.presentation.ModelData
import com.ktvincco.rainbowraycamera.presentation.UiEventHandler

import android.app.Activity
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.ktvincco.rainbowraycamera.ui.components.GeneralComponents
import com.ktvincco.rainbowraycamera.ui.components.WhiteBalanceSelector
import kotlin.math.pow


class CameraControl (
    private var mainActivity: Activity,
    private var modelData: ModelData,
    private var uiEventHandler: UiEventHandler,
    private val uiScale: Float,
    private val screenResolution: Pair<Int, Int>,
    private var generalComponents: GeneralComponents
) {


    @Composable
    fun CaptureSettings() {
        Row(
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.Top,
            modifier = Modifier
                .fillMaxWidth()
        ) {
            Column (
                verticalArrangement = Arrangement.SpaceEvenly,
                horizontalAlignment = Alignment.Start,
                modifier = Modifier
                    .width(uiScale.dp * 90)
                    .background(Color(0x99333333))
            ) {
                Row(
                    horizontalArrangement = Arrangement.SpaceEvenly,
                    verticalAlignment = Alignment.Top,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = uiScale.dp * 5)
                )
                {

                    // Get camera mode
                    val cameraMode = modelData.cameraMode.collectAsState().value

                    val isEnableAppSound = modelData.getCameraOptionStateBoolean(
                        "isEnableAppSound") == true
                    generalComponents.ImageIcon1(
                        R.drawable.icon_sound,
                        modifier = Modifier
                            .width(uiScale.dp * 9)
                            .height(uiScale.dp * 9)
                            .alpha(
                                if (isEnableAppSound) {
                                    1F
                                } else {
                                    0.33F
                                }
                            )
                            .clickable { uiEventHandler.switchIsEnableCaptureSound() })

                    val isVideoMode = cameraMode == "VideoDefault" || cameraMode == "VideoManual"
                    if (isVideoMode) {
                        val isEnableRecordAudio = modelData.getCameraOptionStateBoolean(
                            "isEnableRecordAudio") == true
                        generalComponents.ImageIcon1(
                            R.drawable.icon_recordaudio,
                            modifier = Modifier
                                .width(uiScale.dp * 9)
                                .height(uiScale.dp * 9)
                                .alpha(
                                    if (isEnableRecordAudio) {
                                        1F
                                    } else {
                                        0.33F
                                    }
                                )
                                .clickable { uiEventHandler.switchIsEnableRecordAudio() })
                    }

                    val isEnableGrid = modelData.getCameraOptionStateBoolean(
                        "isEnableGrid") == true
                    generalComponents.ImageIcon1(
                        R.drawable.icon_bars,
                        modifier = Modifier
                            .width(uiScale.dp * 9)
                            .height(uiScale.dp * 9)
                            .alpha(
                                if (isEnableGrid) {
                                    1F
                                } else {
                                    0.33F
                                }
                            )
                            .clickable { uiEventHandler.switchIsEnableGrid() })

                    val isEnableFocusPeaking = modelData.getCameraOptionStateBoolean(
                        "isEnableFocusPeaking") == true
                    generalComponents.ImageIcon1(
                        R.drawable.icon_fp,
                        modifier = Modifier
                            .width(uiScale.dp * 9)
                            .height(uiScale.dp * 9)
                            .alpha(
                                if (isEnableFocusPeaking) {
                                    1F
                                } else {
                                    0.33F
                                }
                            )
                            .clickable { uiEventHandler.switchIsEnableFocusPeaking() })

                    val isEnableOis = modelData.getCameraOptionStateBoolean(
                        "isEnableOis") == true
                    generalComponents.ImageIcon1(
                        R.drawable.icon_ois,
                        modifier = Modifier
                            .width(uiScale.dp * 9)
                            .height(uiScale.dp * 9)
                            .alpha(
                                if (isEnableOis) {
                                    1F
                                } else {
                                    0.33F
                                }
                            )
                            .clickable { uiEventHandler.switchIsEnableOis() })

                    val isRawAvailable = modelData.getCameraOptionStateBoolean(
                        "isRawSensorFormatAvailable") == true
                    val isUseRaw = modelData.getCameraOptionStateBoolean(
                        "IsUseRawSensorFormatWhenAvailable") == true
                    if (isRawAvailable && cameraMode == "PhotoManual") {
                        generalComponents.ImageIcon1(
                            if (isUseRaw) R.drawable.icon_raw else R.drawable.icon_jpg,
                            modifier = Modifier
                                .width(uiScale.dp * 9)
                                .height(uiScale.dp * 9)
                                .clickable {
                                    uiEventHandler.switchIsUseRawSensorFormatWhenAvailable() })
                    }
                }
                Box (
                    modifier = Modifier
                        .height(uiScale.dp * 5)
                )
            }
        }
    }


    @Composable
    fun TopControls() {
        Row(
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .fillMaxWidth()
                .height(uiScale.dp * 18)
                .background(Color(0x991A1A1A))
        ) {
            generalComponents.ImageIcon1(
                R.drawable.icon_galerey,
                modifier = Modifier
                    .width(uiScale.dp * 9)
                    .height(uiScale.dp * 9)
                    .clickable { uiEventHandler.openGalleryButtonClicked() })
            generalComponents.ImageIcon1(
                R.drawable.icon_flashlight,
                modifier = Modifier
                    .width(uiScale.dp * 9)
                    .height(uiScale.dp * 9)
                    .clickable { uiEventHandler.toggleFlashlightButtonClicked() })
            generalComponents.ImageIcon1(
                R.drawable.icon_settings,
                modifier = Modifier
                    .width(uiScale.dp * 9)
                    .height(uiScale.dp * 9)
                    .clickable { uiEventHandler.captureSettingsButtonClicked() })
            generalComponents.ImageIcon1(
                R.drawable.icon_kic_logo_mini,
                modifier = Modifier
                    .width(uiScale.dp * 9)
                    .height(uiScale.dp * 9)
                    .clickable { uiEventHandler.openAboutAppButtonClicked() })
            generalComponents.ImageIcon1(
                R.drawable.icon_crop,
                modifier = Modifier
                    .width(uiScale.dp * 9)
                    .height(uiScale.dp * 9)
                    .clickable { uiEventHandler.outputSizeButtonClicked() })
        }
    }


    @Composable
    fun BottomTopRightButton() {
        Row (
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.End,
            modifier = Modifier
                .fillMaxWidth()
                .padding(uiScale.dp * 4)
        ) {

            // Use button depend cameraMode case
            val cameraMode = modelData.cameraMode.collectAsState().value

            if (cameraMode == "PhotoManual" || cameraMode == "VideoManual") {

                // Auto White Balance switch
                val isAutoWhiteBalanceEnabled =
                    modelData.isAutoWhiteBalanceEnabled.collectAsState().value
                val buttonAlpha = if (isAutoWhiteBalanceEnabled) 1F else 0.5F

                // Draw button
                generalComponents.ImageIcon1(
                    R.drawable.icon_awb,
                    modifier = Modifier
                        .width(uiScale.dp * 9)
                        .height(uiScale.dp * 9)
                        .alpha(buttonAlpha)
                        .clickable {
                            if (isAutoWhiteBalanceEnabled) {
                                modelData.setAutoWhiteBalanceState(false)
                            } else {
                                modelData.setAutoWhiteBalanceState(true)
                                modelData.disableSlider()
                            }
                        }
                )
            }
        }
    }


    @Composable
    fun BottomTopLeftButton() {
        Row (
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Start,
            modifier = Modifier
                .fillMaxWidth()
                .padding(uiScale.dp * 4)
        ) {

            // Use button depend cameraMode case
            val cameraMode = modelData.cameraMode.collectAsState().value

            if (cameraMode == "PhotoEDR" || cameraMode == "PhotoNight") {

                // Night mode switch
                val state = modelData.nightModeSwitchButtonState.collectAsState().value
                if (state != "Disabled") {
                    var icon = R.drawable.icon_night
                    if (state == "Exit") { icon = R.drawable.icon_night_disabled }

                    // Draw button
                    generalComponents.ImageIcon1(
                        icon,
                        modifier = Modifier
                            .width(uiScale.dp * 9)
                            .height(uiScale.dp * 9)
                            .clickable {
                                // Enter or exit from night mode
                                uiEventHandler.switchNightModeButtonClicked()
                            }
                    )
                }
            }

            if (cameraMode == "PhotoManual" || cameraMode == "VideoManual") {

                // Auto exposure switch
                val isAutoExposureEnabled = modelData.isAutoExposureEnabled.collectAsState().value
                val buttonAlpha = if (isAutoExposureEnabled) 1F else 0.5F
                val sliderTarget = modelData.sliderTarget.collectAsState().value

                // Draw button
                generalComponents.ImageIcon1(
                    R.drawable.icon_ae,
                    modifier = Modifier
                        .width(uiScale.dp * 9)
                        .height(uiScale.dp * 9)
                        .alpha(buttonAlpha)
                        .clickable {
                            if (sliderTarget == "ShutterSpeed" || sliderTarget == "Iso") {
                                modelData.disableSlider()
                            }
                            modelData.setAutoExposureState(!isAutoExposureEnabled)
                        }
                )
            }
        }
    }


    @Composable
    fun ZoomOptionBox(text: String, isActive: Boolean, onClick: () -> Unit) {
        Box(
            modifier = Modifier
                .height(uiScale.dp * 8)
                .width(uiScale.dp * 10)
                .clickable { onClick() }
        ) {
            // Text
            generalComponents.TextMain(text = text,
                containerModifier = Modifier.fillMaxSize())
            // Activation mark
            if (isActive) {
                Column (
                    verticalArrangement = Arrangement.Bottom,
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.fillMaxSize()
                ) {
                    Box(
                        modifier = Modifier
                            .height(uiScale.dp * 1)
                            .fillMaxWidth()
                            .background(Color.White)
                    )
                }
            }
        }
    }


    @Composable
    fun ZoomSelector() {
        Column (
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Row (
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.Bottom,
                modifier = Modifier
                    .background(Color(0x66333333))
            ) {
                val cameraZoom = uiEventHandler.cameraZoom.collectAsState().value
                ZoomOptionBox("1X", cameraZoom == 1F) {
                    uiEventHandler.setCameraZoom(1F) }
                ZoomOptionBox("1.4", cameraZoom == 1.4F) {
                    uiEventHandler.setCameraZoom(1.4F) }
                ZoomOptionBox("1.7", cameraZoom == 1.72F) {
                    uiEventHandler.setCameraZoom(1.72F) }
                ZoomOptionBox("2X", cameraZoom == 2F) {
                    uiEventHandler.setCameraZoom(2F) }
            }
        }
    }


    @Composable
    fun FocusIsoShutterSpeedWhiteBalanceBar() {
        Row(
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .fillMaxWidth()
                .height(uiScale.dp * 8)
                .background(Color(0x99333333))
        ) {

            // Get state

            val sliderTarget = modelData.sliderTarget.collectAsState().value
            val isAutoExposureEnabled = modelData.isAutoExposureEnabled.collectAsState().value
            val cameraMode = modelData.cameraMode.collectAsState().value

            // Focus

            Row(
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .width(uiScale.dp * 25)
                    .height(uiScale.dp * 8)
                    .clickable {
                        if (sliderTarget == "Focus") {
                            modelData.disableSlider()
                        } else {
                            modelData.setAutoFocusState(false)
                            modelData.setSliderTargetFocus()
                        }
                    }
            ) {
                // Focus text
                val isAutoFocusEnabled = modelData.isEnableAutoFocus.collectAsState().value
                val manualFocusValue = uiEventHandler.manualFocusValue.collectAsState().value

                generalComponents.TextMain(text = if (isAutoFocusEnabled) { "F Auto" }
                else { "F ${((1F - manualFocusValue) * 100).toInt()}" })
            }

            // Controls for manual mode

            if (cameraMode == "PhotoManual" || cameraMode == "VideoManual") {

                // Iso

                Row(
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .width(uiScale.dp * 25)
                        .height(uiScale.dp * 8)
                        .clickable {
                            if (sliderTarget == "Iso") {
                                modelData.disableSlider()
                            } else {
                                modelData.setAutoExposureState(false)
                                modelData.setSliderTargetIso()
                            }
                        }
                ) {
                    generalComponents.TextMain(text =
                    if (isAutoExposureEnabled) { "ISO Auto" } else {
                        "ISO ${uiEventHandler.manualExposureIso.collectAsState().value}" })
                }

                // Shutter speed

                Row(
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .width(uiScale.dp * 25)
                        .height(uiScale.dp * 8)
                        .clickable {
                            if (sliderTarget == "ShutterSpeed") {
                                modelData.disableSlider()
                            } else {
                                modelData.setAutoExposureState(false)
                                modelData.setSliderTargetShutterSpeed()
                            }
                        }
                ) {
                    val ss = (uiEventHandler.manualExposureShutterSpeed
                        .collectAsState().value.toDouble() / 1000000000.0).toFloat()
                    val sss = if (ss < 1) { "/ ${(1F / ss).toInt()}" }
                        else { String.format("%.1f", ss) }

                    generalComponents.TextMain(text = if (isAutoExposureEnabled) {
                        "S Auto" } else { "S $sss" })
                }

                // White balance

                Row(
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .width(uiScale.dp * 25)
                        .height(uiScale.dp * 8)
                        .clickable {
                            if (sliderTarget == "WhiteBalance") {
                                modelData.disableSlider()
                            } else {
                                modelData.setAutoWhiteBalanceState(false)
                                modelData.setSliderTargetWhiteBalance()
                            }
                        }
                ) {
                    val isAutoWhiteBalanceEnabled =
                        modelData.isAutoWhiteBalanceEnabled.collectAsState().value

                    generalComponents.TextMain(text = if (isAutoWhiteBalanceEnabled) {
                        "WB Auto" } else { "WB M" } )
                }
            }
        }
    }


    @Composable
    fun BottomButtonsBar() {
        Row(
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .fillMaxWidth()
                .height(uiScale.dp * 18)
                .background(Color(0x991A1A1A))
        ) {
            generalComponents.ImageIcon1(
                R.drawable.icon_switch,
                modifier = Modifier
                    .width(uiScale.dp * 9)
                    .height(uiScale.dp * 9)
                    .clickable { uiEventHandler.switchCameraButtonClicked() }
            )

            val sliderTarget = modelData.sliderTarget.collectAsState().value
            generalComponents.ImageIcon1(
                R.drawable.icon_af2,
                modifier = Modifier
                    .width(uiScale.dp * 9)
                    .height(uiScale.dp * 9)
                    .clickable {
                        // Enable auto focus
                        modelData.setAutoFocusState(true)
                        if (sliderTarget == "Focus") {
                            modelData.disableSlider()
                        }
                        // Run focusing
                        uiEventHandler.focusButtonClicked()
                    }
            )

            val cameraMode = modelData.cameraMode.collectAsState().value
            val isCaptureMedaNow = modelData.isCaptureMedaNow.collectAsState().value

            var captureButtonIcon = R.drawable.icon_shoot
            if (cameraMode == "PhotoDefault" || cameraMode == "PhotoNight")
                { captureButtonIcon = if (isCaptureMedaNow) {
                R.drawable.icon_shoot_2 } else { R.drawable.icon_shoot } }
            if (cameraMode == "VideoDefault" || cameraMode == "VideoManual") {
                    captureButtonIcon = if (isCaptureMedaNow) {
                R.drawable.icon_shoot_4 } else { R.drawable.icon_shoot_3 } }
            generalComponents.ImageIcon1(
                captureButtonIcon,
                modifier = Modifier
                    .width(uiScale.dp * 9)
                    .height(uiScale.dp * 9)
                    .clickable { uiEventHandler.captureButtonClicked() }
            )

            val stabilizedCaptureButtonState =
                modelData.stabilizedCaptureButtonState.collectAsState().value

            var stabilizedCaptureButtonIcon = R.drawable.icon_shake
            if (stabilizedCaptureButtonState == 1) {
                stabilizedCaptureButtonIcon = R.drawable.icon_shake_2}
            if (stabilizedCaptureButtonState == 2) {
                stabilizedCaptureButtonIcon = R.drawable.icon_shake_3}
            if (stabilizedCaptureButtonState == 3) {
                stabilizedCaptureButtonIcon = R.drawable.icon_shake_4}
            if (stabilizedCaptureButtonState == 4) {
                stabilizedCaptureButtonIcon = R.drawable.icon_shake_5}
            generalComponents.ImageIcon1(
                stabilizedCaptureButtonIcon,
                modifier = Modifier
                    .width(uiScale.dp * 9)
                    .height(uiScale.dp * 9)
                    .clickable { uiEventHandler.stabilizedCaptureButtonClicked() }
            )

            // Switch mode button
            // Button image
            var cameraModeIcon = R.drawable.icon_photo_camera
            if (cameraMode == "PhotoEDR") {
                cameraModeIcon = R.drawable.icon_sun }
            if (cameraMode == "PhotoNight") {
                cameraModeIcon = R.drawable.icon_night }
            if (cameraMode == "PhotoManual") {
                cameraModeIcon = R.drawable.icon_photo_camera_manual }
            if (cameraMode == "VideoDefault") {
                cameraModeIcon = R.drawable.icon_video_camera }
            if (cameraMode == "VideoManual") {
                cameraModeIcon = R.drawable.icon_video_camera_manual }
            // Hide button when camera mode selector is opened
            val activePages = modelData.activePages.collectAsState().value
            val alpha = if(activePages.contains("CameraModeSelector")) 0F else 1F
            // Draw button
            generalComponents.ImageIcon1(
                cameraModeIcon,
                modifier = Modifier
                    .width(uiScale.dp * 9)
                    .height(uiScale.dp * 9)
                    .alpha(alpha)
                    .clickable { uiEventHandler.switchModeButtonClicked() }
            )
        }
    }


    @Composable
    fun BottomControlsSlider() {
        val sliderTarget = modelData.sliderTarget.collectAsState().value

        var startValueIso by remember { mutableStateOf(0.25F) }
        if(sliderTarget == "Iso") {
            val ir = modelData.exposureIsoRange.collectAsState().value
            generalComponents.Slider1 (startValueIso, { newValue ->
                uiEventHandler.setManualExposureIso((ir.lower + (ir.upper - ir.lower) *
                        (newValue * newValue)).toInt())
                startValueIso = newValue
            })
        }

        var startValueShutterSpeed by remember { mutableStateOf(0.25F) }
        if(sliderTarget == "ShutterSpeed") {
            val sr = modelData.exposureShutterSpeedRange.collectAsState().value
            generalComponents.Slider1 (startValueShutterSpeed, { newValue ->
                uiEventHandler.setManualExposureShutterSpeed((sr.lower + (sr.upper -
                        sr.lower) * (newValue.pow(8)).toDouble()).toLong())
                startValueShutterSpeed = newValue
            })
        }

        var startValueFocus by remember { mutableStateOf(1.0F) }
        if(sliderTarget == "Focus") {
            generalComponents.Slider1 (startValueFocus, { newValue ->
                uiEventHandler.setManualFocusValue(1.0F - newValue)
                startValueFocus = newValue
            })
        }

        var startValueWhiteBalance by remember { mutableStateOf(Pair(0.5F, 0.5F)) }
        if(sliderTarget == "WhiteBalance") {
            WhiteBalanceSelector(
                mainActivity, modelData, uiEventHandler,
                uiScale, screenResolution, generalComponents
            ).WhiteBalanceSelector (startValueWhiteBalance) { value ->
                uiEventHandler.setManualWhiteBalance(value)
                startValueWhiteBalance = value
            }
        }
    }


    @Composable
    fun BottomControls() {
        Column (
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.Bottom,
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {

            // Controls on top of the bottom
            Box (
                modifier = Modifier
                    .fillMaxWidth()
                    .defaultMinSize(minHeight = uiScale.dp * 12)
            ){
                Column (
                    verticalArrangement = Arrangement.Bottom,
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier
                        .matchParentSize()
                ) {
                    ZoomSelector()
                    Spacer(modifier = Modifier.height(uiScale.dp * 4))
                }
                BottomTopLeftButton()
                BottomTopRightButton()
            }

            // Slider
            BottomControlsSlider()

            // Bottom buttons and bars
            FocusIsoShutterSpeedWhiteBalanceBar()
            BottomButtonsBar()
        }
    }


    @SuppressLint("NotConstructor")
    @Composable
    fun CameraControl() {
        Column (
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.SpaceBetween,
            horizontalAlignment = Alignment.CenterHorizontally,
        ){
            TopControls()

            val isEnableCaptureSettingsMenu =
                modelData.isEnableCaptureSettingsMenu.collectAsState().value
            androidx.compose.animation.AnimatedVisibility(
                visible = isEnableCaptureSettingsMenu,
                enter = fadeIn(animationSpec = tween(250)),
                exit = fadeOut(animationSpec = tween(250))
            ) {
                CaptureSettings()
            }

            BottomControls()
        }
    }
}