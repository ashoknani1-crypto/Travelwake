package com.example.travelwake.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.BatteryChargingFull
import androidx.compose.material.icons.filled.Lightbulb
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.material.icons.filled.Mic
import com.example.travelwake.data.model.JourneyStatus
import com.example.travelwake.engine.LocationEngine
import com.example.travelwake.ui.components.BelongingsChecklistCard
import com.example.travelwake.ui.components.GlassCard
import com.example.travelwake.ui.components.HeroDistanceCard
import com.example.travelwake.ui.components.JourneyProgressTimelineCard
import com.example.travelwake.ui.components.OfflineMapDownloadCard
import com.example.travelwake.ui.components.SevereWeatherAlertCard
import com.example.travelwake.ui.components.TravelWakeButton
import com.example.travelwake.ui.components.VoiceCommandDialog
import com.example.travelwake.ui.components.WeatherRecommendationCard
import com.example.travelwake.ui.map.RouteMapFragmentComponent
import com.example.travelwake.viewmodel.TravelWakeViewModel
import com.example.ui.theme.AlarmRed
import com.example.ui.theme.AlertAmber
import com.example.ui.theme.AlertAmberContainer
import com.example.ui.theme.BrightCyan
import com.example.ui.theme.ProfessionalBackground
import com.example.ui.theme.ProfessionalBorder
import com.example.ui.theme.ProfessionalPrimary
import com.example.ui.theme.ProfessionalPrimaryContainer
import com.example.ui.theme.ProfessionalSurface
import com.example.ui.theme.ProfessionalSurfaceVariant
import com.example.ui.theme.ProfessionalTextPrimary
import com.example.ui.theme.ProfessionalTextSecondary
import com.example.ui.theme.SafetyGreen
import com.example.ui.theme.SafetyGreenContainer

