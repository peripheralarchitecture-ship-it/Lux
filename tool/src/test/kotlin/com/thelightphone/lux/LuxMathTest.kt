package com.thelightphone.lux

import kotlin.test.Test
import kotlin.test.assertEquals

class LuxMathTest {
    @Test fun radiusEndpoints() {
        assertEquals(0f, luxToRadius(0f), 1e-6f)
        assertEquals(1f, luxToRadius(100_000f), 1e-6f)
        assertEquals(1f / 6f, luxToRadius(1f), 1e-6f)
    }

    @Test fun bands() {
        assertEquals(Band.NIGHT, bandFor(0.5f))
        assertEquals(Band.INDOOR, bandFor(300f))
        assertEquals(Band.SUN, bandFor(90_000f))
    }

    @Test fun formatting() {
        assertEquals("3.5", formatLux(3.46f))
        assertEquals("12,450", formatLux(12_450f))
    }
}
