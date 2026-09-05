package com.example.travelwake.service

import android.annotation.SuppressLint
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.location.Location
import android.media.AudioAttributes
import android.media.RingtoneManager
import android.os.Build
import android.os.IBinder
import android.os.Looper
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import android.util.Log
import androidx.core.app.NotificationCompat
import com.example.MainActivity
import com.example.travelwake.engine.CustomAlarmSoundEngine
import com.example.travelwake.engine.HapticFeedbackProfile
import com.example.travelwake.engine.LocationEngine
import com.example.travelwake.engine.SoundAlertTheme
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlin.math.roundToInt

object ServiceLocationBridge {
    private val _liveDistanceMeters = MutableStateFlow<Int?>(null)
    val liveDistanceMeters = _liveDistanceMeters.asStateFlow()

    private val _isAlarmTriggered = MutableStateFlow(false)
    val isAlarmTriggered = _isAlarmTriggered.asStateFlow()

    private val _batterySaverInfo = MutableStateFlow(BatterySaverInfo())
    val batterySaverInfo = _batterySaverInfo.asStateFlow()

    fun updateDistance(distance: Int) {
        _liveDistanceMeters.value = distance
    }

    fun setAlarmTriggered(triggered: Boolean) {
        _isAlarmTriggered.value = triggered
    }

    fun updateBatterySaverInfo(info: BatterySaverInfo) {
        _batterySaverInfo.value = info
    }

    fun reset() {
        _liveDistanceMeters.value = null
        _isAlarmTriggered.value = false
        _batterySaverInfo.value = BatterySaverInfo()
    }
}

data class BatterySaverInfo(
    val isEnabled: Boolean = true,
    val currentTierName: String = "Eco Transit Corridor",
    val pollingIntervalSeconds: Int = 15,
    val estimatedBatterySavedPercent: Int = 45,
    val tierDescription: String = "Far from stop: Polling GPS every 15s to prolong battery life."
)

class JourneyMonitoringService : Service() {

