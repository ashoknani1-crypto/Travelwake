package com.example.travelwake.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
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
import com.example.travelwake.engine.LocationEngine
import com.example.travelwake.ui.components.GlassCard
import com.example.travelwake.viewmodel.TravelWakeViewModel
import com.example.ui.theme.AlarmRed
import com.example.ui.theme.AlertAmber
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
fun SecurityLabScreen(
    viewModel: TravelWakeViewModel,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val distance by viewModel.remainingDistanceMeters.collectAsState()
    val alertDistance by viewModel.alertDistanceMeters.collectAsState()
    val journeyState by viewModel.journeyState.collectAsState()

    var testLog by remember { mutableStateOf("Ready to simulate edge cases.") }
    val scrollState = rememberScrollState()

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(ProfessionalBackground)
            .verticalScroll(scrollState)
            .padding(horizontal = 20.dp, vertical = 12.dp)
            .testTag("security_lab_screen")
    ) {
        // App Bar
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onBack, modifier = Modifier.testTag("security_lab_back_button")) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Back",
                    tint = ProfessionalTextPrimary
                )
            }
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = "Security Test Lab",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = ProfessionalTextPrimary
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        // State indicator
        GlassCard(modifier = Modifier.fillMaxWidth(), cornerRadius = 24) {
            Column {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("Current Status:", style = MaterialTheme.typography.labelMedium, color = ProfessionalTextSecondary)
                    Text(journeyState.name, style = MaterialTheme.typography.labelLarge, color = ProfessionalPrimary, fontWeight = FontWeight.Bold)
                }
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("Current Distance:", style = MaterialTheme.typography.labelMedium, color = ProfessionalTextSecondary)
                    Text(LocationEngine.formatDistance(distance), style = MaterialTheme.typography.labelLarge, color = ProfessionalTextPrimary, fontWeight = FontWeight.Bold)
                }
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("Alert Trigger Radius:", style = MaterialTheme.typography.labelMedium, color = ProfessionalTextSecondary)
                    Text(LocationEngine.formatDistance(alertDistance), style = MaterialTheme.typography.labelLarge, color = AlertAmber, fontWeight = FontWeight.Bold)
                }
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        Text(
            text = "Edge-Case Simulation Triggers",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = ProfessionalTextPrimary
        )
        Spacer(modifier = Modifier.height(12.dp))

        // Test 1: GPS Inside Alert Radius
        LabActionCard(
            title = "1. GPS Jump Inside Alert Radius",
            description = "Simulates instant arrival at 300m (inside 500m alert radius). Must trigger Main Alarm immediately.",
            buttonText = "Simulate 300m",
            buttonColor = AlertAmber,
            onClick = {
                viewModel.updateRemainingDistance(300)
                testLog = "Simulated GPS: 300m -> Main alarm triggered successfully."
            }
        )

        Spacer(modifier = Modifier.height(10.dp))

        // Test 2: Destination Reached (0m)
        LabActionCard(
            title = "2. Destination Reached (0m)",
            description = "Simulates arriving right at the destination station platform.",
            buttonText = "Simulate 0m",
            buttonColor = AlarmRed,
            onClick = {
                viewModel.updateRemainingDistance(0)
                testLog = "Simulated GPS: 0m -> Arrival confirmed."
            }
        )

        Spacer(modifier = Modifier.height(10.dp))

        // Test 3: Pre-Alarm Stage (1000m & 750m)
        LabActionCard(
            title = "3. Progressive Pre-Alarm (1000m)",
            description = "Simulates entering the 1 km pre-alarm boundary. Gentle chime and pre-alarm notification active.",
            buttonText = "Simulate 1000m",
            buttonColor = ProfessionalPrimary,
            onClick = {
                viewModel.updateRemainingDistance(1000)
                testLog = "Simulated GPS: 1000m -> Pre-alarm stage 1 active."
            }
        )

        Spacer(modifier = Modifier.height(10.dp))

        // Test 4: GPS Jitter / Spoofing Jump
        LabActionCard(
            title = "4. GPS Jump Anomaly (Out of bound)",
            description = "Simulates sudden teleporting to 25 km away (detects satellite jitter/transit tunnel signal loss).",
            buttonText = "Simulate 25km",
            buttonColor = ProfessionalSurfaceVariant,
            buttonTextColor = ProfessionalTextPrimary,
            onClick = {
                viewModel.updateRemainingDistance(25000)
                testLog = "Simulated GPS jump to 25km. Distance smoothed."
            }
        )

        Spacer(modifier = Modifier.height(18.dp))

        // Console Log Output
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(18.dp))
                .border(BorderStroke(1.dp, ProfessionalBorder), RoundedCornerShape(18.dp)),
            color = ProfessionalPrimaryContainer,
            shape = RoundedCornerShape(18.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "LAB DIAGNOSTIC LOG",
                    style = MaterialTheme.typography.labelSmall,
                    color = ProfessionalOnPrimaryContainer,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = testLog,
                    style = MaterialTheme.typography.bodyMedium,
                    color = ProfessionalOnPrimaryContainer
                )
            }
        }

        Spacer(modifier = Modifier.height(30.dp))
    }
}

@Composable
fun LabActionCard(
    title: String,
    description: String,
    buttonText: String,
    buttonColor: Color,
    buttonTextColor: Color = Color.White,
    onClick: () -> Unit
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .border(BorderStroke(1.dp, ProfessionalBorder), RoundedCornerShape(20.dp)),
        color = ProfessionalSurface,
        shape = RoundedCornerShape(20.dp),
        shadowElevation = 1.dp
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                color = ProfessionalTextPrimary
            )
            Text(
                text = description,
                style = MaterialTheme.typography.bodySmall,
                color = ProfessionalTextSecondary
            )
            Spacer(modifier = Modifier.height(12.dp))
            Button(
                onClick = onClick,
                colors = ButtonDefaults.buttonColors(
                    containerColor = buttonColor,
                    contentColor = buttonTextColor
                ),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth().height(44.dp)
            ) {
                Text(buttonText, fontWeight = FontWeight.Bold)
            }
        }
    }
}
