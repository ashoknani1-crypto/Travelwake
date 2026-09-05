package com.example.travelwake.engine

import com.example.travelwake.data.model.CautionLevel
import com.example.travelwake.data.model.WeatherInfo
import com.example.travelwake.data.model.WeatherRecommendation

object WeatherEngine {

    fun generateWeatherForDestination(destinationName: String, lat: Double, lon: Double): WeatherInfo {
        // Hash location and name to get stable realistic weather
        val hash = (destinationName.hashCode() + (lat * 100).toInt() + (lon * 100).toInt()).let { if (it < 0) -it else it }
        val temp = 18 + (hash % 16) // 18 - 33 C
        val rainChance = (hash * 7) % 100
        val wind = 8 + (hash % 28) // 8 - 35 km/h
        val isStorm = rainChance > 70 && wind > 25
        val condition = when {
            isStorm -> "Thunderstorm"
            rainChance > 60 -> "Heavy Rain"
            rainChance > 35 -> "Scattered Showers"
            temp > 30 -> "Hot & Sunny"
            temp < 15 -> "Chilly & Breezy"
            wind > 25 -> "Windy"
            else -> "Partly Cloudy"
        }

        val recs = mutableListOf<WeatherRecommendation>()

        if (isStorm) {
            recs.add(
                WeatherRecommendation(
                    title = "Thunderstorm Risk",
                    icon = "⛈",
                    cautionLevel = CautionLevel.ALERT,
                    description = "Exercise caution at arrival; stay in sheltered areas."
                )
            )
            recs.add(
                WeatherRecommendation(
                    title = "Protect Electronics",
                    icon = "📱",
                    cautionLevel = CautionLevel.WARNING,
                    description = "Keep phone and electronics in waterproof pockets."
                )
            )
        } else if (rainChance > 60) {
            recs.add(
                WeatherRecommendation(
                    title = "Heavy Rain Expected",
                    icon = "🌧",
                    cautionLevel = CautionLevel.WARNING,
                    description = "Wear rainwear or waterproof jacket."
                )
            )
            recs.add(
                WeatherRecommendation(
                    title = "Carry Umbrella",
                    icon = "☂",
                    cautionLevel = CautionLevel.ADVISORY,
                    description = "Keep an umbrella handy before disembarking."
                )
            )
        } else if (rainChance > 30) {
            recs.add(
                WeatherRecommendation(
                    title = "Rain Possible",
                    icon = "🌦",
                    cautionLevel = CautionLevel.ADVISORY,
                    description = "Carry a compact umbrella for sudden showers."
                )
            )
        }

        if (temp >= 32) {
            recs.add(
                WeatherRecommendation(
                    title = "High Heat Warning",
                    icon = "☀",
                    cautionLevel = CautionLevel.WARNING,
                    description = "Carry drinking water and avoid prolonged sun exposure."
                )
            )
        } else if (temp <= 16) {
            recs.add(
                WeatherRecommendation(
                    title = "Cool Conditions",
                    icon = "❄",
                    cautionLevel = CautionLevel.ADVISORY,
                    description = "Consider wearing a light jacket or layer."
                )
            )
        }

        if (wind >= 28 && recs.size < 3) {
            recs.add(
                WeatherRecommendation(
                    title = "Strong Winds",
                    icon = "💨",
                    cautionLevel = CautionLevel.ADVISORY,
                    description = "Travel carefully in open platforms and transit stops."
                )
            )
        }

        if (recs.isEmpty()) {
            recs.add(
                WeatherRecommendation(
                    title = "Comfortable Conditions",
                    icon = "🌤",
                    cautionLevel = CautionLevel.NORMAL,
                    description = "Ideal travel weather expected at your destination."
                )
            )
        }

        return WeatherInfo(
            temperatureCelsius = temp,
            condition = condition,
            rainProbability = rainChance,
            windSpeedKmh = wind,
            humidity = 50 + (rainChance / 2),
            visibilityKm = if (isStorm) 4 else 10,
            summary = when {
                isStorm -> "Storm alerts active near destination around arrival time."
                rainChance > 50 -> "Rain expected around arrival; carry protective gear."
                temp >= 32 -> "Hot conditions at destination; stay hydrated."
                else -> "Favorable travel conditions expected at arrival."
            },
            recommendations = recs.take(3)
        )
    }

