package com.example.travelwake.ui.map

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Layers
import androidx.compose.material.icons.filled.MyLocation
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.ZoomOutMap
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
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.fragment.app.Fragment
import com.example.travelwake.theme.WeatherThemePalette
import com.example.ui.theme.AlertAmber
import com.example.ui.theme.AlertAmberContainer
import com.example.ui.theme.LocalWeatherThemePalette
import com.example.ui.theme.ProfessionalBorder
import com.example.ui.theme.ProfessionalPrimary
import com.example.ui.theme.ProfessionalSurface

/**
 * Standard Android Fragment hosting the RouteMapView to visualize the user's route
 * and current position during the active trip alarm service.
 */
class RouteMapFragment : Fragment() {

    private var routeMapView: RouteMapView? = null

    // Cached coordinates
    private var userLat: Double = 18.5204
    private var userLon: Double = 73.8567
    private var destLat: Double = 18.5289
    private var destLon: Double = 73.8744
    private var destName: String = "Destination"
    private var alertBuffer: Int = 500
    private var distanceRemaining: Int = 2400

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val mapView = RouteMapView(requireContext()).apply {
            layoutParams = FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.MATCH_PARENT,
                FrameLayout.LayoutParams.MATCH_PARENT
            )
            updateCoordinates(userLat, userLon, destLat, destLon, destName, alertBuffer, distanceRemaining)
        }
        routeMapView = mapView
        return mapView
    }

    fun updateRouteState(
        userLatitude: Double,
        userLongitude: Double,
        destinationLatitude: Double,
        destinationLongitude: Double,
        destinationName: String,
        alertBufferMeters: Int,
        distanceMeters: Int
    ) {
        userLat = userLatitude
        userLon = userLongitude
        destLat = destinationLatitude
        destLon = destinationLongitude
        destName = destinationName
        alertBuffer = alertBufferMeters
        distanceRemaining = distanceMeters
        routeMapView?.updateCoordinates(
            userLat, userLon, destLat, destLon, destName, alertBuffer, distanceRemaining
        )
    }

    fun setWeatherTheme(palette: WeatherThemePalette) {
        routeMapView?.let { map ->
            map.primaryColor = palette.primaryColor.toArgb()
            map.accentColor = palette.accentHighlight.toArgb()
            map.mapBackgroundColor = palette.backgroundColor.toArgb()
            map.roadColor = palette.surfaceVariantColor.toArgb()
            map.gridColor = palette.borderColor.toArgb()
            map.invalidate()
        }
    }

    fun zoomIn() = routeMapView?.zoomIn()
    fun zoomOut() = routeMapView?.zoomOut()
    fun centerOnUser() = routeMapView?.centerOnUser()
}

/**
 * Jetpack Compose Composable that embeds and synchronizes the MapView Fragment
 * during the active trip alarm service.
 */
