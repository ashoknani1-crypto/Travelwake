package com.example.travelwake.theme

import androidx.compose.material3.ColorScheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import com.example.travelwake.data.model.WeatherInfo
import com.example.ui.theme.AlarmRed
import com.example.ui.theme.AlarmRedContainer
import com.example.ui.theme.ProfessionalBackground
import com.example.ui.theme.ProfessionalBorder
import com.example.ui.theme.ProfessionalDivider
import com.example.ui.theme.ProfessionalOnPrimaryContainer
import com.example.ui.theme.ProfessionalPrimary
import com.example.ui.theme.ProfessionalPrimaryContainer
import com.example.ui.theme.ProfessionalSurface
import com.example.ui.theme.ProfessionalSurfaceVariant
import com.example.ui.theme.ProfessionalTextPrimary
import com.example.ui.theme.ProfessionalTextSecondary

enum class WeatherThemeType(
    val displayName: String,
    val iconEmoji: String,
    val description: String
) {
    DEFAULT("Professional Polish", "👔", "Classic high-contrast professional styling"),
    SUNNY_CLEAR("Radiant Sunshine", "☀️", "Warm luminous sky blue with golden accents"),
    RAINY_STORMY("Rain Atmosphere", "🌧️", "Deep slate teal and refreshing ocean cyan"),
    CLOUDY_OVERCAST("Cool Overcast", "⛅", "Soft steel slate with periwinkle twilight tones"),
    SNOWY_FREEZING("Arctic Frost", "❄️", "Glacial crystal cyan with crisp arctic blue accents")
}

data class WeatherThemePalette(
    val themeType: WeatherThemeType,
    val primaryColor: Color,
    val primaryContainerColor: Color,
    val onPrimaryContainerColor: Color,
    val backgroundColor: Color,
    val surfaceColor: Color,
    val surfaceVariantColor: Color,
    val borderColor: Color,
    val accentHighlight: Color,
    val gradientBackground: Brush,
    val lightColorScheme: ColorScheme,
    val darkColorScheme: ColorScheme
)

object WeatherThemeEngine {

    // 1. Classic Default Professional Polish
    val DefaultPalette = WeatherThemePalette(
        themeType = WeatherThemeType.DEFAULT,
        primaryColor = ProfessionalPrimary,
        primaryContainerColor = ProfessionalPrimaryContainer,
        onPrimaryContainerColor = ProfessionalOnPrimaryContainer,
        backgroundColor = ProfessionalBackground,
        surfaceColor = ProfessionalSurface,
        surfaceVariantColor = ProfessionalSurfaceVariant,
        borderColor = ProfessionalBorder,
        accentHighlight = Color(0xFF005AC1),
        gradientBackground = Brush.verticalGradient(
            colors = listOf(Color(0xFFF3F4F9), Color(0xFFEBF0FA))
        ),
        lightColorScheme = lightColorScheme(
            primary = ProfessionalPrimary,
            onPrimary = Color.White,
            primaryContainer = ProfessionalPrimaryContainer,
            onPrimaryContainer = ProfessionalOnPrimaryContainer,
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
            errorContainer = AlarmRedContainer
        ),
        darkColorScheme = darkColorScheme(
            primary = Color(0xFFA8C7FA),
            onPrimary = Color(0xFF003062),
            primaryContainer = Color(0xFF004694),
            onPrimaryContainer = Color(0xFFD3E4FF),
            background = Color(0xFF111318),
            onBackground = Color(0xFFE2E2E9),
            surface = Color(0xFF1B1F27),
            onSurface = Color(0xFFE2E2E9)
        )
    )

