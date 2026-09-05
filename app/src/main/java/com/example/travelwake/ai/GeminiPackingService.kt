package com.example.travelwake.ai

import android.util.Log
import com.example.BuildConfig
import com.example.travelwake.data.model.WeatherInfo
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import java.util.UUID
import java.util.concurrent.TimeUnit

data class GeminiPackingSuggestion(
    val id: String = UUID.randomUUID().toString(),
    val name: String,
    val category: String = "Essentials",
    val reason: String = "",
    val isSelected: Boolean = true
)

class GeminiPackingService(
    private val client: OkHttpClient = OkHttpClient.Builder()
        .connectTimeout(15, TimeUnit.SECONDS)
        .readTimeout(20, TimeUnit.SECONDS)
        .build()
) {
    companion object {
        private const val TAG = "GeminiPackingService"
        // Recommended model according to skill guidance for text tasks
        private const val GEMINI_MODEL = "gemini-2.5-flash"
        private const val BASE_URL = "https://generativelanguage.googleapis.com/v1beta/models"
    }

    /**
     * Calls Gemini API to generate personalized packing suggestions based on
     * the destination's weather forecast and trip duration.
     * Gracefully falls back to local weather/duration rules if API key is not configured or network fails.
     */
    suspend fun suggestPackingChecklist(
        destinationName: String,
        weather: WeatherInfo,
        tripDurationText: String
    ): List<GeminiPackingSuggestion> = withContext(Dispatchers.IO) {
        val apiKey = try {
            BuildConfig.GEMINI_API_KEY
        } catch (e: Throwable) {
            ""
        }

        if (apiKey.isNullOrBlank() || apiKey == "MY_GEMINI_API_KEY") {
            Log.d(TAG, "Gemini API key is empty or placeholder. Utilizing intelligent weather/duration heuristics.")
            return@withContext generateHeuristicPackingList(destinationName, weather, tripDurationText)
        }

        try {
            val prompt = buildPrompt(destinationName, weather, tripDurationText)
            val requestJson = JSONObject().apply {
                val contentsArray = JSONArray().apply {
                    val contentObj = JSONObject().apply {
                        val partsArray = JSONArray().apply {
                            put(JSONObject().put("text", prompt))
                        }
                        put("parts", partsArray)
                    }
                    put(contentObj)
                }
                put("contents", contentsArray)

                val genConfig = JSONObject().apply {
                    put("temperature", 0.3)
                    put("topK", 20)
                    put("topP", 0.8)
                    put("responseMimeType", "application/json")
                }
                put("generationConfig", genConfig)
            }

            val endpoint = "$BASE_URL/$GEMINI_MODEL:generateContent?key=$apiKey"
            val requestBody = requestJson.toString().toRequestBody("application/json; charset=utf-8".toMediaType())

            val request = Request.Builder()
                .url(endpoint)
                .post(requestBody)
                .build()

            val response = client.newCall(request).execute()
            if (!response.isSuccessful) {
                val errorBody = response.body?.string()
                Log.w(TAG, "Gemini API call returned status ${response.code}: $errorBody")
                return@withContext generateHeuristicPackingList(destinationName, weather, tripDurationText)
            }

            val responseText = response.body?.string() ?: ""
            val parsedList = parseGeminiResponse(responseText)
            if (parsedList.isNotEmpty()) {
                parsedList
            } else {
                generateHeuristicPackingList(destinationName, weather, tripDurationText)
            }
        } catch (e: Exception) {
            Log.e(TAG, "Failed calling Gemini API: ${e.message}, falling back to intelligent heuristics", e)
            generateHeuristicPackingList(destinationName, weather, tripDurationText)
        }
    }

    private fun buildPrompt(
        destinationName: String,
        weather: WeatherInfo,
        tripDurationText: String
    ): String {
        return """
            You are an expert travel packing assistant.
            The traveler is going to destination: "$destinationName".
            Trip Duration: "$tripDurationText".
            Destination Weather Forecast:
            - Temperature: ${weather.temperatureCelsius}°C
            - Condition: ${weather.condition}
            - Rain Probability: ${weather.rainProbability}%
            - Wind Speed: ${weather.windSpeedKmh} km/h
            - Humidity: ${weather.humidity}%

            Suggest a personalized, high-utility packing checklist of 5 to 7 items tailored specifically to this weather forecast and trip duration.
            Return ONLY a JSON array of objects with the following schema:
            [
              {
                "name": "Item Name",
                "category": "Weather Protection" | "Clothing" | "Electronics" | "Documents" | "Health & Comfort",
                "reason": "Brief 1-sentence justification mentioning weather or trip duration"
              }
            ]
        """.trimIndent()
    }

    private fun parseGeminiResponse(jsonString: String): List<GeminiPackingSuggestion> {
        val result = mutableListOf<GeminiPackingSuggestion>()
        try {
            val root = JSONObject(jsonString)
            val candidates = root.optJSONArray("candidates") ?: return emptyList()
            if (candidates.length() == 0) return emptyList()

            val firstCandidate = candidates.getJSONObject(0)
            val content = firstCandidate.optJSONObject("content") ?: return emptyList()
            val parts = content.optJSONArray("parts") ?: return emptyList()
            if (parts.length() == 0) return emptyList()

            val rawText = parts.getJSONObject(0).optString("text", "").trim()

            // Clean json markdown if needed
            val cleanJson = if (rawText.startsWith("```json")) {
                rawText.removePrefix("```json").removeSuffix("```").trim()
            } else if (rawText.startsWith("```")) {
                rawText.removePrefix("```").removeSuffix("```").trim()
            } else {
                rawText
            }

            val array = JSONArray(cleanJson)
            for (i in 0 until array.length()) {
                val item = array.getJSONObject(i)
                val name = item.optString("name", "").trim()
                val category = item.optString("category", "Essentials").trim()
                val reason = item.optString("reason", "Recommended for your trip").trim()
                if (name.isNotBlank()) {
                    result.add(
                        GeminiPackingSuggestion(
                            name = name,
                            category = category,
                            reason = reason,
                            isSelected = true
                        )
                    )
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error parsing Gemini response: ${e.message}")
        }
        return result
    }

    /**
     * Intelligent local fallback tailored to exact destination weather and trip duration.
     */
    fun generateHeuristicPackingList(
        destinationName: String,
        weather: WeatherInfo,
        tripDurationText: String
    ): List<GeminiPackingSuggestion> {
        val items = mutableListOf<GeminiPackingSuggestion>()

        // 1. Rain / Storm protection
        if (weather.rainProbability >= 40 || weather.condition.contains("Rain", true) || weather.condition.contains("Showers", true)) {
            items.add(
                GeminiPackingSuggestion(
                    name = "Compact Windproof Umbrella",
                    category = "Weather Protection",
                    reason = "Rain probability is ${weather.rainProbability}% at $destinationName"
                )
            )
            items.add(
                GeminiPackingSuggestion(
                    name = "Waterproof Backpack Cover",
                    category = "Weather Protection",
                    reason = "Keep electronics and baggage dry during transit"
                )
            )
        }

        // 2. Temperature based clothing
        when {
            weather.temperatureCelsius < 12 -> {
                items.add(
                    GeminiPackingSuggestion(
                        name = "Insulated Thermal Fleece / Jacket",
                        category = "Clothing",
                        reason = "Chilly destination temperature of ${weather.temperatureCelsius}°C"
                    )
                )
                items.add(
                    GeminiPackingSuggestion(
                        name = "Lightweight Scarf / Neck Warmer",
                        category = "Clothing",
                        reason = "Protection against ${weather.windSpeedKmh} km/h cold winds"
                    )
                )
            }
            weather.temperatureCelsius > 28 -> {
                items.add(
                    GeminiPackingSuggestion(
                        name = "UV Sunglasses & Sun Hat",
                        category = "Weather Protection",
                        reason = "Sunny conditions at ${weather.temperatureCelsius}°C"
                    )
                )
                items.add(
                    GeminiPackingSuggestion(
                        name = "Electrolyte Hydration Bottle",
                        category = "Health & Comfort",
                        reason = "Stay hydrated during warm travel at destination"
                    )
                )
            }
            else -> {
                items.add(
                    GeminiPackingSuggestion(
                        name = "Breathable Light Layer Jacket",
                        category = "Clothing",
                        reason = "Ideal for mild ${weather.temperatureCelsius}°C ${weather.condition} forecast"
                    )
                )
            }
        }

        // 3. Trip Duration based recommendations
        val durationLower = tripDurationText.lowercase()
        when {
            durationLower.contains("day") || durationLower.contains("weekend") || durationLower.contains("multi") || durationLower.contains("overnight") -> {
                items.add(
                    GeminiPackingSuggestion(
                        name = "High-Capacity Power Bank (20000mAh)",
                        category = "Electronics",
                        reason = "Essential for multi-day battery autonomy"
                    )
                )
                items.add(
                    GeminiPackingSuggestion(
                        name = "Travel Toiletry Kit & Sanitizer",
                        category = "Health & Comfort",
                        reason = "Recommended for $tripDurationText duration"
                    )
                )
                items.add(
                    GeminiPackingSuggestion(
                        name = "Change of Clothes & Socks",
                        category = "Clothing",
                        reason = "Fresh attire for $tripDurationText"
                    )
                )
            }
            else -> { // Short / Day commute
                items.add(
                    GeminiPackingSuggestion(
                        name = "Noise-Cancelling Earbuds / Headphones",
                        category = "Electronics",
                        reason = "Focus and restful commute on transit"
                    )
                )
                items.add(
                    GeminiPackingSuggestion(
                        name = "Digital / Physical Transit Ticket Pass",
                        category = "Documents",
                        reason = "Quick validation at $destinationName turnstiles"
                    )
                )
            }
        }

        // Always essential
        items.add(
            GeminiPackingSuggestion(
                name = "Wallet & Government Photo ID",
                category = "Documents",
                reason = "Mandatory for transit security checkpoints"
            )
        )

        return items
    }
}
