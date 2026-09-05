package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.travelwake.data.model.BelongingEntity
import com.example.travelwake.data.model.CautionLevel
import com.example.travelwake.data.model.JourneyEntity
import com.example.travelwake.data.model.TransportMode
import com.example.travelwake.engine.LocationEngine
import com.example.travelwake.engine.WeatherEngine
import com.example.travelwake.service.BatterySaverInfo
import com.example.travelwake.utils.BelongingsShareHelper
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class ExampleRobolectricTest {

    @Test
    fun `read string from context`() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val appName = context.getString(R.string.app_name)
        assertEquals("TravelWake", appName)
    }

    @Test
    fun `test distance and eta calculations`() {
        val distance = LocationEngine.calculateDistanceMeters(
            18.5204, 73.8567, // Pune Center
            18.5289, 73.8744  // Pune Station
        )
        assertTrue("Distance should be greater than 1000m", distance > 1000)

        val etaTrain = LocationEngine.calculateEtaMinutes(5000, TransportMode.TRAIN)
        assertTrue("Train ETA for 5km should be reasonable", etaTrain in 1..20)

        val formattedKm = LocationEngine.formatDistance(2500)
        assertEquals("2.5 km", formattedKm)

        val formattedMeters = LocationEngine.formatDistance(450)
        assertEquals("450 m", formattedMeters)
    }

    @Test
    fun `test weather engine safety recommendations`() {
        val weather = WeatherEngine.generateWeatherForDestination("Central Station", 18.5204, 73.8567)
        assertNotNull(weather)
        assertTrue(weather.recommendations.isNotEmpty())
        assertTrue(weather.recommendations.size <= 3)
    }

    @Test
    fun `test belongings checklist share formatting`() {
        val items = listOf(
            BelongingEntity(id = "1", name = "Passport & Visa", category = "Essentials", isChecked = true),
            BelongingEntity(id = "2", name = "Power Bank", category = "Electronics", isChecked = false),
            BelongingEntity(id = "3", name = "Light Rain Jacket", category = "Clothing", isChecked = true)
        )

        val text = BelongingsShareHelper.formatChecklistText("Kyoto Grand Terminal", items)
        assertTrue("Contains header", text.contains("TRAVELWAKE PACKING & BELONGINGS CHECKLIST"))
        assertTrue("Contains destination", text.contains("Kyoto Grand Terminal"))
        assertTrue("Contains progress ratio", text.contains("2 of 3"))
        assertTrue("Contains packed item with checkmark", text.contains("[✓] Passport & Visa"))
        assertTrue("Contains unpacked item with box", text.contains("[ ] Power Bank"))
        assertTrue("Contains categories", text.contains("[Essentials]") && text.contains("[Electronics]"))
    }

    @Test
    fun `test journey history entity data fields`() {
        val trip = JourneyEntity(
            id = "trip_456",
            title = "Tokyo Station",
            address = "Marunouchi, Chiyoda City",
            latitude = 35.6812,
            longitude = 139.7671,
            alertDistanceMeters = 1000,
            transportMode = TransportMode.METRO.name,
            status = "COMPLETED",
            startedAt = 1700000000000L,
            completedAt = 1700003600000L,
            distanceRemainingMeters = 0,
            etaMinutes = 0,
            temperatureCelsius = 18,
            weatherCondition = "Partly Cloudy",
            weatherRecommendation = "Ideal commute weather",
            durationMinutes = 60,
            departurePoint = "Shinjuku Hub",
            travelTips = "Use Yamanote line transfer; IC card tap at South Gate."
        )

        assertEquals("Tokyo Station", trip.title)
        assertEquals(60, trip.durationMinutes)
        assertEquals("Shinjuku Hub", trip.departurePoint)
        assertEquals("Partly Cloudy", trip.weatherCondition)
        assertEquals(18, trip.temperatureCelsius)
        assertTrue(trip.travelTips.contains("Yamanote line"))
    }

    @Test
    fun `test battery saving mode adaptive tier configuration`() {
        val infoFar = BatterySaverInfo(
            isEnabled = true,
            currentTierName = "Eco Deep Sleep",
            pollingIntervalSeconds = 45,
            estimatedBatterySavedPercent = 75,
            tierDescription = "Far from stop (>10 km): Polling GPS every 45s (~75% battery saved)"
        )
        assertTrue(infoFar.isEnabled)
        assertEquals(45, infoFar.pollingIntervalSeconds)
        assertEquals(75, infoFar.estimatedBatterySavedPercent)

        val infoNear = BatterySaverInfo(
            isEnabled = true,
            currentTierName = "High Precision Radar",
            pollingIntervalSeconds = 3,
            estimatedBatterySavedPercent = 0,
            tierDescription = "Approaching stop (<=3 km): High-frequency radar active"
        )
        assertEquals(3, infoNear.pollingIntervalSeconds)
    }
}

