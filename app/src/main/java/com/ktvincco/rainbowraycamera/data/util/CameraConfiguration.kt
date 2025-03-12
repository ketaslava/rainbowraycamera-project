package com.ktvincco.rainbowraycamera.data.util


import android.util.Range
import android.util.Size


class CameraConfiguration (
    // Device id
    private val cameraServiceId: Int,
    // Facing
    private val isFrontalCamera: Boolean,
    // Is manual template available
    private val isManualTemplateAvailable: Boolean,
    // Is raw format available
    private val isRawSensorFormatAvailable: Boolean,
    // Image sizes
    private val availableImageSizes: List<Size>,
    private val availableVideoSizes: List<Size>,
    private val rotationRelativeDeviceNormal: Int?,
    private val availableVideoFrameRateBySize: Map<Size, ArrayList<Int>>,
    // Exposure
    private val availableIsoRange: Range<Int>?,
    private val availableShutterSpeedRange: Range<Long>?,
    private val availableApertureSteps: FloatArray?,
    // Focus
    private val availableFocusDistance: FloatArray?,
    private val focusCalibration: Int?,
    // Stabilization
    private val newAvailableStabilizationModes: IntArray?
) {


    // Get parameters


    // Device id
    fun getCameraServiceId(): Int {return cameraServiceId}
    // Facing
    fun getIsFrontalCamera(): Boolean {return isFrontalCamera}
    // Is manual template available
    fun getIsManualTemplateAvailable(): Boolean { return isManualTemplateAvailable }
    // Is raw format available
    fun getIsRawSensorFormatAvailable(): Boolean { return isRawSensorFormatAvailable }

    // Image sizes
    fun getAvailableImageSizes(): List<Size> {
        // ImageSizes
        return if (availableImageSizes.isNotEmpty())
            availableImageSizes.sortedByDescending { it.width * it.height }
        else arrayListOf(Size(1920, 1080))
    }
    fun getAvailableVideoSizes(): List<Size> {
        // ImageSizes
        return if (availableVideoSizes.isNotEmpty())
            availableVideoSizes.sortedByDescending { it.width * it.height }
        else arrayListOf(Size(1920, 1080))
    }
    fun getRotationRelativeDeviceNormal(): Float {
        return if (rotationRelativeDeviceNormal != null)
            ((rotationRelativeDeviceNormal + 360) % 360).toFloat()
        else 0F
    }
    fun getAvailableVideoFrameRatesBySize(targetSize: Size): ArrayList<Int> {
        return availableVideoFrameRateBySize[targetSize] ?: ArrayList(30)
    }
    fun getAvailableVideoFrameRatesBySizeMap(): Map<Size, ArrayList<Int>> {
        return availableVideoFrameRateBySize
    }

    // Exposure
    fun getAvailableIsoRange(): Range<Int> {
        return availableIsoRange ?: Range(0, 14000)
    }
    fun getAvailableShutterSpeedRange(): Range<Long> {
        return availableShutterSpeedRange ?: Range(0, 16000000)
    }
    fun getAvailableApertureSteps(): List<Float> {
        return availableApertureSteps?.toList() ?: arrayListOf(0.0F)
    }

    // Focus
    fun getAvailableFocusDistance(): Range<Float> {
        return if (availableFocusDistance != null && availableFocusDistance.isNotEmpty())
            Range(availableFocusDistance.min(), availableFocusDistance.max())
        else Range(24F, 64F)
    }
    fun getFocusCalibration(): Int { return focusCalibration ?: 0 }

    // Stabilization
    fun getIsOisAvailable(): Boolean {
        return newAvailableStabilizationModes != null &&
                newAvailableStabilizationModes.isNotEmpty()
    }
}