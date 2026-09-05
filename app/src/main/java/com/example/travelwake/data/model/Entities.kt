package com.example.travelwake.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.squareup.moshi.JsonClass

enum class TransportMode(val label: String, val icon: String, val speedKmh: Float) {
    BUS("Bus", "🚌", 25f),
    TRAIN("Train", "🚆", 50f),
    METRO("Metro", "🚇", 40f),
    TAXI("Taxi", "🚕", 35f),
    CAR("Car", "🚗", 45f),
    WALKING("Walking", "🚶", 5f),
    CYCLING("Cycling", "🚲", 15f)
}

enum class JourneyStatus {
    IDLE,
    DESTINATION_SELECTED,
    READY,
    ACTIVE,
    APPROACHING,
    ALARMING,
    ACKNOWLEDGED,
    COMPLETED,
    CANCELLED
}

@Entity(tableName = "journeys")
@JsonClass(generateAdapter = true)
data class JourneyEntity(
    @PrimaryKey val id: String,
    val title: String,
    val address: String,
    val latitude: Double,
    val longitude: Double,
    val alertDistanceMeters: Int = 500,
    val transportMode: String = TransportMode.TRAIN.name,
    val status: String = JourneyStatus.ACTIVE.name,
    val startedAt: Long = System.currentTimeMillis(),
    val completedAt: Long? = null,
    val distanceRemainingMeters: Int = 0,
    val etaMinutes: Int = 0,
    val temperatureCelsius: Int = 24,
    val weatherCondition: String = "Clear",
    val weatherRecommendation: String = "Comfortable conditions",
    val departurePoint: String = "Departure Point",
    val durationMinutes: Int = 0,
    val travelTips: String = ""
)

@Entity(tableName = "saved_places")
@JsonClass(generateAdapter = true)
data class SavedPlaceEntity(
    @PrimaryKey val id: String,
    val name: String,
    val address: String,
    val latitude: Double,
    val longitude: Double,
    val iconType: String = "pin", // home, work, station, airport, college, pin
    val defaultAlertDistanceMeters: Int = 500
)

@Entity(tableName = "belonging_items")
@JsonClass(generateAdapter = true)
data class BelongingEntity(
    @PrimaryKey val id: String,
    val name: String,
    val isChecked: Boolean = false,
    val isDefault: Boolean = true,
    val category: String = "Essentials"
)

@Entity(tableName = "travel_destinations")
@JsonClass(generateAdapter = true)
data class TravelDestinationEntity(
    @PrimaryKey val id: String,
    val destinationName: String,
    val destinationAddress: String,
    val destinationLatitude: Double,
    val destinationLongitude: Double,
    val departurePoint: String,
    val departureLatitude: Double = 0.0,
    val departureLongitude: Double = 0.0,
    val scheduledArrivalTime: Long = System.currentTimeMillis() + 1800_000,
    val arrivalTimeFormatted: String = "",
    val alarmDistanceMeters: Int = 500,
    val alarmSoundEnabled: Boolean = true,
    val alarmVibrationEnabled: Boolean = true,
    val progressiveAlarmEnabled: Boolean = true,
    val transportMode: String = TransportMode.TRAIN.name,
    val notes: String = "",
    val createdAt: Long = System.currentTimeMillis()
)
