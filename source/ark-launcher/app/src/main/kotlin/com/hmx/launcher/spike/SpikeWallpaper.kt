package com.hmx.launcher.spike

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.produceState
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File

/**
 * Pure wallpaper state: null file = HMX owns no image yet, so the intentional tinted fallback
 * applies. The system wallpaper is never read (no WallpaperManager pixels, no screenshots).
 */
data class SpikeWallpaperState(val file: String?) {
    val usesOwnedImage: Boolean get() = file != null

    companion object {
        /** True when the backdrop layer must re-record (wallpaper changed). */
        fun hasBackdropChanged(old: SpikeWallpaperState?, new: SpikeWallpaperState?): Boolean = old != new
    }
}

@Composable
fun SpikeWallpaperHost(state: SpikeWallpaperState, modifier: Modifier = Modifier) {
    val file = state.file?.let(::File)
    val bitmap: Bitmap? = if (file == null) {
        null
    } else {
        produceState<Bitmap?>(initialValue = null, key1 = file) {
            value = withContext(Dispatchers.IO) {
                runCatching { loadDisplayWallpaper(file) }.getOrNull()
            }
        }.value
    }

    if (bitmap != null) {
        Image(
            bitmap = bitmap.asImageBitmap(),
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier = modifier,
        )
    } else {
        FallbackWallpaper(modifier = modifier)
    }
}

internal fun loadDisplayWallpaper(file: File): Bitmap? {
    val bounds = BitmapFactory.Options().apply { inJustDecodeBounds = true }
    BitmapFactory.decodeFile(file.absolutePath, bounds)
    val decodeOptions = BitmapFactory.Options().apply {
        inJustDecodeBounds = false
        inSampleSize = computeInSampleSize(bounds, targetWidth = 1080, targetHeight = 2400)
    }
    return BitmapFactory.decodeFile(file.absolutePath, decodeOptions)
}

// ponytail: fixed 1080x2400 decode target (headroom over the 720x1612 OPPO A38); density-aware
// sizing only if a real device shows wasted memory.
private fun computeInSampleSize(bounds: BitmapFactory.Options, targetWidth: Int, targetHeight: Int): Int {
    var sampleSize = 1
    var width = bounds.outWidth / 2
    var height = bounds.outHeight / 2
    while (width >= targetWidth && height >= targetHeight) {
        sampleSize *= 2
        width /= 2
        height /= 2
    }
    return sampleSize
}

@Composable
private fun FallbackWallpaper(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier.background(
            Brush.verticalGradient(
                colors = listOf(Color(0xFF1B2430), Color(0xFF51607A), Color(0xFF9AA7BD)),
            ),
        ),
    )
}
