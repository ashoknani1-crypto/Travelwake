package com.example.travelwake.notification

import android.app.AlarmManager
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.media.AudioAttributes
import android.media.RingtoneManager
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import android.util.Log
import androidx.core.app.NotificationCompat
import com.example.MainActivity
import com.example.R

/**
 * Unified Notification Management System for TravelWake.
 *
 * Capabilities:
 * 1. Displays scheduled alerts (reminders, departure time alerts).
 * 2. Progressive alarm notifications for journey distance & time thresholds
 *    (e.g., 1000m approaching advisory, 500m wake-up target, urgent arrival alarms).
 * 3. Handles incoming local notifications while the app is in the background.
 */
class TravelWakeNotificationManager private constructor(private val context: Context) {

    companion object {
        private const val TAG = "NotificationManager"

        // Notification Channels
        const val CHANNEL_SCHEDULED_ALERTS = "channel_scheduled_alerts"
        const val CHANNEL_PROGRESSIVE_ALARMS = "channel_progressive_alarms"
        const val CHANNEL_JOURNEY_MONITORING = "channel_journey_monitoring"
        const val CHANNEL_LOCAL_NOTIFICATIONS = "channel_local_notifications"

        // Notification IDs
        const val ID_MONITORING_FOREGROUND = 1001
        const val ID_PROGRESSIVE_THRESHOLD_BASE = 2000
        const val ID_LOCAL_NOTIFICATION_DEFAULT = 3001

        @Volatile
        private var INSTANCE: TravelWakeNotificationManager? = null

        fun getInstance(context: Context): TravelWakeNotificationManager {
            return INSTANCE ?: synchronized(this) {
                val instance = TravelWakeNotificationManager(context.applicationContext)
                INSTANCE = instance
                instance
            }
        }
    }

    private val notificationManager =
        context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

    init {
        createChannels()
    }

