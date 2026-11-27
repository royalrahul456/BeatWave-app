/*
 * ArchiveTune (2026)
 * © Rukamori — github.com/rukamori
 * GPL-3.0 License | Contributors: see git history
 * Do not remove or alter this notice. - Per GPL-3.0 Section 4 & Section 5
 */

package moe.rukamori.archivetune.ui.component

import android.os.Build
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.unit.dp
import dev.chrisbanes.haze.ExperimentalHazeApi
import dev.chrisbanes.haze.HazeInput
import dev.chrisbanes.haze.HazePerformanceMode
import dev.chrisbanes.haze.HazeState
import dev.chrisbanes.haze.blur.HazeBlurStyle
import dev.chrisbanes.haze.blur.HazeColorEffect
import dev.chrisbanes.haze.blur.hazeBlur
import dev.chrisbanes.haze.glass.GlassStyle
import dev.chrisbanes.haze.glass.hazeGlass

val isGlassBackgroundSupported: Boolean
    get() = Build.VERSION.SDK_INT >= Build.VERSION_CODES.S

private val isShaderGlassSupported: Boolean
    get() = Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU

@OptIn(ExperimentalHazeApi::class)
@Composable
fun FloatingBarGlassBackground(
    hazeState: HazeState,
    shape: RoundedCornerShape,
    pureBlack: Boolean,
    modifier: Modifier = Modifier,
) {
    if (!isGlassBackgroundSupported) {
        FloatingBarBlurBackground(
            hazeState = hazeState,
            isAcrylic = false,
            pureBlack = pureBlack,
            modifier = modifier,
        )
        return
    }
    val colorScheme = MaterialTheme.colorScheme
    val surfaceColor = if (pureBlack) Color.Black else colorScheme.surfaceContainerHigh
    val isDarkSurface = pureBlack || colorScheme.surface.luminance() < colorScheme.onSurface.luminance()
    if (!isShaderGlassSupported) {
        FrostedGlassBackground(
            hazeState = hazeState,
            shape = shape,
            surfaceColor = surfaceColor,
            isDarkSurface = isDarkSurface,
            modifier = modifier,
        )
        return
    }
    val hazeInput = remember(hazeState) { HazeInput.Sources(hazeState) }
    val glassStyle =remember(shape, surfaceColor, isDarkSurface) {
        GlassStyle.clear.then {
            shape(shape)
            backgroundColor(surfaceColor)
            tint(surfaceColor.copy(alpha = if (isDarkSurface) 0.12f else 0.10f))
            whitePoint(if (isDarkSurface) -0.04f else 0.12f)
            contrast(0.04f)
            chromaMultiplier(1.3f)
            specularIntensity(0.52f)
            edgeShadow(Color.Black.copy(alpha = 0.10f))
            ambientResponse(0.18f)
        }
    }
    Box(
        modifier = modifier.hazeGlass(
            input = hazeInput,
            style = glassStyle,
            performanceMode = HazePerformanceMode.Default,
            expandLayerBounds = true,
        ),
    )
}

@Composable
private fun FrostedGlassBackground(
    hazeState: HazeState,
    shape: RoundedCornerShape,
    surfaceColor: Color,
    isDarkSurface: Boolean,
    modifier: Modifier,
) {
    val hazeInput = remember(hazeState) { HazeInput.Sources(hazeState) }
    val blurStyle = remember(surfaceColor, isDarkSurface) {
        HazeBlurStyle {
            backgroundColor(surfaceColor)
            blurRadius(28.dp)
            noiseFactor(0f)
            colorEffects(
                listOf(
                    HazeColorEffect.tint(surfaceColor.copy(alpha = if (isDarkSurface) 0.30f else 0.25f)),
                ),
            )
            fallbackColorEffect(HazeColorEffect.tint(surfaceColor))
        }
    }
    val rimBrush = remember(isDarkSurface) {
        Brush.linearGradient(
            colors = listOf(
                Color.White.copy(alpha = if (isDarkSurface) 0.30f else 0.55f),
                Color.White.copy(alpha = 0.06f),
                Color.White.copy(alpha = if (isDarkSurface) 0.16f else 0.30f),
            ),
        )
    }
    Box(
        modifier = modifier
            .hazeBlur(
                input = hazeInput,
                style = blurStyle,
                expandLayerBounds = true,
            )
            .border(width = 1.dp, brush = rimBrush, shape = shape),
    )
}
