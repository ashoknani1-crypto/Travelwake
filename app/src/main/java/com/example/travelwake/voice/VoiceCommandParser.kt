package com.example.travelwake.voice

import android.util.Log
import com.example.travelwake.data.model.PackingCategory
import java.util.Calendar
import java.util.Locale
import java.util.regex.Pattern

/**
 * High-performance deterministic Natural Language Parser for TravelWake voice input.
 * Extracts intent, entities, categories, and parameters without requiring external network APIs.
 */
object VoiceCommandParser {

    private const val TAG = "VoiceCommandParser"

    // Common wake word prefixes and assistant wrappers to strip cleanly
    private val PREFIX_PATTERNS = listOf(
        Pattern.compile("^(?:hey\\s+google\\s+)?(?:ask|tell)\\s+travel\\s*wake\\s+(?:to\\s+)?", Pattern.CASE_INSENSITIVE),
        Pattern.compile("^travel\\s*wake[,:]?\\s+", Pattern.CASE_INSENSITIVE),
        Pattern.compile("^(?:please\\s+)?(?:can\\s+you\\s+)?", Pattern.CASE_INSENSITIVE)
    )

    /**
     * Parses a spoken phrase into a strongly-typed VoiceCommand.
     */
    fun parse(rawText: String): VoiceCommand {
        if (rawText.isBlank()) {
            return VoiceCommand.Unknown(rawText, "Empty speech input")
        }

        // Normalize text: strip prefixes, punctuation, redundant whitespace
        val normalized = cleanPrefixes(rawText).trim()
        val lower = normalized.lowercase(Locale.ROOT)
        Log.d(TAG, "Parsing normalized voice input: '$lower' (from '$rawText')")

        // 1. HELP COMMANDS
        if (lower == "help" || lower.contains("what can i say") || lower.contains("help me") || lower.contains("commands")) {
            return VoiceCommand.Help
        }

        // 2. CORE JOURNEY & ALARM CONTROLS
        if (isSnoozeCommand(lower)) {
            val mins = extractSnoozeMinutes(lower)
            return VoiceCommand.SnoozeAlarm(mins)
        }

        if (isCancelAlarmCommand(lower)) {
            return VoiceCommand.CancelAlarm
        }

        if (isStartJourneyCommand(lower)) {
            return VoiceCommand.StartJourney
        }

        if (isSetAlarmDistanceCommand(lower)) {
            val distance = extractDistanceMeters(lower)
            return VoiceCommand.SetAlarmDistance(distance)
        }

        if (isGetDestinationCommand(lower)) {
            return VoiceCommand.GetDestination
        }

        if (isGetJourneyStatusCommand(lower)) {
            return VoiceCommand.GetJourneyStatus
        }

        // 3. DESTRUCTIVE BULK ACTIONS (REQUIRES CONFIRMATION)
        if (isClearAllPackingCommand(lower)) {
            return VoiceCommand.ClearAllPackItems
        }

        if (isClearAllTodosCommand(lower)) {
            return VoiceCommand.ClearAllTodos
        }

        // 4. REMINDER COMMANDS
        if (isCreateReminderCommand(lower)) {
            return parseReminder(normalized, lower)
        }

        if (isDeleteReminderCommand(lower)) {
            val entity = extractEntityAfterKeyword(lower, listOf("delete reminder", "remove reminder", "cancel reminder"))
            return VoiceCommand.DeleteReminder(entity)
        }

        // 5. QUERY LISTS COMMANDS
        if (isListPackItemsCommand(lower)) {
            return if (lower.contains("incomplete") || lower.contains("left") || lower.contains("haven't") || lower.contains("still need")) {
                VoiceCommand.ListIncompleteItems
            } else {
                VoiceCommand.ListPackItems
            }
        }

        if (isListTodosCommand(lower)) {
            return VoiceCommand.ListTodos
        }

        if (isGetTravelChecklistCommand(lower)) {
            return VoiceCommand.GetTravelChecklist
        }

        // 6. PACK ITEM ACTIONS (MARK PACKED / UNPACK)
        if (isMarkPackedCommand(lower)) {
            val item = extractPackedEntity(lower)
            if (item.isNotBlank()) {
                return VoiceCommand.PackItemAction(item)
            }
        }

        if (isRemovePackItemCommand(lower)) {
            val item = extractRemovePackEntity(lower)
            if (item.isNotBlank()) {
                return VoiceCommand.RemovePackItem(item)
            }
        }

        // 7. TO-DO COMPLETION / REMOVAL
        if (isCompleteTodoCommand(lower)) {
            val task = extractCompleteTodoEntity(lower)
            if (task.isNotBlank()) {
                return VoiceCommand.CompleteTodo(task)
            }
        }

        if (isRemoveTodoCommand(lower)) {
            val task = extractRemoveTodoEntity(lower)
            if (task.isNotBlank()) {
                return VoiceCommand.RemoveTodo(task)
            }
        }

        // 8. ADD TO-DO COMMANDS
        if (isAddTodoCommand(lower)) {
            val taskTitle = extractAddTodoEntity(lower)
            if (taskTitle.isNotBlank()) {
                val priority = if (lower.contains("urgent") || lower.contains("important") || lower.contains("asap")) "HIGH" else "NORMAL"
                return VoiceCommand.AddTodo(title = taskTitle, priority = priority)
            }
        }

        // 9. ADD PACK ITEM COMMANDS (Flexible natural language matching)
        if (isAddPackItemCommand(lower)) {
            val item = extractAddPackItemEntity(lower)
            if (item.isNotBlank()) {
                val category = inferCategoryForItem(item)
                val isEssential = isItemEssential(item) || lower.contains("must") || lower.contains("essential") || lower.contains("don't let me forget")
                return VoiceCommand.AddPackItem(
                    item = item,
                    category = category,
                    essential = isEssential
                )
            }
        }

        // 10. Fallback heuristics: If user just said "Add <something>", default to pack item or todo
        val generalAddMatch = Pattern.compile("^(?:add|put|pack|remember)\\s+([a-zA-Z0-9\\s]{2,40})$", Pattern.CASE_INSENSITIVE).matcher(lower)
        if (generalAddMatch.find()) {
            val item = generalAddMatch.group(1)?.trim() ?: ""
            if (item.isNotBlank()) {
                val category = inferCategoryForItem(item)
                val isEssential = isItemEssential(item)
                return VoiceCommand.AddPackItem(item = item, category = category, essential = isEssential)
            }
        }

        return VoiceCommand.Unknown(rawText, "Could not match supported TravelWake intent")
    }