    // 2. Sunny / Clear Weather Palette
    val SunnyPalette = WeatherThemePalette(
        themeType = WeatherThemeType.SUNNY_CLEAR,
        primaryColor = Color(0xFF006494),
        primaryContainerColor = Color(0xFFCCE5FF),
        onPrimaryContainerColor = Color(0xFF001E30),
        backgroundColor = Color(0xFFF4F9FD),
        surfaceColor = Color(0xFFFFFFFF),
        surfaceVariantColor = Color(0xFFE2EDF7),
        borderColor = Color(0xFFB8CEE2),
        accentHighlight = Color(0xFFE69100),
        gradientBackground = Brush.verticalGradient(
            colors = listOf(Color(0xFFF0F7FD), Color(0xFFFFF9EE))
        ),
        lightColorScheme = lightColorScheme(
            primary = Color(0xFF006494),
            onPrimary = Color.White,
            primaryContainer = Color(0xFFCCE5FF),
            onPrimaryContainer = Color(0xFF001E30),
            secondary = Color(0xFFE69100),
            onSecondary = Color.White,
            secondaryContainer = Color(0xFFFFECC4),
            onSecondaryContainer = Color(0xFF2B1700),
            background = Color(0xFFF4F9FD),
            onBackground = Color(0xFF181C20),
            surface = Color(0xFFFFFFFF),
            onSurface = Color(0xFF181C20),
            surfaceVariant = Color(0xFFE2EDF7),
            onSurfaceVariant = Color(0xFF404850),
            outline = Color(0xFFB8CEE2),
            outlineVariant = Color(0xFFD3E2F0),
            error = AlarmRed,
            onError = Color.White,
            errorContainer = AlarmRedContainer
        ),
        darkColorScheme = darkColorScheme(
            primary = Color(0xFF8BCEFF),
            onPrimary = Color(0xFF003450),
            primaryContainer = Color(0xFF004B71),
            onPrimaryContainer = Color(0xFFCCE5FF),
            background = Color(0xFF101418),
            onBackground = Color(0xFFE1E2E8),
            surface = Color(0xFF1B2026),
            onSurface = Color(0xFFE1E2E8)
        )
    )

    // 3. Rainy / Stormy Weather Palette
    val RainyPalette = WeatherThemePalette(
        themeType = WeatherThemeType.RAINY_STORMY,
        primaryColor = Color(0xFF00677C),
        primaryContainerColor = Color(0xFFB0ECFE),
        onPrimaryContainerColor = Color(0xFF001F27),
        backgroundColor = Color(0xFFF0F6F8),
        surfaceColor = Color(0xFFFFFFFF),
        surfaceVariantColor = Color(0xFFDAE5E9),
        borderColor = Color(0xFFB5C6CD),
        accentHighlight = Color(0xFF009688),
        gradientBackground = Brush.verticalGradient(
            colors = listOf(Color(0xFFEFF7FA), Color(0xFFE0EFF5))
        ),
        lightColorScheme = lightColorScheme(
            primary = Color(0xFF00677C),
            onPrimary = Color.White,
            primaryContainer = Color(0xFFB0ECFE),
            onPrimaryContainer = Color(0xFF001F27),
            secondary = Color(0xFF00838F),
            onSecondary = Color.White,
            secondaryContainer = Color(0xFFB4EAF2),
            onSecondaryContainer = Color(0xFF001F24),
            background = Color(0xFFF0F6F8),
            onBackground = Color(0xFF181C1E),
            surface = Color(0xFFFFFFFF),
            onSurface = Color(0xFF181C1E),
            surfaceVariant = Color(0xFFDAE5E9),
            onSurfaceVariant = Color(0xFF3F484B),
            outline = Color(0xFFB5C6CD),
            outlineVariant = Color(0xFFCFDDE2),
            error = AlarmRed,
            onError = Color.White,
            errorContainer = AlarmRedContainer
        ),
        darkColorScheme = darkColorScheme(
            primary = Color(0xFF56D6F4),
            onPrimary = Color(0xFF003642),
            primaryContainer = Color(0xFF004E5E),
            onPrimaryContainer = Color(0xFFB0ECFE),
            background = Color(0xFF0F1416),
            onBackground = Color(0xFFE0E3E5),
            surface = Color(0xFF192023),
            onSurface = Color(0xFFE0E3E5)
        )
    )

    // 4. Cloudy / Overcast Weather Palette
    val CloudyPalette = WeatherThemePalette(
        themeType = WeatherThemeType.CLOUDY_OVERCAST,
        primaryColor = Color(0xFF425E7A),
        primaryContainerColor = Color(0xFFC7E0FE),
        onPrimaryContainerColor = Color(0xFF001D35),
        backgroundColor = Color(0xFFF2F4F7),
        surfaceColor = Color(0xFFFFFFFF),
        surfaceVariantColor = Color(0xFFE0E4EB),
        borderColor = Color(0xFFBDC4D0),
        accentHighlight = Color(0xFF536E8B),
        gradientBackground = Brush.verticalGradient(
            colors = listOf(Color(0xFFF4F6F9), Color(0xFFEAEFF5))
        ),
        lightColorScheme = lightColorScheme(
            primary = Color(0xFF425E7A),
            onPrimary = Color.White,
            primaryContainer = Color(0xFFC7E0FE),
            onPrimaryContainer = Color(0xFF001D35),
            secondary = Color(0xFF55606F),
            onSecondary = Color.White,
            secondaryContainer = Color(0xFFD9E4F6),
            onSecondaryContainer = Color(0xFF121D2A),
            background = Color(0xFFF2F4F7),
            onBackground = Color(0xFF191C1F),
            surface = Color(0xFFFFFFFF),
            onSurface = Color(0xFF191C1F),
            surfaceVariant = Color(0xFFE0E4EB),
            onSurfaceVariant = Color(0xFF43474F),
            outline = Color(0xFFBDC4D0),
            outlineVariant = Color(0xFFD5DCE6),
            error = AlarmRed,
            onError = Color.White,
            errorContainer = AlarmRedContainer
        ),
        darkColorScheme = darkColorScheme(
            primary = Color(0xFFA6C9EE),
            onPrimary = Color(0xFF10314C),
            primaryContainer = Color(0xFF2A4762),
            onPrimaryContainer = Color(0xFFC7E0FE),
            background = Color(0xFF111417),
            onBackground = Color(0xFFE1E2E6),
            surface = Color(0xFF1A1F24),
            onSurface = Color(0xFFE1E2E6)
        )
    )

