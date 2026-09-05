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
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
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
import com.example.travelwake.data.model.DestinationItem
import com.example.travelwake.ui.components.GlassCard
import com.example.travelwake.viewmodel.TravelWakeViewModel
import com.example.ui.theme.AlarmRed
import com.example.ui.theme.ProfessionalBackground
import com.example.ui.theme.ProfessionalBorder
import com.example.ui.theme.ProfessionalOnPrimaryContainer
import com.example.ui.theme.ProfessionalPrimary
import com.example.ui.theme.ProfessionalPrimaryContainer
import com.example.ui.theme.ProfessionalSurface
import com.example.ui.theme.ProfessionalSurfaceVariant
import com.example.ui.theme.ProfessionalTextMuted
import com.example.ui.theme.ProfessionalTextPrimary
import com.example.ui.theme.ProfessionalTextSecondary

@Composable
fun SearchDestinationScreen(
    viewModel: TravelWakeViewModel,
    onDestinationSelected: () -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    var searchQuery by remember { mutableStateOf("") }
    val isRecording by viewModel.isRecordingAudio.collectAsState()
    val isTranscribing by viewModel.isTranscribing.collectAsState()
    val transitInsight by viewModel.aiTransitInsight.collectAsState()
    val liveConditions by viewModel.aiLiveConditions.collectAsState()

    // Sample transit places and destinations
    val samplePlaces = remember {
        listOf(
            DestinationItem(
                id = "pune_station",
                name = "Pune Railway Station",
                address = "Station Road, Agarkar Nagar, Pune",
                latitude = 18.5289,
                longitude = 73.8744,
                placeType = "Railway Station",
                distanceKm = 8.4f,
                estimatedMinutes = 24
            ),
            DestinationItem(
                id = "mumbai_cst",
                name = "Chhatrapati Shivaji Maharaj Terminus",
                address = "Fort, Mumbai, Maharashtra",
                latitude = 18.9401,
                longitude = 72.8351,
                placeType = "Central Railway Terminus",
                distanceKm = 148.0f,
                estimatedMinutes = 180
            ),
            DestinationItem(
                id = "airport_t2",
                name = "International Airport Terminal 2",
                address = "Sahar Elevated Access Road, Andheri",
                latitude = 19.0990,
                longitude = 72.8745,
                placeType = "Airport Hub",
                distanceKm = 12.6f,
                estimatedMinutes = 35
            ),
            DestinationItem(
                id = "metro_central",
                name = "Metro Central Intermodal Station",
                address = "Line 1, Platform 2, Downtown",
                latitude = 18.5204,
                longitude = 73.8567,
                placeType = "Metro Transit Hub",
                distanceKm = 4.2f,
                estimatedMinutes = 15
            ),
            DestinationItem(
                id = "shivaji_nagar_bus",
                name = "Shivaji Nagar Bus Terminus",
                address = "JM Road, Shivaji Nagar",
                latitude = 18.5314,
                longitude = 73.8446,
                placeType = "State Bus Depo",
                distanceKm = 6.1f,
                estimatedMinutes = 20
            ),
            DestinationItem(
                id = "university_gate",
                name = "Savitribai Phule Pune University Gate",
                address = "Ganeshkhind Road, University Campus",
                latitude = 18.5362,
                longitude = 73.8291,
                placeType = "City Landmark",
                distanceKm = 3.8f,
                estimatedMinutes = 12
            )
        )
    }

    val filteredPlaces = remember(searchQuery) {
        if (searchQuery.isBlank()) {
            samplePlaces
        } else {
            samplePlaces.filter {
                it.name.contains(searchQuery, ignoreCase = true) ||
                        it.address.contains(searchQuery, ignoreCase = true) ||
                        it.placeType.contains(searchQuery, ignoreCase = true)
            }
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(ProfessionalBackground)
            .padding(20.dp)
            .testTag("search_destination_screen")
    ) {
        // Top App Bar
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(
                onClick = onBack,
                modifier = Modifier.testTag("back_button")
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Back",
                    tint = ProfessionalTextPrimary
                )
            }
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "Search Destination",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = ProfessionalTextPrimary
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Search text field + voice button styled with Professional Polish
        OutlinedTextField(
            value = searchQuery,
            onValueChange = { searchQuery = it },
            modifier = Modifier
                .fillMaxWidth()
                .testTag("search_text_input"),
            placeholder = {
                Text(
                    text = "Enter station, airport, stop or place...",
                    color = ProfessionalTextSecondary
                )
            },
            leadingIcon = {
                Icon(
                    imageVector = Icons.Default.Search,
                    contentDescription = "Search",
                    tint = ProfessionalPrimary
                )
            },
            trailingIcon = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    if (searchQuery.isNotEmpty()) {
                        IconButton(onClick = { searchQuery = "" }) {
                            Icon(
                                imageVector = Icons.Default.Clear,
                                contentDescription = "Clear search",
                                tint = ProfessionalTextSecondary
                            )
                        }
                    }

                    // Microphone button for Gemini 3.5 Transcribe
                    IconButton(
                        onClick = {
                            if (isRecording) {
                                viewModel.stopVoiceRecordingAndSearch { spokenText ->
                                    searchQuery = spokenText
                                }
                            } else {
                                viewModel.startVoiceRecording()
                            }
                        },
                        modifier = Modifier.testTag("search_mic_button")
                    ) {
                        if (isTranscribing) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(20.dp),
                                color = ProfessionalPrimary,
                                strokeWidth = 2.dp
                            )
                        } else {
                            Icon(
                                imageVector = Icons.Default.Mic,
                                contentDescription = "Voice search",
                                tint = if (isRecording) AlarmRed else ProfessionalPrimary
                            )
                        }
                    }
                }
            },
            colors = OutlinedTextFieldDefaults.colors(
                focusedContainerColor = ProfessionalSurface,
                unfocusedContainerColor = ProfessionalSurface,
                focusedBorderColor = ProfessionalPrimary,
                unfocusedBorderColor = ProfessionalBorder,
                focusedTextColor = ProfessionalTextPrimary,
                unfocusedTextColor = ProfessionalTextPrimary
            ),
            shape = RoundedCornerShape(20.dp),
            singleLine = true
        )

        AnimatedVisibility(visible = isRecording) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                Box(
                    modifier = Modifier
                        .size(10.dp)
                        .clip(CircleShape)
                        .background(AlarmRed)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Listening... Speak destination clearly",
                    style = MaterialTheme.typography.labelMedium,
                    color = AlarmRed,
                    fontWeight = FontWeight.Bold
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Grounding insight banner if available
        if (!transitInsight.isNullOrBlank() || !liveConditions.isNullOrBlank()) {
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(20.dp))
                    .border(BorderStroke(1.dp, ProfessionalPrimary.copy(alpha = 0.25f)), RoundedCornerShape(20.dp))
                    .testTag("grounding_info_banner"),
                color = ProfessionalPrimaryContainer,
                shape = RoundedCornerShape(20.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "🗺️ Transit & Maps Intelligence",
                        style = MaterialTheme.typography.labelMedium,
                        color = ProfessionalOnPrimaryContainer,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = transitInsight ?: (liveConditions ?: ""),
                        style = MaterialTheme.typography.bodySmall,
                        color = ProfessionalOnPrimaryContainer.copy(alpha = 0.85f)
                    )
                }
            }
            Spacer(modifier = Modifier.height(16.dp))
        }

        Text(
            text = "Suggested Destinations",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = ProfessionalTextPrimary
        )

        Spacer(modifier = Modifier.height(12.dp))

        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(10.dp),
            modifier = Modifier.fillMaxSize()
        ) {
            items(filteredPlaces) { place ->
                DestinationResultCard(
                    place = place,
                    onClick = {
                        viewModel.selectDestination(place)
                        onDestinationSelected()
                    }
                )
            }
        }
    }
}

