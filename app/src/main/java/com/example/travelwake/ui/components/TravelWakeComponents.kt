package com.example.travelwake.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Navigation
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.travelwake.data.model.BelongingEntity
import com.example.travelwake.data.model.CautionLevel
import com.example.travelwake.data.model.TransportMode
import com.example.travelwake.data.model.WeatherInfo
import com.example.travelwake.data.model.WeatherRecommendation
import com.example.travelwake.engine.LocationEngine
import com.example.ui.theme.AlarmRed
import com.example.ui.theme.AlarmRedContainer
import com.example.ui.theme.AlertAmber
import com.example.ui.theme.AlertAmberContainer
import com.example.ui.theme.BrightCyan
import com.example.ui.theme.DarkNavyCard
import com.example.ui.theme.GlassNavyBorder
import com.example.ui.theme.GlassNavyCard
import com.example.ui.theme.LocalGlassmorphismStyle
import com.example.ui.theme.blurredCardElevation
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

/**
 * Reusable Card component implementing the 'Premium Glassmorphism' surface
 * (translucent frosted surfaces, specular borders, and deep navy backdrop support).
 */
@Composable
fun GlassCard(
    modifier: Modifier = Modifier,
    backgroundColor: Color? = null,
    borderColor: Color? = null,
    cornerRadius: Int = 24,
    content: @Composable () -> Unit
) {
    val isDark = MaterialTheme.colorScheme.background.value == com.example.ui.theme.DeepNavyDark.value ||
            MaterialTheme.colorScheme.surface.value == com.example.ui.theme.DarkNavySurface.value ||
            MaterialTheme.colorScheme.surface.value == com.example.ui.theme.GlassNavySurface.value
    val resolvedBg = backgroundColor ?: if (isDark) GlassNavyCard else ProfessionalSurface
    val resolvedBorder = borderColor ?: if (isDark) GlassNavyBorder else ProfessionalBorder

    val cardShape = RoundedCornerShape(cornerRadius.dp)
    val cardModifier = if (isDark) {
        modifier.blurredCardElevation(
            elevation = 8.dp,
            shape = cardShape,
            shadowTint = com.example.ui.theme.DeepNavyAbyss,
            accentGlow = BrightCyan
        )
    } else {
        modifier
    }

    Surface(
        modifier = cardModifier
            .clip(cardShape)
            .border(
                BorderStroke(1.dp, resolvedBorder),
                cardShape
            ),
        color = resolvedBg,
        shape = cardShape,
        shadowElevation = if (isDark) 0.dp else 2.dp
    ) {
        Box(modifier = Modifier.padding(18.dp)) {
            content()
        }
    }
}

/**
 * Hero Distance Card styled after the "Professional Polish" Hero pattern
 * Uses #D3E4FF primary container with #001D36 typography, rounded-3xl, and #005AC1 accents.
 */
