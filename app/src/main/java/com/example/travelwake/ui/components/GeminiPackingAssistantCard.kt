package com.example.travelwake.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
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
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Thermostat
import androidx.compose.material.icons.filled.Umbrella
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.travelwake.ai.GeminiPackingSuggestion
import com.example.travelwake.data.model.WeatherInfo
import com.example.ui.theme.LocalWeatherThemePalette
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

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun GeminiPackingAssistantCard(
    destinationName: String,
    weather: WeatherInfo,
    tripDuration: String,
    suggestions: List<GeminiPackingSuggestion>,
    isLoading: Boolean,
    notice: String?,
    onDurationChange: (String) -> Unit,
    onGenerate: () -> Unit,
    onToggleSuggestion: (String) -> Unit,
    onAddSuggestion: (GeminiPackingSuggestion) -> Unit,
    onAddAllSelected: () -> Unit,
    modifier: Modifier = Modifier
) {
    val activeWeatherTheme = LocalWeatherThemePalette.current
    val durations = listOf("1 Day Commute", "Weekend (2-3 Days)", "Extended (4+ Days)")
    val selectedCount = suggestions.count { it.isSelected }

    Surface(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(24.dp))
            .border(BorderStroke(1.dp, activeWeatherTheme.borderColor), RoundedCornerShape(24.dp))
            .testTag("gemini_packing_assistant_card"),
        color = ProfessionalSurface,
        shape = RoundedCornerShape(24.dp),
        shadowElevation = 2.dp
    ) {
        Column(modifier = Modifier.padding(18.dp)) {
            // Header with Gemini sparkle
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(38.dp)
                            .clip(CircleShape)
                            .background(activeWeatherTheme.primaryContainerColor),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.AutoAwesome,
                            contentDescription = "Gemini AI",
                            tint = activeWeatherTheme.primaryColor,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = "Gemini AI Packing Assistant",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = ProfessionalTextPrimary
                        )
                        Text(
                            text = "Tailored to weather forecast & trip duration",
                            style = MaterialTheme.typography.bodySmall,
                            color = ProfessionalTextSecondary
                        )
                    }
                }

                Surface(
                    color = activeWeatherTheme.primaryContainerColor,
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Text(
                        text = "Gemini 2.5",
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        style = MaterialTheme.typography.labelSmall,
                        color = activeWeatherTheme.primaryColor,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Weather Context Chips for Destination
            Surface(
                modifier = Modifier.fillMaxWidth(),
                color = ProfessionalSurfaceVariant.copy(alpha = 0.6f),
                shape = RoundedCornerShape(16.dp),
                border = BorderStroke(1.dp, ProfessionalBorder.copy(alpha = 0.6f))
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Thermostat,
                            contentDescription = null,
                            tint = activeWeatherTheme.primaryColor,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "${weather.temperatureCelsius}°C ${weather.condition}",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.SemiBold,
                            color = ProfessionalTextPrimary
                        )
                    }

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Umbrella,
                            contentDescription = null,
                            tint = activeWeatherTheme.accentHighlight,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "${weather.rainProbability}% Rain",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.SemiBold,
                            color = ProfessionalTextPrimary
                        )
                    }

                    Text(
                        text = destinationName.take(18),
                        style = MaterialTheme.typography.labelSmall,
                        color = ProfessionalTextSecondary,
                        maxLines = 1
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Trip Duration Selector Chips
            Text(
                text = "Trip Duration",
                style = MaterialTheme.typography.labelSmall,
                color = ProfessionalTextSecondary,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(6.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                durations.forEach { durationOption ->
                    val isSelected = tripDuration == durationOption
                    Surface(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(12.dp))
                            .border(
                                BorderStroke(
                                    1.dp,
                                    if (isSelected) activeWeatherTheme.primaryColor else ProfessionalBorder
                                ),
                                RoundedCornerShape(12.dp)
                            )
                            .clickable { onDurationChange(durationOption) }
                            .testTag("duration_chip_${durationOption.take(4)}"),
                        color = if (isSelected) activeWeatherTheme.primaryContainerColor else ProfessionalSurface,
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text(
                            text = durationOption,
                            modifier = Modifier.padding(vertical = 8.dp, horizontal = 4.dp),
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                            color = if (isSelected) activeWeatherTheme.primaryColor else ProfessionalTextPrimary,
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Generate Button
            Button(
                onClick = onGenerate,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
                    .testTag("generate_gemini_packing_button"),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = activeWeatherTheme.primaryColor,
                    contentColor = Color.White
                ),
                enabled = !isLoading
            ) {
                if (isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(20.dp),
                        color = Color.White,
                        strokeWidth = 2.dp
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = "Analyzing Weather & Duration...",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.SemiBold
                    )
                } else {
                    Icon(
                        imageVector = Icons.Default.AutoAwesome,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = if (suggestions.isEmpty()) "Generate Smart Packing List" else "Regenerate with Gemini",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            // Notice / Context banner
            if (!notice.isNullOrBlank()) {
                Spacer(modifier = Modifier.height(10.dp))
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    color = activeWeatherTheme.primaryContainerColor.copy(alpha = 0.5f),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text(
                        text = "ℹ️ $notice",
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                        style = MaterialTheme.typography.labelSmall,
                        color = activeWeatherTheme.primaryColor,
                        fontWeight = FontWeight.Medium
                    )
                }
            }

            // Suggestions List
            AnimatedVisibility(
                visible = suggestions.isNotEmpty(),
                enter = fadeIn() + expandVertically(),
                exit = fadeOut() + shrinkVertically()
            ) {
                Column(modifier = Modifier.padding(top = 16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Recommended for Your Trip (${suggestions.size})",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            color = ProfessionalTextPrimary
                        )

                        if (selectedCount > 0) {
                            Text(
                                text = "Add All ($selectedCount)",
                                style = MaterialTheme.typography.labelSmall,
                                color = activeWeatherTheme.primaryColor,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier
                                    .clickable { onAddAllSelected() }
                                    .testTag("add_all_gemini_suggestions_button")
                                    .padding(4.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    suggestions.forEach { item ->
                        Surface(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp)
                                .clip(RoundedCornerShape(14.dp))
                                .border(BorderStroke(1.dp, ProfessionalBorder), RoundedCornerShape(14.dp))
                                .clickable { onToggleSuggestion(item.id) }
                                .testTag("suggestion_item_${item.name.replace(" ", "_")}"),
                            color = if (item.isSelected) activeWeatherTheme.primaryContainerColor.copy(alpha = 0.35f) else ProfessionalSurface,
                            shape = RoundedCornerShape(14.dp)
                        ) {
                            Row(
                                modifier = Modifier.padding(10.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Checkbox(
                                    checked = item.isSelected,
                                    onCheckedChange = { onToggleSuggestion(item.id) },
                                    colors = CheckboxDefaults.colors(
                                        checkedColor = activeWeatherTheme.primaryColor,
                                        uncheckedColor = ProfessionalBorder
                                    ),
                                    modifier = Modifier.size(28.dp)
                                )

                                Spacer(modifier = Modifier.width(8.dp))

                                Column(modifier = Modifier.weight(1f)) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Text(
                                            text = item.name,
                                            style = MaterialTheme.typography.titleSmall,
                                            fontWeight = FontWeight.Bold,
                                            color = ProfessionalTextPrimary
                                        )
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Surface(
                                            color = ProfessionalSurfaceVariant,
                                            shape = RoundedCornerShape(6.dp)
                                        ) {
                                            Text(
                                                text = item.category,
                                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                                style = MaterialTheme.typography.labelSmall,
                                                color = ProfessionalTextSecondary,
                                                fontSize = 10.sp
                                            )
                                        }
                                    }

                                    if (item.reason.isNotBlank()) {
                                        Text(
                                            text = item.reason,
                                            style = MaterialTheme.typography.bodySmall,
                                            color = ProfessionalTextMuted,
                                            fontSize = 11.sp
                                        )
                                    }
                                }

                                IconButton(
                                    onClick = { onAddSuggestion(item) },
                                    modifier = Modifier
                                        .size(32.dp)
                                        .clip(CircleShape)
                                        .background(activeWeatherTheme.primaryContainerColor)
                                        .testTag("add_single_suggestion_${item.name.replace(" ", "_")}")
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Add,
                                        contentDescription = "Add to Checklist",
                                        tint = activeWeatherTheme.primaryColor,
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
