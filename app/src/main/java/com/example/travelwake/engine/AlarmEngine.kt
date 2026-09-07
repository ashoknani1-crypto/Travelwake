package com.example.travelwake.engine

import android.content.Context
import android.media.AudioAttributes
import android.media.Ringtone
import android.media.RingtoneManager
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import android.util.Log
import com.example.travelwake.data.model.JourneyStatus
import com.example.travelwake.notification.TravelWakeNotificationManager

/**
 * Result of evaluating distance against alarm thresholds.
 */
data class ThresholdEvaluationResult(
    val triggeredThreshold: Int?,
    val message: String?,
    val shouldTriggerMainAlarm: Boolean,
    val shouldTriggerPreAlarmChime: Boolean,
    val newJourneyStatus: JourneyStatus?
)

/**
 * Centralized AlarmEngine responsible for audio/haptic execution and
 * distance-based threshold evaluation (1km, 750m, 500m, 300m, 100m).
 */
class AlarmEngine(private val context: Context) {
    private val TAG = "AlarmEngine"
    private var ringtone: Ringtone? = null
    private var isAlarmActive: Boolean = false

    // Progressive alarm latches (Each threshold triggers at most once per journey)
    private var triggered1000m = false
    private var triggered750m = false
    private var triggered500m = false
    private var triggered300m = false
    private var triggered100m = false

    private val notificationManager: TravelWakeNotificationManager by lazy {
        TravelWakeNotificationManager.getInstance(context)
    }

