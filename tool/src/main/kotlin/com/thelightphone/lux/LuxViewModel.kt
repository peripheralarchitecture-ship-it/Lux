package com.thelightphone.lux

import androidx.lifecycle.viewModelScope
import com.thelightphone.sdk.LightAmbientLight
import com.thelightphone.sdk.LightViewModel
import com.thelightphone.sdk.SimpleLightScreen
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.withTimeoutOrNull

enum class Phase { Ready, Measuring, Locked, NoSensor, NoReading }

data class LuxUiState(
    val phase: Phase = Phase.Ready,
    /** Live value while measuring, averaged value once locked. */
    val lux: Float? = null,
    val min: Float? = null,
    val max: Float? = null,
    val progress: Float = 0f,
)

/**
 * Lux only touches the sensor for a short, user-initiated window. There is no continuous
 * polling: tap measure, wait a few seconds, read the result.
 */
class LuxViewModel(private val light: LightAmbientLight) : LightViewModel<Unit>() {

    private val _state = MutableStateFlow(
        LuxUiState(phase = if (light.isAvailable) Phase.Ready else Phase.NoSensor)
    )
    val state: StateFlow<LuxUiState> = _state

    private var job: Job? = null
    private var before: LuxUiState = _state.value

    fun measure() {
        if (job?.isActive == true || !light.isAvailable) return
        before = _state.value
        _state.value = LuxUiState(phase = Phase.Measuring)

        job = viewModelScope.launch {
            val startNs = System.nanoTime()
            var firstNs = 0L
            var lastNs = 0L
            var last: Float? = null
            var weighted = 0.0
            var min = Float.MAX_VALUE
            var max = Float.MIN_VALUE

            val ticker = launch {
                while (isActive) {
                    val p = (System.nanoTime() - startNs).toFloat() / (WINDOW_MS * 1_000_000f)
                    _state.value = _state.value.copy(progress = p.coerceIn(0f, 1f))
                    delay(50)
                }
            }

            withTimeoutOrNull(WINDOW_MS) {
                light.observe().collect { v ->
                    val now = System.nanoTime()
                    val prev = last
                    if (prev == null) firstNs = now else weighted += prev.toDouble() * (now - lastNs)
                    last = v
                    lastNs = now
                    if (v < min) min = v
                    if (v > max) max = v
                    _state.value = _state.value.copy(lux = v)
                }
            }
            ticker.cancel()

            val finalValue = last
            _state.value = if (finalValue == null) {
                LuxUiState(phase = Phase.NoReading)
            } else {
                val endNs = System.nanoTime()
                weighted += finalValue.toDouble() * (endNs - lastNs)
                val span = endNs - firstNs
                val avg = if (span > 0) (weighted / span).toFloat() else finalValue
                LuxUiState(phase = Phase.Locked, lux = avg, min = min, max = max, progress = 1f)
            }
        }
    }

    private fun cancelMeasure() {
        if (job?.isActive == true) {
            job?.cancel()
            _state.value = before
        }
    }

    // Never leave the sensor running when the user leaves.
    override fun onScreenHide(screen: SimpleLightScreen<Unit>) {
        super.onScreenHide(screen)
        cancelMeasure()
    }

    override fun onAppPause() {
        super.onAppPause()
        cancelMeasure()
    }

    private companion object {
        const val WINDOW_MS = 3_000L
    }
}
