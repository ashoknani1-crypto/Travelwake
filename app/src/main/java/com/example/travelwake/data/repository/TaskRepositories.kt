package com.example.travelwake.data.repository

import android.app.AlarmManager
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import com.example.MainActivity
import com.example.R
import com.example.travelwake.data.local.PackItemDao
import com.example.travelwake.data.local.ReminderDao
import com.example.travelwake.data.local.TodoDao
import com.example.travelwake.data.model.PackItem
import com.example.travelwake.data.model.Reminder
import com.example.travelwake.data.model.TodoItem
import kotlinx.coroutines.flow.Flow
import java.util.UUID

/**
 * Repository for managing To-Do items and travel tasks.
 */
class TodoRepository(private val dao: TodoDao) {
    val allTodos: Flow<List<TodoItem>> = dao.getAllTodos()
    val incompleteTodos: Flow<List<TodoItem>> = dao.getIncompleteTodos()

    fun getTodosForTrip(tripId: String): Flow<List<TodoItem>> = dao.getTodosForTrip(tripId)

    suspend fun addTodo(
        title: String,
        description: String = "",
        dueAt: Long? = null,
        priority: String = "NORMAL",
        tripId: String? = null,
        category: String = "Trip"
    ): TodoItem {
        val item = TodoItem(
            id = UUID.randomUUID().toString(),
            title = title.trim(),
            description = description.trim(),
            dueAt = dueAt,
            priority = priority,
            tripId = tripId,
            category = category,
            createdAt = System.currentTimeMillis(),
            updatedAt = System.currentTimeMillis()
        )
        dao.insertTodo(item)
        return item
    }

    suspend fun insertTodo(item: TodoItem) {
        dao.insertTodo(item)
    }

    suspend fun insertAll(items: List<TodoItem>) {
        dao.insertAll(items)
    }

    suspend fun updateTodo(item: TodoItem) {
        dao.updateTodo(item.copy(updatedAt = System.currentTimeMillis()))
    }

    suspend fun toggleCompleted(id: String, currentCompleted: Boolean) {
        dao.setCompleted(id, !currentCompleted, System.currentTimeMillis())
    }

    suspend fun markCompleted(id: String) {
        dao.setCompleted(id, true, System.currentTimeMillis())
    }

    suspend fun deleteTodo(id: String) {
        dao.deleteTodoById(id)
    }

    suspend fun clearCompleted() {
        dao.clearCompletedTodos()
    }

    suspend fun findByTitle(query: String): List<TodoItem> {
        return dao.findTodosByTitle(query)
    }
}

/**
 * Repository for managing packing items, essential luggage, and checklists.
 */
class PackItemRepository(private val dao: PackItemDao) {
    val allPackItems: Flow<List<PackItem>> = dao.getAllPackItems()
    val unpackedItems: Flow<List<PackItem>> = dao.getUnpackedItems()
    val unpackedEssentialItems: Flow<List<PackItem>> = dao.getUnpackedEssentialItems()

    fun getPackItemsForTrip(tripId: String): Flow<List<PackItem>> = dao.getPackItemsForTrip(tripId)

    suspend fun addPackItem(
        name: String,
        category: String = "Essentials",
        essential: Boolean = false,
        quantity: Int = 1,
        tripId: String? = null,
        notes: String = ""
    ): PackItem {
        val item = PackItem(
            id = UUID.randomUUID().toString(),
            name = name.trim(),
            category = category,
            essential = essential,
            quantity = quantity.coerceAtLeast(1),
            tripId = tripId,
            notes = notes,
            createdAt = System.currentTimeMillis(),
            updatedAt = System.currentTimeMillis()
        )
        dao.insertPackItem(item)
        return item
    }

    suspend fun insertPackItem(item: PackItem) {
        dao.insertPackItem(item)
    }

    suspend fun insertAll(items: List<PackItem>) {
        dao.insertAll(items)
    }

    suspend fun updatePackItem(item: PackItem) {
        dao.updatePackItem(item.copy(updatedAt = System.currentTimeMillis()))
    }

    suspend fun togglePacked(id: String, currentPacked: Boolean) {
        dao.setPacked(id, !currentPacked, System.currentTimeMillis())
    }

    suspend fun markPacked(id: String) {
        dao.setPacked(id, true, System.currentTimeMillis())
    }