@Composable
fun RouteMapFragmentComponent(
    userLat: Double,
    userLon: Double,
    destLat: Double,
    destLon: Double,
    destName: String,
    alertRadiusMeters: Int,
    remainingDistanceMeters: Int,
    modifier: Modifier = Modifier
) {
    val activeWeatherTheme = LocalWeatherThemePalette.current
    var mapViewInstance = remember { mutableMapOf<String, RouteMapView>() }
    var isSimulatedOffline by remember { mutableStateOf(false) }

    Surface(
        modifier = modifier
            .fillMaxWidth()
            .height(290.dp)
            .clip(RoundedCornerShape(24.dp))
            .border(BorderStroke(1.dp, ProfessionalBorder), RoundedCornerShape(24.dp))
            .testTag("route_map_fragment_card"),
        color = ProfessionalSurface,
        shape = RoundedCornerShape(24.dp),
        shadowElevation = 2.dp
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            // AndroidView embedding the native interactive RouteMapView managed by the Fragment logic
            AndroidView(
                modifier = Modifier
                    .fillMaxSize()
                    .testTag("route_map_native_view"),
                factory = { ctx ->
                    RouteMapView(ctx).apply {
                        updateCoordinates(
                            userLat, userLon, destLat, destLon, destName, alertRadiusMeters, remainingDistanceMeters
                        )
                        primaryColor = activeWeatherTheme.primaryColor.toArgb()
                        accentColor = activeWeatherTheme.accentHighlight.toArgb()
                        mapBackgroundColor = activeWeatherTheme.backgroundColor.toArgb()
                        roadColor = activeWeatherTheme.surfaceVariantColor.toArgb()
                        gridColor = activeWeatherTheme.borderColor.toArgb()
                        mapViewInstance["map"] = this
                    }
                },
                update = { view ->
                    view.updateCoordinates(
                        userLat, userLon, destLat, destLon, destName, alertRadiusMeters, remainingDistanceMeters
                    )
                    view.primaryColor = activeWeatherTheme.primaryColor.toArgb()
                    view.accentColor = activeWeatherTheme.accentHighlight.toArgb()
                    view.mapBackgroundColor = activeWeatherTheme.backgroundColor.toArgb()
                    view.roadColor = activeWeatherTheme.surfaceVariantColor.toArgb()
                    view.gridColor = activeWeatherTheme.borderColor.toArgb()
                }
            )

            // Interactive Floating HUD Controls
            Column(
                modifier = Modifier
                    .align(Alignment.CenterEnd)
                    .padding(end = 12.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Zoom In Button
                Box(
                    modifier = Modifier
                        .size(38.dp)
                        .clip(CircleShape)
                        .background(ProfessionalSurface.copy(alpha = 0.92f))
                        .border(BorderStroke(1.dp, ProfessionalBorder), CircleShape)
                        .clickable { mapViewInstance["map"]?.zoomIn() }
                        .testTag("map_zoom_in_button"),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = "Zoom In",
                        tint = ProfessionalPrimary,
                        modifier = Modifier.size(20.dp)
                    )
                }

                // Zoom Out Button
                Box(
                    modifier = Modifier
                        .size(38.dp)
                        .clip(CircleShape)
                        .background(ProfessionalSurface.copy(alpha = 0.92f))
                        .border(BorderStroke(1.dp, ProfessionalBorder), CircleShape)
                        .clickable { mapViewInstance["map"]?.zoomOut() }
                        .testTag("map_zoom_out_button"),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Remove,
                        contentDescription = "Zoom Out",
                        tint = ProfessionalPrimary,
                        modifier = Modifier.size(20.dp)
                    )
                }

                // Re-center on user position
                Box(
                    modifier = Modifier
                        .size(38.dp)
                        .clip(CircleShape)
                        .background(ProfessionalSurface.copy(alpha = 0.92f))
                        .border(BorderStroke(1.dp, ProfessionalBorder), CircleShape)
                        .clickable { mapViewInstance["map"]?.centerOnUser() }
                        .testTag("map_recenter_button"),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.MyLocation,
                        contentDescription = "Center on GPS",
                        tint = activeWeatherTheme.primaryColor,
                        modifier = Modifier.size(20.dp)
                    )
                }

                // Offline Map Tile / Weak Signal Toggle
                Box(
                    modifier = Modifier
                        .size(38.dp)
                        .clip(CircleShape)
                        .background(if (isSimulatedOffline) AlertAmberContainer else ProfessionalSurface.copy(alpha = 0.92f))
                        .border(BorderStroke(1.dp, if (isSimulatedOffline) AlertAmber else ProfessionalBorder), CircleShape)
                        .clickable {
                            val next = mapViewInstance["map"]?.toggleWeakSignalSimulation() ?: false
                            isSimulatedOffline = next
                        }
                        .testTag("map_offline_tiles_toggle_button"),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Layers,
                        contentDescription = "Toggle Offline Tiles Simulation",
                        tint = if (isSimulatedOffline) AlertAmber else ProfessionalPrimary,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        }
    }
}
