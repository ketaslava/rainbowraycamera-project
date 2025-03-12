package com.ktvincco.rainbowraycamera.data


import android.app.Activity
import android.content.Context
import android.graphics.ImageFormat
import android.hardware.camera2.CameraCharacteristics
import android.hardware.camera2.CameraDevice
import android.hardware.camera2.CameraManager
import android.media.Image
import android.media.MediaRecorder
import android.util.Log
import android.util.Size
import android.view.SurfaceHolder
import com.ktvincco.rainbowraycamera.data.util.CameraConfiguration
import com.ktvincco.rainbowraycamera.data.util.CaptureOptions
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch


class CameraController(private var mainActivity: Activity) {


    // Settings


    companion object {
        const val LOG_TAG = "CameraController"
    }


    // Variables


    private var cameraManager: CameraManager? = null
    private var cameraServicesArray: Array<CameraService> = arrayOf()
    private var activeCameraService: CameraService? = null


    // Private


    private fun getCamerasConfigurations(): List<CameraConfiguration> {

        // Create device cameras configuration list
        var cameraConfigurations: List<CameraConfiguration> = arrayListOf()

        // Get camera characteristics for each camera service
        var cameraServiceId = 0
        for (cameraService in cameraServicesArray) {

            // Get camera characteristics
            val cameraDeviceId = cameraService.getCameraDeviceId()
            val cameraCharacteristics = cameraManager!!.getCameraCharacteristics(cameraDeviceId)

            // Get variables
            val scalerStreamConfMap = cameraCharacteristics.get(
                CameraCharacteristics.SCALER_STREAM_CONFIGURATION_MAP)

            // Get camera facing
            val facing = cameraCharacteristics.get(CameraCharacteristics.LENS_FACING)
            val isFrontalCamera =
                facing != null && facing == CameraCharacteristics.LENS_FACING_FRONT

            // Get available resolutions
            val availableImageSizes = scalerStreamConfMap?.getOutputSizes(
                SurfaceHolder::class.java)?.toList() ?: arrayListOf()
            val availableVideoSizes = scalerStreamConfMap?.getOutputSizes(
                MediaRecorder::class.java)?.toList() ?: arrayListOf()

            // Get available frame rate for video
            val availableVideoFrameRatesBySize = mutableMapOf<Size, ArrayList<Int>>()
            availableVideoSizes.forEach { size ->

                // Get the number of seconds that each frame will take to process
                val minNanoSecondsPerFrame = (scalerStreamConfMap?.getOutputMinFrameDuration(
                    MediaRecorder::class.java, size) ?: 0) / 1_000_000_000.0

                // Compute the frames per second to let user select a configuration
                val maxFps = if (minNanoSecondsPerFrame > 0)
                        (1.0 / minNanoSecondsPerFrame).toInt() else 30

                // Add standard and all missing fps
                val availableFrameRates = arrayListOf(30)
                if (maxFps > 60) availableFrameRates.add(60)
                if (maxFps > 120) availableFrameRates.add(120)
                if (maxFps > 240) availableFrameRates.add(240)
                if (maxFps > 420) availableFrameRates.add(420)
                if (maxFps > 512) availableFrameRates.add(512)
                if (maxFps > 30) { availableFrameRates.add(maxFps) }

                // Fill array
                availableVideoFrameRatesBySize[size] = availableFrameRates
            }

            // Get is manual template available
            val availableTemplates = cameraCharacteristics.get(
                CameraCharacteristics.REQUEST_AVAILABLE_CAPABILITIES)
            val isManualTemplateAvailable = availableTemplates?.contains(
                CameraCharacteristics.REQUEST_AVAILABLE_CAPABILITIES_MANUAL_SENSOR) ?: false

            // Get is raw format available
            val isRawSensorFormatAvailable =
                scalerStreamConfMap?.outputFormats?.contains(ImageFormat.RAW_SENSOR) ?: false

            // Create CameraCharacteristics object
            val cameraConfiguration = CameraConfiguration(

                // Camera service id
                cameraServiceId,

                // Facing
                isFrontalCamera,

                // Is manual template available
                isManualTemplateAvailable,

                // Is raw format available
                isRawSensorFormatAvailable,

                // Image sizes
                availableImageSizes,
                availableVideoSizes,
                cameraCharacteristics.get(CameraCharacteristics.SENSOR_ORIENTATION),
                availableVideoFrameRatesBySize,

                // Exposure
                cameraCharacteristics.get(CameraCharacteristics.SENSOR_INFO_SENSITIVITY_RANGE),
                cameraCharacteristics.get(CameraCharacteristics.SENSOR_INFO_EXPOSURE_TIME_RANGE),
                cameraCharacteristics.get(CameraCharacteristics.LENS_INFO_AVAILABLE_APERTURES),

                // Focus
                cameraCharacteristics.get(
                    CameraCharacteristics.LENS_INFO_AVAILABLE_FOCAL_LENGTHS),
                cameraCharacteristics.get(
                    CameraCharacteristics.LENS_INFO_FOCUS_DISTANCE_CALIBRATION),

                // Stabilization
                cameraCharacteristics.get(
                    CameraCharacteristics.CONTROL_AVAILABLE_VIDEO_STABILIZATION_MODES)
            )

            // Add camera configuration to array
            cameraConfigurations = cameraConfigurations.plus(cameraConfiguration)

            // Next id
            cameraServiceId += 1
        }

        return cameraConfigurations
    }


