package com.example

import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onRoot
import com.example.travelwake.data.model.WeatherInfo
import com.example.travelwake.data.model.WeatherRecommendation
import com.example.travelwake.ui.components.HeroDistanceCard
import com.example.travelwake.ui.components.WeatherRecommendationCard
import com.example.ui.theme.MyApplicationTheme
import com.github.takahirom.roborazzi.RobolectricDeviceQualifiers
import com.github.takahirom.roborazzi.captureRoboImage
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode

@RunWith(RobolectricTestRunner::class)
@GraphicsMode(GraphicsMode.Mode.LEGACY)
@Config(qualifiers = RobolectricDeviceQualifiers.Pixel8, sdk = [34])
class GreetingScreenshotTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun hero_distance_screenshot() {
        composeTestRule.setContent {
            MyApplicationTheme(darkTheme = true) {
                HeroDistanceCard(
                    distanceMeters = 3200,
                    destinationName = "Pune Central Station",
                    etaMinutes = 12,
                    isApproaching = false
                )
            }
        }
        composeTestRule.waitForIdle()
        composeTestRule.onRoot().captureRoboImage(filePath = "src/test/screenshots/hero_distance.png")
    }

    @Test
    fun weather_recommendation_screenshot() {
        composeTestRule.setContent {
            MyApplicationTheme(darkTheme = true) {
                WeatherRecommendationCard(
                    weather = WeatherInfo(
                        temperatureCelsius = 24,
                        condition = "Scattered Showers",
                        rainProbability = 65,
                        recommendations = listOf(
                            WeatherRecommendation("Carry Umbrella", "☂", description = "Keep compact umbrella ready")
                        )
                    ),
                    destinationName = "Airport Terminal 2"
                )
            }
        }
        composeTestRule.waitForIdle()
        composeTestRule.onRoot().captureRoboImage(filePath = "src/test/screenshots/weather_recommendation.png")
    }
}