    private fun cleanPrefixes(input: String): String {
        var res = input
        for (pattern in PREFIX_PATTERNS) {
            val matcher = pattern.matcher(res)
            if (matcher.find()) {
                res = res.substring(matcher.end())
            }
        }
        return res
    }

    // ==========================================
    // INTENT DETECTION HELPERS (Multilingual)
    // ==========================================

    private fun isSnoozeCommand(lower: String): Boolean {
        return lower.contains("snooze") || lower.contains("स्नूज़") || lower.contains("స్నూజ్") ||
                lower.contains("delay alarm") || lower.contains("sleep a bit")
    }

    private fun isCancelAlarmCommand(lower: String): Boolean {
        return lower.contains("cancel alarm") || lower.contains("stop alarm") ||
                lower.contains("i'm awake") || lower.contains("i am awake") ||
                lower.contains("turn off alarm") || lower.contains("silence alarm") ||
                lower.contains("अलार्म बंद") || lower.contains("ఆపండి") || lower.contains("நிறுத்து")
    }

    private fun isStartJourneyCommand(lower: String): Boolean {
        return lower.contains("start journey") || lower.contains("begin journey") ||
                lower.contains("start my travelwake journey") || lower.contains("start my journey") ||
                lower.contains("start travelwake journey") || lower.contains("start trip") ||
                lower.contains("start my trip") || lower.contains("begin trip") ||
                lower.contains("start tracking") || lower.contains("let's go") ||
                lower.contains("यात्रा शुरू") || lower.contains("ప్రయాణం ప్రారంభించండి")
    }

