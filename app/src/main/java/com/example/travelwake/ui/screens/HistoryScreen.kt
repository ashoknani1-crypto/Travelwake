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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.CloudDone
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.DeleteSweep
import androidx.compose.material.icons.filled.DirectionsBus
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material.icons.filled.DirectionsSubway
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Lightbulb
import androidx.compose.material.icons.filled.Navigation
import androidx.compose.material.icons.filled.Place
import androidx.compose.material.icons.filled.Thermostat
import androidx.compose.material.icons.filled.Train
import androidx.compose.material.icons.filled.WbSunny
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.travelwake.data.model.JourneyEntity
import com.example.travelwake.data.model.TransportMode
import com.example.travelwake.viewmodel.TravelWakeViewModel
import com.example.ui.theme.AlarmRed
import com.example.ui.theme.AlertAmber
import com.example.ui.theme.AlertAmberContainer
import com.example.ui.theme.LocalWeatherThemePalette
import com.example.ui.theme.ProfessionalBackground
import com.example.ui.theme.ProfessionalBorder
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

@Composable
fun HistoryScreen(
    viewModel: TravelWakeViewModel,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val journeys by viewModel.allJourneys.collectAsState()
    val weatherTheme = LocalWeatherThemePalette.current
    var showClearConfirmDialog by remember { mutableStateOf(false) }

    val totalDurationMinutes = remember(journeys) {
        journeys.sumOf { j ->
            if (j.durationMinutes > 0) j.durationMinutes
            else if (j.completedAt != null && j.completedAt > j.startedAt) {
                ((j.completedAt - j.startedAt) / 60000L).toInt().coerceAtLeast(1)
            } else 25
        }
    }

    val formattedTotalTime = remember(totalDurationMinutes) {
        val hours = totalDurationMinutes / 60
        val mins = totalDurationMinutes % 60
        if (hours > 0) "${hours}h ${mins}m" else "${mins} mins"
    }

    val avgTemp = remember(journeys) {
        if (journeys.isNotEmpty()) {
            val sum = journeys.sumOf { it.temperatureCelsius }
            "${sum / journeys.size}°C"
        } else "22°C"
    }

    if (showClearConfirmDialog) {
        AlertDialog(
            onDismissRequest = { showClearConfirmDialog = false },
            title = {
                Text(
                    text = "Clear All Trip History?",
                    fontWeight = FontWeight.Bold,
                    color = ProfessionalTextPrimary
                )
            },
            text = {
                Text(
                    text = "This will permanently remove all past journey records from your local database.",
                    color = ProfessionalTextSecondary
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.clearJourneyHistory()
                        showClearConfirmDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = AlarmRed)
                ) {
                    Text("Clear All", color = Color.White)
                }
            },
            dismissButton = {
                TextButton(onClick = { showClearConfirmDialog = false }) {
                    Text("Cancel", color = ProfessionalTextSecondary)
                }
            },
            containerColor = ProfessionalSurface,
            shape = RoundedCornerShape(20.dp)
        )
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(ProfessionalBackground)
            .padding(horizontal = 20.dp, vertical = 12.dp)
            .testTag("history_screen")
    ) {
        // App Bar
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = onBack, modifier = Modifier.testTag("history_back_button")) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back",
                        tint = ProfessionalTextPrimary
                    )
                }
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "Trip History",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = ProfessionalTextPrimary
                )
            }

            if (journeys.isNotEmpty()) {
                IconButton(
                    onClick = { showClearConfirmDialog = true },
                    modifier = Modifier.testTag("clear_history_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.DeleteSweep,
                        contentDescription = "Clear History",
                        tint = ProfessionalTextSecondary
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        if (journeys.isEmpty()) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Box(
                        modifier = Modifier
                            .size(76.dp)
                            .clip(CircleShape)
                            .background(ProfessionalSurfaceVariant),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.History,
                            contentDescription = null,
                            tint = ProfessionalTextSecondary,
                            modifier = Modifier.size(38.dp)
                        )
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "No Past Trips Logged Yet",
                        style = MaterialTheme.typography.titleMedium,
                        color = ProfessionalTextPrimary,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "Completed destination journeys, travel duration,\nand weather conditions will be recorded here.",
                        style = MaterialTheme.typography.bodySmall,
                        color = ProfessionalTextSecondary,
                        lineHeight = 18.sp,
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center
                    )
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                // Trip Analytics Summary Header Card
                item {
                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(20.dp))
                            .border(BorderStroke(1.dp, ProfessionalBorder), RoundedCornerShape(20.dp))
                            .testTag("history_analytics_card"),
                        color = ProfessionalSurface,
                        shape = RoundedCornerShape(20.dp),
                        shadowElevation = 1.dp
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            horizontalArrangement = Arrangement.SpaceAround,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            HistoryStatItem(
                                icon = Icons.Default.Navigation,
                                label = "Total Trips",
                                value = "${journeys.size}",
                                iconTint = weatherTheme.primaryColor
                            )
                            HistoryStatItem(
                                icon = Icons.Default.AccessTime,
                                label = "Time Traveled",
                                value = formattedTotalTime,
                                iconTint = AlertAmber
                            )
                            HistoryStatItem(
                                icon = Icons.Default.Thermostat,
                                label = "Avg Weather",
                                value = avgTemp,
                                iconTint = SafetyGreen
                            )
                        }
                    }
                }

                items(journeys, key = { it.id }) { journey ->
                    JourneyHistoryCard(
                        journey = journey,
                        onDelete = { viewModel.deleteJourney(journey.id) }
                    )
                }

                item {
                    Spacer(modifier = Modifier.height(24.dp))
                }
            }
        }
    }
}

