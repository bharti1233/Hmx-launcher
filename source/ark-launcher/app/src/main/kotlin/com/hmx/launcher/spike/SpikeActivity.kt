package com.hmx.launcher.spike

import android.net.Uri
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.kyant.backdrop.backdrops.layerBackdrop
import java.io.File

/**
 * Phase-1 glass spike demo. Not part of the launcher flow; a separate launcher entry added to
 * the manifest only inside the isolated spike source, to be removed with the spike in Phase 2.
 */
class SpikeActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            var wallpaper by remember { mutableStateOf(SpikeWallpaperState(null)) }
            var showSheet by remember { mutableStateOf(true) }

            val pickWallpaper = rememberLauncherForActivityResult(
                ActivityResultContracts.PickVisualMedia(),
            ) { uri ->
                if (uri != null) {
                    copyToInternalStorage(uri)?.let { wallpaper = SpikeWallpaperState(it.absolutePath) }
                }
            }

            SpikeGlassDemo(
                wallpaper = wallpaper,
                showSheet = showSheet,
                onToggleSheet = { showSheet = !showSheet },
                onPickWallpaper = {
                    pickWallpaper.launch(
                        PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly),
                    )
                },
            )
        }
    }

    /** Photo Picker output is copied into isolated app-private storage. No storage permissions. */
    private fun copyToInternalStorage(uri: Uri): File? {
        val file = File(filesDir, "wallpapers/wallpaper_${System.currentTimeMillis()}.png")
        return runCatching {
            file.parentFile?.mkdirs()
            contentResolver.openInputStream(uri)?.use { input ->
                file.outputStream().use { output -> input.copyTo(output) }
            }
            if (file.length() > 0) file else null
        }.getOrNull()
    }
}

@Composable
private fun SpikeGlassDemo(
    wallpaper: SpikeWallpaperState,
    showSheet: Boolean,
    onToggleSheet: () -> Unit,
    onPickWallpaper: () -> Unit,
) {
    SpikeBackdropHost { backdrop ->
        val profile = rememberSpikeGlassProfile(backdropAvailable = backdrop != null)
        Box(modifier = Modifier.fillMaxSize()) {
            // The wallpaper is the backdrop source: exactly one recorded layer per window.
            SpikeWallpaperHost(
                state = wallpaper,
                modifier = if (backdrop != null) {
                    Modifier.fillMaxSize().layerBackdrop(backdrop)
                } else {
                    Modifier.fillMaxSize()
                },
            )

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .windowInsetsPadding(WindowInsets.safeDrawing)
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Text(
                    text = "HMX spike — profile: $profile (API ${Build.VERSION.SDK_INT})",
                    style = MaterialTheme.typography.labelMedium,
                    textAlign = TextAlign.Center,
                )

                AnimatedVisibility(
                    visible = showSheet,
                    enter = fadeIn() + slideInVertically { it / 4 },
                    exit = fadeOut() + slideOutVertically { it / 4 },
                ) {
                    SpikeGlassSheet(modifier = Modifier.fillMaxWidth()) {
                        Text(text = "HMX glass sheet", style = MaterialTheme.typography.titleMedium)
                        Text(
                            text = "Blur + refraction on API 33+; blur-only on 31-32; tinted on 30.",
                            style = MaterialTheme.typography.labelSmall,
                        )
                        Button(onClick = onToggleSheet) { Text(text = "Close sheet") }
                    }
                }

                SpikeGlassSearchBar(modifier = Modifier.fillMaxWidth(), onClick = onToggleSheet)

                Spacer(modifier = Modifier.weight(1f))

                SpikeGlassDock()

                Spacer(modifier = Modifier.height(8.dp))

                Button(modifier = Modifier.fillMaxWidth(), onClick = onPickWallpaper) {
                    Text(text = if (wallpaper.usesOwnedImage) "Change HMX wallpaper" else "Set HMX wallpaper")
                }
            }
        }
    }
}
