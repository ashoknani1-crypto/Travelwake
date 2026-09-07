package com.example.travelwake.ui.screens

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
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.BugReport
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CloudUpload
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Map
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.Place
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.SignalCellularOff
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material.icons.filled.Vibration
import androidx.compose.material.icons.filled.Watch
import com.example.travelwake.engine.HapticFeedbackProfile
import com.example.travelwake.engine.SoundAlertTheme
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
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
import com.example.travelwake.theme.WeatherThemeEngine
import com.example.travelwake.theme.WeatherThemeType
import com.example.travelwake.ui.components.AlertDistanceSelector
import com.example.travelwake.ui.components.GlassCard
import com.example.travelwake.ui.components.OfflineMapDownloadCard
import com.example.travelwake.ui.components.SevereWeatherAlertCard
import com.example.travelwake.ui.components.ThemeModeSwitcherCard
import com.example.travelwake.viewmodel.TravelWakeViewModel
import com.example.ui.theme.AlertAmber
import com.example.ui.theme.BrightCyan
import com.example.ui.theme.LocalWeatherThemePalette
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
import com.example.ui.theme.SafetyGreen
import com.example.ui.theme.SafetyGreenContainer

@Composable
fun SettingsScreen(
    viewModel: TravelWakeViewModel,
    onNavigateToHistory: () -> Unit,
    onNavigateToSavedPlaces: () -> Unit,
    onNavigateToBelongings: () -> Unit,
    onNavigateToSecurityLab: () -> Unit,
    onNavigateToWearOSPreview: () -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val currentUser by viewModel.currentUser.collectAsState()
    val isAuthLoading by viewModel.isAuthLoading.collectAsState()
    val authStatusMessage by viewModel.authStatusMessage.collectAsState()
    val alertDistance by viewModel.alertDistanceMeters.collectAsState()
    val transportMode by viewModel.transportMode.collectAsState()
    val currentTheme by viewModel.weatherTheme.collectAsState()
    val autoThemeEnabled by viewModel.autoWeatherThemeEnabled.collectAsState()
    val selectedSoundTheme by viewModel.selectedSoundAlertTheme.collectAsState()
    val selectedHapticProfile by viewModel.selectedHapticProfile.collectAsState()
    val tileCacheStats by viewModel.offlineTileCacheStats.collectAsState()
    val batterySaverEnabled by viewModel.batterySaverEnabled.collectAsState()
    val batterySaverInfo by viewModel.batterySaverInfo.collectAsState()

    var progressiveAlertsEnabled by remember { mutableStateOf(true) }
    var soundEnabled by remember { mutableStateOf(true) }
    var vibrationEnabled by remember { mutableStateOf(true) }
    var weatherWarningsEnabled by remember { mutableStateOf(true) }
    var isPreviewingSound by remember { mutableStateOf(false) }

    val scrollState = rememberScrollState()

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(ProfessionalBackground)
            .verticalScroll(scrollState)
            .padding(horizontal = 20.dp, vertical = 12.dp)
            .testTag("settings_screen")
    ) {
        // App Bar
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onBack, modifier = Modifier.testTag("settings_back_button")) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Back",
                    tint = ProfessionalTextPrimary
                )
            }
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = "Settings & Preferences",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = ProfessionalTextPrimary
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Google Sign-In & Firebase Auth Card
        GlassCard(
            modifier = Modifier.fillMaxWidth().testTag("firebase_account_card"),
            cornerRadius = 24
        ) {
            Column {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(46.dp)
                                .clip(CircleShape)
                                .background(ProfessionalSurfaceVariant),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.AccountCircle,
                                contentDescription = "User profile",
                                tint = ProfessionalPrimary,
                                modifier = Modifier.size(32.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                text = if (currentUser?.isAnonymous == false) currentUser?.displayName ?: "User" else "Guest Traveler",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = ProfessionalTextPrimary
                            )
                            Text(
                                text = if (currentUser?.isAnonymous == false) currentUser?.email ?: "" else "Local mode • Sign in to sync across devices",
                                style = MaterialTheme.typography.bodySmall,
                                color = ProfessionalTextSecondary
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                val context = LocalContext.current

                if (authStatusMessage != null) {
                    Text(
                        text = authStatusMessage!!,
                        style = MaterialTheme.typography.bodySmall,
                        color = BrightCyan,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                }

                if (currentUser?.isAnonymous != false) {
                    Button(
                        onClick = {
                            viewModel.signInWithCredentialManager(context)
                        },
                        enabled = !isAuthLoading,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp)
                            .testTag("google_sign_in_button"),
                        shape = RoundedCornerShape(14.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = ProfessionalPrimary,
                            contentColor = Color.White
                        )
                    ) {
                        if (isAuthLoading) {
                            CircularProgressIndicator(
                                color = Color.White,
                                strokeWidth = 2.dp,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Signing In...", fontWeight = FontWeight.Bold)
                        } else {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(imageVector = Icons.Default.CloudUpload, contentDescription = null, modifier = Modifier.size(18.dp))
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Sign In with Google (Credential Manager)", fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                } else {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Surface(
                            color = SafetyGreenContainer,
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(imageVector = Icons.Default.Check, contentDescription = null, tint = SafetyGreen, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = if (currentUser?.provider?.contains("google") == true) "Google Account Synced" else "Firebase Synced",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = SafetyGreen,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                        OutlinedButton(
                            onClick = { viewModel.signOut() },
                            shape = RoundedCornerShape(12.dp),
                            border = BorderStroke(1.dp, ProfessionalBorder),
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = ProfessionalTextPrimary)
                        ) {
                            Text("Sign Out", style = MaterialTheme.typography.labelSmall)
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Navigation shortcuts styled with Professional Polish Action items
        Text(
            text = "Manage Data",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = ProfessionalTextPrimary
        )
        Spacer(modifier = Modifier.height(10.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            OutlinedButton(
                onClick = onNavigateToHistory,
                modifier = Modifier.weight(1f).height(48.dp).testTag("history_nav_button"),
                shape = RoundedCornerShape(14.dp),
                border = BorderStroke(1.dp, ProfessionalBorder),
                colors = ButtonDefaults.outlinedButtonColors(contentColor = ProfessionalTextPrimary)
            ) {
                Icon(imageVector = Icons.Default.History, contentDescription = null, tint = ProfessionalPrimary, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text("Trips", style = MaterialTheme.typography.labelMedium)
            }

            OutlinedButton(
                onClick = onNavigateToSavedPlaces,
                modifier = Modifier.weight(1f).height(48.dp).testTag("saved_places_nav_button"),
                shape = RoundedCornerShape(14.dp),
                border = BorderStroke(1.dp, ProfessionalBorder),
                colors = ButtonDefaults.outlinedButtonColors(contentColor = ProfessionalTextPrimary)
            ) {
                Icon(imageVector = Icons.Default.Place, contentDescription = null, tint = ProfessionalPrimary, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text("Saved", style = MaterialTheme.typography.labelMedium)
            }

            OutlinedButton(
                onClick = onNavigateToBelongings,
                modifier = Modifier.weight(1f).height(48.dp).testTag("belongings_nav_button"),
                shape = RoundedCornerShape(14.dp),
                border = BorderStroke(1.dp, ProfessionalBorder),
                colors = ButtonDefaults.outlinedButtonColors(contentColor = ProfessionalTextPrimary)
            ) {
                Text("🎒 Pack", style = MaterialTheme.typography.labelMedium)
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Default Alert Distance
        GlassCard(modifier = Modifier.fillMaxWidth(), cornerRadius = 24) {
            AlertDistanceSelector(
                selectedMeters = alertDistance,
                onDistanceSelected = { viewModel.setAlertDistance(it) }
            )
        }

        Spacer(modifier = Modifier.height(18.dp))

        // Alarm & Notification Toggles
        GlassCard(modifier = Modifier.fillMaxWidth(), cornerRadius = 24) {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text("Progressive Pre-Alarms", style = MaterialTheme.typography.titleSmall, color = ProfessionalTextPrimary, fontWeight = FontWeight.Bold)
                        Text("Gentle chime at 1km, 750m before main alert", style = MaterialTheme.typography.bodySmall, color = ProfessionalTextSecondary)
                    }
                    Switch(
                        checked = progressiveAlertsEnabled,
                        onCheckedChange = { progressiveAlertsEnabled = it },
                        colors = SwitchDefaults.colors(checkedThumbColor = Color.White, checkedTrackColor = ProfessionalPrimary)
                    )
                }

                HorizontalDivider(color = ProfessionalDivider, thickness = 1.dp)

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text("Alarm Sound", style = MaterialTheme.typography.titleSmall, color = ProfessionalTextPrimary, fontWeight = FontWeight.Bold)
                        Text("Plays high-priority wake alarm audio", style = MaterialTheme.typography.bodySmall, color = ProfessionalTextSecondary)
                    }
                    Switch(
                        checked = soundEnabled,
                        onCheckedChange = { soundEnabled = it },
                        colors = SwitchDefaults.colors(checkedThumbColor = Color.White, checkedTrackColor = ProfessionalPrimary)
                    )
                }

                HorizontalDivider(color = ProfessionalDivider, thickness = 1.dp)

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text("Alarm Vibration", style = MaterialTheme.typography.titleSmall, color = ProfessionalTextPrimary, fontWeight = FontWeight.Bold)
                        Text("Rhythmic wake-up vibration pulses", style = MaterialTheme.typography.bodySmall, color = ProfessionalTextSecondary)
                    }
                    Switch(
                        checked = vibrationEnabled,
                        onCheckedChange = { vibrationEnabled = it },
                        colors = SwitchDefaults.colors(checkedThumbColor = Color.White, checkedTrackColor = ProfessionalPrimary)
                    )
                }

                HorizontalDivider(color = ProfessionalDivider, thickness = 1.dp)

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text("Weather & Packing Advisories", style = MaterialTheme.typography.titleSmall, color = ProfessionalTextPrimary, fontWeight = FontWeight.Bold)
                        Text("Shows rain, temperature, and umbrella alerts", style = MaterialTheme.typography.bodySmall, color = ProfessionalTextSecondary)
                    }
                    Switch(
                        checked = weatherWarningsEnabled,
                        onCheckedChange = { weatherWarningsEnabled = it },
                        colors = SwitchDefaults.colors(checkedThumbColor = Color.White, checkedTrackColor = ProfessionalPrimary)
                    )
                }

                HorizontalDivider(color = ProfessionalDivider, thickness = 1.dp)

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text("Adaptive Battery-Saving Mode", style = MaterialTheme.typography.titleSmall, color = ProfessionalTextPrimary, fontWeight = FontWeight.Bold)
                        Text(
                            if (batterySaverEnabled) "Active: decreases GPS polling frequency when >10km away from destination" else "Disabled: polls GPS frequently throughout journey",
                            style = MaterialTheme.typography.bodySmall,
                            color = if (batterySaverEnabled) SafetyGreen else ProfessionalTextSecondary
                        )
                    }
                    Switch(
                        checked = batterySaverEnabled,
                        onCheckedChange = { viewModel.toggleBatterySavingMode() },
                        colors = SwitchDefaults.colors(checkedThumbColor = Color.White, checkedTrackColor = SafetyGreen),
                        modifier = Modifier.testTag("settings_battery_saver_switch")
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Custom Sound Alert Themes (Synthesized Audio Engine)
        Text(
            text = "Alarm Sound Alert Tone",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = ProfessionalTextPrimary
        )
        Spacer(modifier = Modifier.height(10.dp))

        GlassCard(modifier = Modifier.fillMaxWidth().testTag("sound_theme_settings_card"), cornerRadius = 24) {
            Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Synthesized High-Penetration Tone",
                            style = MaterialTheme.typography.titleSmall,
                            color = ProfessionalTextPrimary,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Acoustically engineered audio designed to wake travelers in loud environments",
                            style = MaterialTheme.typography.bodySmall,
                            color = ProfessionalTextSecondary
                        )
                    }
                }

                HorizontalDivider(color = ProfessionalDivider, thickness = 1.dp)

                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    SoundAlertTheme.values().forEach { theme ->
                        val isSelected = selectedSoundTheme == theme
                        Surface(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(14.dp))
                                .border(
                                    BorderStroke(
                                        if (isSelected) 2.dp else 1.dp,
                                        if (isSelected) ProfessionalPrimary else ProfessionalBorder
                                    ),
                                    RoundedCornerShape(14.dp)
                                )
                                .clickable { viewModel.setSoundAlertTheme(theme) }
                                .testTag("sound_theme_item_${theme.id}"),
                            color = if (isSelected) ProfessionalPrimaryContainer else ProfessionalSurface,
                            shape = RoundedCornerShape(14.dp)
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 14.dp, vertical = 12.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                                    Icon(
                                        imageVector = Icons.Default.MusicNote,
                                        contentDescription = null,
                                        tint = if (isSelected) ProfessionalPrimary else ProfessionalTextSecondary,
                                        modifier = Modifier.size(20.dp)
                                    )
                                    Spacer(modifier = Modifier.width(12.dp))
                                    Column {
                                        Text(
                                            text = theme.displayName,
                                            style = MaterialTheme.typography.titleSmall,
                                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                            color = if (isSelected) ProfessionalPrimary else ProfessionalTextPrimary
                                        )
                                        Text(
                                            text = theme.description,
                                            style = MaterialTheme.typography.bodySmall,
                                            color = ProfessionalTextSecondary,
                                            fontSize = 12.sp
                                        )
                                    }
                                }

                                if (isSelected) {
                                    Icon(
                                        imageVector = Icons.Default.Check,
                                        contentDescription = "Selected",
                                        tint = ProfessionalPrimary,
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                            }
                        }
                    }
                }

                OutlinedButton(
                    onClick = {
                        if (isPreviewingSound) {
                            viewModel.stopSoundAndHapticPreview()
                            isPreviewingSound = false
                        } else {
                            viewModel.previewSoundAlert(selectedSoundTheme)
                            isPreviewingSound = true
                        }
                    },
                    modifier = Modifier.fillMaxWidth().height(46.dp).testTag("preview_sound_button"),
                    shape = RoundedCornerShape(12.dp),
                    border = BorderStroke(1.dp, ProfessionalPrimary),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = ProfessionalPrimary)
                ) {
                    Icon(
                        imageVector = if (isPreviewingSound) Icons.Default.Stop else Icons.Default.PlayArrow,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(if (isPreviewingSound) "Stop Sound Preview" else "Preview ${selectedSoundTheme.displayName}")
                }
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Custom Haptic Feedback Profiles
        Text(
            text = "Haptic Vibration Alert Profile",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = ProfessionalTextPrimary
        )
        Spacer(modifier = Modifier.height(10.dp))

        GlassCard(modifier = Modifier.fillMaxWidth().testTag("haptic_profile_settings_card"), cornerRadius = 24) {
            Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Tactile Vibration Patterns",
                            style = MaterialTheme.typography.titleSmall,
                            color = ProfessionalTextPrimary,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Distinctive motor patterns that pierce through pocket fabric and commute motion",
                            style = MaterialTheme.typography.bodySmall,
                            color = ProfessionalTextSecondary
                        )
                    }
                }

                HorizontalDivider(color = ProfessionalDivider, thickness = 1.dp)

                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    HapticFeedbackProfile.values().forEach { profile ->
                        val isSelected = selectedHapticProfile == profile
                        Surface(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(14.dp))
                                .border(
                                    BorderStroke(
                                        if (isSelected) 2.dp else 1.dp,
                                        if (isSelected) ProfessionalPrimary else ProfessionalBorder
                                    ),
                                    RoundedCornerShape(14.dp)
                                )
                                .clickable { viewModel.setHapticProfile(profile) }
                                .testTag("haptic_profile_item_${profile.id}"),
                            color = if (isSelected) ProfessionalPrimaryContainer else ProfessionalSurface,
                            shape = RoundedCornerShape(14.dp)
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 14.dp, vertical = 12.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                                    Icon(
                                        imageVector = Icons.Default.Vibration,
                                        contentDescription = null,
                                        tint = if (isSelected) ProfessionalPrimary else ProfessionalTextSecondary,
                                        modifier = Modifier.size(20.dp)
                                    )
                                    Spacer(modifier = Modifier.width(12.dp))
                                    Column {
                                        Text(
                                            text = profile.displayName,
                                            style = MaterialTheme.typography.titleSmall,
                                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                            color = if (isSelected) ProfessionalPrimary else ProfessionalTextPrimary
                                        )
                                        Text(
                                            text = profile.description,
                                            style = MaterialTheme.typography.bodySmall,
                                            color = ProfessionalTextSecondary,
                                            fontSize = 12.sp
                                        )
                                    }
                                }

                                if (isSelected) {
                                    Icon(
                                        imageVector = Icons.Default.Check,
                                        contentDescription = "Selected",
                                        tint = ProfessionalPrimary,
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                            }
                        }
                    }
                }

                OutlinedButton(
                    onClick = { viewModel.previewHapticProfile(selectedHapticProfile) },
                    modifier = Modifier.fillMaxWidth().height(46.dp).testTag("test_haptics_button"),
                    shape = RoundedCornerShape(12.dp),
                    border = BorderStroke(1.dp, ProfessionalPrimary),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = ProfessionalPrimary)
                ) {
                    Icon(imageVector = Icons.Default.Vibration, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Test ${selectedHapticProfile.displayName}")
                }
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Offline Map Tile Storage & Signal Persistence
        Text(
            text = "Offline Map Tile Storage & Vector Cache",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = ProfessionalTextPrimary
        )
        Spacer(modifier = Modifier.height(10.dp))

        // Full Offline Map Tile Download & Region Cache Manager
        OfflineMapDownloadCard(
            viewModel = viewModel,
            destinationName = "Upcoming Journey Stop",
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(14.dp))

        GlassCard(modifier = Modifier.fillMaxWidth().testTag("offline_map_tile_settings_card"), cornerRadius = 24) {
            Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                        Icon(imageVector = Icons.Default.Map, contentDescription = null, tint = ProfessionalPrimary, modifier = Modifier.size(22.dp))
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = "Persistent Vector Map Tiles",
                                style = MaterialTheme.typography.titleSmall,
                                color = ProfessionalTextPrimary,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "Ensures route visualization persists when cellular signal is weak",
                                style = MaterialTheme.typography.bodySmall,
                                color = ProfessionalTextSecondary
                            )
                        }
                    }

                    Surface(
                        color = SafetyGreenContainer,
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Text(
                            text = "${tileCacheStats.totalTiles} TILES",
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                            style = MaterialTheme.typography.labelSmall,
                            color = SafetyGreen,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                HorizontalDivider(color = ProfessionalDivider, thickness = 1.dp)

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Surface(
                        modifier = Modifier.weight(1f),
                        color = ProfessionalSurface,
                        shape = RoundedCornerShape(12.dp),
                        border = BorderStroke(1.dp, ProfessionalBorder)
                    ) {
                        Column(modifier = Modifier.padding(10.dp)) {
                            Text("Cache Size", style = MaterialTheme.typography.labelSmall, color = ProfessionalTextSecondary)
                            Text("${tileCacheStats.totalSizeBytes / 1024} KB", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold, color = ProfessionalTextPrimary)
                        }
                    }

                    Surface(
                        modifier = Modifier.weight(1f),
                        color = ProfessionalSurface,
                        shape = RoundedCornerShape(12.dp),
                        border = BorderStroke(1.dp, ProfessionalBorder)
                    ) {
                        Column(modifier = Modifier.padding(10.dp)) {
                            Text("Supported Zooms", style = MaterialTheme.typography.labelSmall, color = ProfessionalTextSecondary)
                            Text("Z12 — Z16", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold, color = ProfessionalPrimary)
                        }
                    }
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                        Icon(imageVector = Icons.Default.SignalCellularOff, contentDescription = null, tint = AlertAmber, modifier = Modifier.size(20.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Column {
                            Text(
                                text = "Simulate Weak Signal Mode",
                                style = MaterialTheme.typography.titleSmall,
                                color = ProfessionalTextPrimary,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "Force offline tile rendering during tunnel travel",
                                style = MaterialTheme.typography.bodySmall,
                                color = ProfessionalTextSecondary
                            )
                        }
                    }
                    Switch(
                        checked = tileCacheStats.simulatedWeakSignal,
                        onCheckedChange = { viewModel.toggleWeakSignalSimulation() },
                        colors = SwitchDefaults.colors(checkedThumbColor = Color.White, checkedTrackColor = AlertAmber),
                        modifier = Modifier.testTag("weak_signal_simulation_switch")
                    )
                }

                OutlinedButton(
                    onClick = { viewModel.preCacheOfflineTilesForRoute() },
                    modifier = Modifier.fillMaxWidth().height(46.dp).testTag("precache_tiles_button"),
                    shape = RoundedCornerShape(12.dp),
                    border = BorderStroke(1.dp, ProfessionalBorder),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = ProfessionalTextPrimary)
                ) {
                    Icon(imageVector = Icons.Default.CloudUpload, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Pre-Cache Journey Route Tiles")
                }
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Dynamic Night & Day Theme Switcher (System auto-switching / light / dark)
        ThemeModeSwitcherCard(
            viewModel = viewModel,
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(20.dp))

        // Real-Time Severe Weather Notification & Alert Tester
        Text(
            text = "Severe Weather Destination Safety",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = ProfessionalTextPrimary
        )
        Spacer(modifier = Modifier.height(10.dp))

        SevereWeatherAlertCard(
            viewModel = viewModel,
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(8.dp))

        GlassCard(modifier = Modifier.fillMaxWidth().testTag("severe_weather_testing_card"), cornerRadius = 24) {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text(
                    text = "Emergency Warning Notification Simulator",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = ProfessionalTextPrimary
                )
                Text(
                    text = "Simulate and push real-time severe thunderstorm, blizzard, or flash flood alerts to the Android alarm notification tray.",
                    style = MaterialTheme.typography.bodySmall,
                    color = ProfessionalTextSecondary
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Button(
                        onClick = {
                            viewModel.triggerSimulatedSevereWeather(
                                hazard = "Severe Thunderstorm Warning",
                                headline = "Damaging 70 mph winds & severe lightning at destination",
                                emergencyAction = "Stay indoors upon arrival; do not exit station platform"
                            )
                        },
                        modifier = Modifier.weight(1f).testTag("sim_storm_alert_button"),
                        colors = ButtonDefaults.buttonColors(containerColor = AlertAmber),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text("Simulate Storm Alert", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold, color = Color.White)
                    }

                    OutlinedButton(
                        onClick = { viewModel.dismissSevereWeatherAlert() },
                        modifier = Modifier.weight(0.7f).testTag("dismiss_sim_alert_button"),
                        shape = RoundedCornerShape(12.dp),
                        border = BorderStroke(1.dp, ProfessionalBorder)
                    ) {
                        Text("Clear Alert", style = MaterialTheme.typography.labelSmall, color = ProfessionalTextSecondary)
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Dynamic Weather Theme Engine
        Text(
            text = "Dynamic Weather Theme Engine",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = ProfessionalTextPrimary
        )
        Spacer(modifier = Modifier.height(10.dp))

        GlassCard(modifier = Modifier.fillMaxWidth().testTag("weather_theme_engine_card"), cornerRadius = 24) {
            Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Auto-Adapt to Weather",
                            style = MaterialTheme.typography.titleSmall,
                            color = ProfessionalTextPrimary,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Dynamically recolors app based on destination forecast",
                            style = MaterialTheme.typography.bodySmall,
                            color = ProfessionalTextSecondary
                        )
                    }
                    Switch(
                        checked = autoThemeEnabled,
                        onCheckedChange = { viewModel.toggleAutoWeatherTheme(it) },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = Color.White,
                            checkedTrackColor = currentTheme.primaryColor
                        ),
                        modifier = Modifier.testTag("auto_weather_theme_switch")
                    )
                }

                HorizontalDivider(color = ProfessionalDivider, thickness = 1.dp)

                Text(
                    text = "Select Palette Manually:",
                    style = MaterialTheme.typography.labelMedium,
                    color = ProfessionalTextSecondary,
                    fontWeight = FontWeight.SemiBold
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    val palettes = listOf(
                        Triple("Sunny", "☀️", WeatherThemeEngine.SunnyPalette),
                        Triple("Rainy", "🌧️", WeatherThemeEngine.RainyPalette),
                        Triple("Cloudy", "⛅", WeatherThemeEngine.CloudyPalette),
                        Triple("Snowy", "❄️", WeatherThemeEngine.SnowyPalette)
                    )

                    palettes.forEach { (name, emoji, palette) ->
                        val isSelected = currentTheme.themeType == palette.themeType
                        Surface(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(12.dp))
                                .border(
                                    BorderStroke(
                                        if (isSelected) 2.dp else 1.dp,
                                        if (isSelected) palette.primaryColor else ProfessionalBorder
                                    ),
                                    RoundedCornerShape(12.dp)
                                )
                                .clickable { viewModel.setManualWeatherTheme(palette) }
                                .testTag("theme_preset_${name.lowercase()}"),
                            color = if (isSelected) palette.primaryContainerColor else ProfessionalSurface,
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Column(
                                modifier = Modifier.padding(vertical = 10.dp, horizontal = 4.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text(text = emoji, fontSize = 20.sp)
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = name,
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                    color = if (isSelected) palette.primaryColor else ProfessionalTextPrimary,
                                    fontSize = 11.sp
                                )
                            }
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Advanced Tools: Wear OS Preview & Security Lab
        Text(
            text = "Companion & Lab Tools",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = ProfessionalTextPrimary
        )
        Spacer(modifier = Modifier.height(10.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            OutlinedButton(
                onClick = onNavigateToWearOSPreview,
                modifier = Modifier.weight(1f).height(50.dp).testTag("wear_os_preview_button"),
                shape = RoundedCornerShape(14.dp),
                border = BorderStroke(1.dp, ProfessionalPrimary),
                colors = ButtonDefaults.outlinedButtonColors(contentColor = ProfessionalPrimary)
            ) {
                Icon(imageVector = Icons.Default.Watch, contentDescription = null, tint = ProfessionalPrimary, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text("Wear OS", fontWeight = FontWeight.Bold)
            }

            OutlinedButton(
                onClick = onNavigateToSecurityLab,
                modifier = Modifier.weight(1f).height(50.dp).testTag("security_lab_button"),
                shape = RoundedCornerShape(14.dp),
                border = BorderStroke(1.dp, ProfessionalBorder),
                colors = ButtonDefaults.outlinedButtonColors(contentColor = ProfessionalTextPrimary)
            ) {
                Icon(imageVector = Icons.Default.BugReport, contentDescription = null, tint = ProfessionalTextSecondary, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text("Security Lab", fontWeight = FontWeight.Bold)
            }
        }

        Spacer(modifier = Modifier.height(32.dp))
    }
}
