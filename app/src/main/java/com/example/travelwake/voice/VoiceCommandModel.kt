package com.example.travelwake.voice

/**
 * Action events for backwards-compatible listeners and dialog integrations.
 */
sealed class VoiceCommandAction {
    data class Snooze(val minutes: Int = 2) : VoiceCommandAction()
    object CancelAlarm : VoiceCommandAction()
    data class SetAlarm(val distanceMeters: Int) : VoiceCommandAction()
    object StartJourney : VoiceCommandAction()
    data class Unknown(val rawText: String) : VoiceCommandAction()
}

/**
 * Strongly typed command types according to the TravelWake Voice Specification.
 */
enum class CommandType {
    ADD_TODO,
    REMOVE_TODO,
    COMPLETE_TODO,
    RESTORE_TODO,

    ADD_PACK_ITEM,
    REMOVE_PACK_ITEM,
    PACK_ITEM,
    UNPACK_ITEM,

    LIST_TODOS,
    LIST_PACK_ITEMS,
    LIST_INCOMPLETE_ITEMS,

    CREATE_REMINDER,
    DELETE_REMINDER,
    SET_TRAVEL_REMINDER,
    GET_TRAVEL_CHECKLIST,

    GET_JOURNEY_STATUS,
    GET_DESTINATION,
    SNOOZE_ALARM,
    CANCEL_ALARM,
    SET_ALARM_DISTANCE,
    START_JOURNEY,

    CLEAR_ALL_PACK_ITEMS,
    CLEAR_ALL_TODOS,

    CANCEL_COMMAND,
    HELP,
    UNKNOWN
}

/**
 * Strongly typed representation of a parsed Voice Command.
 */
sealed class VoiceCommand(val type: CommandType) {
    // To-Do Commands
    data class AddTodo(
        val title: String,
        val category: String = "Trip",
        val priority: String = "NORMAL",
        val dueAt: Long? = null
    ) : VoiceCommand(CommandType.ADD_TODO)

    data class RemoveTodo(val titleOrQuery: String) : VoiceCommand(CommandType.REMOVE_TODO)
    data class CompleteTodo(val titleOrQuery: String) : VoiceCommand(CommandType.COMPLETE_TODO)
    data class RestoreTodo(val titleOrQuery: String) : VoiceCommand(CommandType.RESTORE_TODO)

    // To-Pack Commands
    data class AddPackItem(
        val item: String,
        val category: String = "Essentials",
        val essential: Boolean = false,
        val quantity: Int = 1
    ) : VoiceCommand(CommandType.ADD_PACK_ITEM)

    data class RemovePackItem(val itemOrQuery: String) : VoiceCommand(CommandType.REMOVE_PACK_ITEM)
    data class PackItemAction(val itemOrQuery: String) : VoiceCommand(CommandType.PACK_ITEM)
    data class UnpackItemAction(val itemOrQuery: String) : VoiceCommand(CommandType.UNPACK_ITEM)

    // Query Commands
    object ListTodos : VoiceCommand(CommandType.LIST_TODOS)
    object ListPackItems : VoiceCommand(CommandType.LIST_PACK_ITEMS)
    object ListIncompleteItems : VoiceCommand(CommandType.LIST_INCOMPLETE_ITEMS)
    object GetTravelChecklist : VoiceCommand(CommandType.GET_TRAVEL_CHECKLIST)

    // Reminders
    data class CreateReminder(
        val title: String,
        val triggerTime: Long,
        val timeLabel: String = ""
    ) : VoiceCommand(CommandType.CREATE_REMINDER)

    data class DeleteReminder(val titleOrQuery: String) : VoiceCommand(CommandType.DELETE_REMINDER)
    data class SetTravelReminder(val title: String, val triggerTime: Long) : VoiceCommand(CommandType.SET_TRAVEL_REMINDER)

    // Journey & Core Alarm Commands
    object GetJourneyStatus : VoiceCommand(CommandType.GET_JOURNEY_STATUS)
    object GetDestination : VoiceCommand(CommandType.GET_DESTINATION)
    data class SnoozeAlarm(val minutes: Int = 2) : VoiceCommand(CommandType.SNOOZE_ALARM)
    object CancelAlarm : VoiceCommand(CommandType.CANCEL_ALARM)
    data class SetAlarmDistance(val distanceMeters: Int) : VoiceCommand(CommandType.SET_ALARM_DISTANCE)
    object StartJourney : VoiceCommand(CommandType.START_JOURNEY)

    // Destructive Actions requiring confirmation
    object ClearAllPackItems : VoiceCommand(CommandType.CLEAR_ALL_PACK_ITEMS)
    object ClearAllTodos : VoiceCommand(CommandType.CLEAR_ALL_TODOS)

    object CancelCommand : VoiceCommand(CommandType.CANCEL_COMMAND)
    object Help : VoiceCommand(CommandType.HELP)
    data class Unknown(val rawText: String, val reason: String = "") : VoiceCommand(CommandType.UNKNOWN)
}

/**
 * Result of executing a voice command.
 */
data class VoiceExecutionResult(
    val success: Boolean,
    val spokenFeedback: String,
    val visualDisplay: String = spokenFeedback,
    val command: VoiceCommand,
    val requiresConfirmation: Boolean = false,
    val pendingActionDescription: String? = null
)

/**
 * Supported Multilingual Voice Languages for TravelWake.
 */
enum class VoiceLanguage(
    val code: String,
    val displayName: String,
    val nativeName: String,
    val samplePhrase: String
) {
    SYSTEM_DEFAULT("default", "System Default", "System Default", "Add passport to my pack list"),
    ENGLISH("en-US", "English", "English", "Add charger to my pack list"),
    HINDI("hi-IN", "Hindi", "हिन्दी", "पासपोर्ट पैक लिस्ट में जोड़ो"),
    TELUGU("te-IN", "Telugu", "తెలుగు", "పాస్‌పోర్ట్ ప్యాక్ లిస్ట్‌లో చేర్చండి"),
    MARATHI("mr-IN", "Marathi", "मराठी", "पासपोर्ट पॅक लिस्टमध्ये जोडा"),
    TAMIL("ta-IN", "Tamil", "தமிழ்", "பாஸ்போர்ட் பேக்கிங் பட்டியலில் சேர்"),
    KANNADA("kn-IN", "Kannada", "ಕನ್ನಡ", "ಪಾಸ್‌ಪೋರ್ಟ್ ಪ್ಯಾಕ್ ಲಿಸ್ಟ್‌ಗೆ ಸೇರಿಸಿ"),
    MALAYALAM("ml-IN", "Malayalam", "മലയാളം", "പാസ്‌പോർട്ട് പായ്ക്ക് ലിസ്റ്റിൽ ചേർക്കുക"),
    BENGALI("bn-IN", "Bengali", "বাংলা", "পাসপোর্ট প্যাক তালিকায় যোগ করুন"),
    GUJARATI("gu-IN", "Gujarati", "ગુજરાતી", "પાસપોર્ટ પેક લિસ્ટમાં ઉમેરો"),
    PUNJABI("pa-IN", "Punjabi", "ਪੰਜਾਬੀ", "ਪਾਸਪੋਰਟ ਪੈਕ ਲਿਸਟ ਵਿੱਚ ਜੋੜੋ");

    companion object {
        fun fromCode(code: String): VoiceLanguage {
            return values().firstOrNull { it.code.equals(code, ignoreCase = true) } ?: SYSTEM_DEFAULT
        }
    }
}
