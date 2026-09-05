package com.example.travelwake.ui.screens

import android.widget.Toast
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
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Navigation
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Train
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
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
import com.example.travelwake.data.model.DestinationItem
import com.example.travelwake.data.model.TransportMode
import com.example.travelwake.data.model.TravelDestinationEntity
import com.example.travelwake.viewmodel.TravelWakeViewModel
import com.example.ui.theme.ProfessionalBackground
import com.example.ui.theme.ProfessionalBorder
import com.example.ui.theme.ProfessionalDivider
import com.example.ui.theme.ProfessionalPrimary
import com.example.ui.theme.ProfessionalPrimaryContainer
import com.example.ui.theme.ProfessionalSurface
import com.example.ui.theme.ProfessionalSurfaceVariant
import com.example.ui.theme.ProfessionalTextMuted
import com.example.ui.theme.ProfessionalTextPrimary
import com.example.ui.theme.ProfessionalTextSecondary
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.UUID

@Composable
fun SavedPlacesScreen(
    viewModel: TravelWakeViewModel,
    onPlaceSelected: () -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val savedPlaces by viewModel.savedPlaces.collectAsState()
    val travelDestinations by viewModel.travelDestinations.collectAsState()

    var selectedTabIndex by remember { mutableIntStateOf(0) }
    var showAddDialog by remember { mutableStateOf(false) }

    // Form inputs for new travel destination
    var destName by remember { mutableStateOf("") }
    var destAddress by remember { mutableStateOf("") }
    var departurePointInput by remember { mutableStateOf("Pune Central Hub") }
    var arrivalTimeInput by remember { mutableStateOf("09:30 AM") }
    var alarmDistInput by remember { mutableIntStateOf(500) }
    var soundEnabled by remember { mutableStateOf(true) }
    var vibrationEnabled by remember { mutableStateOf(true) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(ProfessionalBackground)
            .padding(horizontal = 20.dp, vertical = 12.dp)
            .testTag("saved_places_screen")
    ) {
        // App Bar
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = onBack, modifier = Modifier.testTag("saved_places_back_button")) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back",
                        tint = ProfessionalTextPrimary
                    )
                }
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "Destinations & Places",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = ProfessionalTextPrimary
                )
            }

            Box(
                modifier = Modifier
                    .size(42.dp)
                    .clip(CircleShape)
                    .background(ProfessionalPrimary)
                    .clickable { showAddDialog = !showAddDialog }
                    .testTag("toggle_add_place_button"),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = "Add Destination",
                    tint = Color.White,
                    modifier = Modifier.size(24.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Tabs: Travel Destinations (Room) vs Quick Places
        TabRow(
            selectedTabIndex = selectedTabIndex,
            containerColor = ProfessionalSurface,
            contentColor = ProfessionalPrimary,
            indicator = { tabPositions ->
                TabRowDefaults.SecondaryIndicator(
                    modifier = Modifier.tabIndicatorOffset(tabPositions[selectedTabIndex]),
                    color = ProfessionalPrimary
                )
            },
            modifier = Modifier
                .clip(RoundedCornerShape(16.dp))
                .border(BorderStroke(1.dp, ProfessionalBorder), RoundedCornerShape(16.dp))
        ) {
            Tab(
                selected = selectedTabIndex == 0,
                onClick = { selectedTabIndex = 0 },
                text = {
                    Text(
                        text = "Travel Destinations (${travelDestinations.size})",
                        fontWeight = if (selectedTabIndex == 0) FontWeight.Bold else FontWeight.Normal
                    )
                }
            )
            Tab(
                selected = selectedTabIndex == 1,
                onClick = { selectedTabIndex = 1 },
                text = {
                    Text(
                        text = "Quick Places (${savedPlaces.size})",
                        fontWeight = if (selectedTabIndex == 1) FontWeight.Bold else FontWeight.Normal
                    )
                }
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Add Dialog / Form
        if (showAddDialog) {
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(24.dp),
                color = ProfessionalSurface,
                border = BorderStroke(1.dp, ProfessionalBorder),
                shadowElevation = 2.dp
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Text(
                        text = if (selectedTabIndex == 0) "Create Travel Destination (Room)" else "Save Quick Place",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = ProfessionalTextPrimary
                    )
                    Spacer(modifier = Modifier.height(12.dp))

                    OutlinedTextField(
                        value = destName,
                        onValueChange = { destName = it },
                        placeholder = { Text("Destination Name (e.g. Airport, Station, Office)") },
                        modifier = Modifier.fillMaxWidth().testTag("dest_name_input"),
                        singleLine = true,
                        shape = RoundedCornerShape(14.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = ProfessionalPrimary,
                            unfocusedBorderColor = ProfessionalBorder
                        )
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    OutlinedTextField(
                        value = destAddress,
                        onValueChange = { destAddress = it },
                        placeholder = { Text("Address / Concourse / Gate") },
                        modifier = Modifier.fillMaxWidth().testTag("dest_address_input"),
                        singleLine = true,
                        shape = RoundedCornerShape(14.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = ProfessionalPrimary,
                            unfocusedBorderColor = ProfessionalBorder
                        )
                    )

                    if (selectedTabIndex == 0) {
                        Spacer(modifier = Modifier.height(8.dp))
                        OutlinedTextField(
                            value = departurePointInput,
                            onValueChange = { departurePointInput = it },
                            placeholder = { Text("Departure Station / Origin") },
                            modifier = Modifier.fillMaxWidth().testTag("dest_departure_input"),
                            singleLine = true,
                            shape = RoundedCornerShape(14.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = ProfessionalPrimary,
                                unfocusedBorderColor = ProfessionalBorder
                            )
                        )

                        Spacer(modifier = Modifier.height(8.dp))
                        OutlinedTextField(
                            value = arrivalTimeInput,
                            onValueChange = { arrivalTimeInput = it },
                            placeholder = { Text("Arrival Time (e.g. 09:30 AM)") },
                            modifier = Modifier.fillMaxWidth().testTag("dest_arrival_input"),
                            singleLine = true,
                            shape = RoundedCornerShape(14.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = ProfessionalPrimary,
                                unfocusedBorderColor = ProfessionalBorder
                            )
                        )

                        Spacer(modifier = Modifier.height(10.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("Alarm Distance Buffer: ${alarmDistInput}m", style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.SemiBold)
                            Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                listOf(250, 500, 1000).forEach { dist ->
                                    Surface(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(8.dp))
                                            .clickable { alarmDistInput = dist },
                                        color = if (alarmDistInput == dist) ProfessionalPrimary else ProfessionalSurfaceVariant
                                    ) {
                                        Text(
                                            text = "${dist}m",
                                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                            style = MaterialTheme.typography.labelSmall,
                                            color = if (alarmDistInput == dist) Color.White else ProfessionalTextPrimary
                                        )
                                    }
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    Row(horizontalArrangement = Arrangement.End, modifier = Modifier.fillMaxWidth()) {
                        Button(
                            onClick = {
                                if (destName.isNotBlank()) {
                                    if (selectedTabIndex == 0) {
                                        val entity = TravelDestinationEntity(
                                            id = UUID.randomUUID().toString(),
                                            destinationName = destName.trim(),
                                            destinationAddress = if (destAddress.isBlank()) "Central Concourse" else destAddress.trim(),
                                            destinationLatitude = 18.5204 + (Math.random() * 0.05),
                                            destinationLongitude = 73.8567 + (Math.random() * 0.05),
                                            departurePoint = if (departurePointInput.isBlank()) "Origin Station" else departurePointInput.trim(),
                                            scheduledArrivalTime = System.currentTimeMillis() + 2400_000,
                                            arrivalTimeFormatted = if (arrivalTimeInput.isBlank()) "09:30 AM" else arrivalTimeInput.trim(),
                                            alarmDistanceMeters = alarmDistInput,
                                            alarmSoundEnabled = soundEnabled,
                                            alarmVibrationEnabled = vibrationEnabled,
                                            progressiveAlarmEnabled = true,
                                            transportMode = TransportMode.TRAIN.name,
                                            notes = "Added via destination manager"
                                        )
                                        viewModel.saveTravelDestination(entity)
                                        Toast.makeText(context, "Travel destination saved to Room database!", Toast.LENGTH_SHORT).show()
                                    } else {
                                        viewModel.addSavedPlace(
                                            name = destName.trim(),
                                            address = if (destAddress.isBlank()) "Transit Hub" else destAddress.trim(),
                                            lat = 18.5204 + (Math.random() * 0.05),
                                            lon = 73.8567 + (Math.random() * 0.05)
                                        )
                                    }
                                    destName = ""
                                    destAddress = ""
                                    showAddDialog = false
                                }
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = ProfessionalPrimary, contentColor = Color.White),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Text("Save to Room", fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
            Spacer(modifier = Modifier.height(16.dp))
        }

        // List Content
        if (selectedTabIndex == 0) {
            // Room Travel Destinations List
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(travelDestinations) { dest ->
                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(20.dp))
                            .border(BorderStroke(1.dp, ProfessionalBorder), RoundedCornerShape(20.dp))
                            .clickable {
                                viewModel.selectTravelDestination(dest)
                                onPlaceSelected()
                            }
                            .testTag("travel_destination_${dest.id}"),
                        color = ProfessionalSurface,
                        shape = RoundedCornerShape(20.dp),
                        shadowElevation = 1.dp
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                                    Box(
                                        modifier = Modifier
                                            .size(44.dp)
                                            .clip(CircleShape)
                                            .background(ProfessionalPrimaryContainer),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Train,
                                            contentDescription = null,
                                            tint = ProfessionalPrimary,
                                            modifier = Modifier.size(24.dp)
                                        )
                                    }
                                    Spacer(modifier = Modifier.width(12.dp))
                                    Column {
                                        Text(
                                            text = dest.destinationName,
                                            style = MaterialTheme.typography.titleMedium,
                                            fontWeight = FontWeight.Bold,
                                            color = ProfessionalTextPrimary
                                        )
                                        Text(
                                            text = dest.destinationAddress,
                                            style = MaterialTheme.typography.bodySmall,
                                            color = ProfessionalTextSecondary
                                        )
                                    }
                                }

                                IconButton(
                                    onClick = { viewModel.deleteTravelDestinationById(dest.id) },
                                    modifier = Modifier.size(36.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Delete,
                                        contentDescription = "Delete",
                                        tint = ProfessionalTextSecondary
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(10.dp))
                            HorizontalDivider(color = ProfessionalDivider, thickness = 1.dp)
                            Spacer(modifier = Modifier.height(10.dp))

                            // Departure Point, Arrival Time, and Alarm Settings details
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Column {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(
                                            imageVector = Icons.Default.LocationOn,
                                            contentDescription = null,
                                            tint = ProfessionalPrimary,
                                            modifier = Modifier.size(14.dp)
                                        )
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text(
                                            text = "From: ${dest.departurePoint}",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = ProfessionalTextSecondary
                                        )
                                    }
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(
                                            imageVector = Icons.Default.AccessTime,
                                            contentDescription = null,
                                            tint = ProfessionalPrimary,
                                            modifier = Modifier.size(14.dp)
                                        )
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text(
                                            text = "ETA: ${dest.arrivalTimeFormatted}",
                                            style = MaterialTheme.typography.bodySmall,
                                            fontWeight = FontWeight.SemiBold,
                                            color = ProfessionalPrimary
                                        )
                                    }
                                }

                                Column(horizontalAlignment = Alignment.End) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(
                                            imageVector = Icons.Default.Notifications,
                                            contentDescription = null,
                                            tint = ProfessionalPrimary,
                                            modifier = Modifier.size(14.dp)
                                        )
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text(
                                            text = "Alert: ${dest.alarmDistanceMeters}m",
                                            style = MaterialTheme.typography.bodySmall,
                                            fontWeight = FontWeight.Bold,
                                            color = ProfessionalTextPrimary
                                        )
                                    }
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = if (dest.alarmSoundEnabled) "Sound + Haptics" else "Haptics Only",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = ProfessionalTextSecondary
                                    )
                                }
                            }
                        }
                    }
                }
            }
        } else {
            // Quick Saved Places List
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(savedPlaces) { place ->
                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(20.dp))
                            .border(BorderStroke(1.dp, ProfessionalBorder), RoundedCornerShape(20.dp))
                            .clickable {
                                val item = DestinationItem(
                                    id = place.id,
                                    name = place.name,
                                    address = place.address,
                                    latitude = place.latitude,
                                    longitude = place.longitude,
                                    placeType = place.iconType,
                                    distanceKm = 7.2f,
                                    estimatedMinutes = 20
                                )
                                viewModel.selectDestination(item)
                                onPlaceSelected()
                            }
                            .testTag("saved_place_item_${place.id}"),
                        color = ProfessionalSurface,
                        shape = RoundedCornerShape(20.dp),
                        shadowElevation = 1.dp
                    ) {
                        Row(
                            modifier = Modifier.padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                                Box(
                                    modifier = Modifier
                                        .size(44.dp)
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
                                    Text(text = emoji, fontSize = 22.sp)
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
                                }
                            }

                            IconButton(
                                onClick = { viewModel.deleteSavedPlace(place.id) },
                                modifier = Modifier.size(36.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Delete,
                                    contentDescription = "Delete",
                                    tint = ProfessionalTextSecondary
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
