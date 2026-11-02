package com.hmx.launcher.spike

/**
 * Android platform capabilities the glass renderer can rely on. Expressed against an explicit
 * SDK int so the classification stays testable on a plain JVM (no Robolectric needed).
 *
 * - RenderEffect (blur): Android 12 (API 31)+.
 * - RuntimeShader / AGSL (lens refraction): Android 14 (API 33)+.
 */
object SdkCapability {
    const val MIN_SUPPORTED = 30

    fun isRenderEffectSupported(sdkInt: Int): Boolean = sdkInt >= 31

    fun isRuntimeShaderSupported(sdkInt: Int): Boolean = sdkInt >= 33
}

/** What the glass renderer may attempt on this device; chosen explicitly, never assumed. */
sealed interface SpikeGlassProfile {
    data object Tinted : SpikeGlassProfile

    data object Blurred : SpikeGlassProfile

    data object Refractive : SpikeGlassProfile
}

/** Phase-1 source of truth for API-level glass classification. */
object SpikeGlassProfileResolver {
    fun resolve(sdkInt: Int, backdropAvailable: Boolean = true): SpikeGlassProfile = when {
        !backdropAvailable -> SpikeGlassProfile.Tinted
        !SdkCapability.isRenderEffectSupported(sdkInt) -> SpikeGlassProfile.Tinted
        SdkCapability.isRuntimeShaderSupported(sdkInt) -> SpikeGlassProfile.Refractive
        else -> SpikeGlassProfile.Blurred
    }
}
