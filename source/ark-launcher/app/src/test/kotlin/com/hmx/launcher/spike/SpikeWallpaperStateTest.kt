package com.hmx.launcher.spike

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class SpikeWallpaperStateTest {
    @Test
    fun noOwnedWallpaper_reportsFallback() {
        val state = SpikeWallpaperState(null)
        assertFalse(state.usesOwnedImage)
    }

    @Test
    fun ownedWallpaperFile_reportsOwned() {
        val state = SpikeWallpaperState("/data/user/0/hmx/files/wallpapers/a.png")
        assertTrue(state.usesOwnedImage)
    }

    @Test
    fun changingFile_reportsBackdropInvalidation() {
        val old = SpikeWallpaperState("/old.png")
        val new = SpikeWallpaperState("/new.png")
        assertTrue(SpikeWallpaperState.hasBackdropChanged(old, new))
    }

    @Test
    fun sameState_reportsNoInvalidation() {
        val state = SpikeWallpaperState("/old.png")
        assertFalse(SpikeWallpaperState.hasBackdropChanged(state, state))
    }
}
