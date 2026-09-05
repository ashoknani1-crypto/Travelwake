package com.example.travelwake.data.weather

import android.util.Log
import com.example.travelwake.data.model.CautionLevel
import com.example.travelwake.data.model.WeatherInfo
import com.example.travelwake.data.model.WeatherRecommendation
import com.example.travelwake.engine.WeatherEngine
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONObject
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.TimeZone
import java.util.concurrent.TimeUnit
import kotlin.math.roundToInt

data class ArrivalWeatherForecast(
    val temperatureCelsius: Int,
    val condition: String,
    val rainProbability: Int,
    val windSpeedKmh: Int,
    val icon: String,
    val arrivalTimeFormatted: String,
    val recommendationSummary: String,
    val isRealApiData: Boolean = true
)

class WeatherApiService {
    private val TAG = "WeatherApiService"
    private val client = OkHttpClient.Builder()
        .connectTimeout(5, TimeUnit.SECONDS)
        .readTimeout(5, TimeUnit.SECONDS)
        .build()

    suspend fun getForecastForArrivalTime(
        latitude: Double,
        longitude: Double,
        arrivalEpochMs: Long,
        destinationName: String
    ): ArrivalWeatherForecast = withContext(Dispatchers.IO) {
        val arrivalDate = Date(arrivalEpochMs)
        val hourFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:00", Locale.US).apply {
            timeZone = TimeZone.getDefault()
        }
        val targetHourKey = hourFormat.format(arrivalDate)
        val timeDisplayFormat = SimpleDateFormat("hh:mm a", Locale.getDefault())
        val formattedTime = timeDisplayFormat.format(arrivalDate)

        try {
            val url = "https://api.open-meteo.com/v1/forecast" +
                    "?latitude=%.4f&longitude=%.4f".format(Locale.US, latitude, longitude) +
                    "&hourly=temperature_2m,precipitation_probability,weathercode,windspeed_10m" +
                    "&timezone=auto"

            val request = Request.Builder()
                .url(url)
                .header("User-Agent", "TravelWake-Android/1.0")
                .build()

            val response = client.newCall(request).execute()
            if (response.isSuccessful) {
                val bodyString = response.body?.string()
                if (!bodyString.isNullOrBlank()) {
                    val root = JSONObject(bodyString)
                    val hourly = root.optJSONObject("hourly")
                    if (hourly != null) {
                        val times = hourly.optJSONArray("time")
                        val temps = hourly.optJSONArray("temperature_2m")
                        val precipProbs = hourly.optJSONArray("precipitation_probability")
                        val weatherCodes = hourly.optJSONArray("weathercode")
                        val windSpeeds = hourly.optJSONArray("windspeed_10m")

                        if (times != null && temps != null && times.length() > 0) {
                            var bestIndex = 0
                            for (i in 0 until times.length()) {
                                if (times.getString(i).startsWith(targetHourKey)) {
                                    bestIndex = i
                                    break
                                }
                            }

                            val temp = temps.optDouble(bestIndex, 24.0).roundToInt()
                            val rainProb = precipProbs?.optInt(bestIndex, 10) ?: 10
                            val code = weatherCodes?.optInt(bestIndex, 0) ?: 0
                            val wind = windSpeeds?.optDouble(bestIndex, 12.0)?.roundToInt() ?: 12

                            val (condition, icon) = mapWmoCode(code, rainProb)
                            val recommendation = generateRecommendation(temp, condition, rainProb, icon, formattedTime)

                            return@withContext ArrivalWeatherForecast(
                                temperatureCelsius = temp,
                                condition = condition,
                                rainProbability = rainProb,
                                windSpeedKmh = wind,
                                icon = icon,
                                arrivalTimeFormatted = formattedTime,
                                recommendationSummary = recommendation,
                                isRealApiData = true
                            )
                        }
                    }
                }
            }
        } catch (e: Exception) {
            Log.w(TAG, "Failed to fetch live weather forecast from Open-Meteo API: ${e.message}. Using arrival-time fallback engine.")
        }

        // Fallback using WeatherEngine with destination and arrival time
        val fallback = WeatherEngine.generateWeatherForDestination(destinationName, latitude, longitude)
        val recSummary = if (fallback.recommendations.isNotEmpty()) {
            "${fallback.recommendations.first().title} (${fallback.recommendations.first().description})"
        } else {
            "Comfortable conditions expected at $formattedTime arrival"
        }

        ArrivalWeatherForecast(
            temperatureCelsius = fallback.temperatureCelsius,
            condition = fallback.condition,
            rainProbability = fallback.rainProbability,
            windSpeedKmh = fallback.windSpeedKmh,
            icon = if (fallback.rainProbability > 50) "🌧" else "🌤",
            arrivalTimeFormatted = formattedTime,
            recommendationSummary = recSummary,
            isRealApiData = false
        )
    }

    private fun mapWmoCode(code: Int, rainProb: Int): Pair<String, String> {
        return when (code) {
            0 -> "Clear Sky" to "☀️"
            1 -> "Mainly Clear" to "🌤"
            2 -> "Partly Cloudy" to "⛅"
            3 -> "Overcast" to "☁️"
            45, 48 -> "Foggy" to "🌫"
            51, 53, 55 -> "Light Drizzle" to "🌦"
            61, 63 -> "Showers / Rain" to "🌧"
            65 -> "Heavy Downpour" to "🌧"
            71, 73, 75 -> "Snow Fall" to "❄️"
            80, 81, 82 -> "Rain Showers" to "🌧"
            95, 96, 99 -> "Thunderstorm Alert" to "⛈"
            else -> if (rainProb > 40) "Rain Likely" to "🌧" else "Partly Cloudy" to "🌤"
        }
    }

    private fun generateRecommendation(temp: Int, condition: String, rainProb: Int, icon: String, arrivalTime: String): String {
        return when {
            condition.contains("Thunderstorm", ignoreCase = true) ->
                "⛈ Thunderstorm risk around $arrivalTime arrival! Stay inside sheltered transit areas."
            rainProb > 50 || condition.contains("Rain", ignoreCase = true) || condition.contains("Drizzle", ignoreCase = true) ->
                "🌧 Rain expected at arrival ($rainProb% chance). Don't forget your umbrella & rain protection!"
            temp >= 33 ->
                "☀️ High temperature ($temp°C) at $arrivalTime arrival. Stay hydrated."
            temp <= 16 ->
                "🧥 Cool temperatures ($temp°C) at $arrivalTime arrival. Keep a warm layer handy."
            else ->
                "🌤 Pleasant $temp°C travel weather expected at $arrivalTime arrival."
        }
    }
}
