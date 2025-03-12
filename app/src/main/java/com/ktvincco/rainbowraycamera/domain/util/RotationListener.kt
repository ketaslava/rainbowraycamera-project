package com.ktvincco.rainbowraycamera.domain.util

import android.app.Activity
import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import java.util.ArrayList
import kotlin.math.abs
import kotlin.math.sqrt


class RotationListener(private val mainActivity: Activity) : SensorEventListener {


    // Settings


    companion object {
        private const val LOG_TAG = "RotationListener"
        private const val rotationDelay = 2
        private const val stabilizationHistoryLength = 16
        private const val alignmentCursorAreaInDegrees = 90F
    }


    // Variables


    // Outputs
    private var phoneZAxisRotation = 0F
    private var currentRotationDelay = 0
    private var stabilizationRate = 1F
    private var stabilizationLossHistory: ArrayList<Float> = arrayListOf(0F)
    // Sensors
    private val sensorManager: SensorManager =
        mainActivity.getSystemService(Context.SENSOR_SERVICE) as SensorManager
    private val accelerometer: Sensor? = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)
    private val gyroscopeSensor = sensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE)
    // Alignment cursor
    private var onNewAlignmentCursorPosition:
                (newPosition: Triple<Float, Float, Float>) -> Unit = {}
    private var rotationDeltaX = 0F
    private var rotationDeltaY = 0F
    private var rotationDeltaZ = 0F


    // Private


    init {
        // Is accelerometer available on device
        if (accelerometer != null) {
            onResume()
        } else {
            // Use default horizontal value
            phoneZAxisRotation = 0F
        }
    }


    private fun Float.clamp(a: Float, b: Float): Float {
        return when {
            this < a -> a
            this > b -> b
            else -> this
        }
    }


    override fun onSensorChanged(event: SensorEvent?) {
        if (event?.sensor?.type == Sensor.TYPE_ACCELEROMETER) {

            // Get values
            val x = event.values[0]
            val y = event.values[1]
            val z = event.values[2]

            // Calculate rotation

            // Get biggest abs value
            val maxAbsValue = maxOf(abs(x), abs(y), abs(z))

            // Calculate phone rotation
            var newPhoneZAxisRotation = 90F
            if (maxAbsValue == abs(x)) {
                newPhoneZAxisRotation = if (x > 0) { 0F } else { 180F }
            }
            if (maxAbsValue == abs(y)) {
                newPhoneZAxisRotation = if (y > 0) { 90F } else { 270F }
            }
            // Z in set value

            // Apply rotation
            if (phoneZAxisRotation != newPhoneZAxisRotation) {
                currentRotationDelay -= 1
                if (currentRotationDelay < 0) {
                    phoneZAxisRotation = newPhoneZAxisRotation
                    currentRotationDelay = rotationDelay
                    // Log
                    // Log.i(LOG_TAG, "New phoneZAxisRotation: $phoneZAxisRotation")
                }
            } else { currentRotationDelay = rotationDelay }

            // Log
            // Log.i(LOG_TAG, "Accelerometer X $x  Y $y  Z $z")

            // Calculate stabilization rate

            // Calculate base loss
            var stabilizationLoss = abs(sqrt(x * x + y * y + z * z) - 9.80665F)
            if (stabilizationLoss > 1F) { stabilizationLoss = 1F }

            // Add loss to history
            stabilizationLossHistory.add(stabilizationLoss)
            if (stabilizationLossHistory.size > stabilizationHistoryLength) {
                stabilizationLossHistory.removeAt(0) }

            // Remove average of loss history from loss -> Stabilization rate
            var sum = 0.0F
            for (loss in stabilizationLossHistory) { sum += loss }
            val averageLoss = if (stabilizationLossHistory.isNotEmpty()) sum /
                    stabilizationLossHistory.size else 0.0F
            stabilizationLoss -= averageLoss

            // Calculate stabilization rate
            stabilizationRate = abs(stabilizationLoss)

            // Log
            // Log.i(LOG_TAG, "stabilizationRate: $stabilizationRate")
        }

        if (event?.sensor?.type == Sensor.TYPE_GYROSCOPE) {

            // Get values
            var x = event.values[0]
            var y = event.values[1]
            var z = event.values[2]

            // Calculate alignment cursor

            // Incorrect value filter
            val maxVal = alignmentCursorAreaInDegrees / 3
            if (x > maxVal) { x = 0F }
            if (y > maxVal) { y = 0F }
            if (z > maxVal) { z = 0F }

            // Calculate delta
            rotationDeltaX += x
            rotationDeltaY += y
            rotationDeltaZ += z

            // Log
            // Log.i(LOG_TAG, "Gyro X $rotationDeltaX  Y $rotationDeltaY  Z $rotationDeltaZ")

            // Calculate cursor position
            val positionX = (rotationDeltaX / alignmentCursorAreaInDegrees).clamp(-1F, 1F)
            val positionY = (rotationDeltaY / alignmentCursorAreaInDegrees).clamp(-1F, 1F)
            val positionZ = (rotationDeltaZ / 360).clamp(-1F, 1F)

            // Update cursor position
            onNewAlignmentCursorPosition(Triple(positionY, positionX, positionZ))
        }
    }


    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {
        // Ignore
    }


    // Public


    fun onResume() {
        // Init accelerometer
        if (accelerometer != null) {
            sensorManager.registerListener(
                this, accelerometer, SensorManager.SENSOR_DELAY_NORMAL)
            sensorManager.registerListener(
                this, gyroscopeSensor, SensorManager.SENSOR_DELAY_NORMAL)
        }
    }


    fun onPause() {
        // Release accelerometer
        sensorManager.unregisterListener(this)
    }


    // Rotation relative to ground as 0F, 90F, 180F, 270F
    fun getPhoneZAxisRotation(): Float { return phoneZAxisRotation }


    // Better is 0.0F, Worst is 1.0F equal loss in M/S
    fun getStabilizationRate(): Float { return stabilizationRate }


    // Get value for position alignment cursor
    fun assignOnNewAlignmentCursorPosition(
        onPositionChangedCallback: (newPosition: Triple<Float, Float, Float>) -> Unit) {
        onNewAlignmentCursorPosition = onPositionChangedCallback
    }


    fun resetStartPositionForAlignmentCursor() {
        rotationDeltaX = 0F
        rotationDeltaY = 0F
        rotationDeltaZ = 0F
    }
}