@Composable
fun HeroDistanceCard(
    distanceMeters: Int,
    destinationName: String,
    etaMinutes: Int,
    isApproaching: Boolean,
    modifier: Modifier = Modifier
) {
    val pulseScale by animateFloatAsState(
        targetValue = if (isApproaching) 1.02f else 1f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy),
        label = "pulseScale"
    )

    val borderColor by animateColorAsState(
        targetValue = if (isApproaching) AlertAmber else ProfessionalBorder,
        label = "borderColor"
    )

    val formattedDistance = LocationEngine.formatDistance(distanceMeters)

    Surface(
        modifier = modifier
            .scale(pulseScale)
            .clip(RoundedCornerShape(28.dp))
            .border(BorderStroke(1.dp, borderColor), RoundedCornerShape(28.dp))
            .testTag("hero_distance_card"),
        color = if (isApproaching) AlertAmberContainer else ProfessionalPrimaryContainer,
        shape = RoundedCornerShape(28.dp),
        shadowElevation = 2.dp
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(22.dp)
        ) {
            // Top Row: Category label and trailing badge icon
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    if (isApproaching) {
                        Icon(
                            imageVector = Icons.Default.Warning,
                            contentDescription = "Approaching alert",
                            tint = AlertAmber,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "DESTINATION APPROACHING",
                            style = MaterialTheme.typography.labelLarge,
                            color = AlertAmber,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.sp
                        )
                    } else {
                        Text(
                            text = "DESTINATION TRACKING",
                            style = MaterialTheme.typography.labelMedium,
                            color = ProfessionalOnPrimaryContainer,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.sp
                        )
                    }
                }

                // Professional Polish trailing action pill / icon box
                Box(
                    modifier = Modifier
                        .size(42.dp)
                        .clip(RoundedCornerShape(14.dp))
                        .background(if (isApproaching) AlertAmber else ProfessionalPrimary),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = if (isApproaching) Icons.Default.Warning else Icons.Default.Schedule,
                        contentDescription = "Travel Status",
                        tint = Color.White,
                        modifier = Modifier.size(22.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Dominant Distance Display
            Text(
                text = formattedDistance,
                style = MaterialTheme.typography.displayLarge,
                color = if (isApproaching) AlertAmber else ProfessionalOnPrimaryContainer,
                fontWeight = FontWeight.Black,
                letterSpacing = (-1).sp
            )
            Text(
                text = "remaining to destination stop",
                style = MaterialTheme.typography.bodyMedium,
                color = if (isApproaching) AlertAmber else ProfessionalOnPrimaryContainer.copy(alpha = 0.75f),
                fontWeight = FontWeight.Medium
            )

            Spacer(modifier = Modifier.height(18.dp))

            // Inner Destination & ETA Card
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(18.dp))
                    .border(BorderStroke(1.dp, ProfessionalBorder), RoundedCornerShape(18.dp)),
                color = ProfessionalSurface,
                shape = RoundedCornerShape(18.dp),
                shadowElevation = 1.dp
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Destination",
                            style = MaterialTheme.typography.labelSmall,
                            color = ProfessionalTextSecondary,
                            fontWeight = FontWeight.Medium
                        )
                        Text(
                            text = destinationName,
                            style = MaterialTheme.typography.titleMedium,
                            color = ProfessionalTextPrimary,
                            fontWeight = FontWeight.Bold,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column(horizontalAlignment = Alignment.End) {
                        Text(
                            text = "Estimated Arrival",
                            style = MaterialTheme.typography.labelSmall,
                            color = ProfessionalTextSecondary,
                            fontWeight = FontWeight.Medium
                        )
                        Text(
                            text = "~$etaMinutes min",
                            style = MaterialTheme.typography.titleMedium,
                            color = ProfessionalPrimary,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }
}

/**
 * Transport Mode Selector with Professional Polish styling
 */
@Composable
fun TransportModeSelector(
    selectedMode: TransportMode,
    onModeSelected: (TransportMode) -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .horizontalScroll(rememberScrollState()),
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        TransportMode.values().forEach { mode ->
            val isSelected = mode == selectedMode
            val bg = if (isSelected) ProfessionalPrimary else ProfessionalSurface
            val border = if (isSelected) ProfessionalPrimary else ProfessionalBorder
            val contentColor = if (isSelected) Color.White else ProfessionalTextSecondary

            Surface(
                modifier = Modifier
                    .clip(RoundedCornerShape(16.dp))
                    .border(BorderStroke(1.dp, border), RoundedCornerShape(16.dp))
                    .clickable { onModeSelected(mode) }
                    .testTag("transport_mode_${mode.name.lowercase()}"),
                color = bg,
                shape = RoundedCornerShape(16.dp),
                shadowElevation = if (isSelected) 2.dp else 0.dp
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(text = mode.icon, fontSize = 20.sp)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = mode.label,
                        style = MaterialTheme.typography.labelLarge,
                        color = contentColor,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                    )
                }
            }
        }
    }
}

/**
 * Alert Distance Selector with Professional Polish styling
 */