    companion object {
        private const val TAG = "JourneyService"
        const val MONITORING_CHANNEL_ID = "travelwake_monitoring"
        const val ALARM_CHANNEL_ID = "travelwake_alarm_urgent"
        const val WEATHER_ALERTS_CHANNEL_ID = "travelwake_severe_weather"
        const val MONITOR_NOTIF_ID = 1001
        const val ALARM_NOTIF_ID = 1002
        const val WEATHER_NOTIF_ID = 1003

        const val ACTION_START = "ACTION_START"
        const val ACTION_UPDATE = "ACTION_UPDATE"
        const val ACTION_STOP = "ACTION_STOP"
        const val ACTION_ACKNOWLEDGE = "ACTION_ACKNOWLEDGE"
        const val ACTION_SNOOZE = "ACTION_SNOOZE"
        const val ACTION_SET_BATTERY_SAVER = "ACTION_SET_BATTERY_SAVER"
        const val ACTION_PUSH_SEVERE_WEATHER = "ACTION_PUSH_SEVERE_WEATHER"

        const val EXTRA_DESTINATION = "EXTRA_DESTINATION"
        const val EXTRA_DISTANCE = "EXTRA_DISTANCE"
        const val EXTRA_ETA = "EXTRA_ETA"
        const val EXTRA_APPROACHING = "EXTRA_APPROACHING"

        const val EXTRA_DEST_LAT = "EXTRA_DEST_LAT"
        const val EXTRA_DEST_LON = "EXTRA_DEST_LON"
        const val EXTRA_ALERT_DISTANCE = "EXTRA_ALERT_DISTANCE"
        const val EXTRA_DEPARTURE_POINT = "EXTRA_DEPARTURE_POINT"
        const val EXTRA_ARRIVAL_TIME = "EXTRA_ARRIVAL_TIME"
        const val EXTRA_WEATHER_CONDITION = "EXTRA_WEATHER_CONDITION"
        const val EXTRA_WEATHER_TEMP = "EXTRA_WEATHER_TEMP"
        const val EXTRA_WEATHER_RECOMMENDATION = "EXTRA_WEATHER_RECOMMENDATION"
        const val EXTRA_SOUND_THEME = "EXTRA_SOUND_THEME"
        const val EXTRA_HAPTIC_PROFILE = "EXTRA_HAPTIC_PROFILE"
        const val EXTRA_DESTINATION_TRAVEL_TIPS = "EXTRA_DESTINATION_TRAVEL_TIPS"
        const val EXTRA_BATTERY_SAVER_ENABLED = "EXTRA_BATTERY_SAVER_ENABLED"

        const val EXTRA_SEVERE_ALERT_TITLE = "EXTRA_SEVERE_ALERT_TITLE"
        const val EXTRA_SEVERE_ALERT_HEADLINE = "EXTRA_SEVERE_ALERT_HEADLINE"
        const val EXTRA_SEVERE_ALERT_DESC = "EXTRA_SEVERE_ALERT_DESC"
        const val EXTRA_SEVERE_ALERT_ACTION = "EXTRA_SEVERE_ALERT_ACTION"
        const val EXTRA_SEVERE_ALERT_ICON = "EXTRA_SEVERE_ALERT_ICON"

        fun pushSevereWeatherAlert(
            context: Context,
            hazardType: String,
            headline: String,
            description: String,
            emergencyAction: String,
            icon: String = "⚠️"
        ) {
            val intent = Intent(context, JourneyMonitoringService::class.java).apply {
                action = ACTION_PUSH_SEVERE_WEATHER
                putExtra(EXTRA_SEVERE_ALERT_TITLE, hazardType)
                putExtra(EXTRA_SEVERE_ALERT_HEADLINE, headline)
                putExtra(EXTRA_SEVERE_ALERT_DESC, description)
                putExtra(EXTRA_SEVERE_ALERT_ACTION, emergencyAction)
                putExtra(EXTRA_SEVERE_ALERT_ICON, icon)
            }
            context.startService(intent)
        }

        fun startService(
            context: Context,
            destination: String,
            distanceMeters: Int,
            etaMinutes: Int,
            destLat: Double = 18.5289,
            destLon: Double = 73.8744,
            alertDistanceMeters: Int = 500,
            departurePoint: String = "Current Location",
            arrivalTimeFormatted: String = "",
            weatherCondition: String = "Clear Sky",
            weatherTemp: Int = 26,
            weatherRecommendation: String = "Comfortable travel conditions expected at arrival.",
            soundThemeId: String = SoundAlertTheme.HIGH_URGENCY_RADAR.id,
            hapticProfileId: String = HapticFeedbackProfile.ESCALATING_PULSE.id,
            destinationTravelTips: String = "",
            batterySavingModeEnabled: Boolean = true
        ) {
            val intent = Intent(context, JourneyMonitoringService::class.java).apply {
                action = ACTION_START
                putExtra(EXTRA_DESTINATION, destination)
                putExtra(EXTRA_DISTANCE, distanceMeters)
                putExtra(EXTRA_ETA, etaMinutes)
                putExtra(EXTRA_DEST_LAT, destLat)
                putExtra(EXTRA_DEST_LON, destLon)
                putExtra(EXTRA_ALERT_DISTANCE, alertDistanceMeters)
                putExtra(EXTRA_DEPARTURE_POINT, departurePoint)
                putExtra(EXTRA_ARRIVAL_TIME, arrivalTimeFormatted)
                putExtra(EXTRA_WEATHER_CONDITION, weatherCondition)
                putExtra(EXTRA_WEATHER_TEMP, weatherTemp)
                putExtra(EXTRA_WEATHER_RECOMMENDATION, weatherRecommendation)
                putExtra(EXTRA_SOUND_THEME, soundThemeId)
                putExtra(EXTRA_HAPTIC_PROFILE, hapticProfileId)
                putExtra(EXTRA_DESTINATION_TRAVEL_TIPS, destinationTravelTips)
                putExtra(EXTRA_BATTERY_SAVER_ENABLED, batterySavingModeEnabled)
            }
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                context.startForegroundService(intent)
            } else {
                context.startService(intent)
            }
        }