    private fun isSetAlarmDistanceCommand(lower: String): Boolean {
        return lower.contains("set alarm") || lower.contains("set alert") || lower.contains("wake me up at")
    }

    private fun isGetDestinationCommand(lower: String): Boolean {
        return lower.contains("what's my destination") || lower.contains("where am i going") ||
                lower.contains("show destination") || lower.contains("गंतव्य") || lower.contains("గమ్యం")
    }

    private fun isGetJourneyStatusCommand(lower: String): Boolean {
        return lower.contains("journey status") || lower.contains("how far") ||
                lower.contains("distance to destination") || lower.contains("eta")
    }

    private fun isClearAllPackingCommand(lower: String): Boolean {
        return lower.contains("delete my packing list") || lower.contains("clear my packing list") ||
                lower.contains("remove all pack items") || lower.contains("delete all pack items") ||
                lower.contains("clear packing list")
    }

    private fun isClearAllTodosCommand(lower: String): Boolean {
        return lower.contains("delete my to-do list") || lower.contains("clear my to-do list") ||
                lower.contains("clear all tasks") || lower.contains("delete all tasks")
    }

    private fun isCreateReminderCommand(lower: String): Boolean {
        return lower.startsWith("remind me") || lower.contains("set a reminder") ||
                lower.contains("remind me to") || lower.contains("याद दिलाना") || lower.contains("గుర్తు చేయండి")
    }

    private fun isDeleteReminderCommand(lower: String): Boolean {
        return lower.startsWith("delete reminder") || lower.startsWith("remove reminder") || lower.startsWith("cancel reminder")
    }

    private fun isListPackItemsCommand(lower: String): Boolean {
        return lower.contains("what do i need to pack") || lower.contains("what's left to pack") ||
                lower.contains("what haven't i packed") || lower.contains("what is left on my packing list") ||
                lower.contains("show my packing list") || lower.contains("read my packing list") ||
                lower.contains("what to pack") || lower.contains("packing list") ||
                lower.contains("क्या पैक करना है") || lower.contains("ఏమి ప్యాక్ చేయాలి")
    }

    private fun isListTodosCommand(lower: String): Boolean {
        return lower.contains("what do i need to do") || lower.contains("what are my travel tasks") ||
                lower.contains("read my travel tasks") || lower.contains("show my to-do list") ||
                lower.contains("what's on my to-do list") || lower.contains("my tasks") ||
                lower.contains("क्या करना है") || lower.contains("నా టాస్క్‌లు")
    }

    private fun isGetTravelChecklistCommand(lower: String): Boolean {
        return lower.contains("trip checklist") || lower.contains("travel checklist") ||
                lower.contains("what is left for my trip")
    }

    private fun isMarkPackedCommand(lower: String): Boolean {
        return lower.contains("mark") && (lower.contains("packed") || lower.contains("done")) ||
                lower.endsWith("is packed") || lower.endsWith("are packed") ||
                lower.startsWith("packed ") || lower.contains("पैक हो गया") || lower.contains("ప్యాక్ అయింది")
    }

    private fun isRemovePackItemCommand(lower: String): Boolean {
        return (lower.startsWith("remove") || lower.startsWith("delete")) &&
                (lower.contains("pack") || lower.contains("bag") || lower.contains("suitcase")) ||
                lower.contains("from my pack")
    }

    private fun isCompleteTodoCommand(lower: String): Boolean {
        return (lower.contains("mark") && (lower.contains("done") || lower.contains("finished") || lower.contains("completed"))) ||
                (lower.startsWith("complete ") || lower.startsWith("finish ")) && (lower.contains("task") || lower.contains("todo")) ||
                lower.endsWith("is done") || lower.contains("पूर्ण") || lower.contains("పూర్తయింది")
    }

