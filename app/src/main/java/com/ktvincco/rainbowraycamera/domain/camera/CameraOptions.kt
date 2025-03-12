package com.ktvincco.rainbowraycamera.domain.camera

import android.util.Log
import com.ktvincco.rainbowraycamera.data.DataSaver
import com.ktvincco.rainbowraycamera.presentation.ModelData

class CameraOptions (
    private val dataSaver: DataSaver
) {


    // Settings


    private val cameraOptionDefaultStates = mapOf(
        "isEnableAppSound" to "true",
        "isEnableRecordAudio" to "true",
        "isEnableGrid" to "false",
        "isEnableFocusPeaking" to "false",
        "isEnableOis" to "true",
        "isEnableFlashlight" to "false",
        "isFlashlightOptionEnabled" to "false",
        "isRawSensorFormatAvailable" to "false",
        "IsUseRawSensorFormatWhenAvailable" to "false"
    )


    // Public


    fun setOption(name: String, value: String) {
        dataSaver.saveStringByKey("CameraOptions_$name", value) }


    fun setOptionBoolean(name: String, value: Boolean) { setOption(name, value.toString()) }


    fun getOption(name: String): String? {
        return dataSaver.loadStringByKey("CameraOptions_$name")
            ?: cameraOptionDefaultStates[name] }


    fun getOptionBoolean(name: String): Boolean? {
        return getOption(name)?.toBoolean()
    }


    fun fastSwitchBooleanOption(name: String) {
        val value = (dataSaver.loadStringByKey("CameraOptions_$name")
            ?: cameraOptionDefaultStates[name] ?: "false").toBoolean()
        dataSaver.saveStringByKey("CameraOptions_$name", (!value).toString())
    }

    fun updateAllKnownOptionStatesInModelData(modelData: ModelData) {
        for (optionName in cameraOptionDefaultStates.keys) {
            modelData.setCameraOptionState(optionName, getOption(optionName) ?: "")
        }
    }
}