    /**
     * Inspects weather conditions for the destination to determine if a real-time
     * severe weather alert must be pushed to the alarm and notification system.
     */
    fun detectSevereWeatherAlert(destinationName: String, weather: WeatherInfo): com.example.travelwake.data.model.SevereWeatherAlert? {
        val isThunderstorm = weather.condition.contains("Thunderstorm", ignoreCase = true) || (weather.rainProbability > 65 && weather.windSpeedKmh > 25)
        val isHeavyRain = weather.condition.contains("Heavy Rain", ignoreCase = true) || weather.rainProbability >= 80
        val isGaleWind = weather.windSpeedKmh >= 35
        val isExtremeHeat = weather.temperatureCelsius >= 36
        val isFreezingSnow = weather.condition.contains("Snow", ignoreCase = true) || (weather.temperatureCelsius <= 0 && weather.rainProbability > 40)

        return when {
            isThunderstorm -> com.example.travelwake.data.model.SevereWeatherAlert(
                id = "alert_storm_${destinationName.hashCode()}",
                hazardType = "Severe Thunderstorm Warning",
                severity = CautionLevel.ALERT,
                headline = "Thunderstorm & Lightning Hazard at $destinationName",
                description = "High-intensity lightning and localized flash flood risk around arrival time (${weather.windSpeedKmh} km/h gusts).",
                emergencyAction = "Remain inside underground/sheltered concourse upon arrival. Avoid outdoor transit platforms.",
                iconEmoji = "⛈️"
            )
            isHeavyRain -> com.example.travelwake.data.model.SevereWeatherAlert(
                id = "alert_flood_${destinationName.hashCode()}",
                hazardType = "Heavy Downpour / Flash Flood Advisory",
                severity = CautionLevel.WARNING,
                headline = "Torrential Rainfall Imminent at $destinationName",
                description = "Precipitation probability at ${weather.rainProbability}%. Street-level water accumulation likely.",
                emergencyAction = "Prepare rain gear before disembarking and take indoor transit corridors.",
                iconEmoji = "🌊"
            )
            isGaleWind -> com.example.travelwake.data.model.SevereWeatherAlert(
                id = "alert_wind_${destinationName.hashCode()}",
                hazardType = "High Gale Wind Warning",
                severity = CautionLevel.WARNING,
                headline = "Gale Winds Exceeding ${weather.windSpeedKmh} km/h at $destinationName",
                description = "Strong gusts may cause delays to connecting buses and surface rail.",
                emergencyAction = "Hold onto luggage securely and watch for flying debris on open platforms.",
                iconEmoji = "🌪️"
            )
            isExtremeHeat -> com.example.travelwake.data.model.SevereWeatherAlert(
                id = "alert_heat_${destinationName.hashCode()}",
                hazardType = "Extreme Heat Advisory",
                severity = CautionLevel.WARNING,
                headline = "Dangerous Heat Wave (${weather.temperatureCelsius}°C) at $destinationName",
                description = "Ambient temperature exceeds safe thresholds for prolonged exposure.",
                emergencyAction = "Hydrate immediately; seek air-conditioned waiting zones upon exiting transit.",
                iconEmoji = "🔥"
            )
            isFreezingSnow -> com.example.travelwake.data.model.SevereWeatherAlert(
                id = "alert_snow_${destinationName.hashCode()}",
                hazardType = "Winter Blizzard & Black Ice Hazard",
                severity = CautionLevel.ALERT,
                headline = "Sub-Zero Freezing Conditions at $destinationName",
                description = "Freezing temperatures (${weather.temperatureCelsius}°C) create slippery stairs and platform ice.",
                emergencyAction = "Step carefully onto platform edge; button winter coat before stepping outside.",
                iconEmoji = "❄️"
            )
            else -> null
        }
    }
}
