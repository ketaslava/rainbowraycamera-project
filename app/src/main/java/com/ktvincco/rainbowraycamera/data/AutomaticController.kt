package com.ktvincco.rainbowraycamera.data

import android.app.Activity
import com.ktvincco.rainbowraycamera.data.util.CameraConfiguration
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.io.File


class AutomaticController (
    private val mainActivity: Activity,
) {


    // Settings


    companion object {
        private const val LOG_TAG = "AutomaticController"
    }


    // Variables


    // Components
    private val dataSaver = DataSaver(mainActivity)
    // External
    private external fun calculateAutomaticControlFromCaptureResult(
        workingDirectoryPath: String): String
    // Flags
    private var isCaptureResultInProcessing = false
    private var isStopped = false
    // Values
    private var focusValue = 0F
    private var iso = 100
    private var shutterSpeed = 16000000L // Ns = 1/60 Sec


    // Private


    private fun getAutomaticControllerDirectory(): File {
        val processingDirectory = File(mainActivity.filesDir, "/automaticController")
        // Create if not exists
        if (!processingDirectory.exists()) { processingDirectory.mkdirs() }
        return processingDirectory
    }

    private fun loadStringFromFile(fileName: String, defaultValue: String): String {
        return try { dataSaver.readTextFromFile(getAutomaticControllerDirectory(),
            fileName) ?: defaultValue } catch (e: Exception) { defaultValue }
    }

    private fun loadBooleanFromFile(fileName: String, defaultValue: Boolean): Boolean {
        return try { dataSaver.readTextFromFile(getAutomaticControllerDirectory(),
            fileName)?.toBoolean() ?: defaultValue
        } catch (e: Exception) { defaultValue }
    }

    private fun loadIntFromFile(fileName: String, defaultValue: Int): Int {
        return try { dataSaver.readTextFromFile(getAutomaticControllerDirectory(),
            fileName)?.toInt() ?: defaultValue
        } catch (e: Exception) { defaultValue }
    }

    private fun loadLongFromFile(fileName: String, defaultValue: Long): Long {
        return try { dataSaver.readTextFromFile(getAutomaticControllerDirectory(),
            fileName)?.toLong() ?: defaultValue
        } catch (e: Exception) { defaultValue }
    }

    private fun loadFloatFromFile(fileName: String, defaultValue: Float): Float {
        return try { dataSaver.readTextFromFile(getAutomaticControllerDirectory(),
            fileName)?.toFloat() ?: defaultValue
        } catch (e: Exception) { defaultValue }
    }

    private fun writeStringToFile(fileName: String, value: String) {
        dataSaver.saveTextToFile(getAutomaticControllerDirectory(), fileName, value)
    }

    private fun writeIntToFile(fileName: String, value: Int) {
        dataSaver.saveTextToFile(getAutomaticControllerDirectory(), fileName, value.toString())
    }

    private fun writeLongToFile(fileName: String, value: Long) {
        dataSaver.saveTextToFile(getAutomaticControllerDirectory(), fileName, value.toString())
    }

    private fun writeFloatToFile(fileName: String, value: Float) {
        dataSaver.saveTextToFile(getAutomaticControllerDirectory(), fileName, value.toString())
    }

    private fun writeBooleanToFile(fileName: String, value: Boolean) {
        dataSaver.saveTextToFile(getAutomaticControllerDirectory(), fileName,
            if (value) "true" else "false" )
    }


    private fun updateDataFromFiles() {
        focusValue = loadFloatFromFile("/focusValue.txt", 0F)
        iso = loadIntFromFile("/iso.txt", 100)
        shutterSpeed = loadLongFromFile("/shutterSpeed.txt", 16000000)
    }


    private fun resetDataToDefault() {

        // Assign default values
        focusValue = 0F
        iso = 100
        shutterSpeed = 16000000

        // Stop focusing
        writeStringToFile("/focusingStage.txt", "Idle")

        // Reset digits to default (on init) values
        writeFloatToFile("/focusValue.txt", focusValue)
        writeIntToFile("/iso.txt", iso)
        writeLongToFile("/shutterSpeed.txt", shutterSpeed)

        // Reset recommended night mode state
        writeBooleanToFile("/recommendedNightModeState.txt", false)
    }


    // Public


    init {
        // Create /automaticController directory in private app file space
        dataSaver.createDirInPrivateSpace("/automaticController")

        // Reset at start
        resetDataToDefault()
    }


    fun updateCameraConfiguration(newConfiguration: CameraConfiguration) {

        // Stop focusing
        writeStringToFile("/focusingStage.txt", "Idle")

        // Exposure
        // Set is enable auto exposure <- if manual control available
        writeBooleanToFile("/isEnableAutoExposure.txt",
            newConfiguration.getIsManualTemplateAvailable())
        // Write info for auto exposure
        writeIntToFile("/isoMax.txt",
            newConfiguration.getAvailableIsoRange().upper)
        writeIntToFile("/isoMin.txt",
            newConfiguration.getAvailableIsoRange().lower)
        writeLongToFile("/shutterSpeedMax.txt",
            newConfiguration.getAvailableShutterSpeedRange().upper)
        writeLongToFile("/shutterSpeedMin.txt",
            newConfiguration.getAvailableShutterSpeedRange().lower)
    }


    fun processCaptureResult(cameraImageBytes: ByteArray) {

        // Check flag
        if (isCaptureResultInProcessing || isStopped) { return }

        // Set flag
        isCaptureResultInProcessing = true

        // Save image to automatic controller directory
        dataSaver.saveByteArray(cameraImageBytes,
            getAutomaticControllerDirectory(), "input.jpg")

        // Async call to process
        CoroutineScope(Dispatchers.Default).launch {

            // Call
            val result = calculateAutomaticControlFromCaptureResult(
                getAutomaticControllerDirectory().absolutePath)

            // Update values
            updateDataFromFiles()

            // Print log from file
            // Log.i(LOG_TAG, loadStringFromFile("/Log.txt", ""))

            // Reset flag
            isCaptureResultInProcessing = false
        }
    }


    fun resume() { isStopped = false }
    fun stopForCapture() { isStopped = true }


    fun startFocusing(): Float {
        writeStringToFile("/focusingStage.txt", "Start")
        return focusValue
    }
    fun stopFocusing() {
        val focusingStage = loadStringFromFile("/focusingStage.txt", "Idle")
        if (focusingStage != "Idle") {
            focusValue = 0F
            writeFloatToFile("focusValue", focusValue)
            writeStringToFile("/focusingStage.txt", "Idle")
        }
    }
    fun setFocusPoint(newPoint: Pair<Float, Float>) {
        writeFloatToFile("/focusPointX.txt", newPoint.first)
        writeFloatToFile("/focusPointY.txt", newPoint.second)
    }
    fun getFocusPoint(): Pair<Float, Float> {
        return Pair (
            loadFloatFromFile("/focusPointX.txt", 0.5F),
            loadFloatFromFile("/focusPointY.txt", 0.5F)
        )
    }
    fun getFocusValue(): Float {
        return focusValue
    }


    fun getIso(): Int {
        return iso
    }
    fun getShutterSpeed(): Long {
        return shutterSpeed
    }


    fun getRecommendedNightModeState(): Boolean {
        return loadBooleanFromFile("/recommendedNightModeState.txt", false)
    }
}