    private fun isRemoveTodoCommand(lower: String): Boolean {
        return (lower.startsWith("remove") || lower.startsWith("delete")) &&
                (lower.contains("task") || lower.contains("todo") || lower.contains("to-do")) ||
                lower.contains("from my to-do") || lower.contains("from my tasks")
    }

    private fun isAddTodoCommand(lower: String): Boolean {
        return lower.contains("to my to-do list") || lower.contains("to my tasks") ||
                lower.contains("to my todo") || lower.contains("add task") ||
                lower.contains("create task") || lower.contains("कार्य जोड़ो") || lower.contains("టాస్క్ జోడించండి")
    }

    private fun isAddPackItemCommand(lower: String): Boolean {
        return lower.contains("pack list") || lower.contains("packing list") ||
                lower.contains("to my pack") || lower.contains("in my bag") ||
                lower.contains("need to pack") || lower.contains("don't let me forget") ||
                lower.contains("don't forget") || lower.contains("remember to take") ||
                lower.contains("पैक लिस्ट") || lower.contains("ప్యాక్ లిస్ట్") ||
                lower.startsWith("add ") || lower.startsWith("pack ") || lower.startsWith("put ")
    }

    // ==========================================
    // ENTITY EXTRACTION LOGIC
    // ==========================================

    private fun extractPackedEntity(lower: String): String {
        // e.g. "mark passport as packed", "passport is packed", "packed passport"
        val m1 = Pattern.compile("mark\\s+(?:my\\s+)?(.+?)\\s+as\\s+packed", Pattern.CASE_INSENSITIVE).matcher(lower)
        if (m1.find()) return m1.group(1)?.trim() ?: ""

        val m2 = Pattern.compile("mark\\s+(?:my\\s+)?(.+?)\\s+packed", Pattern.CASE_INSENSITIVE).matcher(lower)
        if (m2.find()) return m2.group(1)?.trim() ?: ""

        val m3 = Pattern.compile("(?:my\\s+)?(.+?)\\s+is\\s+packed", Pattern.CASE_INSENSITIVE).matcher(lower)
        if (m3.find()) return m3.group(1)?.trim() ?: ""

        val m4 = Pattern.compile("^packed\\s+(.+)$", Pattern.CASE_INSENSITIVE).matcher(lower)
        if (m4.find()) return m4.group(1)?.trim() ?: ""

        return extractEntityAfterKeyword(lower, listOf("mark", "packed"))
    }

    private fun extractRemovePackEntity(lower: String): String {
        // e.g. "remove charger from my pack list", "remove charger"
        val m1 = Pattern.compile("(?:remove|delete)\\s+(?:my\\s+)?(.+?)\\s+(?:from\\s+(?:my\\s+)?(?:pack(?:ing)?\\s+list|bag|items))", Pattern.CASE_INSENSITIVE).matcher(lower)
        if (m1.find()) return m1.group(1)?.trim() ?: ""

        val m2 = Pattern.compile("^(?:remove|delete)\\s+(?:my\\s+)?(.+)$", Pattern.CASE_INSENSITIVE).matcher(lower)
        if (m2.find()) return m2.group(1)?.replace("from pack list", "")?.replace("pack item", "")?.trim() ?: ""

        return ""
    }

