package com.example.travelwake.engine

import android.annotation.SuppressLint
import android.content.Context
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.os.Bundle
import android.util.Log
import com.example.travelwake.data.model.TransportMode
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlin.math.roundToInt

data class CurrentLocation(
    val latitude: Double = 18.5204, // Default Pune / Central location
    val longitude: Double = 73.8567,
    val accuracyMeters: Float = 5f,
    val speedKmh: Float = 40f,
    val isMock: Boolean = false
)

class LocationEngine(private val context: Context) {
    private val TAG = "LocationEngine"
    private val fusedLocationClient: FusedLocationProviderClient =
        LocationServices.getFusedLocationProviderClient(context)

    private val _currentLocation = MutableStateFlow(CurrentLocation())
    val currentLocation: StateFlow<CurrentLocation> = _currentLocation.asStateFlow()

    private var isSimulating = false
    private var simulationTargetLat = 0.0
    private var simulationTargetLon = 0.0

    @SuppressLint("MissingPermission")
    fun startRealLocationUpdates() {
        try {
            fusedLocationClient.getCurrentLocation(Priority.PRIORITY_HIGH_ACCURACY, null)
                .addOnSuccessListener { loc ->
                    if (loc != null) {
                        _currentLocation.value = CurrentLocation(
                            latitude = loc.latitude,
                            longitude = loc.longitude,
                            accuracyMeters = loc.accuracy,
                            speedKmh = (loc.speed * 3.6f).coerceAtLeast(10f),
                            isMock = loc.isFromMockProvider
                        )
                    }
                }
        } catch (e: Exception) {
            Log.w(TAG, "Location permission not granted or GPS off: ${e.message}")
        }
    }

    fun setSimulatedLocation(lat: Double, lon: Double, speedKmh: Float = 45f) {
        _currentLocation.value = CurrentLocation(
            latitude = lat,
            longitude = lon,
            accuracyMeters = 3f,
            speedKmh = speedKmh,
            isMock = true
        )
    }

    companion object {
        fun calculateDistanceMeters(
            startLat: Double,
            startLon: Double,
            destLat: Double,
            destLon: Double
        ): Int {
            val results = FloatArray(1)
            Location.distanceBetween(startLat, startLon, destLat, destLon, results)
            return results[0].roundToInt()
        }

        fun calculateEtaMinutes(distanceMeters: Int, mode: TransportMode): Int {
            val speedMetersPerMin = (mode.speedKmh * 1000f) / 60f
            val minutes = (distanceMeters / speedMetersPerMin).roundToInt()
            return minutes.coerceAtLeast(1)
        }

        fun formatDistance(meters: Int): String {
            return if (meters >= 1000) {
                val km = meters / 1000.0
                String.format("%.1f km", km)
            } else {
                "$meters m"
            }
        }
    }
}
