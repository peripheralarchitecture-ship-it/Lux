package com.thelightphone.lux

import java.util.Locale
import kotlin.math.log10
import kotlin.math.roundToInt

/** Radar scale: centre = 0.1 lx, outer ring = 100,000 lx, six log decades. */
internal const val MIN_LUX = 0.1f
internal const val RING_COUNT = 6
internal val RING_LABELS = listOf("1", "10", "100", "1K", "10K", "100K")

/** Maps lux to 0..1 along the log radar radius. */
internal fun luxToRadius(lux: Float): Float =
    ((log10(lux.coerceAtLeast(MIN_LUX)) + 1f) / RING_COUNT).coerceIn(0f, 1f)

internal enum class Band(val label: String, val upTo: Float) {
    NIGHT("NIGHT", 5f),
    DIM("DIM", 50f),
    INDOOR("INDOOR", 500f),
    BRIGHT("BRIGHT", 5_000f),
    DAYLIGHT("DAYLIGHT", 50_000f),
    SUN("DIRECT SUN", Float.MAX_VALUE),
}

internal fun bandFor(lux: Float): Band = Band.entries.first { lux < it.upTo }

internal fun formatLux(lux: Float): String =
    if (lux < 10f) String.format(Locale.ROOT, "%.1f", lux)
    else String.format(Locale.ROOT, "%,d", lux.roundToInt())