    private fun extractAddPackItemEntity(lower: String): String {
        // e.g. "add passport to my pack list", "put laptop on my pack list", "I need to pack a jacket"
        val m1 = Pattern.compile("(?:add|put|pack)\\s+(?:a\\s+|an\\s+|my\\s+)?(.+?)\\s+(?:to|on|in)\\s+(?:my\\s+)?(?:pack(?:ing)?\\s+list|bag|suitcase|trip)", Pattern.CASE_INSENSITIVE).matcher(lower)
        if (m1.find()) return m1.group(1)?.trim() ?: ""

        val m2 = Pattern.compile("i\\s+need\\s+to\\s+pack\\s+(?:a\\s+|an\\s+|my\\s+)?(.+)$", Pattern.CASE_INSENSITIVE).matcher(lower)
        if (m2.find()) return m2.group(1)?.trim() ?: ""

        val m3 = Pattern.compile("(?:don't\\s+let\\s+me\\s+forget|don't\\s+forget|remember)\\s+(?:my\\s+)?(.+?)(?:\\s+for\\s+my\\s+trip)?$", Pattern.CASE_INSENSITIVE).matcher(lower)
        if (m3.find()) return m3.group(1)?.trim() ?: ""

        val m4 = Pattern.compile("^(?:add|pack|put)\\s+(?:a\\s+|an\\s+|my\\s+)?(.+)$", Pattern.CASE_INSENSITIVE).matcher(lower)
        if (m4.find()) {
            val candidate = m4.group(1)?.replace("to my pack list", "")?.replace("to pack list", "")?.trim() ?: ""
            return candidate
        }

        return ""
    }

    private fun extractAddTodoEntity(lower: String): String {
        // e.g. "add buy train tickets to my to-do list", "add call Mom to my tasks"
        val m1 = Pattern.compile("(?:add|create)\\s+(.+?)\\s+to\\s+my\\s+(?:to-?do\\s+list|tasks|todo)", Pattern.CASE_INSENSITIVE).matcher(lower)
        if (m1.find()) return m1.group(1)?.trim() ?: ""

        val m2 = Pattern.compile("^(?:add\\s+task|create\\s+task)\\s+(.+)$", Pattern.CASE_INSENSITIVE).matcher(lower)
        if (m2.find()) return m2.group(1)?.trim() ?: ""

        return ""
    }

    private fun extractCompleteTodoEntity(lower: String): String {
        // e.g. "mark buy tickets as done", "buy tickets is done"
        val m1 = Pattern.compile("mark\\s+(.+?)\\s+as\\s+(?:done|completed|finished)", Pattern.CASE_INSENSITIVE).matcher(lower)
        if (m1.find()) return m1.group(1)?.trim() ?: ""

        val m2 = Pattern.compile("(.+?)\\s+is\\s+(?:done|completed|finished)", Pattern.CASE_INSENSITIVE).matcher(lower)
        if (m2.find()) return m2.group(1)?.trim() ?: ""

        val m3 = Pattern.compile("(?:complete|finish)\\s+(?:task\\s+)?(.+)$", Pattern.CASE_INSENSITIVE).matcher(lower)
        if (m3.find()) return m3.group(1)?.trim() ?: ""

        return ""
    }

    private fun extractRemoveTodoEntity(lower: String): String {
        val m1 = Pattern.compile("(?:remove|delete)\\s+(.+?)\\s+(?:from\\s+(?:my\\s+)?(?:tasks|to-?do\\s+list))", Pattern.CASE_INSENSITIVE).matcher(lower)
        if (m1.find()) return m1.group(1)?.trim() ?: ""

        val m2 = Pattern.compile("^(?:remove|delete)\\s+(?:task\\s+)?(.+)$", Pattern.CASE_INSENSITIVE).matcher(lower)
        if (m2.find()) return m2.group(1)?.trim() ?: ""

        return ""
    }

    private fun parseReminder(originalText: String, lower: String): VoiceCommand {
        // "remind me to charge my phone at 9 PM", "remind me to leave at 6 AM"
        val pattern = Pattern.compile("remind\\s+me\\s+(?:to\\s+)?(.+?)(?:\\s+at\\s+|\\s+by\\s+|\\s+in\\s+|\\s+tonight|\\s+tomorrow\\s+morning|$)", Pattern.CASE_INSENSITIVE)
        val matcher = pattern.matcher(lower)
        val title = if (matcher.find()) matcher.group(1)?.trim() ?: "Travel Reminder" else "Travel Reminder"

        val triggerTime = extractTimeFromPhrase(lower)
        val timeLabel = formatTriggerTime(triggerTime)

        return VoiceCommand.CreateReminder(
            title = title.replaceFirstChar { if (it.isLowerCase()) it.titlecase(Locale.ROOT) else it.toString() },
            triggerTime = triggerTime,
            timeLabel = timeLabel
        )
    }

