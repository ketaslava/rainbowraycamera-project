package com.ktvincco.rainbowraycamera.domain.camera


import android.media.Image
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.util.Size
import com.ktvincco.rainbowraycamera.AppSettings
import com.ktvincco.rainbowraycamera.data.AutomaticController
import com.ktvincco.rainbowraycamera.data.CameraController
import com.ktvincco.rainbowraycamera.data.DataSaver
import com.ktvincco.rainbowraycamera.data.util.CaptureOptions
import com.ktvincco.rainbowraycamera.domain.Camera
import com.ktvincco.rainbowraycamera.presentation.ModelData
import com.ktvincco.rainbowraycamera.presentation.UiEventHandler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch


class CaptureSession (
    private val camera: Camera,
    private val cameraController: CameraController,
    private val dataSaver: DataSaver,
    private val modelData: ModelData,
    private val uiEventHandler: UiEventHandler,
    private val automaticController: AutomaticController,
) {


    // Settings


    companion object {
        private const val LOG_TAG = "CaptureSession"
    }
    private val availableCaptureSessionStates =
        arrayListOf("Stopped", "Configured", "Available")
    private val availableCaptureModes = arrayListOf("PreviewImageStream", "ImageStream",
        "PreviewAndImageByRequest", "Video")


    // Variables


    private var stateId = 0
    private var mode = "PreviewImageStream"
    private var updateLoopJob: Job? = null


    // Private


    private fun updatePreviewSurfaceAspectRatio(captureOptions: CaptureOptions) {
        val outSize = captureOptions.getOutputSize()
        modelData.setPreviewSurfaceAspectRatio(outSize.width.toFloat() / outSize.height.toFloat())
    }


    private fun captureCallback(status: Boolean, cameraImage: Image?, exception: String) {
        if (status) {

            // When success camera capture

            // Mark capture session as available if configured
            if(getState() == "Configured") { setCaptureSessionState("Available") }
            // Exit if capture session not available
            if (getState() != "Available") { return }

            // Process frame if available
            if (cameraImage != null) { camera.onNewFrameCaptured(cameraImage) }

        } else {

            // Log
            // Log.e(LOG_TAG, "EXC: captureCallback: $exception")
            Log.e("CaptureCallbackExceptions", "EXC: captureCallback: $exception")

            // EXC when stop video recording
            if (exception == "StopRecording") {

                // Open popup
                modelData.openPopup("Camera error", "Error",
                    "Try to choosing a different shooting resolution",
                    "Ok") {

                    // Wait and capture preview
                    Handler(Looper.getMainLooper()).postDelayed(
                        { restartToPreview() }, 500)
                }

                // End EXC process
                return
            }

            // Error when update
            if (exception == "UpdateCaptureRequestError") { return }

            // previewSurface is not initialized
            if (exception == "nullPreviewSurface") {
                // Wait and capture preview
                Handler(Looper.getMainLooper()).postDelayed(
                    { restartToPreview() }, 256)
                // Exit
                return
            }

            // Illegal configuration may caused by RAW format
            if (exception == "CameraCaptureSessionError" && camera.getOptions().getOptionBoolean(
                    "IsUseRawSensorFormatWhenAvailable") == true) {

                // Set JPEG format
                camera.getOptions().setOptionBoolean(
                    "IsUseRawSensorFormatWhenAvailable", false)

                // Open popup
                modelData.openPopup("Camera error", "Error",
                    "Unexpected error, press OK to restart the camera",
                    "Ok") {

                    // Wait and capture preview
                    Handler(Looper.getMainLooper()).postDelayed({
                        cameraController.updateCameraManager()
                        restart(camera.getConfigurationId(), "PreviewImageStream")
                    }, 500)
                }
            }

            // Another exceptions

            // Stop capture session
            camera.forceStopCaptureMedia()
            stop()

            // Open popup
            modelData.openPopup("Camera error", "Error",
                "Unexpected error, press OK to restart the camera",
                "Ok") {

                // Wait and capture preview
                Handler(Looper.getMainLooper()).postDelayed({
                    cameraController.updateCameraManager()
                    restart(camera.getConfigurationId(), "PreviewImageStream")
                }, 500)
            }
        }
    }


    private suspend fun updateLoop() {
        val timeBetweenUpdates = AppSettings().getTimeoutBetweenCaptureOptionUpdatesMillis()
        while (updateLoopJob?.isActive == true) {
            // Update capture options
            camera.updateCaptureOptions()
            // Update capture session
            cameraController.updateCaptureSession(camera.getCaptureOptions())
            // Delay
            delay(timeBetweenUpdates)
        }
    }


    // Capture session controls


    fun clearStart() {
        // Disable flashlight
        camera.setFlashlightMode(false)
        // Use configuration from last session
        val configurationId = dataSaver.loadIntByKey("cameraConfigurationId") ?: 0
        // Resume
        restartToPreview(configurationId)
    }


    fun restartToPreview(newCamConfId: Int = camera.getConfigurationId()) {
        // Restart
        val m = camera.getMode()
        if (m == "PhotoDefault"|| m == "VideoDefault" || m == "VideoManual") {
            restart(newCamConfId, "PreviewAndImageByRequest")
        }
        if (m == "PhotoEDR" || m == "PhotoNight" || m == "PhotoManual") {
            restart(newCamConfId, "PreviewImageStream")
        }
    }


    private fun restart(newCameraConfigurationId: Int, newCaptureMode: String) {

        // Stop capture
        stop()

        // Assign camera configuration
        camera.setConfigurationId(newCameraConfigurationId)
        // Save as last configuration ID
        dataSaver.saveIntByKey("cameraConfigurationId", newCameraConfigurationId)

        // Assign mode
        if (!availableCaptureModes.contains(newCaptureMode)) {
            Log.e(LOG_TAG, "Illegal capture mode in setCaptureMode()") }
        mode = newCaptureMode

        // Renew capture options
        camera.setCaptureOptions(CaptureOptions(camera.getConfiguration(),
            camera.getCurrentOutputSizePreset(), camera.getCurrentFrameRatePreset(),
            uiEventHandler.mainPreviewSurfaceView.value))

        camera.getCaptureOptions().setCaptureMode(newCaptureMode)
        camera.updateCaptureOptions()

        // Renew automatic controller
        automaticController.updateCameraConfiguration(camera.getConfiguration())
        automaticController.stopFocusing()

        // Update preview surface
        updatePreviewSurfaceAspectRatio(camera.getCaptureOptions())

        // Start capture
        try {
            cameraController.startCaptureSession(
                camera.getConfiguration().getCameraServiceId(),
                camera.getCaptureOptions()
            ) { status, image, exc -> captureCallback(status, image, exc) }
        } catch (e: Exception) {
            // Log
            Log.e(LOG_TAG, "EXC in restartCaptureSession while startCaptureSession")
            e.printStackTrace()
        }

        // Start capture session update loop
        if (updateLoopJob?.isActive == false ||
            updateLoopJob == null) {
            updateLoopJob = CoroutineScope(Dispatchers.Default).launch {
                updateLoop()
            }
        }

        // Set capture session state
        // Mark as configured for default
        setCaptureSessionState("Configured")
        // Special states for modes
        if (mode == "Video" || mode == "PreviewAndImageByRequest") {
            setCaptureSessionState("Available")
            modelData.setIsEnableSurfacePreview(true)
        }
    }


    fun makeSingleImageCapture() {
        // Check
        if (getMode() != "PreviewAndImageByRequest") {
            Log.e(LOG_TAG, "Illegal mode for capture in makeSingleImageCapture") }
        // Request capture
        cameraController.makeSingleImageCapture()
    }


    fun stop() {
        // Set state
        setCaptureSessionState("Stopped")
        modelData.setIsEnableSurfacePreview(false)
        // Stop capture session
        try {
            updateLoopJob?.cancel()
            cameraController.stopCaptureSession()
        } catch (e: Exception) {
            Log.e(LOG_TAG, "EXC in stopCaptureSession"); e.printStackTrace()
        }
        // Set UI
        modelData.setCameraPreviewImage(null)
    }


    // Controls


    fun restartToImageStream() {
        restart(camera.getConfigurationId(), "ImageStream") }
    fun restartToVideo() {
        restart(camera.getConfigurationId(), "Video") }


    // Get and Set


    fun getState(): String { return availableCaptureSessionStates[stateId] }
    private fun setCaptureSessionState(newValue: String) {
        // Incorrect value EXC
        if(!availableCaptureSessionStates.contains(newValue)) {
            Log.e(LOG_TAG, "EXC: incorrect newValue in setCaptureSessionState"); return }
        // Assign state id
        stateId = availableCaptureSessionStates.indexOf(newValue)
    }

    fun isAvailable(): Boolean { return getState() == "Available" }
    fun getMode(): String { return mode }

}