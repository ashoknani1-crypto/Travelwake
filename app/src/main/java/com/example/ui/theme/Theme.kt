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

// Professional Polish Theme Color Schemes
private val ProfessionalPolishLightColorScheme = lightColorScheme(
    primary = ProfessionalPrimary,
    onPrimary = Color.White,
    primaryContainer = ProfessionalPrimaryContainer,
    onPrimaryContainer = ProfessionalOnPrimaryContainer,
    secondary = ProfessionalPrimary,
    onSecondary = Color.White,
    secondaryContainer = ProfessionalSurfaceVariant,
    onSecondaryContainer = ProfessionalTextPrimary,
    tertiary = IndigoAccent,
    background = ProfessionalBackground,
    onBackground = ProfessionalTextPrimary,
    surface = ProfessionalSurface,
    onSurface = ProfessionalTextPrimary,
    surfaceVariant = ProfessionalSurfaceVariant,
    onSurfaceVariant = ProfessionalTextSecondary,
    outline = ProfessionalBorder,
    outlineVariant = ProfessionalDivider,
    error = AlarmRed,
    onError = Color.White,
    errorContainer = AlarmRedContainer,
    onErrorContainer = Color(0xFF410002)
)

private val ProfessionalPolishDarkColorScheme = darkColorScheme(
    primary = Color(0xFFA8C7FA),
    onPrimary = Color(0xFF003062),
    primaryContainer = Color(0xFF004694),
    onPrimaryContainer = Color(0xFFD3E4FF),
    secondary = Color(0xFFA8C7FA),
    onSecondary = Color(0xFF003062),
    tertiary = Color(0xFFC7BCE0),
    background = Color(0xFF111318),
    onBackground = Color(0xFFE2E2E9),
    surface = Color(0xFF1B1F27),
    onSurface = Color(0xFFE2E2E9),
    surfaceVariant = Color(0xFF2A2E38),
    onSurfaceVariant = Color(0xFFC4C6D0),
    outline = Color(0xFF44474E),
    outlineVariant = Color(0xFF2A2E38),
    error = Color(0xFFFFB4AB),
    onError = Color(0xFF690005)
)

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
        if (isDark) ProfessionalPolishDarkColorScheme else ProfessionalPolishLightColorScheme
    }

    CompositionLocalProvider(LocalWeatherThemePalette provides activePalette) {
        MaterialTheme(
            colorScheme = colorScheme,
            typography = Typography,
            content = content
        )
    }
}
