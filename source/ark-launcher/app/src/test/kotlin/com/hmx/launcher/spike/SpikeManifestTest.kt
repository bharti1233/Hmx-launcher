package com.hmx.launcher.spike

import android.content.Intent
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment

/**
 * Phase-1 Gate F static evidence on the JVM: the spike demo is a launchable entry and the app
 * (unmodified ARK manifest) is a viable home candidate. Runtime (RoleManager/widget) tests stay
 * NOT RUN until a device is available.
 */
@RunWith(RobolectricTestRunner::class)
class SpikeManifestTest {
    @Test
    fun spikeActivity_isLaunchable() {
        val context = RuntimeEnvironment.getApplication()
        val intent = Intent(Intent.ACTION_MAIN)
            .addCategory(Intent.CATEGORY_LAUNCHER)
            .setPackage(context.packageName)
        val resolved = context.packageManager.queryIntentActivities(intent, 0)
        assertNotNull(
            resolved.firstOrNull { it.activityInfo.name == "com.hmx.launcher.spike.SpikeActivity" },
        )
    }

    @Test
    fun appRegistersAsHomeCandidate() {
        val context = RuntimeEnvironment.getApplication()
        val intent = Intent(Intent.ACTION_MAIN).addCategory(Intent.CATEGORY_HOME)
        val resolved = context.packageManager.resolveActivity(intent, 0)
        assertNotNull(resolved)
        assertEquals(context.packageName, resolved!!.activityInfo.packageName)
    }
}