    private fun extractTimeFromPhrase(lower: String): Long {
        val cal = Calendar.getInstance()

        // Match "at X PM" or "at X:YY AM/PM"
        val timePattern = Pattern.compile("at\\s+(\\d{1,2})(?::(\\d{2}))?\\s*(am|pm)?", Pattern.CASE_INSENSITIVE)
        val tm = timePattern.matcher(lower)
        if (tm.find()) {
            var hour = tm.group(1)?.toIntOrNull() ?: 8
            val minute = tm.group(2)?.toIntOrNull() ?: 0
            val ampm = tm.group(3)?.lowercase(Locale.ROOT)

            if (ampm == "pm" && hour < 12) hour += 12
            if (ampm == "am" && hour == 12) hour = 0

            cal.set(Calendar.HOUR_OF_DAY, hour)
            cal.set(Calendar.MINUTE, minute)
            cal.set(Calendar.SECOND, 0)
            cal.set(Calendar.MILLISECOND, 0)

            // If parsed time has already passed today, schedule for tomorrow
            if (cal.timeInMillis <= System.currentTimeMillis()) {
                cal.add(Calendar.DAY_OF_YEAR, 1)
            }
            return cal.timeInMillis
        }

        // Relative "in X minutes" or "in X hours"
        val relativePattern = Pattern.compile("in\\s+(\\d+)\\s+(minute|hour)s?", Pattern.CASE_INSENSITIVE)
        val rm = relativePattern.matcher(lower)
        if (rm.find()) {
            val amount = rm.group(1)?.toIntOrNull() ?: 10
            val unit = rm.group(2)?.lowercase(Locale.ROOT) ?: "minute"
            if (unit.startsWith("hour")) {
                cal.add(Calendar.HOUR_OF_DAY, amount)
            } else {
                cal.add(Calendar.MINUTE, amount)
            }
            return cal.timeInMillis
        }

        if (lower.contains("tonight")) {
            cal.set(Calendar.HOUR_OF_DAY, 21) // 9:00 PM
            cal.set(Calendar.MINUTE, 0)
            cal.set(Calendar.SECOND, 0)
            if (cal.timeInMillis <= System.currentTimeMillis()) {
                cal.add(Calendar.DAY_OF_YEAR, 1)
            }
            return cal.timeInMillis
        }

        if (lower.contains("tomorrow morning")) {
            cal.add(Calendar.DAY_OF_YEAR, 1)
            cal.set(Calendar.HOUR_OF_DAY, 7) // 7:00 AM
            cal.set(Calendar.MINUTE, 0)
            cal.set(Calendar.SECOND, 0)
            return cal.timeInMillis
        }

        // Default to 1 hour from now
        cal.add(Calendar.HOUR_OF_DAY, 1)
        return cal.timeInMillis
    }

    private fun formatTriggerTime(timestamp: Long): String {
        val cal = Calendar.getInstance().apply { timeInMillis = timestamp }
        val hour = cal.get(Calendar.HOUR_OF_DAY)
        val minute = cal.get(Calendar.MINUTE)
        val ampm = if (hour >= 12) "PM" else "AM"
        val displayHour = if (hour % 12 == 0) 12 else hour % 12
        return if (minute == 0) "$displayHour $ampm" else String.format(Locale.getDefault(), "%d:%02d %s", displayHour, minute, ampm)
    }

    private fun extractSnoozeMinutes(lower: String): Int {
        val m = Pattern.compile("(\\d+)\\s+minute", Pattern.CASE_INSENSITIVE).matcher(lower)
        if (m.find()) {
            return m.group(1)?.toIntOrNull() ?: 2
        }
        return if (lower.contains("5") || lower.contains("five")) 5
        else if (lower.contains("10") || lower.contains("ten")) 10
        else 2
    }

