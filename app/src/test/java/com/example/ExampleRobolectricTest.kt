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

    @Test
    fun `test custom distance formatting and thresholds`() {
        val customDistanceMeters = 350
        val formatted = LocationEngine.formatDistance(customDistanceMeters)
        assertEquals("350 m", formatted)

        val customFarMeters = 7500
        val formattedFar = LocationEngine.formatDistance(customFarMeters)
        assertEquals("7.5 km", formattedFar)
    }

    @Test
    fun `test glassmorphism theme configuration and colors`() {
        val glassPalette = com.example.travelwake.theme.WeatherThemeEngine.GlassmorphismPalette
        assertNotNull(glassPalette)
        assertEquals(com.example.travelwake.theme.WeatherThemeType.GLASSMORPHISM, glassPalette.themeType)
        assertEquals(com.example.ui.theme.BrightCyan, glassPalette.primaryColor)
        assertEquals(com.example.ui.theme.DeepNavyDark, glassPalette.backgroundColor)
        assertEquals(com.example.ui.theme.DarkNavySurface, glassPalette.surfaceColor)
        assertEquals(com.example.ui.theme.GlassNavyBorder, glassPalette.borderColor)
    }

    @Test
    fun `test user profile with provider and photo url`() {
        val profile = com.example.travelwake.data.model.UserProfile(
            uid = "google_user_123",
            email = "ashokmuddam5@gmail.com",
            displayName = "Ashok",
            photoUrl = "https://lh3.googleusercontent.com/a/photo",
            isAnonymous = false,
            provider = "google.com"
        )
        assertEquals("google_user_123", profile.uid)
        assertEquals("ashokmuddam5@gmail.com", profile.email)
        assertEquals("Ashok", profile.displayName)
        assertEquals("https://lh3.googleusercontent.com/a/photo", profile.photoUrl)
        assertEquals(false, profile.isAnonymous)
        assertEquals("google.com", profile.provider)
    }

    @Test
    fun `test custom material 3 glassmorphism light and dark color schemes`() {
        val lightScheme = com.example.ui.theme.TravelWakeGlassmorphicLightColorScheme
        val darkScheme = com.example.ui.theme.TravelWakeGlassmorphicDarkColorScheme

        assertNotNull(lightScheme)
        assertNotNull(darkScheme)

        // Verify deep navy primary colors
        assertEquals(com.example.ui.theme.DeepNavyMidnight, lightScheme.primary)
        assertEquals(com.example.ui.theme.BrightCyan, darkScheme.primary)

        // Verify translucent surface backgrounds
        assertEquals(androidx.compose.ui.graphics.Color(0xF2FFFFFF), lightScheme.surface)
        assertEquals(com.example.ui.theme.GlassNavySurface, darkScheme.surface)
        assertEquals(com.example.ui.theme.GlassNavyCard, darkScheme.surfaceVariant)

        // Verify deep navy dark background
        assertEquals(com.example.ui.theme.DeepNavyDark, darkScheme.background)
    }

    @Test
    fun `test weather repository graceful fallback implementation`() {
        val weatherRepo = com.example.travelwake.data.repository.WeatherRepository()
        val fallback = weatherRepo.getFallbackWeather("Berlin Central", 52.5200, 13.4050)
        assertNotNull(fallback)
        assertTrue(fallback.temperatureCelsius in -10..45)
        assertNotNull(fallback.condition)
        assertTrue(fallback.recommendations.isNotEmpty())
    }

    @Test
    fun `test notification manager channels and instance creation`() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val notifManager = com.example.travelwake.notification.TravelWakeNotificationManager.getInstance(context)
        assertNotNull(notifManager)

        // Verify channel creation
        val systemNotificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as android.app.NotificationManager
        val scheduledChannel = systemNotificationManager.getNotificationChannel(
            com.example.travelwake.notification.TravelWakeNotificationManager.CHANNEL_SCHEDULED_ALERTS
        )
        assertNotNull(scheduledChannel)
        assertEquals(android.app.NotificationManager.IMPORTANCE_HIGH, scheduledChannel.importance)

        val progressiveChannel = systemNotificationManager.getNotificationChannel(
            com.example.travelwake.notification.TravelWakeNotificationManager.CHANNEL_PROGRESSIVE_ALARMS
        )
        assertNotNull(progressiveChannel)
        assertEquals(android.app.NotificationManager.IMPORTANCE_HIGH, progressiveChannel.importance)
    }

    @Test
    fun `test offline-first room repositories for essentials, tasks, and packing items`() = kotlinx.coroutines.runBlocking {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val db = com.example.travelwake.data.local.AppDatabase.getInstance(context)

        val todoRepo = com.example.travelwake.data.repository.TodoRepository(db.todoDao())
        val packRepo = com.example.travelwake.data.repository.PackItemRepository(db.packItemDao())

        // Insert and read todo item
        val todo = todoRepo.addTodo("Verify Passport and Visa", isPreTrip = true, priority = 1)
        assertNotNull(todo.id)
        val todos = todoRepo.allTodos
        assertNotNull(todos)

        // Insert and read pack item
        val packItem = packRepo.addPackItem("Power Bank 20,000mAh", "Electronics", essential = true)
        assertNotNull(packItem.id)
        assertTrue(packItem.essential)

        // Toggle packed state
        packRepo.togglePacked(packItem.id, true)
    }
}

