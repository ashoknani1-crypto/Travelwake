package com.example.travelwake.data.local

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.travelwake.data.model.PackItem
import com.example.travelwake.data.model.Reminder
import com.example.travelwake.data.model.TodoItem
import kotlinx.coroutines.flow.Flow

@Dao
interface TodoDao {
    @Query("SELECT * FROM todo_items ORDER BY completed ASC, dueAt ASC, createdAt DESC")
    fun getAllTodos(): Flow<List<TodoItem>>

    @Query("SELECT * FROM todo_items WHERE tripId = :tripId ORDER BY completed ASC, dueAt ASC")
    fun getTodosForTrip(tripId: String): Flow<List<TodoItem>>

    @Query("SELECT * FROM todo_items WHERE completed = 0 ORDER BY dueAt ASC")
    fun getIncompleteTodos(): Flow<List<TodoItem>>

    @Query("SELECT * FROM todo_items WHERE id = :id")
    suspend fun getTodoById(id: String): TodoItem?

    @Query("SELECT * FROM todo_items WHERE LOWER(title) LIKE '%' || LOWER(:query) || '%' LIMIT 5")
    suspend fun findTodosByTitle(query: String): List<TodoItem>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTodo(item: TodoItem)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(items: List<TodoItem>)

    @Update
    suspend fun updateTodo(item: TodoItem)

    @Query("UPDATE todo_items SET completed = :completed, updatedAt = :timestamp WHERE id = :id")
    suspend fun setCompleted(id: String, completed: Boolean, timestamp: Long = System.currentTimeMillis())

    @Query("DELETE FROM todo_items WHERE id = :id")
    suspend fun deleteTodoById(id: String)

    @Query("DELETE FROM todo_items WHERE completed = 1")
    suspend fun clearCompletedTodos()

    @Delete
    suspend fun deleteTodo(item: TodoItem)
}

@Dao
interface PackItemDao {
    @Query("SELECT * FROM pack_items ORDER BY packed ASC, essential DESC, category ASC, name ASC")
    fun getAllPackItems(): Flow<List<PackItem>>

    @Query("SELECT * FROM pack_items WHERE tripId = :tripId ORDER BY packed ASC, essential DESC")
    fun getPackItemsForTrip(tripId: String): Flow<List<PackItem>>

    @Query("SELECT * FROM pack_items WHERE packed = 0 ORDER BY essential DESC, name ASC")
    fun getUnpackedItems(): Flow<List<PackItem>>

    @Query("SELECT * FROM pack_items WHERE packed = 0 AND essential = 1")
    fun getUnpackedEssentialItems(): Flow<List<PackItem>>

    @Query("SELECT * FROM pack_items WHERE id = :id")
    suspend fun getPackItemById(id: String): PackItem?

    @Query("SELECT * FROM pack_items WHERE LOWER(name) LIKE '%' || LOWER(:query) || '%' LIMIT 5")
    suspend fun findPackItemsByName(query: String): List<PackItem>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPackItem(item: PackItem)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(items: List<PackItem>)

    @Update
    suspend fun updatePackItem(item: PackItem)

    @Query("UPDATE pack_items SET packed = :packed, updatedAt = :timestamp WHERE id = :id")
    suspend fun setPacked(id: String, packed: Boolean, timestamp: Long = System.currentTimeMillis())

    @Query("UPDATE pack_items SET packed = :packed, updatedAt = :timestamp")
    suspend fun setAllPacked(packed: Boolean, timestamp: Long = System.currentTimeMillis())

    @Query("DELETE FROM pack_items WHERE id = :id")
    suspend fun deletePackItemById(id: String)

    @Query("DELETE FROM pack_items")
    suspend fun deleteAllPackItems()

    @Delete
    suspend fun deletePackItem(item: PackItem)
}

@Dao
interface ReminderDao {
    @Query("SELECT * FROM reminders ORDER BY completed ASC, triggerTime ASC")
    fun getAllReminders(): Flow<List<Reminder>>

    @Query("SELECT * FROM reminders WHERE completed = 0 AND triggerTime >= :fromTime ORDER BY triggerTime ASC")
    fun getActiveReminders(fromTime: Long = System.currentTimeMillis()): Flow<List<Reminder>>

    @Query("SELECT * FROM reminders WHERE id = :id")
    suspend fun getReminderById(id: String): Reminder?

    @Query("SELECT * FROM reminders WHERE LOWER(title) LIKE '%' || LOWER(:query) || '%' LIMIT 5")
    suspend fun findRemindersByTitle(query: String): List<Reminder>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertReminder(reminder: Reminder)

    @Update
    suspend fun updateReminder(reminder: Reminder)

    @Query("UPDATE reminders SET completed = :completed WHERE id = :id")
    suspend fun setCompleted(id: String, completed: Boolean)

    @Query("DELETE FROM reminders WHERE id = :id")
    suspend fun deleteReminderById(id: String)

    @Delete
    suspend fun deleteReminder(reminder: Reminder)
}