    private fun extractDistanceMeters(lower: String): Int {
        val kmMatch = Pattern.compile("(\\d+(?:\\.\\d+)?)\\s*(?:km|kilometer|kilometre)", Pattern.CASE_INSENSITIVE).matcher(lower)
        if (kmMatch.find()) {
            val km = kmMatch.group(1)?.toFloatOrNull() ?: 1.0f
            return (km * 1000).toInt()
        }

        val meterMatch = Pattern.compile("(\\d+)\\s*(?:m|meter)", Pattern.CASE_INSENSITIVE).matcher(lower)
        if (meterMatch.find()) {
            return meterMatch.group(1)?.toIntOrNull() ?: 500
        }

        return when {
            lower.contains("2000") || lower.contains("2 km") -> 2000
            lower.contains("1500") || lower.contains("1.5 km") -> 1500
            lower.contains("1000") || lower.contains("1 km") -> 1000
            lower.contains("750") -> 750
            lower.contains("500") || lower.contains("half kilometer") -> 500
            lower.contains("300") -> 300
            lower.contains("100") -> 100
            else -> 500
        }
    }

    private fun extractEntityAfterKeyword(lower: String, keywords: List<String>): String {
        for (kw in keywords) {
            val idx = lower.indexOf(kw)
            if (idx != -1) {
                return lower.substring(idx + kw.length).trim()
            }
        }
        return ""
    }

    // ==========================================
    // SMART PACKING CATEGORY & ESSENTIAL INFERENCE
    // ==========================================

    fun inferCategoryForItem(item: String): String {
        val lower = item.lowercase(Locale.ROOT)
        return when {
            lower.contains("passport") || lower.contains("ticket") || lower.contains("visa") ||
                    lower.contains("id") || lower.contains("license") || lower.contains("document") ||
                    lower.contains("pnr") || lower.contains("boarding pass") -> PackingCategory.DOCUMENTS.label

            lower.contains("phone") || lower.contains("charger") || lower.contains("laptop") ||
                    lower.contains("power bank") || lower.contains("headphone") || lower.contains("earbud") ||
                    lower.contains("adapter") || lower.contains("cable") || lower.contains("battery") -> PackingCategory.ELECTRONICS.label

            lower.contains("shirt") || lower.contains("pant") || lower.contains("jacket") ||
                    lower.contains("sock") || lower.contains("shoe") || lower.contains("tshirt") ||
                    lower.contains("dress") || lower.contains("towel") || lower.contains("clothes") -> PackingCategory.CLOTHING.label

            lower.contains("brush") || lower.contains("paste") || lower.contains("soap") ||
                    lower.contains("shampoo") || lower.contains("deodorant") || lower.contains("comb") ||
                    lower.contains("sanitizer") || lower.contains("toiletr") -> PackingCategory.TOILETRIES.label

            lower.contains("medicine") || lower.contains("pill") || lower.contains("tablet") ||
                    lower.contains("bandage") || lower.contains("first aid") || lower.contains("prescription") -> PackingCategory.HEALTH.label

            lower.contains("wallet") || lower.contains("money") || lower.contains("cash") ||
                    lower.contains("credit card") || lower.contains("card") -> PackingCategory.MONEY.label

            lower.contains("umbrella") || lower.contains("raincoat") || lower.contains("poncho") ||
                    lower.contains("waterproof") || lower.contains("sunscreen") -> PackingCategory.WEATHER.label

            else -> PackingCategory.TRAVEL.label
        }
    }

    fun isItemEssential(item: String): Boolean {
        val lower = item.lowercase(Locale.ROOT)
        return lower.contains("passport") || lower.contains("ticket") || lower.contains("wallet") ||
                lower.contains("money") || lower.contains("phone") || lower.contains("charger") ||
                lower.contains("power bank") || lower.contains("medicine") || lower.contains("id") ||
                lower.contains("keys") || lower.contains("umbrella")
    }
}
