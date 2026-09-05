package com.example.travelwake.voice

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import android.speech.tts.TextToSpeech
import android.speech.tts.UtteranceProgressListener
import android.util.Log
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.util.Locale

/**
 * Production-ready Hands-Free Voice Assistant Manager.
 * Orchestrates Speech-To-Text (STT), deterministic intent execution, and Text-To-Speech (TTS).
 * Respects Android privacy: only listens when explicitly requested by user or system assistant.
 */
class VoiceCommandManager(
    private val context: Context,
    private var executor: VoiceCommandExecutor? = null
) {
    companion object {
        private const val TAG = "VoiceCommandManager"
    }

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main)
    private val mainHandler = Handler(Looper.getMainLooper())

    private var speechRecognizer: SpeechRecognizer? = null
    private var tts: TextToSpeech? = null
    private var isTtsReady = false

    // State Flows
    private val _isListening = MutableStateFlow(false)
    val isListening: StateFlow<Boolean> = _isListening.asStateFlow()

    private val _isSpeaking = MutableStateFlow(false)
    val isSpeaking: StateFlow<Boolean> = _isSpeaking.asStateFlow()

    private val _recognizedText = MutableStateFlow("")
    val recognizedText: StateFlow<String> = _recognizedText.asStateFlow()

    private val _rmsVolume = MutableStateFlow(0f)
    val rmsVolume: StateFlow<Float> = _rmsVolume.asStateFlow()

    private val _lastExecutionResult = MutableStateFlow<VoiceExecutionResult?>(null)
    val lastExecutionResult: StateFlow<VoiceExecutionResult?> = _lastExecutionResult.asStateFlow()

    private val _lastActionFeedback = MutableStateFlow<String?>(null)
    val lastActionFeedback: StateFlow<String?> = _lastActionFeedback.asStateFlow()

    private val _selectedLanguage = MutableStateFlow(VoiceLanguage.SYSTEM_DEFAULT)
    val selectedLanguage: StateFlow<VoiceLanguage> = _selectedLanguage.asStateFlow()

    private val _isTtsMuted = MutableStateFlow(false)
    val isTtsMuted: StateFlow<Boolean> = _isTtsMuted.asStateFlow()

    var onCommandExecuted: ((VoiceCommand) -> Unit)? = null

    init {
        initializeTextToSpeech()
    }

    fun setExecutor(commandExecutor: VoiceCommandExecutor) {
        this.executor = commandExecutor
    }

    fun setVoiceLanguage(language: VoiceLanguage) {
        _selectedLanguage.value = language
        updateTtsLanguage(language)
    }

    fun toggleTtsMute() {
        _isTtsMuted.value = !_isTtsMuted.value
    }

    private fun initializeTextToSpeech() {
        try {
            tts = TextToSpeech(context.applicationContext) { status ->
                if (status == TextToSpeech.SUCCESS) {
                    isTtsReady = true
                    updateTtsLanguage(_selectedLanguage.value)
                    tts?.setOnUtteranceProgressListener(object : UtteranceProgressListener() {
                        override fun onStart(utteranceId: String?) {
                            _isSpeaking.value = true
                        }
                        override fun onDone(utteranceId: String?) {
                            _isSpeaking.value = false
                        }
                        @Deprecated("Deprecated in Java")
                        override fun onError(utteranceId: String?) {
                            _isSpeaking.value = false
                        }
                    })
                    Log.d(TAG, "TextToSpeech initialized successfully")
                } else {
                    Log.w(TAG, "TextToSpeech initialization failed with status: $status")
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error initializing TTS: ${e.message}")
        }
    }

    private fun updateTtsLanguage(language: VoiceLanguage) {
        if (!isTtsReady || tts == null) return
        try {
            val locale = if (language == VoiceLanguage.SYSTEM_DEFAULT) {
                Locale.getDefault()
            } else {
                Locale.forLanguageTag(language.code)
            }
            val result = tts?.setLanguage(locale)
            if (result == TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED) {
                Log.w(TAG, "Language ${language.code} not fully supported by device TTS; fallback to default")
                tts?.language = Locale.getDefault()
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error setting TTS language: ${e.message}")
        }
    }

    /**
     * Speaks out the brief, friendly confirmation using Android TTS.
     */
    fun speak(text: String) {
        if (_isTtsMuted.value || !isTtsReady || tts == null) return
        try {
            tts?.speak(text, TextToSpeech.QUEUE_FLUSH, null, "TW_VOICE_${System.currentTimeMillis()}")
        } catch (e: Exception) {
            Log.e(TAG, "Error speaking text: ${e.message}")
        }
    }

    /**
     * Starts listening for user voice command using Android SpeechRecognizer.
     */
    fun startListening(onCommand: ((VoiceCommand) -> Unit)? = null) {
        if (onCommand != null) {
            this.onCommandExecuted = onCommand
        }
        _recognizedText.value = "Listening for voice command..."
        _lastActionFeedback.value = null

        mainHandler.post {
            try {
                if (!SpeechRecognizer.isRecognitionAvailable(context)) {
                    Log.w(TAG, "SpeechRecognizer not available on this device; voice readiness mode active")
                    _isListening.value = true
                    _recognizedText.value = "Voice ready: Tap quick actions or type commands"
                    return@post
                }

                speechRecognizer?.destroy()
                speechRecognizer = SpeechRecognizer.createSpeechRecognizer(context).apply {
                    setRecognitionListener(createListener())
                }

                val targetLocale = if (_selectedLanguage.value == VoiceLanguage.SYSTEM_DEFAULT) {
                    Locale.getDefault()
                } else {
                    Locale.forLanguageTag(_selectedLanguage.value.code)
                }

                val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
                    putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
                    putExtra(RecognizerIntent.EXTRA_LANGUAGE, targetLocale)
                    putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS, true)
                    putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 3)
                }

                speechRecognizer?.startListening(intent)
                _isListening.value = true
            } catch (e: Exception) {
                Log.e(TAG, "Error starting speech recognizer: ${e.message}")
                _isListening.value = false
                _recognizedText.value = "Voice input ready"
            }
        }
    }

    /**
     * Stops microphone listening and frees up audio resources.
     */
    fun stopListening() {
        mainHandler.post {
            try {
                speechRecognizer?.stopListening()
                speechRecognizer?.destroy()
                speechRecognizer = null
            } catch (e: Exception) {
                Log.w(TAG, "Error stopping speech recognizer: ${e.message}")
            }
            _isListening.value = false
            _rmsVolume.value = 0f
        }
    }

    fun destroy() {
        stopListening()
        try {
            tts?.stop()
            tts?.shutdown()
            tts = null
        } catch (e: Exception) {
            Log.w(TAG, "Error shutting down TTS: ${e.message}")
        }
    }

    /**
     * Processes spoken natural speech into a parsed command and executes it.
     */
    fun processSpokenPhrase(phrase: String): VoiceCommand {
        Log.d(TAG, "Processing spoken phrase: '$phrase'")
        _recognizedText.value = "\"$phrase\""

        val command = VoiceCommandParser.parse(phrase)
        onCommandExecuted?.invoke(command)

        scope.launch {
            val exec = executor
            if (exec != null) {
                // Check if user is confirming a pending destructive action
                val lower = phrase.lowercase(Locale.ROOT).trim()
                if (lower == "yes" || lower == "confirm" || lower.contains("yes delete") || lower.contains("yes clear")) {
                    val confirmResult = exec.confirmPendingDestructiveAction()
                    if (confirmResult != null) {
                        _lastExecutionResult.value = confirmResult
                        _lastActionFeedback.value = confirmResult.visualDisplay
                        speak(confirmResult.spokenFeedback)
                        return@launch
                    }
                } else if (lower == "no" || lower == "cancel") {
                    exec.dismissPendingAction()
                }

                val result = exec.execute(command)
                _lastExecutionResult.value = result
                _lastActionFeedback.value = result.visualDisplay
                speak(result.spokenFeedback)
            } else {
                _lastActionFeedback.value = "Command recognized: ${command.type}"
            }
        }

        return command
    }

    fun confirmPendingAction() {
        scope.launch {
            val res = executor?.confirmPendingDestructiveAction()
            if (res != null) {
                _lastExecutionResult.value = res
                _lastActionFeedback.value = res.visualDisplay
                speak(res.spokenFeedback)
            }
        }
    }

    fun dismissPendingAction() {
        executor?.dismissPendingAction()
        _lastExecutionResult.value = null
    }

    private fun createListener(): RecognitionListener {
        return object : RecognitionListener {
            override fun onReadyForSpeech(params: Bundle?) {
                _isListening.value = true
                _recognizedText.value = "Listening... Speak your command"
            }

            override fun onBeginningOfSpeech() {
                _recognizedText.value = "Listening to your voice..."
            }

            override fun onRmsChanged(rmsdB: Float) {
                _rmsVolume.value = ((rmsdB + 2f) / 12f).coerceIn(0.1f, 1.0f)
            }

            override fun onBufferReceived(buffer: ByteArray?) {}

            override fun onEndOfSpeech() {
                _isListening.value = false
            }

            override fun onError(error: Int) {
                _isListening.value = false
                val msg = when (error) {
                    SpeechRecognizer.ERROR_NO_MATCH -> "No speech recognized. Tap mic to retry."
                    SpeechRecognizer.ERROR_SPEECH_TIMEOUT -> "Speech timeout. Tap mic to speak."
                    SpeechRecognizer.ERROR_AUDIO -> "Audio recording issue. Check microphone."
                    else -> "Tap microphone to give a voice command"
                }
                _recognizedText.value = msg
            }

            override fun onResults(results: Bundle?) {
                _isListening.value = false
                val matches = results?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                if (!matches.isNullOrEmpty()) {
                    processSpokenPhrase(matches[0])
                }
            }

            override fun onPartialResults(partialResults: Bundle?) {
                val matches = partialResults?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                if (!matches.isNullOrEmpty()) {
                    _recognizedText.value = matches[0]
                }
            }

            override fun onEvent(eventType: Int, params: Bundle?) {}
        }
    }
}