@Composable
fun AlertDistanceSelector(
    selectedMeters: Int,
    onDistanceSelected: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    val presets = listOf(100, 250, 500, 750, 1000, 2000)
    var showCustomDialog by remember { mutableStateOf(false) }
    var customInputText by remember { mutableStateOf(selectedMeters.toString()) }
    val isCustomActive = selectedMeters !in presets

    Column(modifier = modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Wake me before destination",
                style = MaterialTheme.typography.titleMedium,
                color = ProfessionalTextPrimary,
                fontWeight = FontWeight.SemiBold
            )
            Surface(
                color = ProfessionalPrimaryContainer,
                shape = RoundedCornerShape(10.dp),
                border = BorderStroke(1.dp, ProfessionalPrimary.copy(alpha = 0.2f))
            ) {
                Text(
                    text = LocationEngine.formatDistance(selectedMeters),
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                    style = MaterialTheme.typography.titleSmall,
                    color = ProfessionalOnPrimaryContainer,
                    fontWeight = FontWeight.Bold
                )
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Preset chips row including "Custom" chip
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            presets.forEach { preset ->
                val isSelected = preset == selectedMeters
                val bg = if (isSelected) ProfessionalPrimary else ProfessionalSurface
                val border = if (isSelected) ProfessionalPrimary else ProfessionalBorder
                val textColor = if (isSelected) Color.White else ProfessionalTextPrimary

                Surface(
                    modifier = Modifier
                        .clip(RoundedCornerShape(14.dp))
                        .border(BorderStroke(1.dp, border), RoundedCornerShape(14.dp))
                        .clickable { onDistanceSelected(preset) }
                        .testTag("distance_preset_$preset"),
                    color = bg,
                    shape = RoundedCornerShape(14.dp),
                    shadowElevation = if (isSelected) 2.dp else 0.dp
                ) {
                    Text(
                        text = LocationEngine.formatDistance(preset),
                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
                        style = MaterialTheme.typography.labelMedium,
                        color = textColor,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                    )
                }
            }

            // Custom distance chip
            val customBg = if (isCustomActive) ProfessionalPrimary else ProfessionalSurfaceVariant
            val customBorder = if (isCustomActive) ProfessionalPrimary else ProfessionalBorder
            val customTextColor = if (isCustomActive) Color.White else ProfessionalTextPrimary

            Surface(
                modifier = Modifier
                    .clip(RoundedCornerShape(14.dp))
                    .border(BorderStroke(1.dp, customBorder), RoundedCornerShape(14.dp))
                    .clickable {
                        customInputText = selectedMeters.toString()
                        showCustomDialog = true
                    }
                    .testTag("distance_preset_custom"),
                color = customBg,
                shape = RoundedCornerShape(14.dp),
                shadowElevation = if (isCustomActive) 2.dp else 0.dp
            ) {
                Text(
                    text = if (isCustomActive) "Custom (${LocationEngine.formatDistance(selectedMeters)})" else "Custom...",
                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
                    style = MaterialTheme.typography.labelMedium,
                    color = customTextColor,
                    fontWeight = if (isCustomActive) FontWeight.Bold else FontWeight.Medium
                )
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Fine tuning slider
        Slider(
            value = selectedMeters.toFloat().coerceIn(50f, 5000f),
            onValueChange = { onDistanceSelected(it.toInt()) },
            valueRange = 50f..5000f,
            steps = 98,
            colors = SliderDefaults.colors(
                thumbColor = ProfessionalPrimary,
                activeTrackColor = ProfessionalPrimary,
                inactiveTrackColor = ProfessionalSurfaceVariant
            ),
            modifier = Modifier
                .fillMaxWidth()
                .testTag("distance_slider")
        )
    }

    if (showCustomDialog) {
        AlertDialog(
            onDismissRequest = { showCustomDialog = false },
            title = {
                Text("Set Custom Alert Distance", fontWeight = FontWeight.Bold)
            },
            text = {
                Column {
                    Text("Enter wake-up distance in meters (50m - 10,000m):", style = MaterialTheme.typography.bodyMedium)
                    Spacer(modifier = Modifier.height(10.dp))
                    OutlinedTextField(
                        value = customInputText,
                        onValueChange = { customInputText = it.filter { ch -> ch.isDigit() } },
                        label = { Text("Meters") },
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = ProfessionalPrimary,
                            unfocusedBorderColor = ProfessionalBorder
                        ),
                        modifier = Modifier.fillMaxWidth().testTag("custom_distance_input")
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val entered = customInputText.toIntOrNull()
                        if (entered != null && entered in 50..10000) {
                            onDistanceSelected(entered)
                            showCustomDialog = false
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = ProfessionalPrimary)
                ) {
                    Text("Apply", color = Color.White)
                }
            },
            dismissButton = {
                TextButton(onClick = { showCustomDialog = false }) {
                    Text("Cancel", color = ProfessionalTextSecondary)
                }
            }
        )
    }
}

/**
 * Weather Card with rule-based safety recommendations styled with Professional Polish
 */
@Composable
fun WeatherRecommendationCard(
    weather: WeatherInfo,
    destinationName: String,
    modifier: Modifier = Modifier
) {
    GlassCard(
        modifier = modifier.testTag("weather_recommendation_card"),
        cornerRadius = 24
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = when {
                            weather.rainProbability > 50 -> "🌧"
                            weather.temperatureCelsius >= 32 -> "☀"
                            weather.temperatureCelsius <= 15 -> "❄"
                            else -> "🌤"
                        },
                        fontSize = 32.sp
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            text = "${weather.temperatureCelsius}°C • ${weather.condition}",
                            style = MaterialTheme.typography.titleMedium,
                            color = ProfessionalTextPrimary,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Destination forecast near arrival",
                            style = MaterialTheme.typography.bodySmall,
                            color = ProfessionalTextSecondary
                        )
                    }
                }

                if (weather.rainProbability > 0) {
                    Surface(
                        color = ProfessionalPrimaryContainer,
                        shape = RoundedCornerShape(12.dp),
                        border = BorderStroke(1.dp, ProfessionalPrimary.copy(alpha = 0.2f))
                    ) {
                        Text(
                            text = "${weather.rainProbability}% Rain",
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                            style = MaterialTheme.typography.labelMedium,
                            color = ProfessionalOnPrimaryContainer,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Safety recommendations badge list
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                weather.recommendations.forEach { rec ->
                    val (badgeBg, badgeBorder, badgeTextColor) = when (rec.cautionLevel) {
                        CautionLevel.ALERT -> Triple(AlarmRedContainer, AlarmRed.copy(alpha = 0.4f), AlarmRed)
                        CautionLevel.WARNING -> Triple(AlertAmberContainer, AlertAmber.copy(alpha = 0.4f), AlertAmber)
                        CautionLevel.ADVISORY -> Triple(ProfessionalPrimaryContainer, ProfessionalPrimary.copy(alpha = 0.3f), ProfessionalPrimary)
                        CautionLevel.NORMAL -> Triple(SafetyGreenContainer, SafetyGreen.copy(alpha = 0.4f), SafetyGreen)
                    }
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(14.dp))
                            .background(badgeBg)
                            .border(BorderStroke(1.dp, badgeBorder), RoundedCornerShape(14.dp))
                            .padding(horizontal = 14.dp, vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(text = rec.icon, fontSize = 20.sp)
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = rec.title,
                                style = MaterialTheme.typography.labelLarge,
                                color = badgeTextColor,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = rec.description,
                                style = MaterialTheme.typography.bodySmall,
                                color = ProfessionalTextPrimary
                            )
                        }
                    }
                }
            }
        }
    }
}

