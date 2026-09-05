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

class AlarmEngine(private val context: Context) {
    private val TAG = "AlarmEngine"
    private var ringtone: Ringtone? = null
    private var isAlarmActive: Boolean = false

    private val vibrator: Vibrator? by lazy {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val vibratorManager = context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as? VibratorManager
            vibratorManager?.defaultVibrator
        } else {
            @Suppress("DEPRECATION")
            context.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
        }
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