        fun updateService(
            context: Context,
            distanceMeters: Int,
            etaMinutes: Int,
            approaching: Boolean,
            weatherCondition: String? = null,
            weatherTemp: Int? = null,
            weatherRecommendation: String? = null,
            destinationTravelTips: String? = null
        ) {
            val intent = Intent(context, JourneyMonitoringService::class.java).apply {
                action = ACTION_UPDATE
                putExtra(EXTRA_DISTANCE, distanceMeters)
                putExtra(EXTRA_ETA, etaMinutes)
                putExtra(EXTRA_APPROACHING, approaching)
                weatherCondition?.let { putExtra(EXTRA_WEATHER_CONDITION, it) }
                weatherTemp?.let { putExtra(EXTRA_WEATHER_TEMP, it) }
                weatherRecommendation?.let { putExtra(EXTRA_WEATHER_RECOMMENDATION, it) }
                destinationTravelTips?.let { putExtra(EXTRA_DESTINATION_TRAVEL_TIPS, it) }
            }
            context.startService(intent)
        }

        fun setBatterySavingMode(context: Context, enabled: Boolean) {
            val intent = Intent(context, JourneyMonitoringService::class.java).apply {
                action = ACTION_SET_BATTERY_SAVER
                putExtra(EXTRA_BATTERY_SAVER_ENABLED, enabled)
            }
            context.startService(intent)
        }