@Composable
fun DestinationResultCard(
    place: DestinationItem,
    onClick: () -> Unit
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .border(BorderStroke(1.dp, ProfessionalBorder), RoundedCornerShape(20.dp))
            .clickable { onClick() }
            .testTag("destination_card_${place.id}"),
        color = ProfessionalSurface,
        shape = RoundedCornerShape(20.dp),
        shadowElevation = 1.dp
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.weight(1f)
            ) {
                Box(
                    modifier = Modifier
                        .size(46.dp)
                        .clip(CircleShape)
                        .background(ProfessionalBackground),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = when {
                            place.placeType.contains("Train", ignoreCase = true) || place.placeType.contains("Railway", ignoreCase = true) -> "🚆"
                            place.placeType.contains("Metro", ignoreCase = true) -> "🚇"
                            place.placeType.contains("Airport", ignoreCase = true) -> "✈️"
                            place.placeType.contains("Bus", ignoreCase = true) -> "🚌"
                            else -> "📍"
                        },
                        fontSize = 22.sp
                    )
                }

                Spacer(modifier = Modifier.width(14.dp))

                Column {
                    Text(
                        text = place.name,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = ProfessionalTextPrimary
                    )
                    Text(
                        text = place.address,
                        style = MaterialTheme.typography.bodySmall,
                        color = ProfessionalTextSecondary
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Surface(
                        color = ProfessionalSurfaceVariant,
                        shape = RoundedCornerShape(6.dp)
                    ) {
                        Text(
                            text = place.placeType,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
                            style = MaterialTheme.typography.labelSmall,
                            color = ProfessionalPrimary,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }

            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = "${place.distanceKm} km",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = ProfessionalPrimary
                )
                Text(
                    text = "~${place.estimatedMinutes} min",
                    style = MaterialTheme.typography.labelSmall,
                    color = ProfessionalTextSecondary
                )
            }
        }
    }
}
