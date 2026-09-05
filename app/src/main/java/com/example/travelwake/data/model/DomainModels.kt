package com.example.travelwake.data.model

data class DestinationItem(
    val id: String,
    val name: String,
    val address: String,
    val latitude: Double,
    val longitude: Double,
    val placeType: String,
    val distanceKm: Float,
    val estimatedMinutes: Int
)

data class WeatherInfo(
    val temperatureCelsius: Int = 26,
    val condition: String = "Partly Cloudy",
    val rainProbability: Int = 20,
    val windSpeedKmh: Int = 14,
    val humidity: Int = 60,
    val visibilityKm: Int = 10,
    val summary: String = "Comfortable travel conditions expected at arrival",
    val recommendations: List<WeatherRecommendation> = emptyList()
)

data class WeatherRecommendation(
    val title: String,
    val icon: String,
    val cautionLevel: CautionLevel = CautionLevel.NORMAL,
    val description: String
)

enum class CautionLevel {
    NORMAL,
    ADVISORY,
    WARNING,
    ALERT
}

data class SevereWeatherAlert(
    val id: String = "alert_${System.currentTimeMillis()}",
    val hazardType: String,
    val severity: CautionLevel = CautionLevel.ALERT,
    val headline: String,
    val description: String,
    val emergencyAction: String,
    val iconEmoji: String = "⚠️",
    val timestamp: Long = System.currentTimeMillis()
)

enum class AppThemeMode(
    val title: String,
    val description: String,
    val iconEmoji: String
) {
    SYSTEM("Auto (System Default)", "Automatically follows device day/night settings", "🌓"),
    LIGHT("Daylight Theme", "High-contrast daylight theme", "☀️"),
    DARK("Night Travel Theme", "Dimmed OLED-friendly palette for night journeys", "🌙")
}

enum class PreAlarmStage(val distanceMeters: Int, val title: String, val subtitle: String) {
    STAGE_1000M(1000, "Destination Approaching", "1 km remaining"),
    STAGE_750M(750, "Getting Closer", "750 m remaining"),
    STAGE_500M(500, "WAKE UP SOON", "500 m remaining"),
    STAGE_300M(300, "GET READY", "300 m remaining"),
    STAGE_100M(100, "DESTINATION NEAR", "100 m remaining")
}

data class UserProfile(
    val uid: String = "",
    val email: String = "",
    val displayName: String = "",
    val isAnonymous: Boolean = true
)