    private fun setupCameraServices(
        dataSaver: DataSaver,
    ) {

        // Reset camera services array
        cameraServicesArray = emptyArray()

        // Use new manager
        updateCameraManager()

        // Get device cameras array
        val deviceCamerasIdArray = cameraManager!!.cameraIdList

        for (deviceCameraIndex in deviceCamerasIdArray) {

            // Log
            Log.i(LOG_TAG, "Setup camera with id : $deviceCameraIndex")

            // Create new camera service
            val newCameraService = cameraManager?.let {
                CameraService(mainActivity, it, deviceCameraIndex, dataSaver)
            }

            // Add camera service to array
            if (newCameraService != null) {
                cameraServicesArray = cameraServicesArray.plus(newCameraService)
            }
        }
    }


    // Public


    fun updateCameraManager() {
        // Get camera manager
        cameraManager = mainActivity.getSystemService(Context.CAMERA_SERVICE) as CameraManager
    }


    fun setupCameraController(
        dataSaver: DataSaver,
        callback: (camerasConfiguration: List<CameraConfiguration>) -> Unit
    ) {
        // Get and return camera configurations

        // Get configurations
        setupCameraServices(dataSaver)
        var camerasConfiguration = getCamerasConfigurations()

        // If not successfully, try again more times
        if (camerasConfiguration.isEmpty()) {
            CoroutineScope(Dispatchers.Main).launch {

                // Get configurations
                while (camerasConfiguration.isEmpty()) {
                    Log.w(LOG_TAG, "camerasConfiguration is empty, try to get again")
                    delay(512)
                    setupCameraServices(dataSaver)
                    camerasConfiguration = getCamerasConfigurations()
                }

                // Return result and exit from coroutine
                callback(camerasConfiguration)
                cancel()
            }
        } else {

            // Return result
            callback(camerasConfiguration)
        }
    }


    fun startCaptureSession(
        cameraId: Int,
        captureOptions: CaptureOptions,
        captureCallback: (status: Boolean, cameraImage: Image?, exception: String) -> Unit
    ) {
        // Log
        Log.i(LOG_TAG, "Start camera with id: $cameraId")
        // Start camera service
        cameraServicesArray[cameraId].openCamera(captureOptions, captureCallback)
        activeCameraService = cameraServicesArray[cameraId]
    }


    fun updateCaptureSession(captureOptions: CaptureOptions) {
        if (activeCameraService != null) {
            activeCameraService?.updateCaptureSession(captureOptions)
        }
    }


    fun makeSingleImageCapture() {
        activeCameraService?.makeSingleImageCapture()
    }


    fun stopCaptureSession() {
        // Log
        Log.i(LOG_TAG, "Stop current camera")
        // Check is camera service exists
        if (activeCameraService != null) {
            activeCameraService?.closeCamera()
            activeCameraService = null
        }
    }
}