/**
 * Belongings Checklist Card matching the Action List pattern in Professional Polish
 */
@Composable
fun BelongingsChecklistCard(
    belongings: List<BelongingEntity>,
    onToggleItem: (BelongingEntity) -> Unit,
    modifier: Modifier = Modifier
) {
    GlassCard(
        modifier = modifier.testTag("belongings_card"),
        cornerRadius = 24
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
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
                        Text(text = "🎒", fontSize = 18.sp)
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = "Check Belongings",
                        style = MaterialTheme.typography.titleMedium,
                        color = ProfessionalTextPrimary,
                        fontWeight = FontWeight.Bold
                    )
                }
                val checkedCount = belongings.count { it.isChecked }
                Surface(
                    color = if (checkedCount == belongings.size && belongings.isNotEmpty()) SafetyGreenContainer else ProfessionalSurfaceVariant,
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text(
                        text = "$checkedCount / ${belongings.size} checked",
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        style = MaterialTheme.typography.labelMedium,
                        color = if (checkedCount == belongings.size && belongings.isNotEmpty()) SafetyGreen else ProfessionalTextSecondary,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                belongings.take(6).forEachIndexed { index, item ->
                    if (index > 0) {
                        HorizontalDivider(
                            color = ProfessionalDivider,
                            thickness = 1.dp,
                            modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
                        )
                    }
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .clickable { onToggleItem(item) }
                            .padding(vertical = 6.dp, horizontal = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Checkbox(
                            checked = item.isChecked,
                            onCheckedChange = { onToggleItem(item) },
                            colors = CheckboxDefaults.colors(
                                checkedColor = ProfessionalPrimary,
                                checkmarkColor = Color.White,
                                uncheckedColor = ProfessionalBorder
                            )
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = item.name,
                            style = MaterialTheme.typography.bodyLarge,
                            color = if (item.isChecked) ProfessionalTextSecondary else ProfessionalTextPrimary,
                            fontWeight = if (item.isChecked) FontWeight.Normal else FontWeight.Medium
                        )
                    }
                }
            }
        }
    }
}

/**
 * Standard Primary Action Button matching Professional Polish
 * (56dp height, rounded-2xl / 16dp, #005AC1 container, crisp white text)
 */
@Composable
fun TravelWakeButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    containerColor: Color = ProfessionalPrimary,
    contentColor: Color = Color.White,
    testTag: String = "primary_button"
) {
    Button(
        onClick = onClick,
        enabled = enabled,
        modifier = modifier
            .fillMaxWidth()
            .height(56.dp)
            .testTag(testTag),
        shape = RoundedCornerShape(16.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = containerColor,
            contentColor = contentColor,
            disabledContainerColor = ProfessionalSurfaceVariant,
            disabledContentColor = ProfessionalTextMuted
        ),
        elevation = ButtonDefaults.buttonElevation(
            defaultElevation = 2.dp,
            pressedElevation = 4.dp
        )
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            letterSpacing = 0.5.sp
        )
    }
}
