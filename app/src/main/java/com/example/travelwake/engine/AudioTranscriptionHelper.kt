package com.example.travelwake.engine

import android.content.Context
import android.media.MediaRecorder
import android.os.Build
import android.util.Log
import java.io.File
import java.io.FileInputStream

class AudioTranscriptionHelper(private val context: Context) {
    private val TAG = "AudioRecorderHelper"
    private var recorder: MediaRecorder? = null
    private var outputFile: File? = null
    private var isRecording: Boolean = false

    fun startRecording(): Boolean {
        return try {
            val file = File(context.cacheDir, "travelwake_voice_${System.currentTimeMillis()}.m4a")
            outputFile = file

            recorder = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                MediaRecorder(context)
            } else {
                @Suppress("DEPRECATION")
                MediaRecorder()
            }.apply {
                setAudioSource(MediaRecorder.AudioSource.MIC)
                setOutputFormat(MediaRecorder.OutputFormat.MPEG_4)
                setAudioEncoder(MediaRecorder.AudioEncoder.AAC)
                setAudioSamplingRate(44100)
                setAudioEncodingBitRate(96000)
                setOutputFile(file.absolutePath)
                prepare()
                start()
            }
            isRecording = true
            true
        } catch (e: Exception) {
            Log.e(TAG, "Failed to start recording: ${e.message}")
            isRecording = false
            false
        }
    }

    fun stopRecordingAndGetBytes(): ByteArray? {
        if (!isRecording) return null
        return try {
            recorder?.apply {
                stop()
                release()
            }
            recorder = null
            isRecording = false

            val file = outputFile
            if (file != null && file.exists()) {
                val bytes = FileInputStream(file).use { it.readBytes() }
                file.delete()
                bytes
            } else {
                null
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error stopping recorder", e)
            recorder?.release()
            recorder = null
            isRecording = false
            null
        }
    }

    fun isCurrentlyRecording(): Boolean = isRecording
}
