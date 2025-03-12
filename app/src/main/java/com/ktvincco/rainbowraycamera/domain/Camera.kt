package com.ktvincco.rainbowraycamera.domain


import com.ktvincco.rainbowraycamera.data.util.CameraConfiguration
import com.ktvincco.rainbowraycamera.data.CameraController
import com.ktvincco.rainbowraycamera.data.util.CaptureOptions
import com.ktvincco.rainbowraycamera.presentation.ModelData
import com.ktvincco.rainbowraycamera.presentation.UiEventHandler
import com.ktvincco.rainbowraycamera.data.DataSaver

import android.app.Activity
import android.graphics.BitmapFactory
import android.media.Image
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.util.Size
import android.widget.Toast
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import com.ktvincco.rainbowraycamera.AppSettings
import com.ktvincco.rainbowraycamera.R
import com.ktvincco.rainbowraycamera.data.AutomaticController
import com.ktvincco.rainbowraycamera.data.SoundPlayer
import com.ktvincco.rainbowraycamera.domain.camera.CameraOptions
import com.ktvincco.rainbowraycamera.domain.camera.Capture
import com.ktvincco.rainbowraycamera.domain.camera.CaptureSession
import com.ktvincco.rainbowraycamera.domain.camera.Load
import com.ktvincco.rainbowraycamera.domain.util.RotationListener
import com.ktvincco.rainbowraycamera.domain.util.StandardImage
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import java.nio.ByteBuffer