    // 5. Snowy / Freezing Weather Palette
    val SnowyPalette = WeatherThemePalette(
        themeType = WeatherThemeType.SNOWY_FREEZING,
        primaryColor = Color(0xFF006688),
        primaryContainerColor = Color(0xFFC2E8FF),
        onPrimaryContainerColor = Color(0xFF001E2B),
        backgroundColor = Color(0xFFF0F7FB),
        surfaceColor = Color(0xFFFFFFFF),
        surfaceVariantColor = Color(0xFFDDE8F0),
        borderColor = Color(0xFFB5C9D6),
        accentHighlight = Color(0xFF00B4D8),
        gradientBackground = Brush.verticalGradient(
            colors = listOf(Color(0xFFF0F8FF), Color(0xFFE3F2FD))
        ),
        lightColorScheme = lightColorScheme(
            primary = Color(0xFF006688),
            onPrimary = Color.White,
            primaryContainer = Color(0xFFC2E8FF),
            onPrimaryContainer = Color(0xFF001E2B),
            secondary = Color(0xFF007799),
            onSecondary = Color.White,
            secondaryContainer = Color(0xFFB8E8FA),
            onSecondaryContainer = Color(0xFF001F29),
            background = Color(0xFFF0F7FB),
            onBackground = Color(0xFF171D20),
            surface = Color(0xFFFFFFFF),
            onSurface = Color(0xFF171D20),
            surfaceVariant = Color(0xFFDDE8F0),
            onSurfaceVariant = Color(0xFF3F484E),
            outline = Color(0xFFB5C9D6),
            outlineVariant = Color(0xFFCCE0ED),
            error = AlarmRed,
            onError = Color.White,
            errorContainer = AlarmRedContainer
        ),
        darkColorScheme = darkColorScheme(
            primary = Color(0xFF75D1FF),
            onPrimary = Color(0xFF003548),
            primaryContainer = Color(0xFF004D67),
            onPrimaryContainer = Color(0xFFC2E8FF),
            background = Color(0xFF0F1417),
            onBackground = Color(0xFFDEE3E7),
            surface = Color(0xFF182025),
            onSurface = Color(0xFFDEE3E7)
        )
    )

    /**
     * Determines the optimal UI theme based on current destination weather condition and temperature.
     */
    fun determineWeatherTheme(weather: WeatherInfo?): WeatherThemePalette {
        if (weather == null) return DefaultPalette

        val conditionLower = weather.condition.lowercase()
        val temp = weather.temperatureCelsius
        val rainProb = weather.rainProbability

        return when {
            // Freezing or Snowy
            temp <= 2 || conditionLower.contains("snow") || conditionLower.contains("blizzard") ||
                    conditionLower.contains("sleet") || conditionLower.contains("ice") ||
                    conditionLower.contains("freezing") -> SnowyPalette

            // Rain, Thunderstorm, or Drizzle
            rainProb >= 50 || conditionLower.contains("rain") || conditionLower.contains("storm") ||
                    conditionLower.contains("thunder") || conditionLower.contains("drizzle") ||
                    conditionLower.contains("shower") -> RainyPalette

            // Clouds, Fog, Mist, Overcast
            conditionLower.contains("cloud") || conditionLower.contains("overcast") ||
                    conditionLower.contains("fog") || conditionLower.contains("mist") ||
                    conditionLower.contains("haze") -> CloudyPalette

            // Sunny or Clear
            conditionLower.contains("sun") || conditionLower.contains("clear") ||
                    conditionLower.contains("fair") || temp >= 25 -> SunnyPalette

            else -> DefaultPalette
        }
    }
}
