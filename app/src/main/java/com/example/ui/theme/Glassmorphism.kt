package com.example.ui.theme

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * Premium Glassmorphism styling configuration for TravelWake.
 * Provides translucent glass layers, deep navy multi-stop gradients,
 * specular glass border highlights, and blurred card elevation presets.
 */
@Immutable
data class GlassmorphismStyle(
    val surfaceGlass: Color = GlassNavySurface,
    val cardGlass: Color = GlassNavyCard,
    val ultraLightGlass: Color = GlassSurfaceUltraLight,
    val borderRim: Color = GlassNavyBorder,
    val highlightRim: Color = GlassBorderHighlight,
    val deepNavyGradient: Brush = Brush.verticalGradient(
        colors = listOf(
            DeepNavyAbyss,
            DeepNavyMidnight,
            DarkNavySurface
        )
    ),
    val glassCardGradient: Brush = Brush.verticalGradient(
        colors = listOf(
            Color(0x33FFFFFF),
            Color(0x0DFFFFFF)
        )
    ),
    val ambientGlowGradient: Brush = Brush.radialGradient(
        colors = listOf(
            BrightCyan.copy(alpha = 0.25f),
            Color.Transparent
        )
    ),
    // Blurred card elevation levels
    val cardElevationSubtle: Dp = 2.dp,
    val cardElevationFloating: Dp = 8.dp,
    val cardElevationModal: Dp = 16.dp,
    val cardBlurRadius: Dp = 20.dp
)

val LocalGlassmorphismStyle = staticCompositionLocalOf { GlassmorphismStyle() }

/**
 * Modifier extension for applying a genuine frosted glassmorphic surface
 * with translucent glass background, specular border highlight, and optional blurred elevation shadow.
 */
fun Modifier.glassmorphic(
    backgroundColor: Color = GlassNavyCard,
    borderColor: Color = GlassNavyBorder,
    shape: Shape = RoundedCornerShape(24.dp),
    borderWidth: Dp = 1.dp,
    elevation: Dp = 0.dp
): Modifier = this
    .then(
        if (elevation > 0.dp) {
            Modifier.shadow(
                elevation = elevation,
                shape = shape,
                clip = false,
                ambientColor = DeepNavyAbyss.copy(alpha = 0.7f),
                spotColor = BrightCyan.copy(alpha = 0.35f)
            )
        } else {
            Modifier
        }
    )
    .clip(shape)
    .background(backgroundColor)
    .border(BorderStroke(borderWidth, borderColor), shape)

/**
 * Modifier extension for applying a blurred glass card elevation effect
 * using soft colored ambient shadows and a specular glass rim.
 */
fun Modifier.blurredCardElevation(
    elevation: Dp = 8.dp,
    shape: Shape = RoundedCornerShape(24.dp),
    shadowTint: Color = DeepNavyAbyss,
    accentGlow: Color = BrightCyan
): Modifier = this.shadow(
    elevation = elevation,
    shape = shape,
    clip = false,
    ambientColor = shadowTint.copy(alpha = 0.75f),
    spotColor = accentGlow.copy(alpha = 0.3f)
)

/**
 * Composable Box container implementing the TravelWake 'Premium Glassmorphism' surface.
 */
@Composable
fun GlassmorphicContainer(
    modifier: Modifier = Modifier,
    shape: Shape = RoundedCornerShape(24.dp),
    backgroundColor: Color = GlassNavyCard,
    borderColor: Color = GlassNavyBorder,
    borderWidth: Dp = 1.dp,
    elevation: Dp = 0.dp,
    content: @Composable BoxScope.() -> Unit
) {
    Box(
        modifier = modifier.glassmorphic(
            backgroundColor = backgroundColor,
            borderColor = borderColor,
            shape = shape,
            borderWidth = borderWidth,
            elevation = elevation
        ),
        content = content
    )
}