class Camera (
    private var mainActivity: Activity,
    private var modelData: ModelData,
    private var uiEventHandler: UiEventHandler,
    private val cameraController: CameraController,
    private val cameraConfigurations: List<CameraConfiguration>,
    private val dataSaver: DataSaver,
    private val openDomainCallback: (domainName: String) -> Unit,
) {


    // Settings


    companion object {
        private const val LOG_TAG = "CameraDomain"
    }
    private val availableModes = arrayListOf(
        "PhotoDefault", "PhotoEDR", "PhotoNight", "PhotoManual", "VideoDefault", "VideoManual")


    // Variables


    // Components
    private val rotationListener = RotationListener(mainActivity)
    private val appSettings = AppSettings()
    private val soundPlayer = SoundPlayer(mainActivity)
    private val automaticController = AutomaticController(mainActivity)
    private val cameraOptions = CameraOptions(dataSaver)
    // Camera components
    private val load = Load(this, dataSaver, modelData)
    private val captureSession = CaptureSession(
        this, cameraController, dataSaver, modelData, uiEventHandler, automaticController)
    private val capture = Capture(this, captureSession, appSettings, dataSaver,
        modelData, uiEventHandler, automaticController, rotationListener)
    // Camera
    private var configurationId = 0
    private var mode = ""
    private var imageSizePresetForCamera = arrayListOf<Int>()
    private var videoSizePresetForCamera = arrayListOf<Int>()
    private var videoFrameRatePresetForCamera = arrayListOf<Int>()
    // Actions
    private var stabilizedCaptureWaitJob: Job? = null


    // Get and Set


    fun getConfigurations(): List<CameraConfiguration> {
        return cameraConfigurations }

    fun getCaptureOptions(): CaptureOptions { return capture.getCaptureOptions() }
    fun setCaptureOptions(value: CaptureOptions) { capture.setCaptureOptions(value) }

    fun getConfigurationId(): Int { return configurationId }
    fun setConfigurationId(value: Int) {
        configurationId = value
        updateOptions()
    }

    fun getConfiguration(): CameraConfiguration { return cameraConfigurations[configurationId] }

    fun getOptions(): CameraOptions { return cameraOptions }

    fun setImageSizePresetForCamera(value: ArrayList<Int>) {
        imageSizePresetForCamera = value }
    fun setVideoSizePresetForCamera(value: ArrayList<Int>) {
        videoSizePresetForCamera = value }
    fun setVideoFrameRatePresetForCamera(value: ArrayList<Int>) {
        videoFrameRatePresetForCamera = value }


    fun getMode(): String {
        // Use current mode or load last used mode from last session
        return if (mode != "") { mode }
        else { mode = dataSaver.loadStringByKey("CameraMode") ?: "PhotoDefault"
            setMode(mode); mode }
    }
    private fun setMode(newMode: String) {
        // Incorrect mode EXC
        if (!availableModes.contains(newMode)) {
            Log.e(LOG_TAG, "EXC: incorrect newMode in setMode"); return }
        // Assign new mode
        mode = newMode
        dataSaver.saveStringByKey("CameraMode", newMode)
        // Update UI
        modelData.setCameraMode(newMode)
        updateOptions()
    }


    fun isModeIsPhoto(): Boolean {
        val m = getMode()
        return (m == "PhotoDefault" || m == "PhotoEDR" || m == "PhotoNight" || m == "PhotoManual")
    }
    private fun isModeIsVideo(): Boolean {
        val m = getMode()
        return (m == "VideoDefault" || m == "VideoManual")
    }


    private fun getAvailableOutputSizes(): List<Size> {
        var availableOutputSizes = listOf<Size>()
        if (isModeIsPhoto()) {
            availableOutputSizes = getConfiguration().getAvailableImageSizes()
        }
        if (isModeIsVideo()) {
            availableOutputSizes = getConfiguration().getAvailableVideoSizes()
        }
        return availableOutputSizes
    }
    fun getCurrentOutputSizePreset(): Int {
        if (isModeIsPhoto()) {
            return imageSizePresetForCamera[configurationId]
        }
        if (isModeIsVideo()) {
            return videoSizePresetForCamera[configurationId]
        }
        return 0
    }
    private fun setCurrentCameraOutputSizePreset(newImageSizePreset: Int) {
        if (isModeIsPhoto()) {
            imageSizePresetForCamera[configurationId] = newImageSizePreset
            dataSaver.saveIntArrayByKey("imageSizePresetForCamera", imageSizePresetForCamera)
        }
        if (isModeIsVideo()) {
            videoSizePresetForCamera[configurationId] = newImageSizePreset
            dataSaver.saveIntArrayByKey("videoSizePresetForCamera", videoSizePresetForCamera)
        }
    }

    fun getCurrentFrameRatePreset(): Int {
        if (isModeIsVideo()) {
            return videoFrameRatePresetForCamera[configurationId]
        }
        return 0
    }
    private fun setCurrentCameraFrameRatePreset(newPreset: Int) {
        if (isModeIsVideo()) {
            videoFrameRatePresetForCamera[configurationId] = newPreset
            dataSaver.saveIntArrayByKey(
                "videoFrameRatePresetForCamera", videoFrameRatePresetForCamera)
        }
    }


    // Sounds


    fun playCaptureStartSound() { if (cameraOptions.getOptionBoolean(
            "isEnableAppSound") == true) { soundPlayer.playSound(R.raw.capture_start) } }
    fun playCaptureEndSound() { if (cameraOptions.getOptionBoolean(
            "isEnableCaptureSound") == true) { soundPlayer.playSound(R.raw.capture_end) } }
    private fun playClickSound() { if (cameraOptions.getOptionBoolean(
            "isEnableCaptureSound") == true) { soundPlayer.playSound(R.raw.click) } }
    private fun playClick2Sound() { if (cameraOptions.getOptionBoolean(
            "isEnableCaptureSound") == true) { soundPlayer.playSound(R.raw.click2) } }


    // Capture


    fun updateCaptureOptions() { capture.updateCaptureOptions() }
    fun onNewFrameCaptured(cameraImage: Image) { capture.onNewFrameCaptured(cameraImage) }
    fun forceStopCaptureMedia() { capture.forceStopCaptureMedia() }


    // Actions


    fun clearStart() {
        capture.forceStopCaptureMedia()
        captureSession.clearStart()
    }


    fun resume() {
        captureSession.restartToPreview()
    }


    private fun updateOptions() {

        // Update capture options
        modelData.setExposureIsoRange(getConfiguration().getAvailableIsoRange())
        modelData.setExposureShutterSpeedRange(getConfiguration().getAvailableShutterSpeedRange())

        // Update camera options
        cameraOptions.setOptionBoolean(
            "isRawSensorFormatAvailable", getConfiguration().getIsRawSensorFormatAvailable())

        // Update camera options in UI
        cameraOptions.updateAllKnownOptionStatesInModelData(modelData)
    }


    private fun tryToOpenGallery() {
        // Illegal state Check
        if (!captureSession.isAvailable() || capture.isCaptureMediaNow()) { return }
        // Close capture session
        captureSession.stop()
        // Open gallery domain callback
        openDomainCallback("Gallery")
    }


    private fun stabilizedCaptureAction() {
        // Start or stop stabilized capture
        if (stabilizedCaptureWaitJob == null ||
            stabilizedCaptureWaitJob?.isActive == false) {
            stabilizedCaptureWaitJob = CoroutineScope(Dispatchers.Main).launch {
                // Set button image
                modelData.setStabilizedCaptureButtonState(4)
                // Wait after button pressed
                delay(400)
                // Wait until stabilization rate less than target
                val stabRateTarget = AppSettings().getStabilizedCaptureTargetStabRate()
                var stabRate = 1F
                while (stabRate > stabRateTarget) {
                    // Get stabilization rate
                    stabRate = rotationListener.getStabilizationRate()
                    // Set button image
                    var newButtonState = 1
                    if (stabRate > 0.01) { newButtonState = 2 }
                    if (stabRate > 0.04) { newButtonState = 3 }
                    if (stabRate > 0.16) { newButtonState = 4 }
                    modelData.setStabilizedCaptureButtonState(newButtonState)
                    // Delay
                    delay(100)
                }
                // Reset button state
                modelData.setStabilizedCaptureButtonState(0)
                // When phone position is stabilized -> capture
                capture.switchCapture()
                // Remove job
                stabilizedCaptureWaitJob = null
            }
        } else {
            // Cancel job
            stabilizedCaptureWaitJob!!.cancel()
            stabilizedCaptureWaitJob = null
            // Set button idle state
            modelData.setStabilizedCaptureButtonState(0)
        }
    }


    fun setFlashlightMode(newValue: Boolean) {
        cameraOptions.setOptionBoolean("isEnableFlashlight", newValue)
    }


    // State changes


    init {

        // Load data for camera and camera ui
        load.load()

        // Assign callbacks
        assignControlCallbacks()

        // Update camera mode (preload mode and UI)
        getMode()
    }


    fun onNewActivityState(newState: String) { capture.onNewActivityState(newState) }


    fun onNewSystemEvent(newEvent: String) {
        if (newEvent == "onGoBackEvent") { tryToOpenGallery() }
        if (newEvent == "onKeyVolumeDownPressed") { capture.switchCapture() }
        if (newEvent == "onKeyVolumeUpPressed") { capture.switchCapture() }
    }


    // Controls


    // Button callbacks
    private fun assignControlCallbacks() {

        // Switch camera button callback
        uiEventHandler.assignSwitchCameraButtonCallback{
            // Illegal state Check
            if (!captureSession.isAvailable() || capture.isCaptureMediaNow()) {
                return@assignSwitchCameraButtonCallback }
            // Get cameras
            var camerasToSelect: List<String> = arrayListOf()
            for (cameraConfiguration in cameraConfigurations) {
                camerasToSelect = camerasToSelect.plus(
                    "Camera " + cameraConfiguration.getCameraServiceId()
                )
            }
            // Switch cameras
            if (camerasToSelect.size == 2) {
                // Easy switch when 2 cameras available
                captureSession.restartToPreview(if (configurationId == 0) { 1 } else { 0 })
                // Sound
                playClick2Sound()
            } else {
                // Open selector
                modelData.openSelector("Select camera", camerasToSelect, configurationId
                ) { optionIndex ->
                    if (optionIndex != configurationId) {
                        // Open camera with new index
                        captureSession.restartToPreview(optionIndex)
                        playClick2Sound()
                    }
                }
            }
        }


        // Switch mode button
        uiEventHandler.assignSwitchModeButtonCallback{
            // Illegal state Check
            if (!captureSession.isAvailable() || capture.isCaptureMediaNow()) {
                return@assignSwitchModeButtonCallback }
            // Open selector
            val modeId = availableModes.indexOf(getMode())
            modelData.openCameraModeSelector(modeId) { optionIndex ->
                if (optionIndex != modeId) {

                    // Process new mode
                    setMode(availableModes[optionIndex])
                    captureSession.restartToPreview()
                    playClick2Sound()
                }
            }
        }


        // outputSize button callback
        uiEventHandler.assignOutputSizeButtonCallback{
            // Illegal state Check
            if (!captureSession.isAvailable() || capture.isCaptureMediaNow()) {
                return@assignOutputSizeButtonCallback }

            // For photo
            if (isModeIsPhoto()) {

                // Get imageSizes
                var outputSizesToSelect: List<String> = arrayListOf()
                val availableOutputSizes = getAvailableOutputSizes()
                for (outputSize in availableOutputSizes) {
                    outputSizesToSelect = outputSizesToSelect.plus(
                        "${outputSize.width} X ${outputSize.height}"
                    )
                }
                // Open selector
                modelData.openSelector(
                    "Select resolution", outputSizesToSelect,
                    getCurrentOutputSizePreset()
                ) { optionIndex ->
                    if (optionIndex != getCurrentOutputSizePreset()) {
                        // Assign ImageSize preset
                        setCurrentCameraOutputSizePreset(optionIndex)
                        // Restart to preview
                        captureSession.restartToPreview()
                        // Sound
                        playClick2Sound()
                    }
                }
            }

            if (isModeIsVideo()) {

                // Get data
                val availableFrameRatesBySizeMap =
                    getConfiguration().getAvailableVideoFrameRatesBySizeMap()
                // Open selector
                modelData.openSelectorVideoSize(
                    "Select resolution",
                    getAvailableOutputSizes(), getCurrentOutputSizePreset(),
                    availableFrameRatesBySizeMap, getCurrentFrameRatePreset()
                ) { videoSizeIndex, frameRateIndex ->
                    // Check changes
                    if (videoSizeIndex != getCurrentOutputSizePreset() ||
                        frameRateIndex != getCurrentFrameRatePreset()) {
                        // Assign data
                        setCurrentCameraOutputSizePreset(videoSizeIndex)
                        setCurrentCameraFrameRatePreset(frameRateIndex)
                        // Restart to preview
                        captureSession.restartToPreview()
                        // Sound
                        playClick2Sound()
                    }
                }
            }
        }


        // Capture button callback
        uiEventHandler.assignCaptureButtonCallback {
            capture.switchCapture()
        }


        // Open gallery
        uiEventHandler.assignOpenGalleryButtonCallback {
            tryToOpenGallery()
        }


        // Focus button
        uiEventHandler.assignFocusButtonCallback { capture.updateFocus() }


        // Flashlight button
        uiEventHandler.assignToggleFlashlightButtonCallback {
            // Illegal state Check
            if (captureSession.getState() != "Available") {
                return@assignToggleFlashlightButtonCallback }
            // Check is flashlight option enabled
            if(cameraOptions.getOptionBoolean("isFlashlightOptionEnabled") == true) {
                // Switch Flashlight
                cameraOptions.fastSwitchBooleanOption("isEnableFlashlight")
                playClickSound()
            } else {
                Toast.makeText(mainActivity, "Disabled in settings", Toast.LENGTH_SHORT).show()
            }
        }


        // Enable or disable Flashlight option button
        uiEventHandler.assignToggleFlashlightOptionButtonCallback {
            // Check is flashlight option enabled
            cameraOptions.fastSwitchBooleanOption("isFlashlightOptionEnabled")
            updateOptions()
        }


        // Capture settings menu
        uiEventHandler.assignCaptureSettingsButtonCallback {
            // Illegal state Check
            if (captureSession.getState() != "Available") {
                return@assignCaptureSettingsButtonCallback }
            // Switch capture settings menu activity
            modelData.setIsEnableCaptureSettingsMenu(!modelData.isEnableCaptureSettingsMenu.value)
        }


        // Camera options - App sound
        uiEventHandler.assignSwitchIsEnableCaptureSoundCallback {
            // Illegal state Check
            if (!captureSession.isAvailable() || capture.isCaptureMediaNow()) {
                return@assignSwitchIsEnableCaptureSoundCallback }
            // Switch
            cameraOptions.fastSwitchBooleanOption("isEnableAppSound")
            updateOptions()
            // Sound
            playClickSound()
        }


        // Camera options - Record audio
        uiEventHandler.assignSwitchIsEnableRecordAudioCallback {
            // Illegal state Check
            if (!captureSession.isAvailable() || capture.isCaptureMediaNow()) {
                return@assignSwitchIsEnableRecordAudioCallback }
            // Switch
            cameraOptions.fastSwitchBooleanOption("isEnableRecordAudio")
            updateOptions()
        }


        // Camera options - Grid
        uiEventHandler.assignSwitchIsEnableGridCallback {
            // Illegal state Check
            if (captureSession.getState() != "Available") {
                return@assignSwitchIsEnableGridCallback }
            // Switch
            cameraOptions.fastSwitchBooleanOption("isEnableGrid")
            updateOptions()
        }


        // Camera options - FocusPeaking
        uiEventHandler.assignSwitchIsEnableFocusPeakingCallback {
            // Illegal state Check
            if (captureSession.getState() != "Available") {
                return@assignSwitchIsEnableFocusPeakingCallback }
            // Switch option
            cameraOptions.fastSwitchBooleanOption("isEnableFocusPeaking")
            updateOptions()
        }


        // Camera options - Ois
        uiEventHandler.assignSwitchIsEnableOisCallback {
            // Illegal state Check
            if (!captureSession.isAvailable() || capture.isCaptureMediaNow()) {
                return@assignSwitchIsEnableOisCallback }
            // Switch option
            cameraOptions.fastSwitchBooleanOption("isEnableOis")
            updateOptions()
        }


        // Camera options - RAW / JPG
        uiEventHandler.assignSwitchIsUseRawSensorFormatWhenAvailable {
            // Illegal state Check
            if (!captureSession.isAvailable() || capture.isCaptureMediaNow()) {
                return@assignSwitchIsUseRawSensorFormatWhenAvailable }
            // Switch option
            cameraOptions.fastSwitchBooleanOption("IsUseRawSensorFormatWhenAvailable")
            updateOptions()
            captureSession.restartToPreview()
        }


        // Stabilized capture callback
        uiEventHandler.assignStabilizedCaptureButtonCallback {
            // Illegal state Check
            if (!captureSession.isAvailable() || capture.isCaptureMediaNow()) {
                return@assignStabilizedCaptureButtonCallback }
            // Sound
            playClickSound()
            // Run actions
            stabilizedCaptureAction()
        }


        // About app
        uiEventHandler.assignOnOpenAboutAppButtonClicked {
            // Illegal state Check
            if (!captureSession.isAvailable() || capture.isCaptureMediaNow()) {
                return@assignOnOpenAboutAppButtonClicked }
            // Open about app
            captureSession.stop()
            modelData.openAboutApp()
        }
        uiEventHandler.assignOnCloseAboutAppButtonClicked {
            // Open camera
            modelData.openCamera()
            captureSession.restartToPreview()
        }


        // Switch night mode
        uiEventHandler.assignSwitchNightModeButtonCallback {
            // Illegal state Check
            if (!captureSession.isAvailable() || capture.isCaptureMediaNow()) {
                return@assignSwitchNightModeButtonCallback }
            // Switch mode
            val mode = getMode()
            if (mode == "PhotoEDR") { setMode("PhotoNight") }
            if (mode == "PhotoNight") { setMode("PhotoDefault") }
            captureSession.restartToPreview()
            playClick2Sound()
        }


        // Focus point update
        uiEventHandler.assignFocusPointUpdateCallback { newPosition ->
            capture.updateFocus(newPosition)
        }
    }
}