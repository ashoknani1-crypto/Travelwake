package com.example.travelwake.voice

import android.util.Log
import com.example.travelwake.data.model.JourneyStatus
import com.example.travelwake.data.model.PackItem
import com.example.travelwake.data.model.TodoItem
import com.example.travelwake.data.repository.PackItemRepository
import com.example.travelwake.data.repository.ReminderRepository
import com.example.travelwake.data.repository.TodoRepository
import com.example.travelwake.state.JourneyStateMachine
import kotlinx.coroutines.flow.first

/**
 * Executes strongly-typed Voice Commands against the application repositories.
 * Fully isolated from the GPS / Alarm lifecycle to guarantee safety,
 * with state validation provided by [JourneyStateMachine].
 */
class VoiceCommandExecutor(
    private val todoRepository: TodoRepository,
    private val packItemRepository: PackItemRepository,
    private val reminderRepository: ReminderRepository,
    private val journeyStateMachine: JourneyStateMachine? = null,
    private val onSnoozeAlarm: (Int) -> Unit,
    private val onCancelAlarm: () -> Unit,
    private val onSetAlarmDistance: (Int) -> Unit,
    private val onStartJourney: () -> Unit,
    private val getJourneyInfo: () -> Pair<String, String>, // (destinationName, distanceEtaFormatted)
    private val getWeatherWarning: () -> String? // weather recommendation
) {
    companion object {
        private const val TAG = "VoiceCommandExecutor"
    }

    // Pending confirmation state for destructive actions
    private var pendingDestructiveAction: VoiceCommand? = null

    suspend fun execute(command: VoiceCommand): VoiceExecutionResult {
        Log.d(TAG, "Executing command: ${command.type}")

        return when (command) {
            // ------------------------------------
            // PACKING COMMANDS
            // ------------------------------------
            is VoiceCommand.AddPackItem -> {
                val item = packItemRepository.addPackItem(
                    name = command.item,
                    category = command.category,
                    essential = command.essential,
                    quantity = command.quantity
                )
                val essentialNote = if (item.essential) " (marked essential)" else ""
                VoiceExecutionResult(
                    success = true,
                    spokenFeedback = "Added ${item.name}$essentialNote to your pack list.",
                    visualDisplay = "✓ Added \"${item.name}\" to ${item.category}",
                    command = command
                )
            }

            is VoiceCommand.RemovePackItem -> {
                val matches = packItemRepository.findByName(command.itemOrQuery)
                when {
                    matches.isEmpty() -> {
                        VoiceExecutionResult(
                            success = false,
                            spokenFeedback = "I couldn't find \"${command.itemOrQuery}\" in your pack list.",
                            visualDisplay = "✕ Not found: \"${command.itemOrQuery}\"",
                            command = command
                        )
                    }
                    matches.size > 1 -> {
                        // Ambiguous item resolution
                        val names = matches.take(3).joinToString(", ") { it.name }
                        VoiceExecutionResult(
                            success = false,
                            spokenFeedback = "Which item do you mean: $names?",
                            visualDisplay = "Ambiguous: Multiple items match \"${command.itemOrQuery}\"",
                            command = command
                        )
                    }
                    else -> {
                        val target = matches.first()
                        packItemRepository.deletePackItem(target.id)
                        VoiceExecutionResult(
                            success = true,
                            spokenFeedback = "Removed ${target.name}.",
                            visualDisplay = "✓ Removed \"${target.name}\"",
                            command = command
                        )
                    }
                }
            }

            is VoiceCommand.PackItemAction -> {
                val matches = packItemRepository.findByName(command.itemOrQuery)
                if (matches.isNotEmpty()) {
                    val target = matches.first()
                    packItemRepository.markPacked(target.id)
                    VoiceExecutionResult(
                        success = true,
                        spokenFeedback = "${target.name} marked as packed.",
                        visualDisplay = "✓ \"${target.name}\" is packed",
                        command = command
                    )
                } else {
                    // Item not in list, auto-add and mark packed
                    val added = packItemRepository.addPackItem(
                        name = command.itemOrQuery,
                        category = VoiceCommandParser.inferCategoryForItem(command.itemOrQuery),
                        essential = VoiceCommandParser.isItemEssential(command.itemOrQuery)
                    )
                    packItemRepository.markPacked(added.id)
                    VoiceExecutionResult(
                        success = true,
                        spokenFeedback = "Added and marked ${added.name} as packed.",
                        visualDisplay = "✓ Added & packed \"${added.name}\"",
                        command = command
                    )
                }
            }

            is VoiceCommand.UnpackItemAction -> {
                val matches = packItemRepository.findByName(command.itemOrQuery)
                if (matches.isNotEmpty()) {
                    val target = matches.first()
                    packItemRepository.togglePacked(target.id, currentPacked = true)
                    VoiceExecutionResult(
                        success = true,
                        spokenFeedback = "${target.name} marked as unpacked.",
                        visualDisplay = "✓ \"${target.name}\" unpacked",
                        command = command
                    )
                } else {
                    VoiceExecutionResult(
                        success = false,
                        spokenFeedback = "Couldn't find \"${command.itemOrQuery}\" to unpack.",
                        visualDisplay = "✕ Item not found",
                        command = command
                    )
                }
            }

            is VoiceCommand.ListPackItems, is VoiceCommand.ListIncompleteItems -> {
                val allItems = packItemRepository.allPackItems.first()
                val unpacked = allItems.filter { !it.packed }

                val weatherTip = getWeatherWarning()

                if (unpacked.isEmpty()) {
                    val tipSuffix = if (!weatherTip.isNullOrBlank()) " Note: $weatherTip" else ""
                    VoiceExecutionResult(
                        success = true,
                        spokenFeedback = "All packed! You have nothing left to pack.$tipSuffix",
                        visualDisplay = "✓ All ${allItems.size} items are packed!",
                        command = command
                    )
                } else {
                    val sampleNames = unpacked.take(3).joinToString(", ") { it.name }
                    val remainingCount = unpacked.size
                    val spoken = if (remainingCount <= 3) {
                        "You still need to pack: $sampleNames."
                    } else {
                        "You have $remainingCount items left to pack, including $sampleNames."
                    }
                    val tipSuffix = if (!weatherTip.isNullOrBlank()) " Weather notice: $weatherTip" else ""

                    VoiceExecutionResult(
                        success = true,
                        spokenFeedback = "$spoken$tipSuffix",
                        visualDisplay = "🎒 $remainingCount items left to pack: $sampleNames",
                        command = command
                    )
                }
            }

            // ------------------------------------
            // TO-DO COMMANDS
            // ------------------------------------
            is VoiceCommand.AddTodo -> {
                val todo = todoRepository.addTodo(
                    title = command.title,
                    priority = command.priority,
                    category = command.category,
                    dueAt = command.dueAt
                )
                VoiceExecutionResult(
                    success = true,
                    spokenFeedback = "Added ${todo.title} to your to-do list.",
                    visualDisplay = "✓ Added task: \"${todo.title}\"",
                    command = command
                )
            }

            is VoiceCommand.CompleteTodo -> {
                val matches = todoRepository.findByTitle(command.titleOrQuery)
                if (matches.isNotEmpty()) {
                    val target = matches.first()
                    todoRepository.markCompleted(target.id)
                    VoiceExecutionResult(
                        success = true,
                        spokenFeedback = "${target.title} marked as done.",
                        visualDisplay = "✓ Task completed: \"${target.title}\"",
                        command = command
                    )
                } else {
                    VoiceExecutionResult(
                        success = false,
                        spokenFeedback = "I couldn't find \"${command.titleOrQuery}\" in your tasks.",
                        visualDisplay = "✕ Task not found: \"${command.titleOrQuery}\"",
                        command = command
                    )
                }
            }

            is VoiceCommand.RemoveTodo -> {
                val matches = todoRepository.findByTitle(command.titleOrQuery)
                if (matches.isNotEmpty()) {
                    val target = matches.first()
                    todoRepository.deleteTodo(target.id)
                    VoiceExecutionResult(
                        success = true,
                        spokenFeedback = "Removed ${target.title}.",
                        visualDisplay = "✓ Removed task: \"${target.title}\"",
                        command = command
                    )
                } else {
                    VoiceExecutionResult(
                        success = false,
                        spokenFeedback = "Couldn't find \"${command.titleOrQuery}\" to remove.",
                        visualDisplay = "✕ Task not found",
                        command = command
                    )
                }
            }

            is VoiceCommand.ListTodos -> {
                val incomplete = todoRepository.incompleteTodos.first()
                if (incomplete.isEmpty()) {
                    VoiceExecutionResult(
                        success = true,
                        spokenFeedback = "Your to-do list is clear. No pending tasks.",
                        visualDisplay = "✓ All tasks completed!",
                        command = command
                    )
                } else {
                    val sample = incomplete.take(3).joinToString(", ") { it.title }
                    val count = incomplete.size
                    val spoken = if (count <= 3) {
                        "Your tasks are: $sample."
                    } else {
                        "You have $count pending tasks, including $sample."
                    }
                    VoiceExecutionResult(
                        success = true,
                        spokenFeedback = spoken,
                        visualDisplay = "📋 $count tasks: $sample",
                        command = command
                    )
                }
            }

            is VoiceCommand.GetTravelChecklist -> {
                val unpacked = packItemRepository.unpackedItems.first()
                val incompleteTasks = todoRepository.incompleteTodos.first()
                val spoken = "${incompleteTasks.size} tasks remaining, and ${unpacked.size} items not packed."
                VoiceExecutionResult(
                    success = true,
                    spokenFeedback = spoken,
                    visualDisplay = "📋 $spoken",
                    command = command
                )
            }

            // ------------------------------------
            // REMINDERS
            // ------------------------------------
            is VoiceCommand.CreateReminder -> {
                val reminder = reminderRepository.addReminder(
                    title = command.title,
                    triggerTime = command.triggerTime
                )
                val label = if (command.timeLabel.isNotBlank()) " for ${command.timeLabel}" else ""
                VoiceExecutionResult(
                    success = true,
                    spokenFeedback = "Reminder created${label}: ${reminder.title}.",
                    visualDisplay = "⏰ Reminder set${label}: \"${reminder.title}\"",
                    command = command
                )
            }

            is VoiceCommand.DeleteReminder -> {
                val matches = reminderRepository.findByTitle(command.titleOrQuery)
                if (matches.isNotEmpty()) {
                    val target = matches.first()
                    reminderRepository.deleteReminder(target.id)
                    VoiceExecutionResult(
                        success = true,
                        spokenFeedback = "Removed reminder: ${target.title}.",
                        visualDisplay = "✓ Removed reminder \"${target.title}\"",
                        command = command
                    )
                } else {
                    VoiceExecutionResult(
                        success = false,
                        spokenFeedback = "I couldn't find that reminder.",
                        visualDisplay = "✕ Reminder not found",
                        command = command
                    )
                }
            }

            is VoiceCommand.SetTravelReminder -> {
                reminderRepository.addReminder(command.title, command.triggerTime)
                VoiceExecutionResult(
                    success = true,
                    spokenFeedback = "Travel reminder scheduled: ${command.title}.",
                    visualDisplay = "⏰ Reminder set: \"${command.title}\"",
                    command = command
                )
            }

            // ------------------------------------
            // JOURNEY & ALARM CONTROLS
            // ------------------------------------
            is VoiceCommand.SnoozeAlarm -> {
                val currentState = journeyStateMachine?.currentState?.value
                if (currentState != null && currentState != JourneyStatus.ALARMING) {
                    VoiceExecutionResult(
                        success = false,
                        spokenFeedback = "Cannot snooze alarm. There is no active alarm sounding.",
                        visualDisplay = "⚠️ No active alarm to snooze",
                        command = command
                    )
                } else {
                    onSnoozeAlarm(command.minutes)
                    VoiceExecutionResult(
                        success = true,
                        spokenFeedback = "Alarm snoozed for ${command.minutes} minutes.",
                        visualDisplay = "💤 Snoozed for ${command.minutes}m",
                        command = command
                    )
                }
            }

            is VoiceCommand.CancelAlarm -> {
                val currentState = journeyStateMachine?.currentState?.value
                if (currentState != null && currentState != JourneyStatus.ALARMING && currentState != JourneyStatus.APPROACHING) {
                    VoiceExecutionResult(
                        success = false,
                        spokenFeedback = "No active alarm to cancel.",
                        visualDisplay = "⚠️ No active alarm",
                        command = command
                    )
                } else {
                    onCancelAlarm()
                    VoiceExecutionResult(
                        success = true,
                        spokenFeedback = "Arrival acknowledged. Alarm cancelled.",
                        visualDisplay = "🛑 Alarm cancelled",
                        command = command
                    )
                }
            }

            is VoiceCommand.SetAlarmDistance -> {
                onSetAlarmDistance(command.distanceMeters)
                VoiceExecutionResult(
                    success = true,
                    spokenFeedback = "Alarm distance set to ${command.distanceMeters} meters.",
                    visualDisplay = "⏰ Distance set to ${command.distanceMeters}m",
                    command = command
                )
            }

            is VoiceCommand.StartJourney -> {
                val currentState = journeyStateMachine?.currentState?.value
                if (currentState != null && !journeyStateMachine.canStartJourney()) {
                    VoiceExecutionResult(
                        success = false,
                        spokenFeedback = "Journey is already active. Current state: ${currentState.name.lowercase()}.",
                        visualDisplay = "⚠️ Journey already active (${currentState.name})",
                        command = command
                    )
                } else {
                    onStartJourney()
                    VoiceExecutionResult(
                        success = true,
                        spokenFeedback = "Starting journey monitoring.",
                        visualDisplay = "🚀 Journey started",
                        command = command
                    )
                }
            }

            is VoiceCommand.GetDestination -> {
                val (dest, _) = getJourneyInfo()
                val response = if (dest.isNotBlank()) "Your destination is $dest." else "No active destination set."
                VoiceExecutionResult(
                    success = true,
                    spokenFeedback = response,
                    visualDisplay = "📍 $response",
                    command = command
                )
            }

            is VoiceCommand.GetJourneyStatus -> {
                val (dest, etaDist) = getJourneyInfo()
                val response = if (dest.isNotBlank()) {
                    "$dest: $etaDist."
                } else {
                    "No active journey in progress."
                }
                VoiceExecutionResult(
                    success = true,
                    spokenFeedback = response,
                    visualDisplay = "🧭 $response",
                    command = command
                )
            }

            // ------------------------------------
            // DESTRUCTIVE COMMANDS WITH CONFIRMATION SAFETY
            // ------------------------------------
            is VoiceCommand.ClearAllPackItems -> {
                val count = packItemRepository.allPackItems.first().size
                pendingDestructiveAction = command
                VoiceExecutionResult(
                    success = false,
                    spokenFeedback = "This will remove all $count packing items. Say confirm or tap yes to continue.",
                    visualDisplay = "⚠️ Confirm: Delete all $count packing items?",
                    command = command,
                    requiresConfirmation = true,
                    pendingActionDescription = "Delete all $count packing items"
                )
            }

            is VoiceCommand.ClearAllTodos -> {
                val count = todoRepository.allTodos.first().size
                pendingDestructiveAction = command
                VoiceExecutionResult(
                    success = false,
                    spokenFeedback = "This will remove all $count tasks. Say confirm or tap yes to continue.",
                    visualDisplay = "⚠️ Confirm: Delete all $count tasks?",
                    command = command,
                    requiresConfirmation = true,
                    pendingActionDescription = "Delete all $count tasks"
                )
            }

            VoiceCommand.CancelCommand -> {
                pendingDestructiveAction = null
                VoiceExecutionResult(
                    success = true,
                    spokenFeedback = "Cancelled.",
                    visualDisplay = "Cancelled",
                    command = command
                )
            }

            VoiceCommand.Help -> {
                VoiceExecutionResult(
                    success = true,
                    spokenFeedback = "You can say: Add passport to pack list, what's left to pack, add buy tickets to my tasks, or snooze alarm.",
                    visualDisplay = "💡 Try: \"Add charger to pack list\", \"What's left to pack?\", \"Remind me at 6 AM\"",
                    command = command
                )
            }

            is VoiceCommand.Unknown -> {
                VoiceExecutionResult(
                    success = false,
                    spokenFeedback = "I didn't catch that. You can ask to add pack items, tasks, or check your trip checklist.",
                    visualDisplay = "Unrecognized: \"${command.rawText}\"",
                    command = command
                )
            }

            else -> {
                VoiceExecutionResult(
                    success = false,
                    spokenFeedback = "Command completed.",
                    visualDisplay = "Done",
                    command = command
                )
            }
        }
    }

    suspend fun confirmPendingDestructiveAction(): VoiceExecutionResult? {
        val pending = pendingDestructiveAction ?: return null
        pendingDestructiveAction = null

        return when (pending) {
            is VoiceCommand.ClearAllPackItems -> {
                packItemRepository.deleteAllPackItems()
                VoiceExecutionResult(
                    success = true,
                    spokenFeedback = "Packing list cleared.",
                    visualDisplay = "✓ Packing list cleared",
                    command = pending
                )
            }
            is VoiceCommand.ClearAllTodos -> {
                todoRepository.clearCompleted()
                VoiceExecutionResult(
                    success = true,
                    spokenFeedback = "Completed tasks cleared.",
                    visualDisplay = "✓ Tasks cleared",
                    command = pending
                )
            }
            else -> null
        }
    }

    fun dismissPendingAction() {
        pendingDestructiveAction = null
    }
}
