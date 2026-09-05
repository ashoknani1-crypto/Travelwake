package com.example.travelwake.engine

import android.content.Context
import android.media.AudioAttributes
import android.media.AudioFormat
import android.media.AudioManager
import android.media.AudioTrack
import android.media.MediaPlayer
import android.media.RingtoneManager
import android.net.Uri
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import android.util.Log
import java.io.File
import java.io.FileOutputStream
import java.nio.ByteBuffer
import java.nio.ByteOrder
import kotlin.math.PI
import kotlin.math.sin

enum class SoundAlertTheme(val id: String, val displayName: String, val description: String, val iconEmoji: String) {
    HIGH_URGENCY_RADAR(
        "high_urgency_radar",
        "High-Urgency Radar",
        "Pulsing harmonic radar beeps for deep sleepers",
        "🚨"
    ),
    TRANSIT_CHIME(
        "transit_chime",
        "Transit Station Chime",
        "Classic melodic 3-tone railway arrival bell",
        "🔔"
    ),
    GENTLE_SUNRISE_BELL(
        "gentle_sunrise_bell",
        "Gentle Sunrise Bell",
        "Soft acoustic wake notes with escalating volume",
        "🌅"
    ),
    SUBWAY_SIREN(
        "subway_siren",
        "Metro Rapid Alert",
        "High-contrast repeating alert pulses for noisy trains",
        "⚡"
    ),
    SYSTEM_DEFAULT(
        "system_default",
        "System Alarm Default",
        "Default Android alarm ringtone",
        "⏰"
    )
}

enum class HapticFeedbackProfile(val id: String, val displayName: String, val description: String, val iconEmoji: String) {
    ESCALATING_PULSE(
        "escalating_pulse",
        "Escalating Pulse",
        "Gradually increases pulse duration from gentle to strong",
        "📈"
    ),
    HEARTBEAT_RHYTHM(
        "heartbeat_rhythm",
        "Heartbeat Double-Tap",
        "Urgent syncopated double-vibration pattern",
        "💓"
    ),
    INTENSE_SOS(
        "intense_sos",
        "High-Urgency SOS",
        "Persistent rapid vibration bursts to penetrate heavy sleep",
        "🚨"
    ),
    GENTLE_NUDGE(
        "gentle_nudge",
        "Gentle Periodic Nudge",
        "Light vibration pulses suited for quiet commuter cabins",
        "📳"
    )
}

/**
 * High-performance audio synthesizer & haptic generator for destination alarms.
 * Provides rich custom synthesized PCM wav sound alerts and multi-waveform haptic patterns
 * that cut through train rumble and sleep.
 */
class CustomAlarmSoundEngine(private val context: Context) {

    companion object {
        private const val TAG = "CustomAlarmSoundEngine"
        private const val SAMPLE_RATE = 44100
    }

    private var activeMediaPlayer: MediaPlayer? = null

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
     * Returns the vibration waveform pattern in milliseconds for the given profile.
     */
    fun getWaveformPattern(profile: HapticFeedbackProfile): LongArray {
        return when (profile) {
            HapticFeedbackProfile.ESCALATING_PULSE -> longArrayOf(0, 200, 200, 400, 200, 700, 250, 1100)
            HapticFeedbackProfile.HEARTBEAT_RHYTHM -> longArrayOf(0, 160, 90, 160, 550)
            HapticFeedbackProfile.INTENSE_SOS -> longArrayOf(0, 120, 120, 120, 120, 120, 300, 300, 150, 300, 150, 300)
            HapticFeedbackProfile.GENTLE_NUDGE -> longArrayOf(0, 250, 750)
        }
    }

