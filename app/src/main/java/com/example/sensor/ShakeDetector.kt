package com.example.sensor

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import kotlin.math.sqrt

class ShakeDetector(
    context: Context,
    private val onShake: () -> Unit
) : SensorEventListener {

    private val sensorManager = context.getSystemService(Context.SENSOR_SERVICE) as? SensorManager
    private val accelerometer = sensorManager?.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)

    var shakeThreshold: Float = 13.0f // m/s^2 above gravity
    private var lastShakeTimestamp: Long = 0
    private val shakeDebounceMs = 500L

    var isListening: Boolean = false
        private set

    fun start() {
        if (isListening || accelerometer == null) return
        sensorManager?.registerListener(this, accelerometer, SensorManager.SENSOR_DELAY_UI)
        isListening = true
    }

    fun stop() {
        if (!isListening) return
        sensorManager?.unregisterListener(this)
        isListening = false
    }

    override fun onSensorChanged(event: SensorEvent?) {
        if (event == null || event.sensor.type != Sensor.TYPE_ACCELEROMETER) return

        val x = event.values[0]
        val y = event.values[1]
        val z = event.values[2]

        // Net acceleration excluding Earth's gravity (~9.81 m/s^2)
        val gForce = sqrt((x * x + y * y + z * z).toDouble()).toFloat()
        val netAcceleration = gForce - SensorManager.GRAVITY_EARTH

        if (netAcceleration > shakeThreshold) {
            val now = System.currentTimeMillis()
            if (now - lastShakeTimestamp >= shakeDebounceMs) {
                lastShakeTimestamp = now
                onShake()
            }
        }
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {
        // No-op
    }
}
