package com.example.travelwake.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.Navigation
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.travelwake.data.model.DestinationItem
import com.example.travelwake.data.model.JourneyStatus
import com.example.travelwake.data.model.SavedPlaceEntity
import com.example.travelwake.ui.ads.NonIntrusiveBannerAd
import com.example.travelwake.ui.components.GlassCard
import com.example.travelwake.ui.components.TravelWakeButton
import com.example.travelwake.ui.components.WeatherRecommendationCard
import com.example.travelwake.viewmodel.TravelWakeViewModel
import com.example.ui.theme.AlarmRed
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

@Composable
fun HomeScreen(
    viewModel: TravelWakeViewModel,
    onNavigateToSearch: () -> Unit,
    onNavigateToSetup: () -> Unit,
    onNavigateToActive: () -> Unit,
    onNavigateToSettings: () -> Unit,
    onNavigateToHistory: () -> Unit,
    onNavigateToBelongings: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val destination by viewModel.selectedDestination.collectAsState()
    val weather by viewModel.destinationWeather.collectAsState()
    val savedPlaces by viewModel.savedPlaces.collectAsState()
    val quickTip by viewModel.aiQuickTip.collectAsState()
    val journeyState by viewModel.journeyState.collectAsState()
    val currentUser by viewModel.currentUser.collectAsState()
    val isRecording by viewModel.isRecordingAudio.collectAsState()
    val isTranscribing by viewModel.isTranscribing.collectAsState()
    val packItems by viewModel.allPackItems.collectAsState()
    val todos by viewModel.allTodos.collectAsState()
    val reminders by viewModel.allReminders.collectAsState()

    val scrollState = rememberScrollState()

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(ProfessionalBackground)
            .verticalScroll(scrollState)
            .padding(horizontal = 20.dp, vertical = 12.dp)
            .testTag("home_screen")
    ) {
        // Top Header matching the "Professional Polish" design
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 4.dp, bottom = 12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Menu / History Circular Action Button (bg-[#E1E2EC], icon-[#44474E])
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .background(ProfessionalSurfaceVariant)
                    .clickable { onNavigateToHistory() }
                    .testTag("history_button"),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.History,
                    contentDescription = "History",
                    tint = ProfessionalTextSecondary,
                    modifier = Modifier.size(24.dp)
                )
            }

            // User Profile Avatar (bg-[#005AC1], 2dp white border)
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(
                    onClick = onNavigateToSettings,
                    modifier = Modifier.testTag("settings_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.Settings,
                        contentDescription = "Settings",
                        tint = ProfessionalTextSecondary
                    )
                }

                Spacer(modifier = Modifier.width(4.dp))

                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .clip(CircleShape)
                        .background(ProfessionalPrimary)
                        .border(2.dp, Color.White, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = if (currentUser?.displayName.isNullOrBlank()) "TW" else currentUser!!.displayName!!.take(2).uppercase(),
                        style = MaterialTheme.typography.titleMedium,
                        color = Color.White,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
        }

        // Title and Subtitle as per Professional Polish typography
        Text(
            text = if (currentUser?.isAnonymous == false && !currentUser?.displayName.isNullOrBlank()) {
                "Hello, ${currentUser?.displayName}"
            } else {
                "Morning, Traveler"
            },
            style = MaterialTheme.typography.displaySmall,
            fontWeight = FontWeight.Normal,
            color = ProfessionalTextPrimary
        )
        Text(
            text = "GPS smart destination alarm • Never miss your stop",
            style = MaterialTheme.typography.bodyMedium,
            color = ProfessionalTextSecondary,
            modifier = Modifier.padding(top = 2.dp, bottom = 18.dp)
        )

        // Active Journey Banner if journey is in progress
        if (journeyState == JourneyStatus.ACTIVE || journeyState == JourneyStatus.APPROACHING || journeyState == JourneyStatus.ALARMING) {
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(24.dp))
                    .border(BorderStroke(1.dp, ProfessionalPrimary), RoundedCornerShape(24.dp))
                    .clickable { onNavigateToActive() }
                    .testTag("active_journey_banner"),
                color = ProfessionalPrimaryContainer,
                shape = RoundedCornerShape(24.dp),
                shadowElevation = 2.dp
            ) {
                Row(
                    modifier = Modifier.padding(18.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(12.dp)
                                .clip(CircleShape)
                                .background(SafetyGreen)
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                text = "ACTIVE JOURNEY EN ROUTE",
                                style = MaterialTheme.typography.labelMedium,
                                color = ProfessionalOnPrimaryContainer,
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 1.sp
                            )
                            Text(
                                text = destination?.name ?: "En Route",
                                style = MaterialTheme.typography.titleMedium,
                                color = ProfessionalOnPrimaryContainer,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                    Text(
                        text = "VIEW LIVE →",
                        style = MaterialTheme.typography.labelLarge,
                        color = ProfessionalPrimary,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
            Spacer(modifier = Modifier.height(18.dp))
        }

        // Hero Card styled after the "Next Meeting" Hero Card in Design HTML
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(28.dp))
                .border(BorderStroke(1.dp, ProfessionalBorder), RoundedCornerShape(28.dp))
                .clickable {
                    if (destination != null) onNavigateToSetup() else onNavigateToSearch()
                }
                .testTag("travel_map_area"),
            color = ProfessionalPrimaryContainer,
            shape = RoundedCornerShape(28.dp),
            shadowElevation = 2.dp
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(22.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = if (destination != null) "ACTIVE DESTINATION" else "UPCOMING TRIP",
                        style = MaterialTheme.typography.labelMedium,
                        color = ProfessionalOnPrimaryContainer,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = destination?.name ?: "Set Your Stop",
                        style = MaterialTheme.typography.headlineSmall,
                        color = ProfessionalOnPrimaryContainer,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = if (destination != null) {
                            "${destination?.distanceKm} km • Alert armed at 500m"
                        } else {
                            "Sleep peacefully • We wake you in time"
                        },
                        style = MaterialTheme.typography.bodyMedium,
                        color = ProfessionalOnPrimaryContainer.copy(alpha = 0.75f)
                    )
                }

                Spacer(modifier = Modifier.width(14.dp))

                Box(
                    modifier = Modifier
                        .size(54.dp)
                        .clip(RoundedCornerShape(18.dp))
                        .background(ProfessionalPrimary),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Navigation,
                        contentDescription = "GPS Ready",
                        tint = Color.White,
                        modifier = Modifier.size(28.dp)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Two-Column Grid Cards matching Design HTML (analytics / notifications grid)
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // Card 1: White rounded-3xl with border #C4C6D0
            Surface(
                modifier = Modifier
                    .weight(1f)
                    .height(130.dp)
                    .clip(RoundedCornerShape(24.dp))
                    .border(BorderStroke(1.dp, ProfessionalBorder), RoundedCornerShape(24.dp)),
                color = ProfessionalSurface,
                shape = RoundedCornerShape(24.dp),
                shadowElevation = 1.dp
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.SpaceBetween
                ) {
                    Icon(
                        imageVector = Icons.Default.Navigation,
                        contentDescription = "GPS Monitoring",
                        tint = ProfessionalPrimary,
                        modifier = Modifier.size(26.dp)
                    )
                    Column {
                        Text(
                            text = "500m",
                            style = MaterialTheme.typography.titleLarge,
                            color = ProfessionalTextPrimary,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Default Buffer",
                            style = MaterialTheme.typography.bodySmall,
                            color = ProfessionalTextSecondary
                        )
                    }
                }
            }

            // Card 2: #E1E2EC rounded-3xl
            Surface(
                modifier = Modifier
                    .weight(1f)
                    .height(130.dp)
                    .clip(RoundedCornerShape(24.dp)),
                color = ProfessionalSurfaceVariant,
                shape = RoundedCornerShape(24.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.SpaceBetween
                ) {
                    Icon(
                        imageVector = Icons.Default.Notifications,
                        contentDescription = "Audible & Haptic Alerts",
                        tint = ProfessionalTextSecondary,
                        modifier = Modifier.size(26.dp)
                    )
                    Column {
                        Text(
                            text = "Active",
                            style = MaterialTheme.typography.titleLarge,
                            color = ProfessionalTextPrimary,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Sound & Haptics",
                            style = MaterialTheme.typography.bodySmall,
                            color = ProfessionalTextSecondary
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(18.dp))

        // Search Bar with Voice Input matching the Professional Action List Card
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(24.dp))
                .border(BorderStroke(1.dp, ProfessionalBorder), RoundedCornerShape(24.dp))
                .clickable { onNavigateToSearch() }
                .testTag("destination_search_card"),
            color = ProfessionalSurface,
            shape = RoundedCornerShape(24.dp),
            shadowElevation = 1.dp
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 14.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f)
                ) {
                    Box(
                        modifier = Modifier
                            .size(42.dp)
                            .clip(CircleShape)
                            .background(ProfessionalBackground),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Search,
                            contentDescription = "Search destination",
                            tint = ProfessionalTextSecondary,
                            modifier = Modifier.size(22.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(14.dp))
                    Column {
                        Text(
                            text = "Where are you going?",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = ProfessionalTextPrimary
                        )
                        Text(
                            text = destination?.name ?: "Search station, bus stop, metro or place...",
                            style = MaterialTheme.typography.bodySmall,
                            color = ProfessionalTextSecondary,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }

                // Voice microphone icon
                IconButton(
                    onClick = {
                        if (isRecording) {
                            viewModel.stopVoiceRecordingAndSearch { query ->
                                onNavigateToSearch()
                            }
                        } else {
                            viewModel.startVoiceRecording()
                        }
                    },
                    modifier = Modifier.testTag("voice_mic_button")
                ) {
                    if (isTranscribing) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(22.dp),
                            color = ProfessionalPrimary,
                            strokeWidth = 2.dp
                        )
                    } else {
                        Icon(
                            imageVector = Icons.Default.Mic,
                            contentDescription = "Voice input",
                            tint = if (isRecording) AlarmRed else ProfessionalPrimary,
                            modifier = Modifier.size(26.dp)
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(18.dp))

        // Travel Prep & Tasks Quick Glance Card
        val unpackedCount = packItems.count { !it.packed }
        val activeTodosCount = todos.count { !it.completed }
        val pendingRemindersCount = reminders.count { !it.completed }

        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(24.dp))
                .border(BorderStroke(1.dp, ProfessionalBorder), RoundedCornerShape(24.dp))
                .clickable { onNavigateToBelongings() }
                .testTag("home_travel_prep_card"),
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
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(text = "🎒", fontSize = 18.sp)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Travel Prep & Tasks",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = ProfessionalTextPrimary
                        )
                    }
                    Text(
                        text = "MANAGE →",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color = ProfessionalPrimary
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Surface(
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(12.dp),
                        color = ProfessionalSurfaceVariant
                    ) {
                        Column(modifier = Modifier.padding(10.dp)) {
                            Text(
                                text = "$unpackedCount to pack",
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.Bold,
                                color = ProfessionalTextPrimary
                            )
                            Text(
                                text = "Luggage items",
                                style = MaterialTheme.typography.labelSmall,
                                color = ProfessionalTextSecondary
                            )
                        }
                    }

                    Surface(
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(12.dp),
                        color = ProfessionalSurfaceVariant
                    ) {
                        Column(modifier = Modifier.padding(10.dp)) {
                            Text(
                                text = "$activeTodosCount pending",
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.Bold,
                                color = ProfessionalTextPrimary
                            )
                            Text(
                                text = "To-dos",
                                style = MaterialTheme.typography.labelSmall,
                                color = ProfessionalTextSecondary
                            )
                        }
                    }

                    Surface(
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(12.dp),
                        color = ProfessionalSurfaceVariant
                    ) {
                        Column(modifier = Modifier.padding(10.dp)) {
                            Text(
                                text = "$pendingRemindersCount alarms",
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.Bold,
                                color = ProfessionalTextPrimary
                            )
                            Text(
                                text = "Reminders",
                                style = MaterialTheme.typography.labelSmall,
                                color = ProfessionalTextSecondary
                            )
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Saved Destinations List styled with Professional Polish Action List container
        Text(
            text = "Saved Destinations",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = ProfessionalTextPrimary
        )
        Spacer(modifier = Modifier.height(10.dp))

        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(24.dp))
                .border(BorderStroke(1.dp, ProfessionalBorder), RoundedCornerShape(24.dp)),
            color = ProfessionalSurface,
            shape = RoundedCornerShape(24.dp),
            shadowElevation = 1.dp
        ) {
            Column(modifier = Modifier.padding(vertical = 4.dp, horizontal = 4.dp)) {
                savedPlaces.take(4).forEachIndexed { index, place ->
                    if (index > 0) {
                        HorizontalDivider(
                            color = ProfessionalDivider,
                            thickness = 1.dp,
                            modifier = Modifier.padding(horizontal = 16.dp)
                        )
                    }
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(18.dp))
                            .clickable {
                                val item = DestinationItem(
                                    id = place.id,
                                    name = place.name,
                                    address = place.address,
                                    latitude = place.latitude,
                                    longitude = place.longitude,
                                    placeType = place.iconType,
                                    distanceKm = 6.4f,
                                    estimatedMinutes = 18
                                )
                                viewModel.selectDestination(item)
                                onNavigateToSetup()
                            }
                            .padding(horizontal = 14.dp, vertical = 12.dp)
                            .testTag("saved_place_${place.id}"),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.weight(1f)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(40.dp)
                                    .clip(CircleShape)
                                    .background(ProfessionalBackground),
                                contentAlignment = Alignment.Center
                            ) {
                                val emoji = when (place.iconType.lowercase()) {
                                    "home" -> "🏠"
                                    "work" -> "💼"
                                    "station" -> "🚆"
                                    "airport" -> "✈️"
                                    else -> "📍"
                                }
                                Text(text = emoji, fontSize = 20.sp)
                            }
                            Spacer(modifier = Modifier.width(14.dp))
                            Column {
                                Text(
                                    text = place.name,
                                    style = MaterialTheme.typography.titleSmall,
                                    color = ProfessionalTextPrimary,
                                    fontWeight = FontWeight.SemiBold,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                                Text(
                                    text = place.address,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = ProfessionalTextSecondary,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                        }
                        Icon(
                            imageVector = Icons.Default.ChevronRight,
                            contentDescription = "Select",
                            tint = ProfessionalPrimary,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(18.dp))

        // Smart Travel Tip (Gemini flash-lite)
        if (!quickTip.isNullOrBlank()) {
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(20.dp))
                    .border(BorderStroke(1.dp, ProfessionalPrimary.copy(alpha = 0.2f)), RoundedCornerShape(20.dp))
                    .testTag("ai_tip_card"),
                color = ProfessionalPrimaryContainer,
                shape = RoundedCornerShape(20.dp)
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(text = "⚡", fontSize = 22.sp)
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            text = "Travel Safety Tip",
                            style = MaterialTheme.typography.labelLarge,
                            color = ProfessionalOnPrimaryContainer,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = quickTip ?: "",
                            style = MaterialTheme.typography.bodySmall,
                            color = ProfessionalOnPrimaryContainer.copy(alpha = 0.85f)
                        )
                    }
                }
            }
            Spacer(modifier = Modifier.height(18.dp))
        }

        // Destination Weather Card preview if destination selected
        if (destination != null) {
            WeatherRecommendationCard(
                weather = weather,
                destinationName = destination!!.name,
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(20.dp))
        }

        // Start Journey / Configure Journey Primary CTA Button
        TravelWakeButton(
            text = if (destination != null) "CONFIGURE & START JOURNEY" else "SELECT DESTINATION TO START",
            onClick = {
                if (destination != null) {
                    onNavigateToSetup()
                } else {
                    onNavigateToSearch()
                }
            },
            containerColor = ProfessionalPrimary,
            contentColor = Color.White,
            testTag = "start_journey_main_cta"
        )

        Spacer(modifier = Modifier.height(20.dp))

        // Non-intrusive banner ad placed safely at bottom, without interfering with transit/alarm buttons
        NonIntrusiveBannerAd(
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(30.dp))
    }
}
