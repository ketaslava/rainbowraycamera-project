package com.ktvincco.rainbowraycamera.data.util


import android.hardware.camera2.params.RggbChannelVector
import android.util.Log
import android.util.Size
import android.view.Surface
import android.view.SurfaceView
import com.google.common.math.IntMath.sqrt
import com.ktvincco.rainbowraycamera.AppSettings
import java.math.RoundingMode


class CaptureOptions (
    private val cameraConfiguration: CameraConfiguration,
    private val imageSizePreset: Int,
    private val videoFrameRatePreset: Int,
    private val previewSurfaceView: SurfaceView?
) {


    // Settings


    private var appSettings = AppSettings()
    companion object {
        private const val LOG_TAG = "CaptureOptions"
    }


    // Variables


    // Capture target
    private var captureMode = "PreviewImageStream"

    // Format
    private var isUseRawSensorFormatForImages = false

    // Image size
    private var outputSize: Size = Size(1920, 1920)
    private var videoRotationRelativeGround = 0F

    // Exposure
    private var isEnableHardwareAutoExposure = true
    private val hardwareAutoExposureCorrection = -1
    private var iso: Int = 100
    private var shutterSpeed: Long = 100
    private var aperture = 0F // Max

    // Focus
    private var isEnableAutoFocusByHardware = false
    private var hardwareAutoFocusStep = 0
    private val hardwareAutoFocusStepStart = 3
    private var focusDistance = 0.0F
    private var focusDistanceMax = 10.0F // API uses value from 0 to 10 as from infinity to lowest

    // White balance
    private var isEnableAutoWhiteBalance = false
    private var manualWhiteBalance = Pair(0.5F, 0.5F)

    // Zoom
    private var zoomValue = 1.0F

    // Stabilization
    private var isEnableOis = false

    // Record audio
    private var isEnableRecordAudio = true

    // Flashlight
    private var isEnableFlashlight = false


    // Private


    private fun getGetPreviewImageSize(imgSize: Size): Size {
        val cameraPreviewImageSizeFactor = appSettings.getCameraPreviewImageSizeFactor()
        val scaleMultiplier = sqrt(imgSize.width, RoundingMode.HALF_UP).toFloat() *
                cameraPreviewImageSizeFactor.toFloat() / imgSize.width.toFloat()
        return Size((imgSize.width * scaleMultiplier).toInt(),
            (imgSize.height * scaleMultiplier).toInt())
    }


    // Set variables


    // Capture mode
    fun setCaptureMode(newCaptureMode: String) {

        // Set values by capture mode
        if (newCaptureMode == "PreviewImageStream") {

            // Set parameters
            outputSize = getGetPreviewImageSize(
                cameraConfiguration.getAvailableImageSizes()[imageSizePreset])
        }
        if (newCaptureMode == "ImageStream") {

            // Set parameters
            outputSize = cameraConfiguration.getAvailableImageSizes()[imageSizePreset]
        }
        if (newCaptureMode == "PreviewAndImageByRequest") {

            // Set parameters
            outputSize = cameraConfiguration.getAvailableImageSizes()[imageSizePreset]
        }
        if (newCaptureMode == "Video") {

            // Set parameters
            outputSize = cameraConfiguration.getAvailableVideoSizes()[imageSizePreset]
        }

        // Assign capture mode
        captureMode = newCaptureMode
    }


    // Format
    fun setIsUseRawSensorFormatForImages(newValue: Boolean) {
        isUseRawSensorFormatForImages = newValue}


    // Rotation
    fun setVideoRotationRelativeGround(newValue: Float) { videoRotationRelativeGround = newValue }


    // Exposure
    fun setIsEnableHardwareAutoExposure(newValue: Boolean) {
        isEnableHardwareAutoExposure = newValue
    }
    fun setIso(newValue: Int) {
        iso = newValue
    }
    fun setShutterSpeed(newValue: Long) {
        shutterSpeed = newValue
    }


    // White Balance
    fun setIsEnableAutoWhiteBalance(newValue: Boolean) {
        isEnableAutoWhiteBalance = newValue
    }
    fun setManualWhiteBalance(newValue: Pair<Float, Float>) {
        manualWhiteBalance = newValue
    }


    // Focus
    fun setIsEnableAutoFocusByHardware(newValue: Boolean) {
        isEnableAutoFocusByHardware = newValue }
    fun startHardwareAutoFocus() {
        hardwareAutoFocusStep = hardwareAutoFocusStepStart }
    fun setManualFocusValue(newValue: Float) {
        focusDistance = focusDistanceMax * newValue
    }


    // Zoom
    fun setZoomValue(newValue: Float) {zoomValue = newValue}

    // Stabilization
    fun setIsEnableOis(newValue: Boolean) {isEnableOis = newValue}

    // Record audio
    fun setIsEnableRecordAudio(newValue: Boolean) {isEnableRecordAudio = newValue}

    // Flashlight
    fun setFlashlightMode(newValue: Boolean) { isEnableFlashlight = newValue }


    // Get variables


    // Capture target
    fun getCaptureMode(): String { return captureMode }
    fun getPreviewSurface(): Surface? { return previewSurfaceView?.holder?.surface }


    // Format
    fun getIsUseRawSensorFormatForImages(): Boolean { return isUseRawSensorFormatForImages }


    // Image size
    fun getOutputSize(): Size {return outputSize}


    // Video frame rate
    fun getVideoFrameRate(): Int {
        val frameRates = cameraConfiguration.getAvailableVideoFrameRatesBySize(outputSize)
        return frameRates[videoFrameRatePreset]
    }


    // Capture template
    fun isManualTemplateAvailable(): Boolean {
        return cameraConfiguration.getIsManualTemplateAvailable()
    }
    fun getVideoRotationRelativeGround(): Float { return videoRotationRelativeGround }


    // Exposure
    fun getIsEnableHardwareAutoExposure(): Boolean { return isEnableHardwareAutoExposure }
    fun getHardwareAutoExposureCorrection(): Int { return hardwareAutoExposureCorrection }
    fun getIso(): Int { return iso }
    fun getShutterSpeed(): Long {
        // No more than 1 / 2 S while PreviewImageStream
        return if (shutterSpeed / 1000000000 > 0.5 && captureMode == "PreviewImageStream") {
            500000000 } else { shutterSpeed }
    }
    fun getAperture(): Float { return aperture }


    // Focus
    fun getIsEnableAutoFocusByHardware(): Boolean { return isEnableAutoFocusByHardware }
    fun getHardwareAutoFocusStep(): Int {
        val currentStep = hardwareAutoFocusStep
        if (hardwareAutoFocusStep > 0) { hardwareAutoFocusStep -= 1 }
        return currentStep
    }
    fun getFocusDistance(): Float {
        return if (focusDistance != focusDistanceMax) { focusDistance }
        else { Float.POSITIVE_INFINITY }
    }


    // White Balance
    fun getIsEnableAutoWhiteBalance(): Boolean { return isEnableAutoWhiteBalance }
    fun getManualWhiteBalance(): RggbChannelVector {

        // Calculate RggbChannelVector

        val go = if(manualWhiteBalance.first < 0.5) { 1.0F }
            else { 1F - (manualWhiteBalance.first - 0.5F) * 2F }
        val b = if(manualWhiteBalance.first > 0.5) { 1.0F }
            else { manualWhiteBalance.first * 2 }

        val r = if(manualWhiteBalance.second < 0.5) { 1.0F }
        else { 1F - (manualWhiteBalance.second - 0.5F) * 2F }
        val ge = if(manualWhiteBalance.second > 0.5) { 1.0F }
        else { manualWhiteBalance.second * 2 }

        // Log.i(LOG_TAG, "r: $r,  ge: $ge,  go: $go,  b: $b")

        return RggbChannelVector(r, ge, go, b)
    }


    // Zoom
    fun getZoomValue(): Float { return zoomValue }


    // Stabilization
    fun getIsEnableOis(): Boolean {
        return if (cameraConfiguration.getIsOisAvailable()) { isEnableOis } else { false }
    }


    // Record audio
    fun getIsEnableRecordAudio(): Boolean { return isEnableRecordAudio }


    // Flashlight
    fun getIsEnableFlashlight(): Boolean { return isEnableFlashlight }
}