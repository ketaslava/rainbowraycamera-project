package com.ktvincco.rainbowraycamera.domain.camera


import android.graphics.BitmapFactory
import android.media.Image
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.util.Size
import androidx.compose.runtime.collectAsState
import com.ktvincco.rainbowraycamera.AppSettings
import com.ktvincco.rainbowraycamera.data.AutomaticController
import com.ktvincco.rainbowraycamera.data.DataSaver
import com.ktvincco.rainbowraycamera.data.util.CaptureOptions
import com.ktvincco.rainbowraycamera.domain.Camera
import com.ktvincco.rainbowraycamera.domain.util.RotationListener
import com.ktvincco.rainbowraycamera.domain.util.StandardImage
import com.ktvincco.rainbowraycamera.presentation.ModelData
import com.ktvincco.rainbowraycamera.presentation.UiEventHandler
import java.nio.ByteBuffer


class Capture (
    private val camera: Camera,
    private val captureSession: CaptureSession,
    private val appSettings: AppSettings,
    private val dataSaver: DataSaver,
    private val modelData: ModelData,
    private val uiEventHandler: UiEventHandler,
    private val automaticController: AutomaticController,
    private val rotationListener: RotationListener,
) {


    // Setting


    companion object {
        private const val LOG_TAG = "CaptureDomain"
    }


    // Variables


    private var isCaptureSessionPaused = true
    private var isCaptureMediaNow = false
    private var targetImagesCountForCurrentCapture = 0
    private var captureOptions = CaptureOptions(
        camera.getConfiguration(), 0, 0,null)


    // Private


    private fun getIsCurrentCaptureMirrorRelativeToNormal(): Boolean {
        return camera.getConfiguration().getIsFrontalCamera() }
    private fun getIsCurrentCaptureMirrorByVertical(): Boolean {
        val sensorRotation = camera.getConfiguration().getRotationRelativeDeviceNormal()
        if (sensorRotation == 90F || sensorRotation == 270F) { return true }
        if (sensorRotation == 0F || sensorRotation == 180F) { return false }
        return false
    }


    private fun updateRecommendedNightModeState() {
        val state = automaticController.getRecommendedNightModeState()
        val mode = camera.getMode()

        // Set switch button state
        if (mode == "PhotoEDR" && state) {
            modelData.enableNightModeSwitchButtonAsEnter()
        } else if (mode == "PhotoNight" && !state) {
            modelData.enableNightModeSwitchButtonAsExit()
        } else {
            modelData.disableNightModeSwitchButton()
        }
    }


    private fun getRelativeGroundRotation(): Float {
        return camera.getConfiguration().getRotationRelativeDeviceNormal() +
                rotationListener.getPhoneZAxisRotation() - 90F
    }


    // Capture options


    private fun assignAutoExposureByHardware() {
        captureOptions.setIsEnableHardwareAutoExposure(true)
    }

    private fun assignAutoExposureByAutomaticController() {
        captureOptions.setIsEnableHardwareAutoExposure(false)
        captureOptions.setIso(automaticController.getIso())
        captureOptions.setShutterSpeed(automaticController.getShutterSpeed())
    }

    private fun assignManualExposureByUser() {
        captureOptions.setIsEnableHardwareAutoExposure(false)
        captureOptions.setIso(uiEventHandler.manualExposureIso.value)
        captureOptions.setShutterSpeed(uiEventHandler.manualExposureShutterSpeed.value)
    }


    fun updateCaptureOptions() {

        // Get camera mode
        val cameraMode = camera.getMode()

        // Format
        captureOptions.setIsUseRawSensorFormatForImages(
            // Capture media in PhotoManual
            cameraMode == "PhotoManual" && captureSession.getMode() == "ImageStream" &&
                    // With enabled RAW_SENSOR format
                    camera.getConfiguration().getIsRawSensorFormatAvailable() &&
                    camera.getOptions().getOptionBoolean(
                        "IsUseRawSensorFormatWhenAvailable") == true)

        // Video rotation
        var videoRotation = rotationListener.getPhoneZAxisRotation()
        if (camera.getConfiguration().getIsFrontalCamera()) { videoRotation += 180F }
        if (videoRotation > 360F) { videoRotation -= 360F }
        captureOptions.setVideoRotationRelativeGround(videoRotation)

        // Exposure

        if(!camera.getConfiguration().getIsManualTemplateAvailable()) {
            // When manual controls are unavailable
            assignAutoExposureByHardware() }
        else {
            // Get is manual control enabled by user
            val isManualCont = modelData.isAutoExposureEnabled.value

            // Always hardware auto
            if(arrayListOf("PhotoDefault", "VideoDefault").contains(cameraMode)) {
                assignAutoExposureByHardware()
            }
            // Always software auto
            if(arrayListOf("PhotoEDR", "PhotoNight").contains(cameraMode)) {
                assignAutoExposureByAutomaticController()
            }
            // Hardware auto or manual
            if(arrayListOf("VideoManual").contains(cameraMode)) {
                if (isManualCont) { assignAutoExposureByHardware() }
                else { assignManualExposureByUser() }
            }
            // Software auto or manual
            if(arrayListOf("PhotoManual").contains(cameraMode)) {
                assignManualExposureByUser()
                if (isManualCont) { assignAutoExposureByAutomaticController() }
                else { assignManualExposureByUser() }
            }
        }

        // Focus

        if (modelData.isEnableAutoFocus.value) {
            // Modes where auto focus by automaticController
            if(arrayListOf("PhotoEDR", "PhotoNight", "PhotoManual").contains(cameraMode)) {
                // Use auto focus by automaticController
                captureOptions.setIsEnableAutoFocusByHardware(false)
                captureOptions.setManualFocusValue(automaticController.getFocusValue())
            } else {
                // Use auto focus by Hardware
                captureOptions.setIsEnableAutoFocusByHardware(true)
            }
        } else {
            // Use manual focus
            captureOptions.setIsEnableAutoFocusByHardware(false)
            captureOptions.setManualFocusValue(uiEventHandler.manualFocusValue.value)
        }

        // White Balance

        // Check is manual White Balance available in current mode and auto mode disabled
        if(arrayListOf("PhotoManual", "VideoManual").contains(cameraMode) &&
            !modelData.isAutoWhiteBalanceEnabled.value) {
            // Use manual White Balance
            captureOptions.setIsEnableAutoWhiteBalance(false)
            captureOptions.setManualWhiteBalance(uiEventHandler.manualWhiteBalance.value)
        } else {
            // Use auto White Balance
            captureOptions.setIsEnableAutoWhiteBalance(true)
        }

        // Flashlight
        captureOptions.setFlashlightMode(camera.getOptions().getOptionBoolean(
            "isEnableFlashlight") == true)

        // Ois
        captureOptions.setIsEnableOis(camera.getOptions().getOptionBoolean(
            "isEnableOis") == true)

        // Record audio
        captureOptions.setIsEnableRecordAudio(camera.getOptions().getOptionBoolean(
            "isEnableRecordAudio") == true)

        // Zoom
        captureOptions.setZoomValue(uiEventHandler.cameraZoom.value)

        // Advanced
        updateRecommendedNightModeState()
    }


    // Capture


    fun onNewFrameCaptured(cameraImage: Image) {
        // Process new image from CameraService

        // Get camera mode
        val m = camera.getMode()

        // Preview
        if (captureSession.getMode() == "PreviewImageStream" &&
            arrayListOf("PhotoEDR", "PhotoNight", "PhotoManual").contains(m)) {

            // Translate image to bitmap AND null bitmap EXC
            val buffer: ByteBuffer = cameraImage.planes[0].buffer
            val bytes = ByteArray(buffer.remaining())
            buffer.get(bytes)
            val imageBitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.size) ?: return

            // Generate standard image
            var newImage = StandardImage(
                imageBitmap, camera.getConfiguration().getRotationRelativeDeviceNormal(),
                getIsCurrentCaptureMirrorRelativeToNormal(), getIsCurrentCaptureMirrorByVertical())

            // Crop preview image to original aspect ratio
            val targetImageSize = getCaptureOptions().getOutputSize()
            newImage = newImage.cropImageByNewAspectRatio(
                targetImageSize.width.toFloat() / targetImageSize.height.toFloat()
            )

            // Check imageSize (bigger than captureOptions is not available)
            // With special check for minimal resolutions
            val imgRes = newImage.getImageSize()
            val curRes = getCaptureOptions().getOutputSize()
            val availableRes = camera.getConfiguration().getAvailableImageSizes()
            val minRes = availableRes[availableRes.size - 1]
            if (imgRes.width > (curRes.width * 2.2F) + 1281F &&
                imgRes.width > minRes.width * 2 ||
                imgRes.height > (curRes.height * 2.2F) + 1281F &&
                imgRes.height > minRes.height * 2) {
                return }

            // Focus peaking
            if (camera.getOptions().getOptionBoolean("isEnableFocusPeaking") == true) {
                newImage = newImage.applyFocusPeakEffect()
            }

            // Set as camera preview image
            modelData.setCameraPreviewImage(newImage)

            // Send to automatic controller
            automaticController.processCaptureResult(bytes)
        }

        // Single image photo by request
        if (captureSession.getMode() == "PreviewAndImageByRequest" &&
            arrayListOf("PhotoDefault").contains(m)) {

            // Check imageSize, only captureOptions imageSize available
            if (Size(cameraImage.width, cameraImage.height) !=
                getCaptureOptions().getOutputSize()) { return }

            // Translate image to ByteArray
            val buffer: ByteBuffer = cameraImage.planes[0].buffer
            val bytes = ByteArray(buffer.remaining())
            buffer.get(bytes)

            // Save image

            // Select format
            val contentFormat = if(captureOptions.getIsUseRawSensorFormatForImages()
            ) "raw" else "jpg"
            // Create collection
            dataSaver.createNewCollection("Photo", contentFormat,
                getRelativeGroundRotation(), getIsCurrentCaptureMirrorRelativeToNormal(),
                getIsCurrentCaptureMirrorByVertical())
            // Write image
            dataSaver.writeByteArrayToCurrentCollection(bytes)
            // Save collection
            dataSaver.saveCurrentCollection()
        }

        // Multi image photo
        if (captureSession.getMode() == "ImageStream" &&
            arrayListOf("PhotoEDR", "PhotoNight").contains(m)) {

            // Check imageSize, only captureOptions imageSize available
            if (Size(cameraImage.width, cameraImage.height) !=
                getCaptureOptions().getOutputSize()) { return }

            // Translate image to ByteArray
            val buffer: ByteBuffer = cameraImage.planes[0].buffer
            val bytes = ByteArray(buffer.remaining())
            buffer.get(bytes)

            // Save image
            dataSaver.writeByteArrayToCurrentCollection(bytes)

            // Check is it a last image to complete collection
            if (dataSaver.getImagesCountForCurrentCollection() >=
                targetImagesCountForCurrentCapture) { stopCaptureMultiImagePhoto() }
        }

        // Single image photo by ImageStream
        if (captureSession.getMode() == "ImageStream" &&
            arrayListOf("PhotoManual").contains(m)) {

            // Check imageSize, only captureOptions imageSize available
            if (Size(cameraImage.width, cameraImage.height) !=
                getCaptureOptions().getOutputSize()) { return }

            // Image count limit
            targetImagesCountForCurrentCapture -= 1
            if (targetImagesCountForCurrentCapture < 0) { return }

            // Translate image to ByteArray
            val buffer: ByteBuffer = cameraImage.planes[0].buffer
            val bytes = ByteArray(buffer.remaining())
            buffer.get(bytes)

            // Save image

            // Select format
            val contentFormat = if(captureOptions.getIsUseRawSensorFormatForImages()
                ) "raw" else "jpg"
            // Create collection
            dataSaver.createNewCollection("Photo", contentFormat,
                getRelativeGroundRotation(), getIsCurrentCaptureMirrorRelativeToNormal(),
                getIsCurrentCaptureMirrorByVertical())
            // Write image
            dataSaver.writeByteArrayToCurrentCollection(bytes)
            // Save collection
            dataSaver.saveCurrentCollection()

            // Stop capture
            stopCaptureSingleImagePhotoByImageStream()
        }
    }


    // Photo


    private fun captureSingleImagePhotoByRequest() {
        // Illegal state EXC
        if (!captureSession.isAvailable() || isCaptureMediaNow) { return }
        // Init actions
        modelData.blinkScreenWhileCapturePhoto()
        // Request image from camera
        captureSession.makeSingleImageCapture()
    }


    private fun captureSingleImagePhotoByImageStream() {
        // Illegal state EXC
        if (!captureSession.isAvailable() || isCaptureMediaNow) { return }
        // Set ui state
        modelData.setIsEnableAlignmentCursor(true)
        modelData.setIsCaptureMedaNow(true)
        // Alignment cursor
        rotationListener.resetStartPositionForAlignmentCursor()
        rotationListener.assignOnNewAlignmentCursorPosition {
            modelData.setAlignmentCursorPosition(it) }
        // Play sound
        camera.playCaptureStartSound()
        // Set state
        setIsCaptureMediaNow(true)
        targetImagesCountForCurrentCapture = 1
        // Set capture mode
        captureSession.restartToImageStream()
    }


    private fun stopCaptureSingleImagePhotoByImageStream() {
        // Set ui state
        modelData.setIsEnableAlignmentCursor(false)
        modelData.setIsCaptureMedaNow(false)
        // PlaySound
        camera.playCaptureEndSound()
        // Enable preview again
        captureSession.restartToPreview()
        // Wait and set state
        Handler(Looper.getMainLooper()).postDelayed({
            // Set state
            setIsCaptureMediaNow(false)
        }, 256)
    }


    private fun captureMultiImagePhoto() {
        // Illegal state EXC
        if (!captureSession.isAvailable() || isCaptureMediaNow) { return }
        // Set state
        setIsCaptureMediaNow(true)
        // Set ui state
        modelData.setIsEnableCaptureSettingsMenu(false)
        modelData.setIsEnableAlignmentCursor(true)
        // Alignment cursor
        rotationListener.resetStartPositionForAlignmentCursor()
        rotationListener.assignOnNewAlignmentCursorPosition {
            modelData.setAlignmentCursorPosition(it) }
        // Play sound
        camera.playCaptureStartSound()
        // Create new collection
        // Assign content type
        val contentType = if (camera.getMode() == "PhotoEDR") { "MultiImagePhoto" } else {
            "MultiImageNightPhoto" }
        // Generate standard image
        dataSaver.createNewCollection(contentType, "jpg", getRelativeGroundRotation(),
            getIsCurrentCaptureMirrorRelativeToNormal(), getIsCurrentCaptureMirrorByVertical())
        // Set target images count for capture end
        targetImagesCountForCurrentCapture = appSettings.getTargetImagesCountForPhotoCollection()
        // Set capture mode
        captureSession.restartToImageStream()
    }


    private fun stopCaptureMultiImagePhoto() {
        // Illegal state EXC
        if (!captureSession.isAvailable() || !isCaptureMediaNow) { return }
        // Set ui state
        modelData.setIsEnableAlignmentCursor(false)
        modelData.setIsCaptureMedaNow(false)
        // Save collection
        dataSaver.saveCurrentCollection()
        // PlaySound
        camera.playCaptureEndSound()
        // Enable preview again
        captureSession.restartToPreview()
        // Wait and set state
        Handler(Looper.getMainLooper()).postDelayed({
            // Set state
            setIsCaptureMediaNow(false)
        }, 200)
    }


    // Video


    private fun startCaptureVideo() {
        // Illegal state EXC
        if (!captureSession.isAvailable() || isCaptureMediaNow) { return }
        // Set state
        setIsCaptureMediaNow(true)
        modelData.setIsEnableCaptureSettingsMenu(false)
        // Create new collection
        dataSaver.createNewCollection("Video", "mp4",
            getRelativeGroundRotation(), getIsCurrentCaptureMirrorRelativeToNormal(),
            getIsCurrentCaptureMirrorByVertical())
        // Set capture mode
        captureSession.restartToVideo()
    }


    private fun stopCaptureVideo() {
        // Illegal state EXC
        if (captureSession.getState() != "Available" || !isCaptureMediaNow) { return }
        // Stop capture session
        captureSession.stop()
        // Set state
        modelData.setIsCaptureMedaNow(false)
        // Wait and capture preview
        Handler(Looper.getMainLooper()).postDelayed({
            // Enable preview again
            captureSession.restartToPreview()
            // Save collection
            dataSaver.saveCurrentCollection()
            // Set state
            setIsCaptureMediaNow(false)
        }, 500)
    }


    // Capture media


    fun forceStopCaptureMedia() {
        // Stop capture media
        if (isCaptureMediaNow) {
            // Save collection and delete if broken
            dataSaver.saveCurrentCollection()
            dataSaver.deleteAllBrokenCollections()
            // Set state
            isCaptureMediaNow = false
            modelData.setIsEnableAlignmentCursor(false)
        }
    }


    // Actions


    fun switchCapture() {
        // Get camera mode
        val m = camera.getMode()
        // Process event
        if (m == "PhotoDefault" && !isCaptureMediaNow()) { captureSingleImagePhotoByRequest() }
        if ((m == "PhotoEDR" || m == "PhotoNight") && !isCaptureMediaNow()) {
            captureMultiImagePhoto() }
        if (m == "PhotoManual" && !isCaptureMediaNow()) { captureSingleImagePhotoByImageStream() }
        if (m == "VideoDefault" || m == "VideoManual") {
            if (!isCaptureMediaNow()) { startCaptureVideo() } else { stopCaptureVideo() } }
    }


    fun updateFocus(newPosition: Pair<Float, Float> = Pair(-1.0F, -1.0F)) {

        // Disable autofocus when session is not available or when capture photo
        if (!captureSession.isAvailable() || (camera.isModeIsPhoto() &&
            isCaptureMediaNow())) { return }

        // Apply for Hardware or AutomaticController focus
        if(captureOptions.getIsEnableAutoFocusByHardware()) {

            // Log
            // Log.i(LOG_TAG, "Start Hardware auto focus")
            // Start focusing
            captureOptions.startHardwareAutoFocus()

        } else {
            // Check is position changed in available range
            if(newPosition != automaticController.getFocusPoint() &&
                    newPosition != Pair(-1.0F, -1.0F)) {
                // Set new focus point
                automaticController.setFocusPoint(newPosition)
                // Start focusing
                automaticController.startFocusing()
            }
            // Check is event called without newPosition
            if(newPosition == Pair(-1.0F, -1.0F)) {
                // Start focusing
                automaticController.startFocusing()
            }
        }
    }


    // State changes


    fun onNewActivityState(newState: String) {
        if (newState == "onPause") {
            // Sensor listener
            rotationListener.onPause()
            // Force stop capture media
            forceStopCaptureMedia()
            // Capture session
            captureSession.stop()
            isCaptureSessionPaused = true
        }
        if (newState == "onResume") {
            if (isCaptureSessionPaused) {
                isCaptureSessionPaused = false
                // Wait and restart capture session
                Handler(Looper.getMainLooper()).postDelayed({
                    // Capture session
                    captureSession.restartToPreview()
                }, 500)
                // Sensor listener
                rotationListener.onResume()
            }
        }
    }


    // Get and set


    fun getCaptureOptions(): CaptureOptions { return captureOptions }
    fun setCaptureOptions(value: CaptureOptions) { captureOptions = value }


    fun isCaptureMediaNow(): Boolean { return isCaptureMediaNow }
    private fun setIsCaptureMediaNow(newValue: Boolean) { isCaptureMediaNow = newValue
        if (newValue) automaticController.stopForCapture() else automaticController.resume()
        modelData.setIsCaptureMedaNow(newValue) }

}