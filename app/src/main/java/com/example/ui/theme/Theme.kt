package com.example.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import com.example.travelwake.theme.WeatherThemeEngine
import com.example.travelwake.theme.WeatherThemePalette

val LocalWeatherThemePalette = staticCompositionLocalOf { WeatherThemeEngine.DefaultPalette }

/**
 * TravelWake Glassmorphism Light Color Scheme:
 * Features deep navy primary (#0A1128 / #005AC1), crisp translucent surface backgrounds,
 * deep navy accents, and specular borders.
 */
val TravelWakeGlassmorphicLightColorScheme = lightColorScheme(
    primary = DeepNavyMidnight,
    onPrimary = Color.White,
    primaryContainer = ProfessionalPrimaryContainer,
    onPrimaryContainer = DeepNavyMidnight,
    secondary = ProfessionalPrimary,
    onSecondary = Color.White,
    secondaryContainer = ProfessionalSurfaceVariant,
    onSecondaryContainer = DeepNavyMidnight,
    tertiary = IndigoAccent,
    background = ProfessionalBackground,
    onBackground = DeepNavyDark,
    surface = Color(0xF2FFFFFF), // 95% translucent crisp white glass
    onSurface = DeepNavyDark,
    surfaceVariant = Color(0xE6E1E2EC), // Translucent frosted surface variant
    onSurfaceVariant = ProfessionalTextSecondary,
    outline = GlassBorderHighlight,
    outlineVariant = ProfessionalDivider,
    error = AlarmRed,
    onError = Color.White,
    errorContainer = AlarmRedContainer,
    onErrorContainer = Color(0xFF410002)
)

/**
 * TravelWake Glassmorphism Dark Color Scheme:
 * Features deep navy primary, translucent surface backgrounds (GlassNavySurface, GlassNavyCard),
 * bright cyan highlights, and specular hairline borders.
 */
val TravelWakeGlassmorphicDarkColorScheme = darkColorScheme(
    primary = BrightCyan,
    onPrimary = DeepNavyDark,
    primaryContainer = Color(0xFF004D70),
    onPrimaryContainer = Color(0xFFC7E7FF),
    secondary = ElectricBlue,
    onSecondary = Color.White,
    secondaryContainer = DarkNavyCard,
    onSecondaryContainer = Color(0xFFD8E2FF),
    tertiary = IndigoAccent,
    background = DeepNavyDark,
    onBackground = TextPrimaryDark,
    surface = GlassNavySurface, // Translucent deep navy surface background
    onSurface = TextPrimaryDark,
    surfaceVariant = GlassNavyCard, // Translucent deep navy card background
    onSurfaceVariant = TextSecondaryDark,
    outline = GlassNavyBorder,
    outlineVariant = Color(0xFF1E293B),
    error = Color(0xFFFFB4AB),
    onError = Color(0xFF690005)
)

// Legacy aliases maintained for backward compatibility
private val ProfessionalPolishLightColorScheme = TravelWakeGlassmorphicLightColorScheme
private val PremiumGlassmorphismDarkColorScheme = TravelWakeGlassmorphicDarkColorScheme
private val ProfessionalPolishDarkColorScheme = TravelWakeGlassmorphicDarkColorScheme

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false,
    weatherPalette: WeatherThemePalette? = null,
    themeMode: com.example.travelwake.data.model.AppThemeMode? = null,
    content: @Composable () -> Unit
) {
    val systemInDark = isSystemInDarkTheme()
    val isDark = when (themeMode) {
        com.example.travelwake.data.model.AppThemeMode.SYSTEM -> systemInDark
        com.example.travelwake.data.model.AppThemeMode.LIGHT -> false
        com.example.travelwake.data.model.AppThemeMode.DARK -> true
        null -> darkTheme
    }

    val activePalette = weatherPalette ?: WeatherThemeEngine.DefaultPalette
    val colorScheme = if (weatherPalette != null) {
        if (isDark) activePalette.darkColorScheme else activePalette.lightColorScheme
    } else {
        if (isDark) TravelWakeGlassmorphicDarkColorScheme else TravelWakeGlassmorphicLightColorScheme
    }

    CompositionLocalProvider(
        LocalWeatherThemePalette provides activePalette,
        LocalGlassmorphismStyle provides GlassmorphismStyle()
    ) {
        MaterialTheme(
            colorScheme = colorScheme,
            typography = Typography,
            content = content
        )
    }
}
