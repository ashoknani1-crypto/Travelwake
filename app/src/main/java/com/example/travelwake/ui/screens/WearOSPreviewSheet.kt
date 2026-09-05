package com.example.travelwake.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.travelwake.engine.LocationEngine
import com.example.travelwake.viewmodel.TravelWakeViewModel
import com.example.ui.theme.AlarmRed
import com.example.ui.theme.ProfessionalBackground
import com.example.ui.theme.ProfessionalBorder
import com.example.ui.theme.ProfessionalPrimary
import com.example.ui.theme.ProfessionalPrimaryContainer
import com.example.ui.theme.ProfessionalSurface
import com.example.ui.theme.ProfessionalTextPrimary
import com.example.ui.theme.ProfessionalTextSecondary
import com.example.ui.theme.SafetyGreen

@Composable
fun WearOSPreviewScreen(
    viewModel: TravelWakeViewModel,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val destination by viewModel.selectedDestination.collectAsState()
    val distance by viewModel.remainingDistanceMeters.collectAsState()
    val alertDistance by viewModel.alertDistanceMeters.collectAsState()
    val weather by viewModel.destinationWeather.collectAsState()

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(ProfessionalBackground)
            .padding(horizontal = 20.dp, vertical = 12.dp)
            .testTag("wear_os_preview_screen"),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // App Bar
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onBack, modifier = Modifier.testTag("wear_os_back_button")) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Back",
                    tint = ProfessionalTextPrimary
                )
            }
            Text(
                text = "Wear OS Companion Preview",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = ProfessionalTextPrimary
            )
        }

        Spacer(modifier = Modifier.height(14.dp))

        Text(
            text = "Circular Smartwatch Display Simulation",
            style = MaterialTheme.typography.bodyMedium,
            color = ProfessionalTextSecondary
        )

        Spacer(modifier = Modifier.height(24.dp))

        // Circular Smartwatch Frame
        Box(
            modifier = Modifier
                .size(300.dp)
                .clip(CircleShape)
                .background(Color(0xFF0F141C))
                .border(BorderStroke(8.dp, Color(0xFF2C313B)), CircleShape)
                .border(BorderStroke(2.dp, ProfessionalPrimary), CircleShape)
                .padding(24.dp),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxSize()
            ) {
                // Top watch indicator
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Text(text = "🔔", fontSize = 16.sp)
                    Text(
                        text = " WAKE UP",
                        style = MaterialTheme.typography.labelSmall,
                        color = AlarmRed,
                        fontWeight = FontWeight.Black
                    )
                }

                // Center watch info
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = LocationEngine.formatDistance(if (distance > 0) distance else alertDistance),
                        style = MaterialTheme.typography.displaySmall,
                        color = Color.White,
                        fontWeight = FontWeight.Black
                    )
                    Text(
                        text = destination?.name ?: "Pune Station",
                        style = MaterialTheme.typography.labelMedium,
                        color = Color.White.copy(alpha = 0.9f),
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center,
                        maxLines = 1
                    )
                    Text(
                        text = "${weather.temperatureCelsius}°C ${if (weather.rainProbability > 40) "🌧 Umbrella" else "🌤 Clear"}",
                        style = MaterialTheme.typography.labelSmall,
                        color = ProfessionalPrimaryContainer
                    )
                }

                // Watch Action: AWAKE Button
                Button(
                    onClick = { viewModel.acknowledgeAlarm() },
                    modifier = Modifier
                        .fillMaxWidth(0.85f)
                        .height(46.dp)
                        .testTag("wear_os_awake_button"),
                    shape = RoundedCornerShape(23.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = SafetyGreen)
                ) {
                    Text(
                        text = "AWAKE",
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.Black,
                        color = Color.White
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(28.dp))

        Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(20.dp),
            color = ProfessionalSurface,
            border = BorderStroke(1.dp, ProfessionalBorder),
            shadowElevation = 1.dp
        ) {
            Column(modifier = Modifier.padding(18.dp)) {
                Text(
                    text = "Smartwatch Integration Highlights",
                    style = MaterialTheme.typography.titleSmall,
                    color = ProfessionalPrimary,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = "• Vibrates on your wrist before you reach the station\n• Shows remaining distance in crisp typography\n• Displays quick weather warning (e.g. carry umbrella)\n• One-tap dismiss directly from your watch",
                    style = MaterialTheme.typography.bodySmall,
                    color = ProfessionalTextPrimary
                )
            }
        }
    }
}