@Composable
fun ActiveJourneyScreen(
    viewModel: TravelWakeViewModel,
    onNavigateToAlarm: () -> Unit,
    onBack: () -> Unit,
    onEndJourney: () -> Unit,
    modifier: Modifier = Modifier
) {
    val destination by viewModel.selectedDestination.collectAsState()
    val distance by viewModel.remainingDistanceMeters.collectAsState()
    val alertDistance by viewModel.alertDistanceMeters.collectAsState()
    val etaMinutes by viewModel.etaMinutes.collectAsState()
    val weather by viewModel.destinationWeather.collectAsState()
    val belongings by viewModel.belongings.collectAsState()
    val preAlarmMessage by viewModel.preAlarmMessage.collectAsState()
    val journeyState by viewModel.journeyState.collectAsState()
    val userLat by viewModel.currentUserLatitude.collectAsState()
    val userLon by viewModel.currentUserLongitude.collectAsState()
    val originLat by viewModel.originLatitude.collectAsState()
    val originLon by viewModel.originLongitude.collectAsState()
    val departurePoint by viewModel.departurePoint.collectAsState()
    val transportMode by viewModel.transportMode.collectAsState()
    val weatherTheme by viewModel.weatherTheme.collectAsState()
    val autoWeatherTheme by viewModel.autoWeatherThemeEnabled.collectAsState()
    val destinationTravelTips by viewModel.destinationTravelTips.collectAsState()
    val isLoadingTips by viewModel.isLoadingTravelTips.collectAsState()
    val batterySaverEnabled by viewModel.batterySaverEnabled.collectAsState()
    val batterySaverInfo by viewModel.batterySaverInfo.collectAsState()

    // If journey enters ALARMING state, automatically show Alarm Screen
    if (journeyState == JourneyStatus.ALARMING) {
        onNavigateToAlarm()
    }

    val isApproaching = distance <= alertDistance || journeyState == JourneyStatus.APPROACHING
    val scrollState = rememberScrollState()
    var showVoiceDialog by remember { mutableStateOf(false) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(weatherTheme.backgroundColor)
            .verticalScroll(scrollState)
            .padding(horizontal = 20.dp, vertical = 12.dp)
            .testTag("active_journey_screen")
    ) {
        // App Bar
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(
                    onClick = onBack,
                    modifier = Modifier.testTag("active_back_button")
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back",
                        tint = ProfessionalTextPrimary
                    )
                }
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "Live Journey",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = ProfessionalTextPrimary
                )
            }

            // Live status badge & Voice Assistant button
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(
                    onClick = { showVoiceDialog = true },
                    modifier = Modifier.testTag("active_voice_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.Mic,
                        contentDescription = "Hands-free Voice Commands",
                        tint = weatherTheme.primaryColor
                    )
                }

                Spacer(modifier = Modifier.width(4.dp))

                Surface(
                    color = if (isApproaching) AlertAmberContainer else SafetyGreenContainer,
                    shape = RoundedCornerShape(12.dp),
                    border = BorderStroke(1.dp, if (isApproaching) AlertAmber.copy(alpha = 0.4f) else SafetyGreen.copy(alpha = 0.4f))
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(8.dp)
                                .clip(CircleShape)
                                .background(if (isApproaching) AlertAmber else SafetyGreen)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = if (isApproaching) "APPROACHING" else "GPS ACTIVE",
                            style = MaterialTheme.typography.labelSmall,
                            color = if (isApproaching) AlertAmber else SafetyGreen,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 0.5.sp
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Pre-Alarm progressive alert banner
        if (!preAlarmMessage.isNullOrBlank()) {
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(20.dp))
                    .border(BorderStroke(1.dp, AlertAmber), RoundedCornerShape(20.dp))
                    .testTag("pre_alarm_banner"),
                color = AlertAmberContainer,
                shape = RoundedCornerShape(20.dp)
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Warning,
                        contentDescription = "Pre-Alarm",
                        tint = AlertAmber
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = preAlarmMessage ?: "",
                        style = MaterialTheme.typography.titleSmall,
                        color = AlertAmber,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
            Spacer(modifier = Modifier.height(14.dp))
        }

        // Real-Time Severe Weather Warning Card for Destination
        SevereWeatherAlertCard(
            viewModel = viewModel,
            modifier = Modifier.padding(bottom = 14.dp)
        )

        // Dynamic Weather Theme Ambiance Indicator
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(18.dp))
                .border(BorderStroke(1.dp, weatherTheme.borderColor), RoundedCornerShape(18.dp))
                .testTag("weather_theme_ambiance_indicator"),
            color = weatherTheme.surfaceColor,
            shape = RoundedCornerShape(18.dp)
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(text = weatherTheme.themeType.iconEmoji, fontSize = 20.sp)
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = "${weatherTheme.themeType.displayName} Palette",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            color = weatherTheme.primaryColor
                        )
                        Text(
                            text = "Auto-adapted to ${weather.condition} (${weather.temperatureCelsius}°C)",
                            style = MaterialTheme.typography.bodySmall,
                            color = ProfessionalTextSecondary
                        )
                    }
                }

                Surface(
                    color = weatherTheme.primaryContainerColor,
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Text(
                        text = if (autoWeatherTheme) "ACTIVE" else "MANUAL",
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        style = MaterialTheme.typography.labelSmall,
                        color = weatherTheme.primaryColor,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Route Map View Fragment (Live GPS route visualization with offline tile support)
        RouteMapFragmentComponent(
            userLat = userLat,
            userLon = userLon,
            destLat = destination?.latitude ?: 18.5289,
            destLon = destination?.longitude ?: 73.8744,
            destName = destination?.name ?: "Destination",
            alertRadiusMeters = alertDistance,
            remainingDistanceMeters = distance
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Offline Map Tile Downloader & Cache status for destination
        OfflineMapDownloadCard(
            viewModel = viewModel,
            destinationName = destination?.name ?: "Destination Region",
            destLat = destination?.latitude ?: userLat,
            destLon = destination?.longitude ?: userLon,
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Visual Progress Bar & Journey Timeline (Calculates GPS completion % vs destination)
        JourneyProgressTimelineCard(
            originLat = originLat,
            originLon = originLon,
            departureName = departurePoint.ifBlank { "Departure Point" },
            currentLat = userLat,
            currentLon = userLon,
            destLat = destination?.latitude ?: 18.5289,
            destLon = destination?.longitude ?: 73.8744,
            destinationName = destination?.name ?: "Destination",
            remainingDistanceMeters = distance,
            alertDistanceMeters = alertDistance,
            etaMinutes = etaMinutes,
            transportMode = transportMode,
            modifier = Modifier.fillMaxWidth().testTag("journey_progress_timeline_card")
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Hero Distance Card (Animated, dominant typography)
        HeroDistanceCard(
            distanceMeters = distance,
            destinationName = destination?.name ?: "En Route",
            etaMinutes = etaMinutes,
            isApproaching = isApproaching
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Adaptive Battery-Saving Mode Card
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(20.dp))
                .border(
                    BorderStroke(
                        1.dp,
                        if (batterySaverEnabled) SafetyGreen.copy(alpha = 0.5f) else ProfessionalBorder
                    ),
                    RoundedCornerShape(20.dp)
                )
                .testTag("battery_saver_card"),
            color = if (batterySaverEnabled) SafetyGreenContainer.copy(alpha = 0.25f) else ProfessionalSurface,
            shape = RoundedCornerShape(20.dp),
            shadowElevation = 1.dp
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(14.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f)
                ) {
                    Box(
                        modifier = Modifier
                            .size(38.dp)
                            .clip(CircleShape)
                            .background(if (batterySaverEnabled) SafetyGreenContainer else ProfessionalSurfaceVariant),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.BatteryChargingFull,
                            contentDescription = "Battery Saver",
                            tint = if (batterySaverEnabled) SafetyGreen else ProfessionalTextSecondary,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = "Adaptive Battery Saver",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            color = ProfessionalTextPrimary
                        )
                        Text(
                            text = if (batterySaverEnabled) batterySaverInfo.tierDescription.ifBlank { "Adaptive GPS polling active" } else "Disabled (Standard frequent polling)",
                            style = MaterialTheme.typography.bodySmall,
                            color = if (batterySaverEnabled) SafetyGreen else ProfessionalTextSecondary
                        )
                    }
                }

                Switch(
                    checked = batterySaverEnabled,
                    onCheckedChange = { viewModel.toggleBatterySavingMode() },
                    modifier = Modifier.testTag("battery_saver_toggle")
                )
            }
        }

        Spacer(modifier = Modifier.height(18.dp))

        // Destination Weather Safety Card
        WeatherRecommendationCard(
            weather = weather,
            destinationName = destination?.name ?: "Destination"
        )

        Spacer(modifier = Modifier.height(18.dp))

        // Destination-Specific Gemini Travel Tips Card
        if (destinationTravelTips.isNotBlank() || isLoadingTips) {
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(20.dp))
                    .border(BorderStroke(1.dp, weatherTheme.borderColor), RoundedCornerShape(20.dp))
                    .testTag("gemini_destination_tips_card"),
                color = weatherTheme.surfaceColor,
                shape = RoundedCornerShape(20.dp),
                shadowElevation = 1.dp
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(weatherTheme.primaryContainerColor),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Lightbulb,
                                    contentDescription = null,
                                    tint = weatherTheme.primaryColor,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text(
                                    text = "Gemini Travel Tips & Safety",
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = weatherTheme.primaryColor
                                )
                                Text(
                                    text = "Local transit, customs & emergency contacts",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = ProfessionalTextSecondary
                                )
                            }
                        }

                        IconButton(
                            onClick = {
                                destination?.name?.let { viewModel.fetchDestinationTravelTips(it) }
                            },
                            modifier = Modifier.testTag("refresh_travel_tips_button")
                        ) {
                            Icon(
                                imageVector = Icons.Default.Refresh,
                                contentDescription = "Refresh Tips",
                                tint = weatherTheme.primaryColor,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    if (isLoadingTips) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(vertical = 8.dp)
                        ) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(16.dp),
                                strokeWidth = 2.dp,
                                color = weatherTheme.primaryColor
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Consulting Gemini for destination travel advice...",
                                style = MaterialTheme.typography.bodySmall,
                                color = ProfessionalTextSecondary
                            )
                        }
                    } else {
                        Text(
                            text = destinationTravelTips,
                            style = MaterialTheme.typography.bodySmall,
                            color = ProfessionalTextPrimary,
                            lineHeight = 18.sp
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(18.dp))
        }

        Spacer(modifier = Modifier.height(18.dp))

        // Belongings Checklist
        BelongingsChecklistCard(
            belongings = belongings,
            onToggleItem = { viewModel.toggleBelonging(it) }
        )

        Spacer(modifier = Modifier.height(20.dp))

        // Safety Lab / GPS Simulation Controls
        GlassCard(
            modifier = Modifier.fillMaxWidth().testTag("simulation_controls_card"),
            cornerRadius = 24
        ) {
            Column {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "🛠️ Journey Simulation Lab",
                        style = MaterialTheme.typography.labelLarge,
                        color = ProfessionalPrimary,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Threshold: ${LocationEngine.formatDistance(alertDistance)}",
                        style = MaterialTheme.typography.labelSmall,
                        color = ProfessionalTextSecondary
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedButton(
                        onClick = { viewModel.simulateAdvanceCloser(500) },
                        modifier = Modifier.weight(1f).testTag("sim_step_button"),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = ProfessionalTextPrimary),
                        border = BorderStroke(1.dp, ProfessionalBorder)
                    ) {
                        Text("-500m Closer", style = MaterialTheme.typography.labelMedium)
                    }

                    OutlinedButton(
                        onClick = { viewModel.simulateAdvanceCloser(200) },
                        modifier = Modifier.weight(1f).testTag("sim_step_200_button"),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = ProfessionalTextPrimary),
                        border = BorderStroke(1.dp, ProfessionalBorder)
                    ) {
                        Text("-200m", style = MaterialTheme.typography.labelMedium)
                    }

                    OutlinedButton(
                        onClick = { viewModel.simulateTriggerAlarm() },
                        modifier = Modifier.weight(1f).testTag("sim_trigger_alarm_button"),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = AlertAmber),
                        border = BorderStroke(1.dp, AlertAmber.copy(alpha = 0.6f))
                    ) {
                        Text("Trigger Alarm", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold)
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedButton(
                        onClick = { viewModel.simulateAutoTravel(speedMultiplier = 2) },
                        modifier = Modifier.weight(1f).testTag("sim_auto_travel_button"),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = ProfessionalPrimary),
                        border = BorderStroke(1.dp, ProfessionalPrimary.copy(alpha = 0.5f))
                    ) {
                        Icon(imageVector = Icons.Default.PlayArrow, contentDescription = "Play", modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Auto Approach", style = MaterialTheme.typography.labelMedium)
                    }

                    OutlinedButton(
                        onClick = { viewModel.stopSimulation() },
                        modifier = Modifier.weight(1f).testTag("sim_stop_button"),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = ProfessionalTextSecondary),
                        border = BorderStroke(1.dp, ProfessionalBorder)
                    ) {
                        Text("Pause Sim", style = MaterialTheme.typography.labelMedium)
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedButton(
                    onClick = {
                        viewModel.triggerSimulatedSevereWeather(
                            hazard = "Severe Thunderstorm Warning",
                            headline = "Severe storm with localized flash flooding near arrival station",
                            emergencyAction = "Remain inside underground transit hub; wait for storm to pass"
                        )
                    },
                    modifier = Modifier.fillMaxWidth().testTag("sim_severe_weather_button"),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = AlertAmber),
                    border = BorderStroke(1.dp, AlertAmber.copy(alpha = 0.5f))
                ) {
                    Text("⛈️ Push Severe Weather Alert to Alarm", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold)
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // END JOURNEY Button
        TravelWakeButton(
            text = "END JOURNEY",
            onClick = {
                viewModel.endJourney()
                onEndJourney()
            },
            containerColor = AlarmRed,
            contentColor = Color.White,
            testTag = "end_journey_button"
        )

        Spacer(modifier = Modifier.height(30.dp))
    }

    if (showVoiceDialog) {
        VoiceCommandDialog(
            viewModel = viewModel,
            onDismiss = { showVoiceDialog = false }
        )
    }
}
