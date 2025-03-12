package com.ktvincco.rainbowraycamera.domain.camera


import android.util.Log
import com.ktvincco.rainbowraycamera.data.DataSaver
import com.ktvincco.rainbowraycamera.domain.Camera
import com.ktvincco.rainbowraycamera.presentation.ModelData


class Load (
    private val camera: Camera,
    private val dataSaver: DataSaver,
    private val modelData: ModelData
) {


    // Private


    // Load options for camera work
    private fun loadCaptureSettings() {

        // Load or create imageSizePresets for PHOTO
        val loadedImageSizePresetForCamera =
            dataSaver.loadIntArrayByKey("imageSizePresetForCamera")
        if (loadedImageSizePresetForCamera != null) {
            // Assign loaded value
            camera.setImageSizePresetForCamera(loadedImageSizePresetForCamera)
        } else {
            // Fill image size preset for camera list as 0
            val imageSizePresetForCamera = arrayListOf<Int>()
            camera.getConfigurations().forEach { _ -> imageSizePresetForCamera.add(0) }
            camera.setImageSizePresetForCamera(imageSizePresetForCamera)
        }

        // Load or create videoSizePresetForCamera
        var videoSizePresetForCamera = arrayListOf<Int>()
        val loadedVideoSizePresetForCamera =
            dataSaver.loadIntArrayByKey("videoSizePresetForCamera")
        if (loadedVideoSizePresetForCamera != null) {
            // Assign value
            videoSizePresetForCamera = loadedVideoSizePresetForCamera
        } else {
            // For each camera select presets with 60 FPS available
            // or select first (0 index) preset
            var i = 0
            camera.getConfigurations().forEach { conf ->
                var selectedPreset = 0
                var currentPreset = 0
                for (imageSize in conf.getAvailableVideoSizes()) {
                    val frameRates = conf.getAvailableVideoFrameRatesBySize(imageSize)
                    if (frameRates.contains(60) && selectedPreset == 0) {
                        selectedPreset = currentPreset }
                    currentPreset += 1
                }
                // Apply preset
                videoSizePresetForCamera.add(selectedPreset)
                // Next camera
                i += 1
            }
        }
        // Assign
        camera.setVideoSizePresetForCamera(videoSizePresetForCamera)

        // Load or create videoFrameRatePresetForCamera
        val loadedVideoFrameRatePresetForCamera =
            dataSaver.loadIntArrayByKey("videoFrameRatePresetForCamera")
        if (loadedVideoFrameRatePresetForCamera != null) {
            // Assign value
            camera.setVideoFrameRatePresetForCamera(loadedVideoFrameRatePresetForCamera)
        } else {
            // Assign frame rates for cameras
            val videoFrameRatePresetForCamera = arrayListOf<Int>()
            // For each camera select 60 if available or 30 fps preset
            var i = 0
            camera.getConfigurations().forEach { conf ->
                // Get available frame rates for selected image size
                val sizes = conf.getAvailableVideoSizes()
                val frameRates = conf.getAvailableVideoFrameRatesBySize(
                    sizes[videoSizePresetForCamera[i]])
                // Apply
                videoFrameRatePresetForCamera.add(
                    if (frameRates.contains(60)) frameRates.indexOf(60)
                    else if (frameRates.contains(30)) frameRates.indexOf(30) else 0)
                // Next camera
                i += 1
            }
            // Assign
            camera.setVideoFrameRatePresetForCamera(videoFrameRatePresetForCamera)
        }
    }


    // Public


    fun load() {
        loadCaptureSettings()
    }

}