@Composable
private fun HistoryStatItem(
    icon: ImageVector,
    label: String,
    value: String,
    iconTint: Color
) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Box(
            modifier = Modifier
                .size(38.dp)
                .clip(CircleShape)
                .background(iconTint.copy(alpha = 0.12f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = iconTint,
                modifier = Modifier.size(20.dp)
            )
        }
        Spacer(modifier = Modifier.height(6.dp))
        Text(
            text = value,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.ExtraBold,
            color = ProfessionalTextPrimary
        )
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = ProfessionalTextSecondary
        )
    }
}

@Composable
fun JourneyHistoryCard(
    journey: JourneyEntity,
    onDelete: () -> Unit
) {
    val weatherTheme = LocalWeatherThemePalette.current
    var isTipsExpanded by remember { mutableStateOf(false) }

    val dateFormat = SimpleDateFormat("dd MMM yyyy, hh:mm a", Locale.getDefault())
    val dateStr = dateFormat.format(Date(journey.startedAt))

    val displayDuration = if (journey.durationMinutes > 0) {
        val h = journey.durationMinutes / 60
        val m = journey.durationMinutes % 60
        if (h > 0) "${h}h ${m}m" else "${m} mins"
    } else if (journey.completedAt != null && journey.completedAt > journey.startedAt) {
        val diffMins = ((journey.completedAt - journey.startedAt) / 60000L).toInt().coerceAtLeast(1)
        val h = diffMins / 60
        val m = diffMins % 60
        if (h > 0) "${h}h ${m}m" else "${m} mins"
    } else "35 mins"

    val transportIcon = when (journey.transportMode) {
        TransportMode.TRAIN.name -> Icons.Default.Train
        TransportMode.METRO.name -> Icons.Default.DirectionsSubway
        TransportMode.BUS.name -> Icons.Default.DirectionsBus
        else -> Icons.Default.DirectionsCar
    }

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(22.dp))
            .border(BorderStroke(1.dp, ProfessionalBorder), RoundedCornerShape(22.dp))
            .testTag("journey_history_${journey.id}"),
        color = ProfessionalSurface,
        shape = RoundedCornerShape(22.dp),
        shadowElevation = 1.dp
    ) {
        Column(modifier = Modifier.padding(18.dp)) {
            // Header: Title, address, status, delete button
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f)
                ) {
                    Box(
                        modifier = Modifier
                            .size(42.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(weatherTheme.primaryContainerColor),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = transportIcon,
                            contentDescription = journey.transportMode,
                            tint = weatherTheme.primaryColor,
                            modifier = Modifier.size(22.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(12.dp))

                    Column {
                        Text(
                            text = journey.title,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = ProfessionalTextPrimary,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Text(
                            text = journey.address,
                            style = MaterialTheme.typography.bodySmall,
                            color = ProfessionalTextSecondary,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Surface(
                        color = SafetyGreenContainer,
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text(
                            text = journey.status,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                            style = MaterialTheme.typography.labelSmall,
                            color = SafetyGreen,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    IconButton(
                        onClick = onDelete,
                        modifier = Modifier.size(32.dp).testTag("delete_journey_${journey.id}")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Delete,
                            contentDescription = "Delete Journey",
                            tint = ProfessionalTextMuted,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Route & Duration Row
            Surface(
                color = ProfessionalSurfaceVariant,
                shape = RoundedCornerShape(14.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 14.dp, vertical = 10.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Departure & Route
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Place,
                            contentDescription = null,
                            tint = ProfessionalTextSecondary,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "${journey.departurePoint.ifBlank { "Departure" }} ➔ ${journey.title}",
                            style = MaterialTheme.typography.labelMedium,
                            color = ProfessionalTextPrimary,
                            fontWeight = FontWeight.SemiBold,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }

                    // Duration Badge
                    Surface(
                        color = weatherTheme.primaryContainerColor,
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.AccessTime,
                                contentDescription = null,
                                tint = weatherTheme.primaryColor,
                                modifier = Modifier.size(14.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = displayDuration,
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.Bold,
                                color = weatherTheme.primaryColor
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Weather Conditions Encountered Block
            Surface(
                color = ProfessionalSurfaceVariant.copy(alpha = 0.6f),
                shape = RoundedCornerShape(14.dp),
                border = BorderStroke(1.dp, ProfessionalBorder.copy(alpha = 0.5f)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.WbSunny,
                                contentDescription = null,
                                tint = AlertAmber,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "Encountered Weather:",
                                style = MaterialTheme.typography.labelSmall,
                                color = ProfessionalTextSecondary,
                                fontWeight = FontWeight.Medium
                            )
                        }

                        Text(
                            text = "${journey.temperatureCelsius}°C • ${journey.weatherCondition}",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold,
                            color = ProfessionalTextPrimary
                        )
                    }

                    if (journey.weatherRecommendation.isNotBlank()) {
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = journey.weatherRecommendation,
                            style = MaterialTheme.typography.bodySmall,
                            color = ProfessionalTextSecondary,
                            fontSize = 12.sp,
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
            }

            // Optional Gemini Travel Tips Accordion
            if (journey.travelTips.isNotBlank()) {
                Spacer(modifier = Modifier.height(10.dp))
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .clickable { isTipsExpanded = !isTipsExpanded }
                        .padding(vertical = 4.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Lightbulb,
                            contentDescription = null,
                            tint = weatherTheme.primaryColor,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Gemini Travel & Safety Tips",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = weatherTheme.primaryColor
                        )
                    }
                    Icon(
                        imageVector = if (isTipsExpanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                        contentDescription = "Toggle Tips",
                        tint = ProfessionalTextSecondary,
                        modifier = Modifier.size(16.dp)
                    )
                }

                AnimatedVisibility(visible = isTipsExpanded) {
                    Surface(
                        color = weatherTheme.primaryContainerColor.copy(alpha = 0.5f),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 4.dp)
                    ) {
                        Text(
                            text = journey.travelTips,
                            style = MaterialTheme.typography.bodySmall,
                            color = ProfessionalTextPrimary,
                            modifier = Modifier.padding(10.dp),
                            lineHeight = 16.sp,
                            fontSize = 12.sp
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Footer Timestamp & Cloud Sync
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "${journey.transportMode} Transit",
                    style = MaterialTheme.typography.labelSmall,
                    color = ProfessionalTextSecondary
                )

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.CloudDone,
                        contentDescription = "Cloud synced",
                        tint = SafetyGreen,
                        modifier = Modifier.size(15.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = dateStr,
                        style = MaterialTheme.typography.labelSmall,
                        color = ProfessionalTextSecondary
                    )
                }
            }
        }
    }
}
