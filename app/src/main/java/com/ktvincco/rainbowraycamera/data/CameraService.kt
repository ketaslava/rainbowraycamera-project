package com.ktvincco.rainbowraycamera.data


import android.Manifest
import android.app.Activity
import android.content.pm.PackageManager
import android.graphics.ImageFormat
import android.hardware.camera2.CameraCaptureSession
import android.hardware.camera2.CameraDevice
import android.hardware.camera2.CameraManager
import android.hardware.camera2.CaptureRequest
import android.media.Image
import android.media.ImageReader
import android.media.MediaRecorder
import android.os.Build
import android.os.Handler
import android.os.HandlerThread
import android.util.Log
import android.view.Surface
import androidx.core.content.ContextCompat
import com.ktvincco.rainbowraycamera.data.util.CaptureOptions
import java.io.IOException
import android.util.Size


class CameraService(
    private val mainActivity: Activity,
    private val cameraManager: CameraManager,
    private val cameraDeviceId: String,
    private val dataSaver: DataSaver,
) {


    // Settings


    companion object {
        private const val LOG_TAG = "CameraService"
    }


    // Variables


    // Camera
    private var isCameraMustBeOpened = false
    private var cameraDevice: CameraDevice? = null
    private var captureSession: CameraCaptureSession? = null
    // Capture
    private var captureOptions: CaptureOptions? = null
    private var captureCallback: (
        status: Boolean, cameraImage: Image?, exception: String) -> Unit = {_,_,_->}
    private var isCaptureVideoNow = false
    private val captureThread = HandlerThread("CaptureThread")
    // Capture target
    private var captureTargets = mutableListOf<Surface>()
    private var imageReader = ImageReader.newInstance(
        1000, 1000, ImageFormat.JPEG, 1)
    private var mediaRecorder = MediaRecorder()


    // Private


    private fun selectTemplate(): Int {
        return if (captureOptions!!.isManualTemplateAvailable()) {
            CameraDevice.TEMPLATE_MANUAL
        } else {
            CameraDevice.TEMPLATE_STILL_CAPTURE
        }
    }


    private fun createImageReader(): ImageReader {

        // Null check -> return existent imageReader
        if (captureOptions == null) { return imageReader }

        // Get image size
        val imageSize = captureOptions!!.getOutputSize()

        // Get format
        val format = if(!captureOptions!!.getIsUseRawSensorFormatForImages()) {
            ImageFormat.JPEG } else { ImageFormat.RAW_SENSOR }

        // Format Log // DEV
        Log.i("FormatFormatFormat", "is use RAW format: ${
            captureOptions!!.getIsUseRawSensorFormatForImages()}")

        // Create ImageReader
        val newImageReader = ImageReader.newInstance(
            imageSize.width,
            imageSize.height,
            format, // Use the format that suits your needs
            2 // Max images in the reader's queue
        )

        // Set up listener for each captured frame
        newImageReader.setOnImageAvailableListener(
            { reader ->
                val image: Image? = reader?.acquireLatestImage()
                image?.let {
                    captureCallback(true, it, "")
                    it.close()
                }
            },
            Handler(captureThread.looper) // Capture thread
        )

        return newImageReader
    }


    private fun createMediaRecorder() {

        // Log
        Log.i(LOG_TAG, "Create media recorder")

        // Null check
        if (captureOptions == null) {return}

        // Create new media recorder
        mediaRecorder = MediaRecorder()

        // Set video source
        mediaRecorder.setVideoSource(MediaRecorder.VideoSource.SURFACE)
        // Set audio source
        if (captureOptions!!.getIsEnableRecordAudio()) {
            mediaRecorder.setAudioSource(MediaRecorder.AudioSource.MIC)
        }

        // Set file format
        mediaRecorder.setOutputFormat(MediaRecorder.OutputFormat.MPEG_4)

        // Set video codec
        mediaRecorder.setVideoEncoder(MediaRecorder.VideoEncoder.H264)
        // Set audio codec
        if (captureOptions!!.getIsEnableRecordAudio()) {
            mediaRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AAC)
        }

        // Set default video parameters
        mediaRecorder.setVideoEncodingBitRate(10000000) // 10 000 Kbps
        mediaRecorder.setVideoFrameRate(captureOptions!!.getVideoFrameRate())
        // Set default audio parameters
        if (captureOptions!!.getIsEnableRecordAudio()) {
            mediaRecorder.setAudioChannels(1)
            mediaRecorder.setAudioSamplingRate(44100)
            mediaRecorder.setAudioEncodingBitRate(128000)
        }

        // Set video resolution
        val videoResolution = captureOptions!!.getOutputSize()
        mediaRecorder.setVideoSize(videoResolution.width, videoResolution.height)

        // Set file path
        val outputFilePath = dataSaver.getCurrentCollectionPathForVideoFile()
        mediaRecorder.setOutputFile(outputFilePath)

        // Set rotation
        val rotation = captureOptions!!.getVideoRotationRelativeGround().toInt()
        var normalizedAngle = rotation % 360
        if (normalizedAngle < 0) { normalizedAngle += 360 }
        mediaRecorder.setOrientationHint(normalizedAngle)

        // Prepare media recorder
        try {
            mediaRecorder.prepare()
        } catch (e: IOException) {
            // Log
            Log.i(LOG_TAG, "EXC while prepare mediaRecorder")
            e.printStackTrace()
            // Close camera
            closeCamera()
            // Callback
            captureCallback(false, null, "MediaRecorderPrepareError")
        }
    }


    private fun applyCaptureOptions (
        captureRequestBuilder: CaptureRequest.Builder, captureOptions: CaptureOptions
    ): CaptureRequest.Builder {

        // Configure builder
        captureRequestBuilder.apply {

            // Default value options

            // Control mode
            set(CaptureRequest.CONTROL_MODE, CaptureRequest.CONTROL_MODE_AUTO)

            // Black level
            set(CaptureRequest.BLACK_LEVEL_LOCK, false)

            // Color aberration fix
            set(CaptureRequest.COLOR_CORRECTION_ABERRATION_MODE,
                CaptureRequest.COLOR_CORRECTION_ABERRATION_MODE_HIGH_QUALITY)

            // Anti-banding mode
            set(CaptureRequest.CONTROL_AE_ANTIBANDING_MODE,
                CaptureRequest.CONTROL_AE_ANTIBANDING_MODE_AUTO)

            // Controlled options

            // Exposure
            if (captureOptions.getIsEnableHardwareAutoExposure()) {
                // Auto (By Hardware)
                set(CaptureRequest.CONTROL_AE_MODE, CaptureRequest.CONTROL_AE_MODE_ON)
                set(CaptureRequest.CONTROL_AE_PRECAPTURE_TRIGGER,
                    CaptureRequest.CONTROL_AE_PRECAPTURE_TRIGGER_IDLE)
                set(CaptureRequest.CONTROL_AE_EXPOSURE_COMPENSATION,
                    captureOptions.getHardwareAutoExposureCorrection())
            } else {
                // Manual (By AutoController)
                set(CaptureRequest.CONTROL_AE_MODE, CaptureRequest.CONTROL_AE_MODE_OFF)
                set(CaptureRequest.SENSOR_SENSITIVITY, captureOptions.getIso())
                set(CaptureRequest.SENSOR_EXPOSURE_TIME, captureOptions.getShutterSpeed())
                set(CaptureRequest.LENS_APERTURE, captureOptions.getAperture())
            }

            // Focus
            if (captureOptions.getIsEnableAutoFocusByHardware()) {
                // Auto mode
                set(CaptureRequest.CONTROL_AF_MODE, CaptureRequest.CONTROL_AF_MODE_AUTO)
                // Process step
                val step = captureOptions.getHardwareAutoFocusStep()
                if (step == 3) { set(CaptureRequest.CONTROL_AF_TRIGGER,
                    CaptureRequest.CONTROL_AF_TRIGGER_CANCEL) }
                if (step == 2) { set(CaptureRequest.CONTROL_AF_TRIGGER,
                    CaptureRequest.CONTROL_AF_TRIGGER_IDLE) }
                if (step == 1) {
                    set(CaptureRequest.CONTROL_AF_TRIGGER,
                        CaptureRequest.CONTROL_AF_TRIGGER_START) }
            } else {
                // Manual mode
                set(CaptureRequest.CONTROL_AF_MODE, CaptureRequest.CONTROL_AF_MODE_OFF)
                // Focus value
                set(CaptureRequest.LENS_FOCUS_DISTANCE, captureOptions.getFocusDistance())
            }

            // White Balance
            if (captureOptions.getIsEnableAutoWhiteBalance()) {

                // Auto White Balance
                set(CaptureRequest.CONTROL_AWB_LOCK, false)
                set(CaptureRequest.CONTROL_AWB_MODE, CaptureRequest.CONTROL_AWB_MODE_AUTO)
            } else {

                // Manual White Balance
                set(CaptureRequest.CONTROL_AWB_LOCK, true)
                set(CaptureRequest.CONTROL_AWB_MODE, CaptureRequest.CONTROL_AWB_MODE_OFF)
                set(CaptureRequest.COLOR_CORRECTION_GAINS, captureOptions.getManualWhiteBalance())
            }

            // Zoom
            if (Build.VERSION.SDK_INT >= 30) {
                set(CaptureRequest.CONTROL_ZOOM_RATIO, captureOptions.getZoomValue())
            }

            // Stabilization
            if (captureOptions.getIsEnableOis()) {
                val m = captureOptions.getCaptureMode()
                if ((m == "PreviewImageStream" || m == "ImageStream" ||
                        m == "PreviewAndImageByRequest") && Build.VERSION.SDK_INT >= 33) {
                    set(CaptureRequest.CONTROL_VIDEO_STABILIZATION_MODE,
                        CaptureRequest.CONTROL_VIDEO_STABILIZATION_MODE_PREVIEW_STABILIZATION)
                }
                if (m == "Video") {
                    set(CaptureRequest.CONTROL_VIDEO_STABILIZATION_MODE,
                        CaptureRequest.CONTROL_VIDEO_STABILIZATION_MODE_ON)
                }
            } else {
                set(CaptureRequest.CONTROL_VIDEO_STABILIZATION_MODE,
                    CaptureRequest.CONTROL_VIDEO_STABILIZATION_MODE_OFF)
            }

            // Flashlight
            if (captureOptions.getIsEnableFlashlight()) {
                captureRequestBuilder.set(CaptureRequest.FLASH_MODE,
                    CaptureRequest.FLASH_MODE_TORCH)
            } else {
                captureRequestBuilder.set(CaptureRequest.FLASH_MODE,
                    CaptureRequest.FLASH_MODE_OFF)
            }
        }

        // Return configured builder
        return captureRequestBuilder
    }


    private fun updateCaptureRequest() {
        try {
            // Null check
            if (cameraDevice == null) { return }
            if (captureOptions == null) { return }

            // Create capture request
            var captureRequestBuilder =
                cameraDevice!!.createCaptureRequest(selectTemplate())

            // Apply capture options
            captureRequestBuilder = applyCaptureOptions(captureRequestBuilder, captureOptions!!)

            // Configure output, Add capture targets to builder
            val m = captureOptions!!.getCaptureMode()

            if (m == "PreviewImageStream" || m == "ImageStream" || m == "Video") {
                for (captureTarget in captureTargets) {
                    captureRequestBuilder.addTarget(captureTarget) } }

            if (m == "PreviewAndImageByRequest") {
                captureRequestBuilder.addTarget(captureOptions!!.getPreviewSurface()!!)
            }

            // Advanced check
            if (isCaptureSessionAvailable() && isCameraMustBeOpened) { // Start capture
                captureSession?.setRepeatingRequest(captureRequestBuilder.build(), null, null) }

        } catch (e: Exception) {
            // Log
            Log.e(LOG_TAG, "EXC while update capture request")
            e.printStackTrace()
            // Callback
            captureCallback(false, null, "UpdateCaptureRequestError")
        }
    }


    private fun isCaptureSessionAvailable(): Boolean {
        // check is captureSession available
        return cameraDevice != null && captureSession != null
    }


    // Callback after camera device initialization
    private val cameraCallback = object : CameraDevice.StateCallback() {

        override fun onOpened(camera: CameraDevice) {
            // Set camera device
            cameraDevice = camera

            // Log
            Log.i(LOG_TAG, "Camera device opened")

            // Create CaptureSession
            createCameraCaptureSession()
        }

        override fun onDisconnected(camera: CameraDevice) {
            // Log
            Log.i(LOG_TAG, "Camera device disconnected")

            // Close camera device
            closeCamera()
        }

        override fun onError(camera: CameraDevice, error: Int) {
            // Log
            Log.e(LOG_TAG, "Camera device error: $error")

            // Close camera device
            closeCamera()

            // Init callback with exception
            captureCallback(false, null, "CameraDeviceError: $error")
        }
    }


    // Create capture session for capture
    private fun createCameraCaptureSession() {
        try {
            // Create capture session
            @Suppress("DEPRECATION")
            cameraDevice?.createCaptureSession(captureTargets,
                object : CameraCaptureSession.StateCallback() {

                    override fun onConfigured(session: CameraCaptureSession) {
                        // Assign variable
                        captureSession = session

                        // Log
                        Log.i(LOG_TAG, "Camera capture session configured")

                        // Missing configuration -> exit
                        if (!isCameraMustBeOpened) { closeCamera(); return }

                        // Start capture session
                        updateCaptureRequest()

                        // Start video recording if target is mediaRecorder
                        if (captureOptions!!.getCaptureMode() == "Video") {
                            mediaRecorder.start()
                            isCaptureVideoNow = true
                        }
                    }

                    override fun onConfigureFailed(session: CameraCaptureSession) {
                        // Assign variable
                        captureSession = null
                        // Log
                        Log.e(LOG_TAG, "Camera capture session error")
                        // Callback
                        captureCallback(false, null,
                            "CameraCaptureSessionError")
                    }

                }, null)

        } catch (e: Exception) {
            Log.e(LOG_TAG, "Camera access error: ${e.message}")
            // Callback
            captureCallback(false, null, "CameraAccessError")
        }
    }


    // Public


    fun getCameraDeviceId(): String {
        return cameraDeviceId
    }


    fun openCamera(
        newCaptureOptions: CaptureOptions,
        newCaptureCallback: (status: Boolean, cameraImage: Image?, exception: String) -> Unit
    ) {
        // Log
        Log.i(LOG_TAG, "Open camera")

        // Set state
        isCameraMustBeOpened = true

        // Assign variables
        captureOptions = newCaptureOptions
        captureCallback = newCaptureCallback

        // Start thread
        if (!captureThread.isAlive) {
            captureThread.start()
        }

        // Create capture target to capture frames
        val m = captureOptions!!.getCaptureMode()
        if (m == "PreviewImageStream" || m == "ImageStream") {
            imageReader = createImageReader()
            captureTargets = mutableListOf(imageReader.surface)
        }
        if (m == "PreviewAndImageByRequest") {
            // Create reader
            imageReader = createImageReader()
            // Get variables
            val previewSurface = captureOptions!!.getPreviewSurface()
            // Check
            if (previewSurface == null) {
                captureCallback(false, null, "nullPreviewSurface")
                return
            }
            // Set capture targets
            captureTargets = mutableListOf(imageReader.surface, previewSurface)
        }
        if (m == "Video") {
            // Check is camera was stopped
            if (!isCameraMustBeOpened) { return }
            // Create media recorder
            createMediaRecorder()
            // Get variables
            val previewSurface = captureOptions!!.getPreviewSurface()
            // Check
            if (previewSurface == null) {
                captureCallback(false, null, "nullPreviewSurface")
                return
            }
            // Set capture targets
            captureTargets = mutableListOf(mediaRecorder.surface, previewSurface)
        }

        // Check permissions is important for android system
        if (ContextCompat.checkSelfPermission(mainActivity, Manifest.permission.CAMERA) ==
            PackageManager.PERMISSION_GRANTED
        ) {
            // Try to open camera, send execute signal
            try {
                cameraManager.openCamera(cameraDeviceId, cameraCallback, null)
            } catch (e: Exception) {
                // Log
                Log.e(LOG_TAG, "EXC: open camera request error")
                e.printStackTrace()
                // Callback
                captureCallback(false, null, "OpenCameraRequestError")
            }
        } else {
            // Log
            Log.e(LOG_TAG, "EXC: open camera request permissions error")
            // Callback
            captureCallback(false, null, "CameraPermissionsError")
        }
    }


    fun updateCaptureSession(newCaptureOptions: CaptureOptions) {

        // Assign variables
        captureOptions = newCaptureOptions

        // Rebuild capture session
        if (isCaptureSessionAvailable()) {
            updateCaptureRequest()
        }
    }


    fun makeSingleImageCapture() {
        try {

            // Log
            Log.i(LOG_TAG, "makeSingleImageCapture")

            // Null check
            if (cameraDevice == null) { return }
            if (captureOptions == null) { return }

            // Create capture request
            var captureRequestBuilder =
                cameraDevice!!.createCaptureRequest(selectTemplate())

            // Apply capture options
            captureRequestBuilder = applyCaptureOptions(captureRequestBuilder, captureOptions!!)

            // Configure output, Add capture targets to builder
            captureRequestBuilder.addTarget(imageReader.surface)

            // Advanced check and Capture
            if (isCaptureSessionAvailable() && isCameraMustBeOpened) {
                captureSession?.capture(captureRequestBuilder.build(), null, null) }

        } catch (e: Exception) {
            // Log
            Log.e(LOG_TAG, "EXC while makeSingleFrameCapture")
            e.printStackTrace()
            // Callback
            captureCallback(false, null, "makeSingleImageCaptureError")
        }
    }


    fun closeCamera() {

        // Log
        Log.i(LOG_TAG, "Close camera")

        // Set state
        isCameraMustBeOpened = false

        // Stop video recording if enabled
        if (isCaptureVideoNow) {
            try {
                mediaRecorder.stop()
                mediaRecorder.release()
            } catch (e: Exception) {
                e.printStackTrace()
                captureCallback(false, null, "StopRecording")
            }
            // Set state
            isCaptureVideoNow = false
        }

        // Close camera
        cameraDevice?.close()
        cameraDevice = null
        captureSession = null
    }
}