    private fun createChannels() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val alarmSound = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM)
                ?: RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)

            val audioAttributes = AudioAttributes.Builder()
                .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                .setUsage(AudioAttributes.USAGE_ALARM)
                .build()

            // 1. Scheduled alerts channel (Time/Departure/Packing reminders)
            val scheduledChannel = NotificationChannel(
                CHANNEL_SCHEDULED_ALERTS,
                "Scheduled Alerts & Reminders",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Alerts for scheduled departure reminders and packing tasks"
                enableVibration(true)
                vibrationPattern = longArrayOf(0, 300, 200, 300)
            }

            // 2. Progressive alarms channel (Threshold wake-ups: 1000m, 500m, 100m)
            val progressiveChannel = NotificationChannel(
                CHANNEL_PROGRESSIVE_ALARMS,
                "Journey Threshold Alarms",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Progressive transit wake-up alarms as destination approaches"
                enableLights(true)
                enableVibration(true)
                vibrationPattern = longArrayOf(0, 500, 200, 500, 200, 1000)
                setSound(alarmSound, audioAttributes)
                lockscreenVisibility = Notification.VISIBILITY_PUBLIC
            }

            // 3. Ongoing journey monitoring channel
            val journeyChannel = NotificationChannel(
                CHANNEL_JOURNEY_MONITORING,
                "Journey Transit Monitor",
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "Displays live transit distance and ETA in background"
                setShowBadge(false)
            }

            // 4. Local incoming notifications channel (Background notifications)
            val localChannel = NotificationChannel(
                CHANNEL_LOCAL_NOTIFICATIONS,
                "Travel Updates & Alerts",
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                description = "General background notifications and travel advisory updates"
            }

            notificationManager.createNotificationChannels(
                listOf(scheduledChannel, progressiveChannel, journeyChannel, localChannel)
            )
        }
    }

    /**
     * Display a scheduled alert immediately or when triggered by AlarmManager.
     */
    fun showScheduledAlert(
        id: Int = (System.currentTimeMillis() % 100000).toInt(),
        title: String,
        message: String,
        actionLabel: String? = null
    ) {
        val pendingIntent = createLaunchPendingIntent()

        val builder = NotificationCompat.Builder(context, CHANNEL_SCHEDULED_ALERTS)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle(title)
            .setContentText(message)
            .setStyle(NotificationCompat.BigTextStyle().bigText(message))
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .setCategory(NotificationCompat.CATEGORY_REMINDER)

        notificationManager.notify(id, builder.build())
    }

    /**
     * Schedule a future alert via AlarmManager to ensure delivery even if app is terminated or in background.
     */
    fun scheduleAlert(
        alertId: String,
        title: String,
        message: String,
        triggerEpochMs: Long
    ) {
        try {
            val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as? AlarmManager ?: return
            val intent = Intent(context, LocalNotificationReceiver::class.java).apply {
                putExtra(LocalNotificationReceiver.EXTRA_TITLE, title)
                putExtra(LocalNotificationReceiver.EXTRA_MESSAGE, message)
                putExtra(LocalNotificationReceiver.EXTRA_CHANNEL, CHANNEL_SCHEDULED_ALERTS)
                putExtra(LocalNotificationReceiver.EXTRA_ID, alertId.hashCode())
            }
            val pendingIntent = PendingIntent.getBroadcast(
                context,
                alertId.hashCode(),
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                alarmManager.setExactAndAllowWhileIdle(
                    AlarmManager.RTC_WAKEUP,
                    triggerEpochMs,
                    pendingIntent
                )
            } else {
                alarmManager.setExact(
                    AlarmManager.RTC_WAKEUP,
                    triggerEpochMs,
                    pendingIntent
                )
            }
            Log.d(TAG, "Scheduled future alert '$title' for epoch $triggerEpochMs")
        } catch (e: Exception) {
            Log.e(TAG, "Failed to schedule alert: ${e.message}")
        }
    }

    /**
     * Displays a progressive alarm notification for journey distance thresholds.
     * Escalates sound, vibration, and visual urgency based on proximity.
     *
     * @param distanceMeters Remaining distance to destination
     * @param thresholdMeters Trigger threshold (e.g. 1000m, 500m, 100m)
     * @param destinationName Name of destination
     * @param etaMinutes Estimated arrival time in minutes
     */
    fun showProgressiveThresholdAlarm(
        distanceMeters: Int,
        thresholdMeters: Int,
        destinationName: String,
        etaMinutes: Int
    ) {
        val title = when {
            distanceMeters <= 150 -> "🚨 ARRIVING NOW: $destinationName"
            distanceMeters <= thresholdMeters -> "🔔 WAKE UP: Approaching $destinationName"
            else -> "📍 Transit Notice: $destinationName in ${distanceMeters}m"
        }

        val body = when {
            distanceMeters <= 150 -> "You are within 150m of your stop! Prepare to disembark and gather your belongings."
            distanceMeters <= thresholdMeters -> "Distance: ${distanceMeters}m remaining (~$etaMinutes min). Check your essentials."
            else -> "Passing transit milestone ($distanceMeters meters remaining)."
        }

        val launchIntent = createLaunchPendingIntent()

        val notification = NotificationCompat.Builder(context, CHANNEL_PROGRESSIVE_ALARMS)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle(title)
            .setContentText(body)
            .setStyle(NotificationCompat.BigTextStyle().bigText(body))
            .setPriority(NotificationCompat.PRIORITY_MAX)
            .setCategory(NotificationCompat.CATEGORY_ALARM)
            .setFullScreenIntent(launchIntent, true)
            .setAutoCancel(true)
            .setOngoing(distanceMeters <= thresholdMeters)
            .setContentIntent(launchIntent)
            .build()

        val notifId = ID_PROGRESSIVE_THRESHOLD_BASE + thresholdMeters
        notificationManager.notify(notifId, notification)
        triggerThresholdHaptics(distanceMeters <= thresholdMeters)
    }

    /**
     * Deliver an incoming local notification while the application is in the background.
     */
    fun showIncomingLocalNotification(
        title: String,
        message: String,
        id: Int = ID_LOCAL_NOTIFICATION_DEFAULT
    ) {
        val launchIntent = createLaunchPendingIntent()

        val notification = NotificationCompat.Builder(context, CHANNEL_LOCAL_NOTIFICATIONS)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle(title)
            .setContentText(message)
            .setStyle(NotificationCompat.BigTextStyle().bigText(message))
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setAutoCancel(true)
            .setContentIntent(launchIntent)
            .build()

        notificationManager.notify(id, notification)
    }

    /**
     * Cancel an active notification by ID.
     */
    fun cancelNotification(id: Int) {
        notificationManager.cancel(id)
    }

    private fun createLaunchPendingIntent(): PendingIntent {
        val launchIntent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        return PendingIntent.getActivity(
            context,
            0,
            launchIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
    }

    private fun triggerThresholdHaptics(isUrgent: Boolean) {
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                val vibratorManager = context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as? VibratorManager
                val vibrator = vibratorManager?.defaultVibrator
                val pattern = if (isUrgent) {
                    longArrayOf(0, 400, 150, 400, 150, 600)
                } else {
                    longArrayOf(0, 200, 100, 200)
                }
                vibrator?.vibrate(VibrationEffect.createWaveform(pattern, -1))
            } else {
                @Suppress("DEPRECATION")
                val vibrator = context.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
                val pattern = if (isUrgent) {
                    longArrayOf(0, 400, 150, 400, 150, 600)
                } else {
                    longArrayOf(0, 200, 100, 200)
                }
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    vibrator?.vibrate(VibrationEffect.createWaveform(pattern, -1))
                } else {
                    @Suppress("DEPRECATION")
                    vibrator?.vibrate(pattern, -1)
                }
            }
        } catch (e: Exception) {
            Log.w(TAG, "Haptic trigger failed: ${e.message}")
        }
    }
}

/**
 * BroadcastReceiver for delivering background local notifications scheduled with AlarmManager.
 */
class LocalNotificationReceiver : BroadcastReceiver() {
    companion object {
        const val EXTRA_TITLE = "extra_notif_title"
        const val EXTRA_MESSAGE = "extra_notif_message"
        const val EXTRA_CHANNEL = "extra_notif_channel"
        const val EXTRA_ID = "extra_notif_id"
    }

    override fun onReceive(context: Context, intent: Intent) {
        val title = intent.getStringExtra(EXTRA_TITLE) ?: "TravelWake Alert"
        val message = intent.getStringExtra(EXTRA_MESSAGE) ?: "Upcoming travel milestone"
        val notifId = intent.getIntExtra(EXTRA_ID, (System.currentTimeMillis() % 10000).toInt())

        val notifManager = TravelWakeNotificationManager.getInstance(context)
        notifManager.showScheduledAlert(
            id = notifId,
            title = title,
            message = message
        )
    }
}