        fun stopService(context: Context) {
            val intent = Intent(context, JourneyMonitoringService::class.java).apply {
                action = ACTION_STOP
            }
            context.startService(intent)
        }
    }

    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private var locationCallback: LocationCallback? = null
    private lateinit var customAlarmSoundEngine: CustomAlarmSoundEngine

    private var destinationName = "Destination"
    private var destLatitude = 18.5289
    private var destLongitude = 73.8744
    private var alertDistanceMeters = 500
    private var departurePoint = "Departure Point"
    private var arrivalTimeFormatted = "Arrival Time"
    private var distanceMeters = 5000
    private var etaMinutes = 15
    private var isApproaching = false

    private var weatherCondition = "Clear Sky"
    private var weatherTemp = 24
    private var weatherRecommendation = "Safe travels!"
    private var destinationTravelTips = ""
    private var batterySavingModeEnabled = true
    private var currentPollingTier = -1 // 0: Far (45s), 1: Mid (15s), 2: Near (3s)

    private var activeSevereHazard: String? = null
    private var activeSevereHeadline: String? = null
    private var activeSevereAction: String? = null

    private var soundThemeId = SoundAlertTheme.HIGH_URGENCY_RADAR.id
    private var hapticProfileId = HapticFeedbackProfile.ESCALATING_PULSE.id
    private var isAlarmTriggered = false

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onCreate() {
        super.onCreate()
        createNotificationChannels()
        customAlarmSoundEngine = CustomAlarmSoundEngine(this)
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            ACTION_STOP -> {
                stopTracking()
                stopAlarmSoundAndHaptics()
                stopForeground(STOP_FOREGROUND_REMOVE)
                stopSelf()
                ServiceLocationBridge.reset()
                return START_NOT_STICKY
            }
            ACTION_ACKNOWLEDGE -> {
                isAlarmTriggered = false
                stopAlarmSoundAndHaptics()
                ServiceLocationBridge.setAlarmTriggered(false)
                val manager = getSystemService(NOTIFICATION_SERVICE) as NotificationManager
                manager.cancel(ALARM_NOTIF_ID)
                manager.notify(MONITOR_NOTIF_ID, buildMonitoringNotification())
            }
            ACTION_SNOOZE -> {
                isAlarmTriggered = false
                stopAlarmSoundAndHaptics()
                ServiceLocationBridge.setAlarmTriggered(false)
                val manager = getSystemService(NOTIFICATION_SERVICE) as NotificationManager
                manager.cancel(ALARM_NOTIF_ID)
                manager.notify(MONITOR_NOTIF_ID, buildMonitoringNotification())
            }
            ACTION_START -> {
                destinationName = intent.getStringExtra(EXTRA_DESTINATION) ?: destinationName
                distanceMeters = intent.getIntExtra(EXTRA_DISTANCE, distanceMeters)
                etaMinutes = intent.getIntExtra(EXTRA_ETA, etaMinutes)
                destLatitude = intent.getDoubleExtra(EXTRA_DEST_LAT, destLatitude)
                destLongitude = intent.getDoubleExtra(EXTRA_DEST_LON, destLongitude)
                alertDistanceMeters = intent.getIntExtra(EXTRA_ALERT_DISTANCE, alertDistanceMeters)
                departurePoint = intent.getStringExtra(EXTRA_DEPARTURE_POINT) ?: departurePoint
                arrivalTimeFormatted = intent.getStringExtra(EXTRA_ARRIVAL_TIME) ?: arrivalTimeFormatted
                weatherCondition = intent.getStringExtra(EXTRA_WEATHER_CONDITION) ?: weatherCondition
                weatherTemp = intent.getIntExtra(EXTRA_WEATHER_TEMP, weatherTemp)
                weatherRecommendation = intent.getStringExtra(EXTRA_WEATHER_RECOMMENDATION) ?: weatherRecommendation
                soundThemeId = intent.getStringExtra(EXTRA_SOUND_THEME) ?: soundThemeId
                hapticProfileId = intent.getStringExtra(EXTRA_HAPTIC_PROFILE) ?: hapticProfileId
                intent.getStringExtra(EXTRA_DESTINATION_TRAVEL_TIPS)?.let { destinationTravelTips = it }
                batterySavingModeEnabled = intent.getBooleanExtra(EXTRA_BATTERY_SAVER_ENABLED, batterySavingModeEnabled)

                startForeground(MONITOR_NOTIF_ID, buildMonitoringNotification())
                startLocationTracking()
            }
            ACTION_UPDATE -> {
                distanceMeters = intent.getIntExtra(EXTRA_DISTANCE, distanceMeters)
                etaMinutes = intent.getIntExtra(EXTRA_ETA, etaMinutes)
                isApproaching = intent.getBooleanExtra(EXTRA_APPROACHING, isApproaching)
                intent.getStringExtra(EXTRA_WEATHER_CONDITION)?.let { weatherCondition = it }
                intent.getIntExtra(EXTRA_WEATHER_TEMP, -999).takeIf { it != -999 }?.let { weatherTemp = it }
                intent.getStringExtra(EXTRA_WEATHER_RECOMMENDATION)?.let { weatherRecommendation = it }
                intent.getStringExtra(EXTRA_DESTINATION_TRAVEL_TIPS)?.let { destinationTravelTips = it }

                applyAdaptivePolling(force = false)
                checkAndTriggerAlarmIfNeeded()

                val manager = getSystemService(NOTIFICATION_SERVICE) as NotificationManager
                manager.notify(MONITOR_NOTIF_ID, buildMonitoringNotification())
            }
            ACTION_SET_BATTERY_SAVER -> {
                batterySavingModeEnabled = intent.getBooleanExtra(EXTRA_BATTERY_SAVER_ENABLED, true)
                applyAdaptivePolling(force = true)
            }
            ACTION_PUSH_SEVERE_WEATHER -> {
                val hazard = intent.getStringExtra(EXTRA_SEVERE_ALERT_TITLE) ?: "Severe Weather Alert"
                val headline = intent.getStringExtra(EXTRA_SEVERE_ALERT_HEADLINE) ?: "Adverse weather at destination"
                val desc = intent.getStringExtra(EXTRA_SEVERE_ALERT_DESC) ?: ""
                val action = intent.getStringExtra(EXTRA_SEVERE_ALERT_ACTION) ?: "Proceed with caution."
                val icon = intent.getStringExtra(EXTRA_SEVERE_ALERT_ICON) ?: "⚠️"

                activeSevereHazard = hazard
                activeSevereHeadline = headline
                activeSevereAction = action

                val manager = getSystemService(NOTIFICATION_SERVICE) as NotificationManager
                val openIntent = packageManager.getLaunchIntentForPackage(packageName)
                val openPendingIntent = PendingIntent.getActivity(
                    this,
                    4,
                    openIntent,
                    PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
                )

                val weatherNotification = NotificationCompat.Builder(this, WEATHER_ALERTS_CHANNEL_ID)
                    .setSmallIcon(android.R.drawable.ic_dialog_alert)
                    .setContentTitle("$icon $hazard")
                    .setContentText(headline)
                    .setStyle(
                        NotificationCompat.BigTextStyle().bigText(
                            "⚠️ REAL-TIME SEVERE WEATHER FOR $destinationName\n\n" +
                                    "$desc\n\n" +
                                    "🚨 Emergency Recommended Action:\n$action"
                        )
                    )
                    .setContentIntent(openPendingIntent)
                    .setPriority(NotificationCompat.PRIORITY_HIGH)
                    .setCategory(NotificationCompat.CATEGORY_ALARM)
                    .setAutoCancel(true)
                    .build()

                manager.notify(WEATHER_NOTIF_ID, weatherNotification)

                // Refresh monitoring notification to reflect weather warning
                manager.notify(MONITOR_NOTIF_ID, buildMonitoringNotification())
            }
        }
        return START_STICKY
    }

    private fun startLocationTracking() {
        applyAdaptivePolling(force = true)
    }

    private fun applyAdaptivePolling(force: Boolean = false) {
        val targetTier = when {
            !batterySavingModeEnabled -> 2 // Force high accuracy if disabled
            distanceMeters > 10000 -> 0    // Far: > 10 km
            distanceMeters > 3000 -> 1     // Mid: 3 km to 10 km
            else -> 2                      // Near: <= 3 km or near alert boundary
        }

        val tierName: String
        val intervalMs: Long
        val minDistance: Float
        val priority: Int
        val savingsPercent: Int
        val description: String

        when (targetTier) {
            0 -> {
                tierName = "Eco Deep Sleep"
                intervalMs = 45000L
                minDistance = 100f
                priority = Priority.PRIORITY_BALANCED_POWER_ACCURACY
                savingsPercent = 75
                description = "Far from stop (>10 km): Polling GPS every 45s (~75% battery saved)"
            }
            1 -> {
                tierName = "Eco Transit Corridor"
                intervalMs = 15000L
                minDistance = 25f
                priority = Priority.PRIORITY_BALANCED_POWER_ACCURACY
                savingsPercent = 45
                description = "En route (3–10 km): Polling GPS every 15s (~45% battery saved)"
            }
            else -> {
                tierName = "High-Precision Wake Zone"
                intervalMs = 3000L
                minDistance = 5f
                priority = Priority.PRIORITY_HIGH_ACCURACY
                savingsPercent = 0
                description = "Approaching stop (<3 km): 3s GPS updates for pinpoint wake alert"
            }
        }

        ServiceLocationBridge.updateBatterySaverInfo(
            BatterySaverInfo(
                isEnabled = batterySavingModeEnabled,
                currentTierName = tierName,
                pollingIntervalSeconds = (intervalMs / 1000).toInt(),
                estimatedBatterySavedPercent = savingsPercent,
                tierDescription = description
            )
        )

        if (targetTier != currentPollingTier || force) {
            currentPollingTier = targetTier
            reconfigureLocationUpdates(intervalMs, minDistance, priority)
        }
    }

    @SuppressLint("MissingPermission")
    private fun reconfigureLocationUpdates(intervalMs: Long, minDistance: Float, priority: Int) {
        try {
            stopTracking()
            val locationRequest = LocationRequest.Builder(priority, intervalMs)
                .setMinUpdateIntervalMillis((intervalMs / 2).coerceAtLeast(1500L))
                .setMinUpdateDistanceMeters(minDistance)
                .build()

            locationCallback = object : LocationCallback() {
                override fun onLocationResult(locationResult: LocationResult) {
                    val location = locationResult.lastLocation ?: return
                    handleLocationUpdate(location)
                }
            }

            fusedLocationClient.requestLocationUpdates(
                locationRequest,
                locationCallback as LocationCallback,
                Looper.getMainLooper()
            )
            Log.d(TAG, "Dynamic GPS Polling adjusted: interval=${intervalMs}ms, priority=$priority for $destinationName")
        } catch (e: SecurityException) {
            Log.w(TAG, "Location permission missing in foreground service: ${e.message}")
        } catch (e: Exception) {
            Log.e(TAG, "Error updating location polling: ${e.message}")
        }
    }

    private fun handleLocationUpdate(location: Location) {
        val results = FloatArray(1)
        Location.distanceBetween(
            location.latitude,
            location.longitude,
            destLatitude,
            destLongitude,
            results
        )
        val calculatedDistance = results[0].roundToInt()
        distanceMeters = calculatedDistance
        etaMinutes = (calculatedDistance / 500).coerceAtLeast(1) // ~30 km/h approx

        ServiceLocationBridge.updateDistance(calculatedDistance)
        applyAdaptivePolling(force = false)
        checkAndTriggerAlarmIfNeeded()

        val manager = getSystemService(NOTIFICATION_SERVICE) as NotificationManager
        manager.notify(MONITOR_NOTIF_ID, buildMonitoringNotification())
    }

    private fun checkAndTriggerAlarmIfNeeded() {
        if (distanceMeters <= alertDistanceMeters && !isAlarmTriggered) {
            isAlarmTriggered = true
            isApproaching = true
            ServiceLocationBridge.setAlarmTriggered(true)
            triggerUrgentAlarmNotification()
            triggerCustomHapticFeedback()
            triggerCustomSoundAlert()
        } else if (distanceMeters <= (alertDistanceMeters * 1.5).toInt()) {
            isApproaching = true
        }
    }

    private fun triggerUrgentAlarmNotification() {
        val manager = getSystemService(NOTIFICATION_SERVICE) as NotificationManager
        manager.notify(ALARM_NOTIF_ID, buildAlarmNotification())
    }

    private fun triggerCustomHapticFeedback() {
        val profile = HapticFeedbackProfile.values().find { it.id == hapticProfileId } ?: HapticFeedbackProfile.ESCALATING_PULSE
        customAlarmSoundEngine.triggerHaptics(profile, repeat = true)
    }

    private fun triggerCustomSoundAlert() {
        val theme = SoundAlertTheme.values().find { it.id == soundThemeId } ?: SoundAlertTheme.HIGH_URGENCY_RADAR
        customAlarmSoundEngine.playSoundAlert(theme, looping = true)
    }

    private fun stopAlarmSoundAndHaptics() {
        try {
            customAlarmSoundEngine.stopSound()
            customAlarmSoundEngine.stopHaptics()
            stopVibration()
        } catch (e: Exception) {
            Log.w(TAG, "Error stopping alarm audio/haptics: ${e.message}")
        }
    }

    private fun buildMonitoringNotification(): Notification {
        val openAppIntent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_SINGLE_TOP
        }
        val pendingIntent = PendingIntent.getActivity(
            this,
            0,
            openAppIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val distanceStr = LocationEngine.formatDistance(distanceMeters)
        val title = if (isApproaching) "🚨 Approaching: $destinationName" else "Tracking to $destinationName"
        val body = "$distanceStr remaining • ETA ~$etaMinutes min | Arrival: $weatherTemp°C $weatherCondition"

        val bigText = "$distanceStr to $destinationName (ETA ~$etaMinutes min)\n" +
                "📍 From: $departurePoint\n" +
                "🌤️ Arrival Weather Forecast: $weatherTemp°C, $weatherCondition\n" +
                "💡 Advice: $weatherRecommendation"

        return NotificationCompat.Builder(this, MONITORING_CHANNEL_ID)
            .setSmallIcon(android.R.drawable.ic_lock_idle_alarm)
            .setContentTitle(title)
            .setContentText(body)
            .setStyle(NotificationCompat.BigTextStyle().bigText(bigText))
            .setContentIntent(pendingIntent)
            .setOngoing(true)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .setCategory(NotificationCompat.CATEGORY_NAVIGATION)
            .build()
    }

    private fun buildAlarmNotification(): Notification {
        val openAppIntent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_SINGLE_TOP
            putExtra("SHOW_ALARM", true)
        }
        val openPendingIntent = PendingIntent.getActivity(
            this,
            1,
            openAppIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val ackIntent = Intent(this, JourneyMonitoringService::class.java).apply {
            action = ACTION_ACKNOWLEDGE
        }
        val ackPendingIntent = PendingIntent.getService(
            this,
            2,
            ackIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val snoozeIntent = Intent(this, JourneyMonitoringService::class.java).apply {
            action = ACTION_SNOOZE
        }
        val snoozePendingIntent = PendingIntent.getService(
            this,
            3,
            snoozeIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val distanceStr = LocationEngine.formatDistance(distanceMeters)
        val title = "🚨 WAKE UP! Arriving at $destinationName"
        val tipsBlock = if (destinationTravelTips.isNotBlank()) {
            "\n\n🧭 Local Travel & Safety Tips (Gemini):\n$destinationTravelTips\n"
        } else ""
        val severeWeatherBlock = if (activeSevereHazard != null && activeSevereHeadline != null) {
            "\n\n🚨 URGENT SEVERE WEATHER WARNING ($activeSevereHazard):\n$activeSevereHeadline\nEmergency Action: $activeSevereAction\n"
        } else ""
        val bigText = "You are within $distanceStr of your destination!\n\n" +
                "🌤️ Arrival Forecast ($arrivalTimeFormatted): $weatherTemp°C, $weatherCondition\n" +
                "💡 Weather Alert: $weatherRecommendation$severeWeatherBlock$tipsBlock\n" +
                "Please collect all belongings from luggage racks and seat pockets before disembarking."

        val currentProfile = HapticFeedbackProfile.values().find { it.id == hapticProfileId } ?: HapticFeedbackProfile.ESCALATING_PULSE
        val hapticPattern = customAlarmSoundEngine.getWaveformPattern(currentProfile)
        val alarmSoundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM)

        return NotificationCompat.Builder(this, ALARM_CHANNEL_ID)
            .setSmallIcon(android.R.drawable.ic_lock_idle_alarm)
            .setContentTitle(title)
            .setContentText("Within $distanceStr! Forecast: $weatherTemp°C $weatherCondition")
            .setStyle(NotificationCompat.BigTextStyle().bigText(bigText))
            .setContentIntent(openPendingIntent)
            .setPriority(NotificationCompat.PRIORITY_MAX)
            .setCategory(NotificationCompat.CATEGORY_ALARM)
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
            .setSound(alarmSoundUri)
            .setVibrate(hapticPattern)
            .setAutoCancel(false)
            .setOngoing(true)
            .addAction(android.R.drawable.ic_menu_close_clear_cancel, "I'M AWAKE", ackPendingIntent)
            .addAction(android.R.drawable.ic_lock_idle_alarm, "SNOOZE (2m)", snoozePendingIntent)
            .build()
    }

    private fun triggerVibration() {
        try {
            val pattern = longArrayOf(0, 800, 400, 800, 400, 800)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                val vibratorManager = getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as? VibratorManager
                vibratorManager?.defaultVibrator?.vibrate(
                    VibrationEffect.createWaveform(pattern, 0)
                )
            } else {
                @Suppress("DEPRECATION")
                val vibrator = getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    vibrator?.vibrate(VibrationEffect.createWaveform(pattern, 0))
                } else {
                    @Suppress("DEPRECATION")
                    vibrator?.vibrate(pattern, 0)
                }
            }
        } catch (e: Exception) {
            Log.w(TAG, "Vibration failed: ${e.message}")
        }
    }

    private fun stopVibration() {
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                val vibratorManager = getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as? VibratorManager
                vibratorManager?.defaultVibrator?.cancel()
            } else {
                @Suppress("DEPRECATION")
                val vibrator = getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
                vibrator?.cancel()
            }
        } catch (e: Exception) {
            Log.w(TAG, "Error stopping vibration: ${e.message}")
        }
    }

    private fun stopTracking() {
        locationCallback?.let {
            fusedLocationClient.removeLocationUpdates(it)
            locationCallback = null
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        stopTracking()
        stopAlarmSoundAndHaptics()
    }

    private fun createNotificationChannels() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val monitorChannel = NotificationChannel(
                MONITORING_CHANNEL_ID,
                "Active Journey Distance Monitor",
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "Monitors distance to destination using Fused Location"
            }

            val alarmSoundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM)
            val audioAttributes = AudioAttributes.Builder()
                .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                .setUsage(AudioAttributes.USAGE_ALARM)
                .build()

            val alarmChannel = NotificationChannel(
                ALARM_CHANNEL_ID,
                "Urgent Travel Destination Wake Alarm",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Triggers high-priority wake alarm and shows arrival weather"
                enableVibration(true)
                vibrationPattern = longArrayOf(0, 1000, 500, 1000, 500, 1000)
                setSound(alarmSoundUri, audioAttributes)
            }

            val manager = getSystemService(NOTIFICATION_SERVICE) as NotificationManager
            manager.createNotificationChannel(monitorChannel)
            manager.createNotificationChannel(alarmChannel)

            val weatherChannel = NotificationChannel(
                WEATHER_ALERTS_CHANNEL_ID,
                "Real-Time Severe Weather Alerts",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Pushes critical thunderstorm, flash flood, and severe weather alerts for travel destinations"
                enableVibration(true)
                vibrationPattern = longArrayOf(0, 600, 300, 600)
            }
            manager.createNotificationChannel(weatherChannel)
        }
    }
}
