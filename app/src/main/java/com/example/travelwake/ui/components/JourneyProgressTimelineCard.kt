package com.example.travelwake.ui.components

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.DirectionsBus
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material.icons.filled.DirectionsSubway
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.MyLocation
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.Place
import androidx.compose.material.icons.filled.Train
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.travelwake.data.model.TransportMode
import com.example.travelwake.engine.LocationEngine
import com.example.ui.theme.AlarmRed
import com.example.ui.theme.AlertAmber
import com.example.ui.theme.AlertAmberContainer
import com.example.ui.theme.LocalWeatherThemePalette
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
import kotlin.math.roundToInt

/**
 * Visual Progress Bar & Interactive Journey Timeline.
 * Dynamically computes progress percentage based on current GPS coordinates
 * relative to origin and destination coordinates, rendering a smooth progress bar
 * with animated travel vehicle marker, distance traversed, and 4 waypoint milestone nodes.
 */
@Composable
fun JourneyProgressTimelineCard(
    originLat: Double,
    originLon: Double,
    departureName: String,
    currentLat: Double,
    currentLon: Double,
    destLat: Double,
    destLon: Double,
    destinationName: String,
    remainingDistanceMeters: Int,
    alertDistanceMeters: Int,
    etaMinutes: Int,
    transportMode: TransportMode,
    modifier: Modifier = Modifier
) {
    val weatherTheme = LocalWeatherThemePalette.current
    var isTimelineExpanded by remember { mutableStateOf(true) }

    // 1. Calculate Real-Time Journey Geometry
    val totalRouteDistance = remember(originLat, originLon, destLat, destLon) {
        maxOf(LocationEngine.calculateDistanceMeters(originLat, originLon, destLat, destLon), 1000)
    }

    val actualRemaining = LocationEngine.calculateDistanceMeters(currentLat, currentLon, destLat, destLon)
    val distanceRemainingEffective = minOf(remainingDistanceMeters, actualRemaining)
    val distanceCoveredMeters = (totalRouteDistance - distanceRemainingEffective).coerceAtLeast(0)

    val progressFraction = (distanceCoveredMeters.toFloat() / totalRouteDistance.toFloat()).coerceIn(0f, 1f)
    val completionPercentage = (progressFraction * 100f).roundToInt()

    val isInsideAlertZone = distanceRemainingEffective <= alertDistanceMeters

    // Smooth responsive animation using gentle spring physics for GPS location transitions
    val animatedProgress by animateFloatAsState(
        targetValue = progressFraction,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioLowBouncy,
            stiffness = Spring.StiffnessLow
        ),
        label = "journeyProgressSpring"
    )

    // Pulsing, shimmer, and radar wave transitions for active transit feedback
    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val pulseAlpha by infiniteTransition.animateFloat(
        initialValue = 0.4f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulseAlpha"
    )
    val shimmerFraction by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(2200, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "shimmerFraction"
    )
    val radarScale by infiniteTransition.animateFloat(
        initialValue = 0.8f,
        targetValue = 1.9f,
        animationSpec = infiniteRepeatable(
            animation = tween(1500, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "radarScale"
    )
    val radarAlpha by infiniteTransition.animateFloat(
        initialValue = 0.5f,
        targetValue = 0f,
        animationSpec = infiniteRepeatable(
            animation = tween(1500, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "radarAlpha"
    )
    val vehicleBob by infiniteTransition.animateFloat(
        initialValue = -1.5f,
        targetValue = 1.5f,
        animationSpec = infiniteRepeatable(
            animation = tween(900, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "vehicleBob"
    )

    val activeColor by animateColorAsState(
        targetValue = if (isInsideAlertZone) AlertAmber else weatherTheme.primaryColor,
        animationSpec = tween(500),
        label = "activeColor"
    )
    val accentColor by animateColorAsState(
        targetValue = if (isInsideAlertZone) AlertAmberContainer else weatherTheme.accentHighlight,
        animationSpec = tween(500),
        label = "accentColor"
    )

    Surface(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(24.dp))
            .border(BorderStroke(1.dp, weatherTheme.borderColor), RoundedCornerShape(24.dp))
            .testTag("journey_progress_timeline_card"),
        color = weatherTheme.surfaceColor,
        shape = RoundedCornerShape(24.dp),
        shadowElevation = 2.dp
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(18.dp)
        ) {
            // Header Row: Title, completion badge, and expand toggle
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(weatherTheme.primaryContainerColor),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = when (transportMode) {
                                TransportMode.TRAIN -> Icons.Default.Train
                                TransportMode.METRO -> Icons.Default.DirectionsSubway
                                TransportMode.BUS -> Icons.Default.DirectionsBus
                                else -> Icons.Default.DirectionsCar
                            },
                            contentDescription = "Transport Mode",
                            tint = weatherTheme.primaryColor,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = "Journey Completion",
                            style = MaterialTheme.typography.labelMedium,
                            color = ProfessionalTextSecondary,
                            fontWeight = FontWeight.SemiBold
                        )
                        Text(
                            text = if (isInsideAlertZone) "Within Alert Perimeter!" else "En Route to $destinationName",
                            style = MaterialTheme.typography.titleSmall,
                            color = if (isInsideAlertZone) AlertAmber else ProfessionalTextPrimary,
                            fontWeight = FontWeight.Bold,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }

                // Completion Percentage Pill with animated number transition
                Surface(
                    color = if (isInsideAlertZone) AlertAmberContainer else weatherTheme.primaryContainerColor,
                    shape = RoundedCornerShape(12.dp),
                    border = BorderStroke(1.dp, if (isInsideAlertZone) AlertAmber else weatherTheme.primaryColor.copy(alpha = 0.3f))
                ) {
                    AnimatedContent(
                        targetState = completionPercentage,
                        transitionSpec = { fadeIn() togetherWith fadeOut() },
                        label = "completionPercentageAnimation"
                    ) { targetPct ->
                        Text(
                            text = "$targetPct% Done",
                            modifier = Modifier
                                .padding(horizontal = 10.dp, vertical = 5.dp)
                                .testTag("journey_completion_percentage_text"),
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.ExtraBold,
                            color = if (isInsideAlertZone) AlertAmber else weatherTheme.primaryColor
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // 2. High-Fidelity Visual Progress Bar with Fluid GPS Animations
            BoxWithConstraints(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(44.dp)
                    .testTag("visual_progress_bar_container")
            ) {
                val trackWidth = maxWidth

                // Background track
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(12.dp)
                        .align(Alignment.Center)
                        .clip(RoundedCornerShape(6.dp))
                        .background(ProfessionalSurfaceVariant)
                )

                // Active Filled Gradient Bar with fluid animated width
                val barWidth = trackWidth * animatedProgress
                Box(
                    modifier = Modifier
                        .width(barWidth)
                        .height(12.dp)
                        .align(Alignment.CenterStart)
                        .clip(RoundedCornerShape(6.dp))
                        .background(
                            Brush.horizontalGradient(
                                colors = listOf(
                                    activeColor,
                                    accentColor,
                                    activeColor
                                )
                            )
                        )
                )

                // Shimmer sweep across the active bar reflecting live GPS updates
                if (animatedProgress > 0.05f) {
                    val shimmerX = barWidth * shimmerFraction
                    Box(
                        modifier = Modifier
                            .offset(x = (shimmerX - 25.dp).coerceAtLeast(0.dp))
                            .width(50.dp)
                            .height(12.dp)
                            .align(Alignment.CenterStart)
                            .clip(RoundedCornerShape(6.dp))
                            .background(
                                Brush.horizontalGradient(
                                    colors = listOf(
                                        Color.Transparent,
                                        Color.White.copy(alpha = 0.45f),
                                        Color.Transparent
                                    )
                                )
                            )
                    )
                }

                // Geofence Alert Zone Target Marker along the track
                val alertFraction = (1.0f - (alertDistanceMeters.toFloat() / totalRouteDistance.toFloat())).coerceIn(0.5f, 0.95f)
                Box(
                    modifier = Modifier
                        .offset(x = trackWidth * alertFraction - 8.dp)
                        .align(Alignment.CenterStart)
                        .size(16.dp)
                        .clip(CircleShape)
                        .background(if (isInsideAlertZone) AlertAmber else ProfessionalBorder)
                        .border(BorderStroke(2.dp, Color.White), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Box(
                        modifier = Modifier
                            .size(6.dp)
                            .clip(CircleShape)
                            .background(if (isInsideAlertZone) Color.White else ProfessionalTextMuted)
                    )
                }

                val thumbOffset = ((trackWidth - 34.dp) * animatedProgress).coerceAtLeast(0.dp)

                // GPS Location Radar Ping Wave reflecting active location polling
                Box(
                    modifier = Modifier
                        .offset(x = thumbOffset - 6.dp)
                        .align(Alignment.CenterStart)
                        .size(46.dp)
                        .scale(radarScale)
                        .clip(CircleShape)
                        .background(activeColor.copy(alpha = radarAlpha))
                )

                // Moving Animated Vehicle Marker
                Box(
                    modifier = Modifier
                        .offset(x = thumbOffset, y = vehicleBob.dp)
                        .align(Alignment.CenterStart)
                        .size(34.dp)
                        .shadow(6.dp, CircleShape)
                        .clip(CircleShape)
                        .background(activeColor)
                        .border(BorderStroke(2.dp, Color.White), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = when (transportMode) {
                            TransportMode.TRAIN -> Icons.Default.Train
                            TransportMode.METRO -> Icons.Default.DirectionsSubway
                            TransportMode.BUS -> Icons.Default.DirectionsBus
                            else -> Icons.Default.DirectionsCar
                        },
                        contentDescription = "Current Position",
                        tint = Color.White,
                        modifier = Modifier.size(17.dp)
                    )
                }
            }

            // Sub-metrics row under the progress bar
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "${LocationEngine.formatDistance(distanceCoveredMeters)} covered",
                    style = MaterialTheme.typography.bodySmall,
                    color = ProfessionalTextSecondary,
                    fontWeight = FontWeight.Medium
                )
                Text(
                    text = "${LocationEngine.formatDistance(distanceRemainingEffective)} to stop (ETA ~$etaMinutes min)",
                    style = MaterialTheme.typography.bodySmall,
                    color = weatherTheme.primaryColor,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Expand / Collapse Timeline Details Button
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .clickable { isTimelineExpanded = !isTimelineExpanded }
                    .padding(vertical = 4.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = if (isTimelineExpanded) "GPS Waypoint Timeline" else "Show Full Waypoint Timeline",
                    style = MaterialTheme.typography.labelMedium,
                    color = ProfessionalTextPrimary,
                    fontWeight = FontWeight.Bold
                )
                Icon(
                    imageVector = if (isTimelineExpanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                    contentDescription = "Toggle Timeline Details",
                    tint = ProfessionalTextSecondary,
                    modifier = Modifier.size(20.dp)
                )
            }

            // 3. Multi-Node Journey Timeline
            AnimatedVisibility(visible = isTimelineExpanded) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 10.dp)
                        .testTag("journey_timeline_nodes_container")
                ) {
                    // Node 1: Departure Point
                    TimelineMilestoneNode(
                        title = departureName,
                        subtitle = "Origin Point • Journey Initiated",
                        icon = Icons.Default.CheckCircle,
                        iconTint = SafetyGreen,
                        iconBg = SafetyGreen.copy(alpha = 0.15f),
                        isPassed = true,
                        showConnectingLine = true
                    )

                    // Node 2: Live Current GPS Position
                    TimelineMilestoneNode(
                        title = "Live GPS: %.4f°, %.4f°".format(currentLat, currentLon),
                        subtitle = "Transiting via ${transportMode.name.lowercase().replaceFirstChar { it.uppercase() }} • $completionPercentage% complete",
                        icon = Icons.Default.MyLocation,
                        iconTint = weatherTheme.primaryColor,
                        iconBg = weatherTheme.primaryContainerColor,
                        isPassed = true,
                        isCurrent = true,
                        showConnectingLine = true
                    )

                    // Node 3: Pre-Alarm Geofence Perimeter
                    TimelineMilestoneNode(
                        title = "Alarm Radius (${LocationEngine.formatDistance(alertDistanceMeters)})",
                        subtitle = if (isInsideAlertZone) "🚨 Inside Trigger Perimeter — Alarm Active!" else "Approaching boundary zone",
                        icon = Icons.Default.NotificationsActive,
                        iconTint = if (isInsideAlertZone) AlertAmber else ProfessionalTextMuted,
                        iconBg = if (isInsideAlertZone) AlertAmberContainer else ProfessionalSurfaceVariant,
                        isPassed = isInsideAlertZone,
                        isAlertZone = true,
                        showConnectingLine = true
                    )

                    // Node 4: Destination Arrival Stop
                    TimelineMilestoneNode(
                        title = destinationName,
                        subtitle = "Arrival Stop • ETA ~$etaMinutes min remaining",
                        icon = Icons.Default.Place,
                        iconTint = if (completionPercentage >= 100) SafetyGreen else AlarmRed,
                        iconBg = if (completionPercentage >= 100) SafetyGreen.copy(alpha = 0.15f) else AlarmRed.copy(alpha = 0.12f),
                        isPassed = completionPercentage >= 100,
                        showConnectingLine = false
                    )
                }
            }
        }
    }
}

@Composable
private fun TimelineMilestoneNode(
    title: String,
    subtitle: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    iconTint: Color,
    iconBg: Color,
    isPassed: Boolean,
    showConnectingLine: Boolean,
    isCurrent: Boolean = false,
    isAlertZone: Boolean = false
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.Top
    ) {
        // Left column: Node icon + vertical connecting line
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.width(36.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(28.dp)
                    .clip(CircleShape)
                    .background(iconBg)
                    .border(
                        BorderStroke(
                            if (isCurrent) 2.dp else 1.dp,
                            if (isCurrent) iconTint else iconTint.copy(alpha = 0.5f)
                        ),
                        CircleShape
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = iconTint,
                    modifier = Modifier.size(16.dp)
                )
            }

            if (showConnectingLine) {
                Box(
                    modifier = Modifier
                        .width(2.dp)
                        .height(26.dp)
                        .background(
                            if (isPassed) iconTint.copy(alpha = 0.6f) else ProfessionalDivider
                        )
                )
            }
        }

        Spacer(modifier = Modifier.width(12.dp))

        // Right column: Title & descriptive subtext
        Column(modifier = Modifier.padding(bottom = if (showConnectingLine) 10.dp else 0.dp)) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleSmall,
                color = if (isAlertZone && isPassed) AlertAmber else ProfessionalTextPrimary,
                fontWeight = if (isCurrent || isPassed) FontWeight.Bold else FontWeight.Medium,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = if (isCurrent) ProfessionalPrimary else ProfessionalTextSecondary,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}
