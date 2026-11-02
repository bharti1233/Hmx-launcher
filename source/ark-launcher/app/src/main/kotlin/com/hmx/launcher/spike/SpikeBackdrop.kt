package com.hmx.launcher.spike

import android.os.Build
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.remember
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.kyant.backdrop.Backdrop
import com.kyant.backdrop.BackdropEffectScope
import com.kyant.backdrop.backdrops.LayerBackdrop
import com.kyant.backdrop.backdrops.layerBackdrop
import com.kyant.backdrop.backdrops.rememberLayerBackdrop
import com.kyant.backdrop.drawBackdrop
import com.kyant.backdrop.effects.blur
import com.kyant.backdrop.effects.lens
import com.kyant.backdrop.highlight.Highlight
import com.kyant.backdrop.shadow.Shadow
import org.arkikeskus.launcher.R

/**
 * One shared backdrop per window: the wallpaper layer records into it via [layerBackdrop] and
 * every glass surface samples the same instance. Never create additional backdrops per surface.
 */
val LocalSpikeBackdrop = staticCompositionLocalOf<Backdrop?> { null }

@Composable
fun SpikeBackdropHost(content: @Composable (LayerBackdrop?) -> Unit) {
    val backdrop = rememberLayerBackdrop()
    CompositionLocalProvider(LocalSpikeBackdrop provides backdrop) {
        content(backdrop)
    }
}

/** Profile the current device should get; also shown on screen so screenshots self-document. */
@Composable
fun rememberSpikeGlassProfile(backdropAvailable: Boolean = LocalSpikeBackdrop.current != null): SpikeGlassProfile =
    remember(backdropAvailable) {
        SpikeGlassProfileResolver.resolve(
            sdkInt = Build.VERSION.SDK_INT,
            backdropAvailable = backdropAvailable,
        )
    }

/**
 * Shared experimental glass material (temporary Phase-1 prototype, not a production API).
 * When no shared backdrop exists the fallback is an intentional tinted translucent material.
 */
@Composable
fun SpikeGlassSurface(
    modifier: Modifier = Modifier,
    shape: Shape = RoundedCornerShape(28.dp),
    effects: BackdropEffectScope.() -> Unit = { blur(radius = 24.dp.toPx()) },
    fallbackColor: Color = Color(0xD9FFFFFF),
    content: @Composable ColumnScope.() -> Unit,
) {
    val backdrop = LocalSpikeBackdrop.current
    if (backdrop == null) {
        Column(modifier = modifier.background(color = fallbackColor, shape = shape), content = content)
    } else {
        Column(
            modifier = modifier.drawBackdrop(
                backdrop = backdrop,
                shape = { shape },
                effects = effects,
                highlight = { Highlight.Default },
                shadow = { Shadow.Default },
            ),
            content = content,
        )
    }
}

/** C1: rounded floating dock with translucent glass and clear icon blocks. */
@Composable
fun SpikeGlassDock(
    modifier: Modifier = Modifier,
    appIconColors: List<Color> = listOf(
        Color(0xFF41A4FF),
        Color(0xFFFF8A3D),
        Color(0xFF7ADB7A),
        Color(0xFFC77DFF),
    ),
) {
    SpikeGlassSurface(modifier = modifier) {
        Row(
            modifier = Modifier.padding(horizontal = 18.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            appIconColors.forEach { color ->
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .background(color = color, shape = RoundedCornerShape(14.dp)),
                )
            }
        }
    }
}

/** C2: rounded search pill; touch handling is a plain clickable, no special focus states. */
@Composable
fun SpikeGlassSearchBar(
    modifier: Modifier = Modifier,
    placeholder: String = "Search",
    onClick: () -> Unit = {},
) {
    SpikeGlassSurface(
        modifier = modifier.height(52.dp),
        effects = { blur(radius = 20.dp.toPx()) },
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .clickable(onClick = onClick)
                .padding(horizontal = 16.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Image(
                painter = painterResource(R.drawable.spike_ic_search),
                contentDescription = null,
                modifier = Modifier.size(22.dp),
            )
            Text(
                text = placeholder,
                style = MaterialTheme.typography.bodyLarge,
                modifier = Modifier.padding(start = 10.dp),
            )
        }
    }
}

/**
 * C3: floating panel; blur always by profile, lens refraction requested on top (the library
 * no-ops it below API 33 internally, and the profile resolver is the documented contract).
 */
@Composable
fun SpikeGlassSheet(
    modifier: Modifier = Modifier,
    effects: BackdropEffectScope.() -> Unit = {
        blur(radius = 18.dp.toPx())
        lens(
            refractionHeight = 16.dp.toPx(),
            refractionAmount = 10.dp.toPx(),
            depthEffect = true,
            chromaticAberration = false,
        )
    },
    content: @Composable ColumnScope.() -> Unit,
) {
    SpikeGlassSurface(modifier = modifier, effects = effects, content = content)
}
