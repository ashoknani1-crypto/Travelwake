package com.example.travelwake.ui.screens

import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
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
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.BookmarkBorder
import androidx.compose.material.icons.filled.Cloud
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Vibration
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.travelwake.ui.components.AlertDistanceSelector
import com.example.travelwake.ui.components.GeminiPackingAssistantCard
import com.example.travelwake.ui.components.GlassCard
import com.example.travelwake.ui.components.TransportModeSelector
import com.example.travelwake.ui.components.TravelWakeButton
import com.example.travelwake.ui.components.WeatherRecommendationCard
import com.example.travelwake.viewmodel.TravelWakeViewModel
import com.example.ui.theme.ProfessionalBackground
import com.example.ui.theme.ProfessionalBorder
import com.example.ui.theme.ProfessionalDivider
import com.example.ui.theme.ProfessionalOnPrimaryContainer
import com.example.ui.theme.ProfessionalPrimary
import com.example.ui.theme.ProfessionalPrimaryContainer
import com.example.ui.theme.ProfessionalSurface
import com.example.ui.theme.ProfessionalSurfaceVariant
import com.example.ui.theme.ProfessionalTextMuted
import com.example.ui.theme.ProfessionalTextPrimary
import com.example.ui.theme.ProfessionalTextSecondary
import com.example.ui.theme.SafetyGreen
import com.example.ui.theme.SafetyGreenContainer
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun JourneySetupScreen(
    viewModel: TravelWakeViewModel,
    onStartJourney: () -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val destination by viewModel.selectedDestination.collectAsState()
    val alertDistance by viewModel.alertDistanceMeters.collectAsState()
    val transportMode by viewModel.transportMode.collectAsState()
    val etaMinutes by viewModel.etaMinutes.collectAsState()
    val weather by viewModel.destinationWeather.collectAsState()
    val arrivalForecast by viewModel.arrivalWeatherForecast.collectAsState()
    val belongings by viewModel.belongings.collectAsState()

    val departurePoint by viewModel.departurePoint.collectAsState()
    val scheduledArrivalTime by viewModel.scheduledArrivalTime.collectAsState()
    val arrivalTimeFormatted by viewModel.arrivalTimeFormatted.collectAsState()

    val tripDuration by viewModel.tripDurationText.collectAsState()
    val geminiSuggestions by viewModel.geminiPackingSuggestions.collectAsState()
    val isGeneratingPacking by viewModel.isGeneratingPacking.collectAsState()
    val geminiNotice by viewModel.geminiPackingNotice.collectAsState()

    val soundEnabled by viewModel.alarmSoundEnabled.collectAsState()
    val vibrationEnabled by viewModel.alarmVibrationEnabled.collectAsState()
    val progressiveEnabled by viewModel.progressiveAlarmEnabled.collectAsState()

    var isEditingDeparture by remember { mutableStateOf(false) }
    var departureInput by remember { mutableStateOf(departurePoint) }
    var isSavedToRoom by remember { mutableStateOf(false) }
    var newBelongingInput by remember { mutableStateOf("") }

    val scrollState = rememberScrollState()

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(ProfessionalBackground)
            .verticalScroll(scrollState)
            .padding(horizontal = 20.dp, vertical = 12.dp)
            .testTag("journey_setup_screen")
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
                    modifier = Modifier.testTag("setup_back_button")
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back",
                        tint = ProfessionalTextPrimary
                    )
                }
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Journey Setup",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = ProfessionalTextPrimary
                )
            }

            // Save to Room Database quick button
            IconButton(
                onClick = {
                    viewModel.saveCurrentTravelDestination("Saved from Journey Setup")
                    isSavedToRoom = true
                    Toast.makeText(context, "Destination saved to Room database!", Toast.LENGTH_SHORT).show()
                },
                modifier = Modifier.testTag("save_destination_icon_button")
            ) {
                Icon(
                    imageVector = if (isSavedToRoom) Icons.Default.Bookmark else Icons.Default.BookmarkBorder,
                    contentDescription = "Save Destination",
                    tint = if (isSavedToRoom) ProfessionalPrimary else ProfessionalTextSecondary
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Selected Destination Overview Card styled after Professional Polish Hero
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(24.dp))
                .border(BorderStroke(1.dp, ProfessionalBorder), RoundedCornerShape(24.dp))
                .testTag("destination_overview_card"),
            color = ProfessionalPrimaryContainer,
            shape = RoundedCornerShape(24.dp),
            shadowElevation = 2.dp
        ) {
            Column(modifier = Modifier.padding(20.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "TARGET DESTINATION",
                            style = MaterialTheme.typography.labelSmall,
                            color = ProfessionalOnPrimaryContainer,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.sp
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = destination?.name ?: "Selected Destination",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = ProfessionalOnPrimaryContainer
                        )
                        Text(
                            text = destination?.address ?: "Transit stop address",
                            style = MaterialTheme.typography.bodySmall,
                            color = ProfessionalOnPrimaryContainer.copy(alpha = 0.8f)
                        )
                    }
                    Surface(
                        color = ProfessionalSurface,
                        border = BorderStroke(1.dp, ProfessionalBorder),
                        shape = RoundedCornerShape(14.dp)
                    ) {
                        Text(
                            text = "${destination?.distanceKm ?: 8.4f} km",
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            color = ProfessionalPrimary
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(14.dp))
                        .background(ProfessionalSurface)
                        .border(BorderStroke(1.dp, ProfessionalBorder), RoundedCornerShape(14.dp))
                        .padding(horizontal = 14.dp, vertical = 10.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Estimated transit time:",
                        style = MaterialTheme.typography.bodySmall,
                        color = ProfessionalTextSecondary
                    )
                    Text(
                        text = "~$etaMinutes min via ${transportMode.label}",
                        style = MaterialTheme.typography.labelLarge,
                        color = ProfessionalPrimary,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Departure Point & Scheduled Arrival Time Card
        Text(
            text = "Route & Arrival Schedule",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = ProfessionalTextPrimary
        )
        Spacer(modifier = Modifier.height(10.dp))

        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(24.dp))
                .border(BorderStroke(1.dp, ProfessionalBorder), RoundedCornerShape(24.dp))
                .testTag("departure_arrival_card"),
            color = ProfessionalSurface,
            shape = RoundedCornerShape(24.dp),
            shadowElevation = 1.dp
        ) {
            Column(modifier = Modifier.padding(18.dp)) {
                // Departure Point Section
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(ProfessionalSurfaceVariant),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.LocationOn,
                                contentDescription = null,
                                tint = ProfessionalPrimary,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = "Departure Point",
                                style = MaterialTheme.typography.labelSmall,
                                color = ProfessionalTextSecondary
                            )
                            Text(
                                text = departurePoint,
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.SemiBold,
                                color = ProfessionalTextPrimary
                            )
                        }
                    }

                    Text(
                        text = if (isEditingDeparture) "Done" else "Edit",
                        style = MaterialTheme.typography.labelLarge,
                        color = ProfessionalPrimary,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier
                            .clickable {
                                if (isEditingDeparture && departureInput.isNotBlank()) {
                                    viewModel.setDeparturePoint(departureInput)
                                }
                                isEditingDeparture = !isEditingDeparture
                            }
                            .padding(6.dp)
                    )
                }

                if (isEditingDeparture) {
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = departureInput,
                        onValueChange = { departureInput = it },
                        placeholder = { Text("Enter departure stop/station") },
                        modifier = Modifier.fillMaxWidth().testTag("departure_input_field"),
                        singleLine = true,
                        shape = RoundedCornerShape(14.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = ProfessionalPrimary,
                            unfocusedBorderColor = ProfessionalBorder
                        )
                    )
                }

                Spacer(modifier = Modifier.height(14.dp))
                HorizontalDivider(color = ProfessionalDivider, thickness = 1.dp)
                Spacer(modifier = Modifier.height(14.dp))

                // Scheduled Arrival Time Section
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(ProfessionalSurfaceVariant),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.AccessTime,
                                contentDescription = null,
                                tint = ProfessionalPrimary,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = "Scheduled Arrival Time",
                                style = MaterialTheme.typography.labelSmall,
                                color = ProfessionalTextSecondary
                            )
                            Text(
                                text = arrivalTimeFormatted.ifBlank { "08:45 AM" },
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = ProfessionalPrimary
                            )
                        }
                    }

                    Surface(
                        color = SafetyGreenContainer,
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Text(
                            text = "Auto-Synced",
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                            style = MaterialTheme.typography.labelSmall,
                            color = SafetyGreen,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Quick Arrival Adjustment Chips
                Text(
                    text = "Quick Adjust Arrival Time:",
                    style = MaterialTheme.typography.labelSmall,
                    color = ProfessionalTextSecondary
                )
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    listOf(15, 30, 45, 60).forEach { mins ->
                        val targetMs = System.currentTimeMillis() + (mins * 60_000L)
                        val formattedTime = SimpleDateFormat("hh:mm a", Locale.getDefault()).format(Date(targetMs))
                        Surface(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(12.dp))
                                .border(BorderStroke(1.dp, ProfessionalBorder), RoundedCornerShape(12.dp))
                                .clickable {
                                    viewModel.setArrivalTime(targetMs, formattedTime)
                                }
                                .testTag("chip_adjust_time_${mins}m"),
                            color = ProfessionalSurfaceVariant,
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Text(
                                text = "+$mins min",
                                modifier = Modifier.padding(vertical = 8.dp),
                                textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.SemiBold,
                                color = ProfessionalTextPrimary
                            )
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Transport Mode Selector
        Text(
            text = "Transport Mode",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = ProfessionalTextPrimary
        )
        Spacer(modifier = Modifier.height(10.dp))
        TransportModeSelector(
            selectedMode = transportMode,
            onModeSelected = { viewModel.setTransportMode(it) }
        )

        Spacer(modifier = Modifier.height(20.dp))

        // Alarm Settings Card (Distance, Sound, Vibration, Progressive)
        Text(
            text = "Alarm & Notification Settings",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = ProfessionalTextPrimary
        )
        Spacer(modifier = Modifier.height(10.dp))

        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(24.dp))
                .border(BorderStroke(1.dp, ProfessionalBorder), RoundedCornerShape(24.dp))
                .testTag("alarm_settings_card"),
            color = ProfessionalSurface,
            shape = RoundedCornerShape(24.dp),
            shadowElevation = 1.dp
        ) {
            Column(modifier = Modifier.padding(18.dp)) {
                // Alert Distance Selector
                AlertDistanceSelector(
                    selectedMeters = alertDistance,
                    onDistanceSelected = { viewModel.setAlertDistance(it) }
                )

                Spacer(modifier = Modifier.height(16.dp))
                HorizontalDivider(color = ProfessionalDivider, thickness = 1.dp)
                Spacer(modifier = Modifier.height(14.dp))

                // Alarm Sound Switch
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.VolumeUp,
                            contentDescription = null,
                            tint = if (soundEnabled) ProfessionalPrimary else ProfessionalTextMuted,
                            modifier = Modifier.size(22.dp)
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                text = "Loud Wake-Up Alarm",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.SemiBold,
                                color = ProfessionalTextPrimary
                            )
                            Text(
                                text = "Overrides silent mode when stop approaches",
                                style = MaterialTheme.typography.bodySmall,
                                color = ProfessionalTextSecondary
                            )
                        }
                    }
                    Switch(
                        checked = soundEnabled,
                        onCheckedChange = {
                            viewModel.setAlarmSettings(alertDistance, it, vibrationEnabled, progressiveEnabled)
                        },
                        colors = SwitchDefaults.colors(checkedThumbColor = Color.White, checkedTrackColor = ProfessionalPrimary),
                        modifier = Modifier.testTag("alarm_sound_switch")
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Vibration Switch
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Vibration,
                            contentDescription = null,
                            tint = if (vibrationEnabled) ProfessionalPrimary else ProfessionalTextMuted,
                            modifier = Modifier.size(22.dp)
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                text = "Urgent Haptic Vibration",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.SemiBold,
                                color = ProfessionalTextPrimary
                            )
                            Text(
                                text = "Repeated pulse pattern for deep sleep",
                                style = MaterialTheme.typography.bodySmall,
                                color = ProfessionalTextSecondary
                            )
                        }
                    }
                    Switch(
                        checked = vibrationEnabled,
                        onCheckedChange = {
                            viewModel.setAlarmSettings(alertDistance, soundEnabled, it, progressiveEnabled)
                        },
                        colors = SwitchDefaults.colors(checkedThumbColor = Color.White, checkedTrackColor = ProfessionalPrimary),
                        modifier = Modifier.testTag("alarm_vibration_switch")
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Progressive Pre-Alarm Warnings Switch
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Notifications,
                            contentDescription = null,
                            tint = if (progressiveEnabled) ProfessionalPrimary else ProfessionalTextMuted,
                            modifier = Modifier.size(22.dp)
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                text = "Progressive Pre-Alarm Chimes",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.SemiBold,
                                color = ProfessionalTextPrimary
                            )
                            Text(
                                text = "Gentle heads-up at 1 km and 750 m",
                                style = MaterialTheme.typography.bodySmall,
                                color = ProfessionalTextSecondary
                            )
                        }
                    }
                    Switch(
                        checked = progressiveEnabled,
                        onCheckedChange = {
                            viewModel.setAlarmSettings(alertDistance, soundEnabled, vibrationEnabled, it)
                        },
                        colors = SwitchDefaults.colors(checkedThumbColor = Color.White, checkedTrackColor = ProfessionalPrimary),
                        modifier = Modifier.testTag("alarm_progressive_switch")
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Destination Weather & Forecast at Arrival Time
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Arrival Weather Forecast",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = ProfessionalTextPrimary
            )

            Surface(
                color = ProfessionalSurfaceVariant,
                shape = RoundedCornerShape(10.dp)
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Cloud,
                        contentDescription = null,
                        tint = ProfessionalPrimary,
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "Open-Meteo API",
                        style = MaterialTheme.typography.labelSmall,
                        color = ProfessionalTextSecondary
                    )
                }
            }
        }
        Spacer(modifier = Modifier.height(10.dp))

        WeatherRecommendationCard(
            weather = weather,
            destinationName = destination?.name ?: "Destination"
        )

        Spacer(modifier = Modifier.height(20.dp))

        // Gemini AI Smart Packing Assistant Card
        GeminiPackingAssistantCard(
            destinationName = destination?.name ?: "Destination",
            weather = weather,
            tripDuration = tripDuration,
            suggestions = geminiSuggestions,
            isLoading = isGeneratingPacking,
            notice = geminiNotice,
            onDurationChange = { viewModel.setTripDurationText(it) },
            onGenerate = { viewModel.generatePackingSuggestionsWithGemini() },
            onToggleSuggestion = { viewModel.toggleSuggestionSelection(it) },
            onAddSuggestion = { viewModel.addSuggestedItemToBelongings(it) },
            onAddAllSelected = { viewModel.addAllSelectedSuggestionsToBelongings() }
        )

        Spacer(modifier = Modifier.height(20.dp))

        // Dynamic Belongings Checklist Card
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(24.dp))
                .border(BorderStroke(1.dp, ProfessionalBorder), RoundedCornerShape(24.dp))
                .testTag("belongings_setup_card"),
            color = ProfessionalSurface,
            shape = RoundedCornerShape(24.dp),
            shadowElevation = 1.dp
        ) {
            Column(modifier = Modifier.padding(18.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "Belongings Checklist",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = ProfessionalTextPrimary
                        )
                        Text(
                            text = "${belongings.count { it.isChecked }} of ${belongings.size} packed",
                            style = MaterialTheme.typography.bodySmall,
                            color = ProfessionalTextSecondary
                        )
                    }

                    Row {
                        Text(
                            text = "Check All",
                            style = MaterialTheme.typography.labelSmall,
                            color = ProfessionalPrimary,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier
                                .clickable { viewModel.checkAllBelongings() }
                                .padding(4.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Reset",
                            style = MaterialTheme.typography.labelSmall,
                            color = ProfessionalTextSecondary,
                            fontWeight = FontWeight.SemiBold,
                            modifier = Modifier
                                .clickable { viewModel.resetBelongingsCheck() }
                                .padding(4.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Belongings Items List
                belongings.take(6).forEach { item ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { viewModel.toggleBelonging(item) }
                            .padding(vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Checkbox(
                            checked = item.isChecked,
                            onCheckedChange = { viewModel.toggleBelonging(item) },
                            colors = CheckboxDefaults.colors(
                                checkedColor = ProfessionalPrimary,
                                checkmarkColor = Color.White,
                                uncheckedColor = ProfessionalBorder
                            )
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = item.name,
                            style = MaterialTheme.typography.bodyMedium,
                            color = if (item.isChecked) ProfessionalTextSecondary else ProfessionalTextPrimary,
                            fontWeight = if (item.isChecked) FontWeight.Normal else FontWeight.Medium
                        )
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Quick Add Item Field
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedTextField(
                        value = newBelongingInput,
                        onValueChange = { newBelongingInput = it },
                        placeholder = { Text("Add custom item (e.g. Headphones)", fontSize = 12.sp) },
                        modifier = Modifier.weight(1f).testTag("setup_add_belonging_input"),
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = ProfessionalPrimary,
                            unfocusedBorderColor = ProfessionalBorder
                        )
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(
                        onClick = {
                            if (newBelongingInput.isNotBlank()) {
                                viewModel.addCustomBelonging(newBelongingInput)
                                newBelongingInput = ""
                            }
                        },
                        modifier = Modifier.height(48.dp).testTag("setup_add_belonging_btn"),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = ProfessionalPrimary,
                            contentColor = Color.White
                        )
                    ) {
                        Icon(imageVector = Icons.Default.Add, contentDescription = "Add")
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Save to Database Button
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(18.dp))
                .border(BorderStroke(1.dp, ProfessionalBorder), RoundedCornerShape(18.dp))
                .clickable {
                    viewModel.saveCurrentTravelDestination("Saved Destination via Setup")
                    isSavedToRoom = true
                    Toast.makeText(context, "Saved destination, route and alarm settings to Room database!", Toast.LENGTH_SHORT).show()
                }
                .testTag("save_destination_to_room_btn"),
            color = ProfessionalSurfaceVariant,
            shape = RoundedCornerShape(18.dp)
        ) {
            Row(
                modifier = Modifier.padding(16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                Icon(
                    imageVector = if (isSavedToRoom) Icons.Default.Bookmark else Icons.Default.BookmarkBorder,
                    contentDescription = null,
                    tint = ProfessionalPrimary,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = if (isSavedToRoom) "Saved to Travel Destinations (Room)" else "Save Destination & Settings to Room",
                    style = MaterialTheme.typography.labelLarge,
                    color = ProfessionalPrimary,
                    fontWeight = FontWeight.Bold
                )
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Primary START JOURNEY CTA Button
        TravelWakeButton(
            text = "START JOURNEY NOW",
            onClick = {
                viewModel.startJourney()
                onStartJourney()
            },
            containerColor = ProfessionalPrimary,
            contentColor = Color.White,
            testTag = "start_journey_confirm_button"
        )

        Spacer(modifier = Modifier.height(28.dp))
    }
}
