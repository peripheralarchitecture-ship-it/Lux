package com.thelightphone.sdk

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.conflate

/**
 * Read-only access to the ambient light sensor, in lux.
 *
 * No manifest permission is required for [Sensor.TYPE_LIGHT]. The sensor is only powered while
 * a collector of [observe] is active, so tools should collect for a bounded window (for
 * example, when the user taps "measure") instead of leaving it running.
 */
class LightAmbientLight(androidContext: Context) {
    private val sensorManager by lazy {
        androidContext.getSystemService(Context.SENSOR_SERVICE) as SensorManager
    }
    private val sensor: Sensor? by lazy { sensorManager.getDefaultSensor(Sensor.TYPE_LIGHT) }

    /** False when this device has no ambient light sensor. */
    val isAvailable: Boolean get() = sensor != null

    /**
     * Emits illuminance in lux whenever the sensor reports a change. Most light sensors report
     * on change only, so a steady scene may emit a single value. Completes immediately if there
     * is no sensor. The sensor is unregistered when collection stops.
     */
    fun observe(): Flow<Float> = callbackFlow {
        val s = sensor
        if (s == null) {
            close()
        } else {
            val listener = object : SensorEventListener {
                override fun onSensorChanged(event: SensorEvent) {
                    trySend(event.values[0])
                }

                override fun onAccuracyChanged(sensor: Sensor, accuracy: Int) = Unit
            }
            sensorManager.registerListener(listener, s, SensorManager.SENSOR_DELAY_UI)
            awaitClose { sensorManager.unregisterListener(listener) }
        }
    }.conflate()
}