    /**
     * Triggers vibration using the selected haptic profile.
     */
    fun triggerHaptics(profile: HapticFeedbackProfile, repeat: Boolean = true) {
        try {
            val pattern = getWaveformPattern(profile)
            val repeatIndex = if (repeat) 0 else -1

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                val effect = VibrationEffect.createWaveform(pattern, repeatIndex)
                vibrator?.vibrate(effect)
            } else {
                @Suppress("DEPRECATION")
                vibrator?.vibrate(pattern, repeatIndex)
            }
            Log.d(TAG, "Triggered haptics profile: ${profile.displayName} (repeat=$repeat)")
        } catch (e: Exception) {
            Log.e(TAG, "Error triggering haptics", e)
        }
    }

    /**
     * Previews a single shot of the chosen haptic profile.
     */
    fun previewHaptics(profile: HapticFeedbackProfile) {
        triggerHaptics(profile, repeat = false)
    }

    /**
     * Cancels active haptic vibration.
     */
    fun stopHaptics() {
        try {
            vibrator?.cancel()
        } catch (e: Exception) {
            Log.e(TAG, "Error cancelling haptics", e)
        }
    }

    /**
     * Synthesizes and writes a custom WAV audio file into the app's cache directory
     * if it does not already exist. Returns the File reference.
     */
    fun getOrCreateCustomSoundFile(theme: SoundAlertTheme): File? {
        if (theme == SoundAlertTheme.SYSTEM_DEFAULT) return null

        val soundDir = File(context.cacheDir, "alarm_sounds").apply {
            if (!exists()) mkdirs()
        }
        val targetFile = File(soundDir, "${theme.id}.wav")
        if (targetFile.exists() && targetFile.length() > 1000) {
            return targetFile
        }

        try {
            val pcmData = when (theme) {
                SoundAlertTheme.HIGH_URGENCY_RADAR -> generateRadarTonePcm()
                SoundAlertTheme.TRANSIT_CHIME -> generateTransitChimePcm()
                SoundAlertTheme.GENTLE_SUNRISE_BELL -> generateSunriseBellPcm()
                SoundAlertTheme.SUBWAY_SIREN -> generateSubwaySirenPcm()
                SoundAlertTheme.SYSTEM_DEFAULT -> ByteArray(0)
            }

            writeWavFile(targetFile, pcmData, SAMPLE_RATE, channels = 1)
            Log.d(TAG, "Synthesized custom alarm audio wav: ${targetFile.absolutePath} (${targetFile.length()} bytes)")
            return targetFile
        } catch (e: Exception) {
            Log.e(TAG, "Failed to generate custom sound file", e)
            return null
        }
    }

    /**
     * Plays the selected custom sound alert in a loop for destination alarm waking.
     */
    fun playSoundAlert(theme: SoundAlertTheme, looping: Boolean = true) {
        stopSound()
        try {
            val wavFile = getOrCreateCustomSoundFile(theme)
            if (wavFile != null && wavFile.exists()) {
                activeMediaPlayer = MediaPlayer().apply {
                    setAudioAttributes(
                        AudioAttributes.Builder()
                            .setUsage(AudioAttributes.USAGE_ALARM)
                            .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                            .build()
                    )
                    setDataSource(wavFile.absolutePath)
                    isLooping = looping
                    prepare()
                    start()
                }
                Log.d(TAG, "Playing custom sound alert: ${theme.displayName}")
            } else {
                // Fallback to system alarm
                val alarmUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM)
                    ?: RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
                activeMediaPlayer = MediaPlayer().apply {
                    setAudioAttributes(
                        AudioAttributes.Builder()
                            .setUsage(AudioAttributes.USAGE_ALARM)
                            .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                            .build()
                    )
                    setDataSource(context, alarmUri)
                    isLooping = looping
                    prepare()
                    start()
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error playing sound alert", e)
        }
    }

    /**
     * Previews the sound for 3 seconds.
     */
    fun previewSound(theme: SoundAlertTheme) {
        playSoundAlert(theme, looping = false)
    }

    /**
     * Stops active alarm audio playback.
     */
    fun stopSound() {
        try {
            activeMediaPlayer?.let {
                if (it.isPlaying) it.stop()
                it.release()
            }
            activeMediaPlayer = null
        } catch (e: Exception) {
            Log.e(TAG, "Error stopping sound alert", e)
        }
    }

    // --- PCM Audio Synthesis Helpers ---

    private fun generateRadarTonePcm(): ByteArray {
        val durationSeconds = 2.0
        val numSamples = (SAMPLE_RATE * durationSeconds).toInt()
        val pcm = ByteArray(numSamples * 2)

        for (i in 0 until numSamples) {
            val t = i.toDouble() / SAMPLE_RATE
            // 880Hz / 1320Hz alternating beep
            val freq = if ((t % 0.5) < 0.25) 880.0 else 1320.0
            val env = if ((t % 0.25) < 0.2) 1.0 else 0.05
            val sample = (sin(2.0 * PI * freq * t) * env * 0.75 * Short.MAX_VALUE).toInt().toShort()
            pcm[i * 2] = (sample.toInt() and 0xFF).toByte()
            pcm[i * 2 + 1] = ((sample.toInt() shr 8) and 0xFF).toByte()
        }
        return pcm
    }

    private fun generateTransitChimePcm(): ByteArray {
        val durationSeconds = 2.4
        val numSamples = (SAMPLE_RATE * durationSeconds).toInt()
        val pcm = ByteArray(numSamples * 2)

        val notes = listOf(523.25, 659.25, 783.99) // C5, E5, G5
        val noteDuration = 0.7

        for (i in 0 until numSamples) {
            val t = i.toDouble() / SAMPLE_RATE
            val noteIndex = (t / noteDuration).toInt().coerceAtMost(notes.lastIndex)
            val noteTime = t % noteDuration
            val freq = notes[noteIndex]
            val decay = kotlin.math.exp(-3.5 * noteTime)
            val sample = (sin(2.0 * PI * freq * t) * decay * 0.8 * Short.MAX_VALUE).toInt().toShort()
            pcm[i * 2] = (sample.toInt() and 0xFF).toByte()
            pcm[i * 2 + 1] = ((sample.toInt() shr 8) and 0xFF).toByte()
        }
        return pcm
    }

    private fun generateSunriseBellPcm(): ByteArray {
        val durationSeconds = 3.0
        val numSamples = (SAMPLE_RATE * durationSeconds).toInt()
        val pcm = ByteArray(numSamples * 2)

        for (i in 0 until numSamples) {
            val t = i.toDouble() / SAMPLE_RATE
            val harmonic1 = sin(2.0 * PI * 440.0 * t)
            val harmonic2 = 0.5 * sin(2.0 * PI * 880.0 * t)
            val harmonic3 = 0.25 * sin(2.0 * PI * 1320.0 * t)
            val envelope = kotlin.math.exp(-1.5 * (t % 1.5))
            val sample = ((harmonic1 + harmonic2 + harmonic3) * envelope * 0.6 * Short.MAX_VALUE).toInt().toShort()
            pcm[i * 2] = (sample.toInt() and 0xFF).toByte()
            pcm[i * 2 + 1] = ((sample.toInt() shr 8) and 0xFF).toByte()
        }
        return pcm
    }

    private fun generateSubwaySirenPcm(): ByteArray {
        val durationSeconds = 2.0
        val numSamples = (SAMPLE_RATE * durationSeconds).toInt()
        val pcm = ByteArray(numSamples * 2)

        for (i in 0 until numSamples) {
            val t = i.toDouble() / SAMPLE_RATE
            val sweepFreq = 600.0 + 400.0 * sin(2.0 * PI * 3.0 * t)
            val sample = (sin(2.0 * PI * sweepFreq * t) * 0.7 * Short.MAX_VALUE).toInt().toShort()
            pcm[i * 2] = (sample.toInt() and 0xFF).toByte()
            pcm[i * 2 + 1] = ((sample.toInt() shr 8) and 0xFF).toByte()
        }
        return pcm
    }

    private fun writeWavFile(file: File, pcmData: ByteArray, sampleRate: Int, channels: Int) {
        val totalAudioLen = pcmData.size.toLong()
        val totalDataLen = totalAudioLen + 36
        val byteRate = (16 * sampleRate * channels / 8).toLong()

        FileOutputStream(file).use { out ->
            val header = ByteArray(44)
            header[0] = 'R'.code.toByte()
            header[1] = 'I'.code.toByte()
            header[2] = 'F'.code.toByte()
            header[3] = 'F'.code.toByte()
            header[4] = (totalDataLen and 0xff).toByte()
            header[5] = ((totalDataLen shr 8) and 0xff).toByte()
            header[6] = ((totalDataLen shr 16) and 0xff).toByte()
            header[7] = ((totalDataLen shr 24) and 0xff).toByte()
            header[8] = 'W'.code.toByte()
            header[9] = 'A'.code.toByte()
            header[10] = 'V'.code.toByte()
            header[11] = 'E'.code.toByte()
            header[12] = 'f'.code.toByte()
            header[13] = 'm'.code.toByte()
            header[14] = 't'.code.toByte()
            header[15] = ' '.code.toByte()
            header[16] = 16 // 16 for PCM
            header[17] = 0
            header[18] = 0
            header[19] = 0
            header[20] = 1 // PCM format = 1
            header[21] = 0
            header[22] = channels.toByte()
            header[23] = 0
            header[24] = (sampleRate and 0xff).toByte()
            header[25] = ((sampleRate shr 8) and 0xff).toByte()
            header[26] = ((sampleRate shr 16) and 0xff).toByte()
            header[27] = ((sampleRate shr 24) and 0xff).toByte()
            header[28] = (byteRate and 0xff).toByte()
            header[29] = ((byteRate shr 8) and 0xff).toByte()
            header[30] = ((byteRate shr 16) and 0xff).toByte()
            header[31] = ((byteRate shr 24) and 0xff).toByte()
            header[32] = (channels * 16 / 8).toByte() // block align
            header[33] = 0
            header[34] = 16 // bits per sample
            header[35] = 0
            header[36] = 'd'.code.toByte()
            header[37] = 'a'.code.toByte()
            header[38] = 't'.code.toByte()
            header[39] = 'a'.code.toByte()
            header[40] = (totalAudioLen and 0xff).toByte()
            header[41] = ((totalAudioLen shr 8) and 0xff).toByte()
            header[42] = ((totalAudioLen shr 16) and 0xff).toByte()
            header[43] = ((totalAudioLen shr 24) and 0xff).toByte()

            out.write(header, 0, 44)
            out.write(pcmData)
        }
    }
}
