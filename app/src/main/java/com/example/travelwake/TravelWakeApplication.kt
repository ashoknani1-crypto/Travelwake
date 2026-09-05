package com.example.travelwake

import android.app.Application
import com.example.travelwake.data.local.AppDatabase
import com.example.travelwake.data.model.BelongingEntity
import com.example.travelwake.data.model.SavedPlaceEntity
import com.example.travelwake.data.model.TransportMode
import com.example.travelwake.data.model.TravelDestinationEntity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class TravelWakeApplication : Application() {
    override fun onCreate() {
        super.onCreate()

        // Pre-populate default belongings, sample saved places, and travel destinations if empty
        val db = AppDatabase.getInstance(this)
        CoroutineScope(Dispatchers.IO).launch {
            val defaults = listOf(
                BelongingEntity(id = "1", name = "Phone", isChecked = false, isDefault = true, category = "Electronics"),
                BelongingEntity(id = "2", name = "Wallet & Cards", isChecked = false, isDefault = true, category = "Essentials"),
                BelongingEntity(id = "3", name = "Backpack", isChecked = false, isDefault = true, category = "Bags"),
                BelongingEntity(id = "4", name = "Laptop & Charger", isChecked = false, isDefault = true, category = "Electronics"),
                BelongingEntity(id = "5", name = "Earphones", isChecked = false, isDefault = true, category = "Electronics"),
                BelongingEntity(id = "6", name = "Train Ticket & Transit Pass", isChecked = false, isDefault = true, category = "Documents"),
                BelongingEntity(id = "7", name = "Water Bottle & Umbrella", isChecked = false, isDefault = true, category = "Essentials")
            )
            db.belongingDao().insertAll(defaults)

            // Seed travel destinations with departure points, arrival times, and alarm settings
            val currentTime = System.currentTimeMillis()
            db.travelDestinationDao().insertDestination(
                TravelDestinationEntity(
                    id = "dest_central_station",
                    destinationName = "Central Railway Station",
                    destinationAddress = "Platform 1 & Main Concourse",
                    destinationLatitude = 18.5289,
                    destinationLongitude = 73.8744,
                    departurePoint = "Pune Suburban Hub",
                    departureLatitude = 18.5204,
                    departureLongitude = 73.8567,
                    scheduledArrivalTime = currentTime + 2700_000, // +45 min
                    arrivalTimeFormatted = "09:15 AM",
                    alarmDistanceMeters = 500,
                    alarmSoundEnabled = true,
                    alarmVibrationEnabled = true,
                    progressiveAlarmEnabled = true,
                    transportMode = TransportMode.TRAIN.name,
                    notes = "Express Line 4 • Morning commute"
                )
            )
            db.travelDestinationDao().insertDestination(
                TravelDestinationEntity(
                    id = "dest_tech_hub",
                    destinationName = "Tech Innovation Park",
                    destinationAddress = "Cyber City Metro Station Hub",
                    destinationLatitude = 18.5529,
                    destinationLongitude = 73.9312,
                    departurePoint = "Green Ridge Station",
                    departureLatitude = 18.5080,
                    departureLongitude = 73.8340,
                    scheduledArrivalTime = currentTime + 3600_000, // +60 min
                    arrivalTimeFormatted = "10:30 AM",
                    alarmDistanceMeters = 1000,
                    alarmSoundEnabled = true,
                    alarmVibrationEnabled = true,
                    progressiveAlarmEnabled = true,
                    transportMode = TransportMode.METRO.name,
                    notes = "Exit via Gate 2 towards Office Towers"
                )
            )

            // Sample saved places for quick navigation
            db.savedPlaceDao().insertPlace(
                SavedPlaceEntity(
                    id = "sp_station",
                    name = "Central Railway Station",
                    address = "Platform 1 & Main Transit Concourse",
                    latitude = 18.5289,
                    longitude = 73.8744,
                    iconType = "station",
                    defaultAlertDistanceMeters = 500
                )
            )
            db.savedPlaceDao().insertPlace(
                SavedPlaceEntity(
                    id = "sp_airport",
                    name = "International Airport Terminal 2",
                    address = "Airport Road, Terminal Departures",
                    latitude = 18.5822,
                    longitude = 73.9197,
                    iconType = "airport",
                    defaultAlertDistanceMeters = 1000
                )
            )
            db.savedPlaceDao().insertPlace(
                SavedPlaceEntity(
                    id = "sp_work",
                    name = "Tech Innovation Park (Office)",
                    address = "Cyber City Metro Station Hub",
                    latitude = 18.5529,
                    longitude = 73.9312,
                    iconType = "work",
                    defaultAlertDistanceMeters = 500
                )
            )
            db.savedPlaceDao().insertPlace(
                SavedPlaceEntity(
                    id = "sp_home",
                    name = "Home",
                    address = "Oak Ridge Avenue, Green Park",
                    latitude = 18.5080,
                    longitude = 73.8340,
                    iconType = "home",
                    defaultAlertDistanceMeters = 500
                )
            )
        }
    }
}
