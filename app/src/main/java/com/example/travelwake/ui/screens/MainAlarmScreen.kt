package com.example.travelwake.ui.screens

import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
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
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
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
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.travelwake.engine.LocationEngine
import com.example.travelwake.ui.components.SevereWeatherAlertCard
import com.example.travelwake.ui.components.VoiceCommandDialog
import com.example.travelwake.voice.VoiceCommand
import com.example.travelwake.viewmodel.TravelWakeViewModel
import com.example.ui.theme.AlarmRed
import com.example.ui.theme.AlarmRedContainer
import com.example.ui.theme.AlertAmber
import com.example.ui.theme.AlertAmberContainer
import com.example.ui.theme.ProfessionalBackground
import com.example.ui.theme.ProfessionalBorder
import com.example.ui.theme.ProfessionalOnPrimaryContainer
import com.example.ui.theme.ProfessionalPrimary
import com.example.ui.theme.ProfessionalPrimaryContainer
import com.example.ui.theme.ProfessionalSurface
import com.example.ui.theme.ProfessionalTextPrimary
import com.example.ui.theme.ProfessionalTextSecondary
import com.example.ui.theme.SafetyGreen
import com.example.ui.theme.SafetyGreenContainer

@Composable
fun MainAlarmScreen(
    viewModel: TravelWakeViewModel,
    onAwakeConfirmed: () -> Unit,
    modifier: Modifier = Modifier
) {
    val destination by viewModel.selectedDestination.collectAsState()
    val distance by viewModel.remainingDistanceMeters.collectAsState()
    val alertDistance by viewModel.alertDistanceMeters.collectAsState()
    val weather by viewModel.destinationWeather.collectAsState()
    val belongings by viewModel.belongings.collectAsState()
    var showVoiceDialog by remember { mutableStateOf(false) }

    // High contrast alarm pulsating animation
    val infiniteTransition = rememberInfiniteTransition(label = "alarm_bell_pulse")
    val bellScale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.15f,
        animationSpec = infiniteRepeatable(
            animation = tween(400),
            repeatMode = RepeatMode.Reverse
        ),
        label = "bellScale"
    )

    val scrollState = rememberScrollState()

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(ProfessionalBackground)
            .verticalScroll(scrollState)
            .padding(horizontal = 24.dp, vertical = 24.dp)
            .testTag("main_alarm_screen"),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.fillMaxWidth()
        ) {
            Spacer(modifier = Modifier.height(16.dp))

            // Animated large Alarm Bell icon in AlarmRedContainer
            Box(
                modifier = Modifier
                    .size(96.dp)
                    .scale(bellScale)
                    .clip(CircleShape)
                    .background(AlarmRedContainer)
                    .border(BorderStroke(2.dp, AlarmRed), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Text(text = "🔔", fontSize = 48.sp)
            }

            Spacer(modifier = Modifier.height(18.dp))

            // High Contrast Typography WAKE UP!
            Text(
                text = "WAKE UP!",
                style = MaterialTheme.typography.displayMedium,
                fontWeight = FontWeight.Black,
                color = AlarmRed,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(6.dp))

            val displayDistance = LocationEngine.formatDistance(if (distance > 0) distance else alertDistance)
            Text(
                text = "Your stop is $displayDistance away",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = ProfessionalTextPrimary,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(10.dp))

            Surface(
                color = ProfessionalPrimaryContainer,
                shape = RoundedCornerShape(16.dp),
                border = BorderStroke(1.dp, ProfessionalPrimary.copy(alpha = 0.3f))
            ) {
                Text(
                    text = destination?.name ?: "Destination Approaching",
                    modifier = Modifier.padding(horizontal = 18.dp, vertical = 10.dp),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = ProfessionalOnPrimaryContainer,
                    textAlign = TextAlign.Center
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Severe Weather Alert for destination if active
            SevereWeatherAlertCard(
                viewModel = viewModel,
                modifier = Modifier.padding(bottom = 12.dp)
            )

            Spacer(modifier = Modifier.height(20.dp))

            // Critical Weather Warning Banner
            if (weather.recommendations.isNotEmpty()) {
                val primaryRec = weather.recommendations.first()
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(18.dp),
                    color = AlertAmberContainer,
                    border = BorderStroke(1.5.dp, AlertAmber.copy(alpha = 0.5f))
                ) {
                    Row(
                        modifier = Modifier.padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(text = primaryRec.icon, fontSize = 24.sp)
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                text = "${weather.temperatureCelsius}°C • ${primaryRec.title}",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold,
                                color = AlertAmber
                            )
                            Text(
                                text = primaryRec.description,
                                style = MaterialTheme.typography.bodySmall,
                                color = ProfessionalTextPrimary
                            )
                        }
                    }
                }
                Spacer(modifier = Modifier.height(18.dp))
            }

            // CHECK BELONGINGS BEFORE GETTING DOWN
            Surface(
                modifier = Modifier.fillMaxWidth().testTag("alarm_belongings_card"),
                shape = RoundedCornerShape(24.dp),
                color = ProfessionalSurface,
                border = BorderStroke(1.dp, ProfessionalBorder),
                shadowElevation = 1.dp
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Text(
                        text = "CHECK BEFORE GETTING DOWN",
                        style = MaterialTheme.typography.labelLarge,
                        color = ProfessionalPrimary,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(12.dp))

                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        belongings.take(5).forEach { item ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(10.dp))
                                    .clickable { viewModel.toggleBelonging(item) }
                                    .padding(vertical = 4.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Checkbox(
                                    checked = item.isChecked,
                                    onCheckedChange = { viewModel.toggleBelonging(item) },
                                    colors = CheckboxDefaults.colors(
                                        checkedColor = SafetyGreen,
                                        checkmarkColor = Color.White,
                                        uncheckedColor = ProfessionalBorder
                                    )
                                )
                                Text(
                                    text = item.name,
                                    style = MaterialTheme.typography.titleMedium,
                                    color = if (item.isChecked) SafetyGreen else ProfessionalTextPrimary,
                                    fontWeight = if (item.isChecked) FontWeight.Bold else FontWeight.Medium
                                )
                            }
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(28.dp))

        // Large Action Buttons
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Primary Action: "I'M AWAKE" (64dp height)
            Button(
                onClick = {
                    viewModel.acknowledgeAlarm()
                    onAwakeConfirmed()
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(60.dp)
                    .testTag("awake_button"),
                shape = RoundedCornerShape(18.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = SafetyGreen,
                    contentColor = Color.White
                ),
                elevation = ButtonDefaults.buttonElevation(defaultElevation = 2.dp)
            ) {
                Text(
                    text = "I'M AWAKE",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp
                )
            }

            // Secondary: Snooze (2 minutes)
            OutlinedButton(
                onClick = {
                    viewModel.snoozeAlarm()
                    onAwakeConfirmed()
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp)
                    .testTag("snooze_button"),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.outlinedButtonColors(contentColor = ProfessionalTextPrimary),
                border = BorderStroke(1.5.dp, ProfessionalBorder)
            ) {
                Text(
                    text = "Snooze (2 min)",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
                )
            }

            // Hands-free Voice Command Action
            OutlinedButton(
                onClick = { showVoiceDialog = true },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
                    .testTag("alarm_voice_command_button"),
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.outlinedButtonColors(contentColor = ProfessionalPrimary),
                border = BorderStroke(1.dp, ProfessionalPrimary.copy(alpha = 0.6f))
            ) {
                Icon(
                    imageVector = Icons.Default.Mic,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp),
                    tint = ProfessionalPrimary
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Hands-Free Voice (Say \"Snooze\" or \"I'm Awake\")",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }

    if (showVoiceDialog) {
        VoiceCommandDialog(
            viewModel = viewModel,
            onDismiss = { showVoiceDialog = false },
            onActionExecuted = { action ->
                if (action is VoiceCommand.SnoozeAlarm || action is VoiceCommand.CancelAlarm) {
                    onAwakeConfirmed()
                }
            }
        )
    }
}