    suspend fun setAllPacked(packed: Boolean) {
        dao.setAllPacked(packed, System.currentTimeMillis())
    }

    suspend fun deletePackItem(id: String) {
        dao.deletePackItemById(id)
    }

    suspend fun deleteAllPackItems() {
        dao.deleteAllPackItems()
    }

    suspend fun findByName(query: String): List<PackItem> {
        return dao.findPackItemsByName(query)
    }
}

/**
 * Repository for managing and scheduling voice and travel reminders.
 */
class ReminderRepository(
    private val dao: ReminderDao,
    private val context: Context
) {
    companion object {
        private const val TAG = "ReminderRepository"
        const val REMINDER_CHANNEL_ID = "travelwake_reminders_channel"
    }

    init {
        createNotificationChannel()
    }

    val allReminders: Flow<List<Reminder>> = dao.getAllReminders()
    val activeReminders: Flow<List<Reminder>> = dao.getActiveReminders()

    suspend fun addReminder(
        title: String,
        triggerTime: Long,
        repeatRule: String? = null,
        tripId: String? = null
    ): Reminder {
        val reminder = Reminder(
            id = UUID.randomUUID().toString(),
            title = title.trim(),
            triggerTime = triggerTime,
            repeatRule = repeatRule,
            tripId = tripId,
            completed = false,
            createdAt = System.currentTimeMillis()
        )
        dao.insertReminder(reminder)
        scheduleSystemAlarm(reminder)
        return reminder
    }

    suspend fun markCompleted(id: String) {
        dao.setCompleted(id, true)
    }

    suspend fun deleteReminder(id: String) {
        dao.deleteReminderById(id)
        cancelSystemAlarm(id)
    }

    suspend fun findByTitle(query: String): List<Reminder> {
        return dao.findRemindersByTitle(query)
    }

    private fun scheduleSystemAlarm(reminder: Reminder) {
        try {
            val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as? AlarmManager ?: return
            val intent = Intent(context, ReminderBroadcastReceiver::class.java).apply {
                putExtra("reminder_id", reminder.id)
                putExtra("reminder_title", reminder.title)
            }
            val pendingIntent = PendingIntent.getBroadcast(
                context,
                reminder.id.hashCode(),
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                alarmManager.setExactAndAllowWhileIdle(
                    AlarmManager.RTC_WAKEUP,
                    reminder.triggerTime,
                    pendingIntent
                )
            } else {
                alarmManager.setExact(
                    AlarmManager.RTC_WAKEUP,
                    reminder.triggerTime,
                    pendingIntent
                )
            }
            Log.d(TAG, "Scheduled reminder '${reminder.title}' at ${reminder.triggerTime}")
        } catch (e: Exception) {
            Log.e(TAG, "Error scheduling reminder alarm: ${e.message}")
        }
    }

    private fun cancelSystemAlarm(id: String) {
        try {
            val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as? AlarmManager ?: return
            val intent = Intent(context, ReminderBroadcastReceiver::class.java)
            val pendingIntent = PendingIntent.getBroadcast(
                context,
                id.hashCode(),
                intent,
                PendingIntent.FLAG_NO_CREATE or PendingIntent.FLAG_IMMUTABLE
            )
            if (pendingIntent != null) {
                alarmManager.cancel(pendingIntent)
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error cancelling reminder alarm: ${e.message}")
        }
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                REMINDER_CHANNEL_ID,
                "TravelWake Reminders",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Notifies travel tasks, packing reminders, and departure alerts"
                enableVibration(true)
            }
            val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as? NotificationManager
            notificationManager?.createNotificationChannel(channel)
        }
    }
}

/**
 * Receiver that delivers scheduled reminder notifications to the Android notification tray.
 */
class ReminderBroadcastReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val title = intent.getStringExtra("reminder_title") ?: "TravelWake Reminder"
        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as? NotificationManager ?: return

        val launchIntent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        val pendingIntent = PendingIntent.getActivity(
            context,
            0,
            launchIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(context, ReminderRepository.REMINDER_CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle("Travel Reminder")
            .setContentText(title)
            .setStyle(NotificationCompat.BigTextStyle().bigText(title))
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .build()

        notificationManager.notify(title.hashCode(), notification)
    }
}
