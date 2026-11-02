package com.hmx.launcher.spike

import org.junit.Assert.assertEquals
import org.junit.Test

/** Pure-JVM policy tests: profile selection stays explicit per the Phase-1 gate D contract. */
class SpikeGlassProfileTest {
    @Test
    fun api30_selectsTinted() {
        assertEquals(SpikeGlassProfile.Tinted, SpikeGlassProfileResolver.resolve(30))
    }

    @Test
    fun api31_selectsBlurred() {
        assertEquals(SpikeGlassProfile.Blurred, SpikeGlassProfileResolver.resolve(31))
    }

    @Test
    fun api32_boundary_selectsBlurred() {
        assertEquals(SpikeGlassProfile.Blurred, SpikeGlassProfileResolver.resolve(32))
    }

    @Test
    fun api33_selectsRefractive() {
        assertEquals(SpikeGlassProfile.Refractive, SpikeGlassProfileResolver.resolve(33))
    }

    @Test
    fun api35_selectsRefractive() {
        assertEquals(SpikeGlassProfile.Refractive, SpikeGlassProfileResolver.resolve(35))
    }

    @Test
    fun missingBackdrop_downgradesToTinted() {
        assertEquals(
            SpikeGlassProfile.Tinted,
            SpikeGlassProfileResolver.resolve(35, backdropAvailable = false),
        )
    }
}
