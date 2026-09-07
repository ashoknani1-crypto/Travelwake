package com.example.travelwake.data.repository

import android.util.Log
import com.example.travelwake.data.model.CautionLevel
import com.example.travelwake.data.model.WeatherInfo
import com.example.travelwake.data.model.WeatherRecommendation
import com.example.travelwake.data.weather.ArrivalWeatherForecast
import com.example.travelwake.data.weather.WeatherApiService
import com.example.travelwake.engine.WeatherEngine
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * Repository interface and implementation that integrates with a weather service
 * to fetch temperature and conditions based on destination coordinates,
 * including a graceful fallback implementation if the service is unreachable.
 */
class WeatherRepository(
    private val weatherApiService: WeatherApiService = WeatherApiService()
) {
    companion object {
        private const val TAG = "WeatherRepository"
    }

    /**
     * Fetches current destination weather and forecast using destination coordinates.
     * If the remote network service fails or is unreachable, provides an offline-first graceful fallback.
     */
    suspend fun getDestinationWeather(
        latitude: Double,
        longitude: Double,
        destinationName: String = "Destination",
        arrivalEpochMs: Long = System.currentTimeMillis() + 1800_000L
    ): WeatherInfo = withContext(Dispatchers.IO) {
        try {
            val forecast: ArrivalWeatherForecast = weatherApiService.getForecastForArrivalTime(
                latitude = latitude,
                longitude = longitude,
                arrivalEpochMs = arrivalEpochMs,
                destinationName = destinationName
            )

            val rec = WeatherRecommendation(
                title = forecast.condition,
                icon = forecast.icon,
                cautionLevel = if (forecast.rainProbability > 50) CautionLevel.WARNING else CautionLevel.NORMAL,
                description = forecast.recommendationSummary
            )

            WeatherInfo(
                temperatureCelsius = forecast.temperatureCelsius,
                condition = forecast.condition,
                rainProbability = forecast.rainProbability,
                windSpeedKmh = forecast.windSpeedKmh,
                humidity = 60,
                visibilityKm = 10,
                summary = "${forecast.condition} expected at $destinationName (${forecast.temperatureCelsius}°C)",
                recommendations = listOf(rec)
            )
        } catch (e: Exception) {
            Log.w(TAG, "Weather service call failed (${e.message}). Engaging graceful local fallback.")
            getFallbackWeather(destinationName, latitude, longitude)
        }
    }

    /**
     * Fetch arrival forecast directly with full fallback guarantee.
     */
    suspend fun getArrivalForecast(
        latitude: Double,
        longitude: Double,
        arrivalEpochMs: Long,
        destinationName: String
    ): ArrivalWeatherForecast = withContext(Dispatchers.IO) {
        try {
            weatherApiService.getForecastForArrivalTime(latitude, longitude, arrivalEpochMs, destinationName)
        } catch (e: Exception) {
            Log.w(TAG, "Arrival forecast failed (${e.message}). Falling back to WeatherEngine.")
            val fallback = WeatherEngine.generateWeatherForDestination(destinationName, latitude, longitude)
            ArrivalWeatherForecast(
                temperatureCelsius = fallback.temperatureCelsius,
                condition = fallback.condition,
                rainProbability = fallback.rainProbability,
                windSpeedKmh = fallback.windSpeedKmh,
                icon = if (fallback.rainProbability > 50) "🌧" else "🌤",
                arrivalTimeFormatted = "Expected Arrival",
                recommendationSummary = "Weather estimation based on local climate model",
                isRealApiData = false
            )
        }
    }

    /**
     * Graceful fallback implementation utilizing WeatherEngine to construct
     * realistic travel forecasts and safety recommendations even completely offline.
     */
    fun getFallbackWeather(destinationName: String, latitude: Double, longitude: Double): WeatherInfo {
        return WeatherEngine.generateWeatherForDestination(destinationName, latitude, longitude)
    }
}