    private val vibrator: Vibrator? by lazy {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val vibratorManager = context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as? VibratorManager
            vibratorManager?.defaultVibrator
        } else {
            @Suppress("DEPRECATION")
            context.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
        }
    }

    /**
     * Resets all threshold latches for a new journey.
     */
    fun resetThresholds() {
        triggered1000m = false
        triggered750m = false
        triggered500m = false
        triggered300m = false
        triggered100m = false
    }

    /**
     * Evaluates remaining distance against the defined threshold intervals:
     * 1000m, 750m, user alert distance (e.g. 500m), 300m, 100m.
     * Fires corresponding notifications via [TravelWakeNotificationManager].
     */
    fun evaluateDistanceThreshold(
        meters: Int,
        alertDistanceMeters: Int,
        currentStatus: JourneyStatus,
        destinationName: String,
        etaMinutes: Int,
        progressiveEnabled: Boolean = true
    ): ThresholdEvaluationResult {
        if (!progressiveEnabled) {
            if (meters <= alertDistanceMeters && (currentStatus == JourneyStatus.ACTIVE || currentStatus == JourneyStatus.APPROACHING)) {
                notificationManager.showProgressiveThresholdAlarm(
                    distanceMeters = meters,
                    thresholdMeters = alertDistanceMeters,
                    destinationName = destinationName,
                    etaMinutes = etaMinutes
                )
                return ThresholdEvaluationResult(
                    triggeredThreshold = alertDistanceMeters,
                    message = "WAKE UP! Your destination is $alertDistanceMeters m away",
                    shouldTriggerMainAlarm = true,
                    shouldTriggerPreAlarmChime = false,
                    newJourneyStatus = JourneyStatus.ALARMING
                )
            }
            return ThresholdEvaluationResult(null, null, false, false, null)
        }

        // 1. 1 km (1000m) Threshold
        if (meters <= 1000 && !triggered1000m && alertDistanceMeters < 1000) {
            triggered1000m = true
            val msg = "1 km to destination — Prepare your belongings"
            triggerPreAlarmChime()
            notificationManager.showProgressiveThresholdAlarm(
                distanceMeters = meters,
                thresholdMeters = 1000,
                destinationName = destinationName,
                etaMinutes = etaMinutes
            )
            return ThresholdEvaluationResult(
                triggeredThreshold = 1000,
                message = msg,
                shouldTriggerMainAlarm = false,
                shouldTriggerPreAlarmChime = true,
                newJourneyStatus = null
            )
        }

        // 2. 750 m Threshold
        if (meters <= 750 && !triggered750m && alertDistanceMeters < 750) {
            triggered750m = true
            val msg = "750 m remaining — Next stop approaching"
            triggerPreAlarmChime()
            notificationManager.showProgressiveThresholdAlarm(
                distanceMeters = meters,
                thresholdMeters = 750,
                destinationName = destinationName,
                etaMinutes = etaMinutes
            )
            return ThresholdEvaluationResult(
                triggeredThreshold = 750,
                message = msg,
                shouldTriggerMainAlarm = false,
                shouldTriggerPreAlarmChime = true,
                newJourneyStatus = null
            )
        }

        // 3. User Alert Distance (e.g. 500 m) Threshold
        if (meters <= alertDistanceMeters && !triggered500m &&
            (currentStatus == JourneyStatus.ACTIVE || currentStatus == JourneyStatus.APPROACHING || currentStatus == JourneyStatus.RECOVERY)
        ) {
            triggered500m = true
            val msg = "WAKE UP! Your destination is $alertDistanceMeters m away"
            notificationManager.showProgressiveThresholdAlarm(
                distanceMeters = meters,
                thresholdMeters = alertDistanceMeters,
                destinationName = destinationName,
                etaMinutes = etaMinutes
            )
            return ThresholdEvaluationResult(
                triggeredThreshold = alertDistanceMeters,
                message = msg,
                shouldTriggerMainAlarm = true,
                shouldTriggerPreAlarmChime = false,
                newJourneyStatus = JourneyStatus.ALARMING
            )
        }

        // 4. 300 m Escalated Warning
        if (meters <= 300 && !triggered300m) {
            triggered300m = true
            val msg = "🚨 300m! Disembarkation imminent — Gather luggage now"
            notificationManager.showProgressiveThresholdAlarm(
                distanceMeters = meters,
                thresholdMeters = 300,
                destinationName = destinationName,
                etaMinutes = etaMinutes
            )
            val shouldAlarm = currentStatus != JourneyStatus.ALARMING && currentStatus != JourneyStatus.ACKNOWLEDGED
            return ThresholdEvaluationResult(
                triggeredThreshold = 300,
                message = msg,
                shouldTriggerMainAlarm = shouldAlarm,
                shouldTriggerPreAlarmChime = false,
                newJourneyStatus = if (shouldAlarm) JourneyStatus.ALARMING else null
            )
        }

        // 5. 100 m Final Warning
        if (meters <= 100 && !triggered100m) {
            triggered100m = true
            val msg = "🚨 FINAL STOP: 100m to destination platform"
            notificationManager.showProgressiveThresholdAlarm(
                distanceMeters = meters,
                thresholdMeters = 100,
                destinationName = destinationName,
                etaMinutes = etaMinutes
            )
            val shouldAlarm = currentStatus != JourneyStatus.ACKNOWLEDGED
            return ThresholdEvaluationResult(
                triggeredThreshold = 100,
                message = msg,
                shouldTriggerMainAlarm = shouldAlarm,
                shouldTriggerPreAlarmChime = false,
                newJourneyStatus = null
            )
        }

        return ThresholdEvaluationResult(null, null, false, false, null)
    }

    fun triggerMainAlarm(soundEnabled: Boolean = true, vibrateEnabled: Boolean = true) {
        if (isAlarmActive) return
        isAlarmActive = true
        Log.d(TAG, "Triggering Main Alarm! WAKE UP!")

        if (soundEnabled) {
            try {
                var alarmUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM)
                if (alarmUri == null) {
                    alarmUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
                }
                ringtone = RingtoneManager.getRingtone(context, alarmUri)?.apply {
                    audioAttributes = AudioAttributes.Builder()
                        .setUsage(AudioAttributes.USAGE_ALARM)
                        .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                        .build()
                    play()
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error playing alarm sound", e)
            }
        }

        if (vibrateEnabled) {
            try {
                val pattern = longArrayOf(0, 800, 400, 800, 400, 1000)
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    val effect = VibrationEffect.createWaveform(pattern, 0)
                    vibrator?.vibrate(effect)
                } else {
                    @Suppress("DEPRECATION")
                    vibrator?.vibrate(pattern, 0)
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error triggering vibration", e)
            }
        }
    }

    fun triggerPreAlarmChime() {
        try {
            val notifUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
            val chime = RingtoneManager.getRingtone(context, notifUri)
            chime?.play()

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                val effect = VibrationEffect.createOneShot(300, VibrationEffect.DEFAULT_AMPLITUDE)
                vibrator?.vibrate(effect)
            } else {
                @Suppress("DEPRECATION")
                vibrator?.vibrate(300)
            }
        } catch (e: Exception) {
            Log.e(TAG, "Pre-alarm chime error", e)
        }
    }

    fun stopAlarm() {
        isAlarmActive = false
        try {
            ringtone?.stop()
            ringtone = null
            vibrator?.cancel()
        } catch (e: Exception) {
            Log.e(TAG, "Error stopping alarm", e)
        }
    }

    fun isAlarming(): Boolean = isAlarmActive
}
