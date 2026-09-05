package com.example.travelwake.data.local

import android.content.Context
import androidx.room.Dao
import androidx.room.Database
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.Update
import com.example.travelwake.data.model.BelongingEntity
import com.example.travelwake.data.model.JourneyEntity
import com.example.travelwake.data.model.PackItem
import com.example.travelwake.data.model.Reminder
import com.example.travelwake.data.model.SavedPlaceEntity
import com.example.travelwake.data.model.TodoItem
import com.example.travelwake.data.model.TravelDestinationEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface TravelDestinationDao {
    @Query("SELECT * FROM travel_destinations ORDER BY createdAt DESC")
    fun getAllDestinations(): Flow<List<TravelDestinationEntity>>

    @Query("SELECT * FROM travel_destinations WHERE id = :id")
    fun getDestinationById(id: String): Flow<TravelDestinationEntity?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertDestination(destination: TravelDestinationEntity)

    @Update
    suspend fun updateDestination(destination: TravelDestinationEntity)

    @Delete
    suspend fun deleteDestination(destination: TravelDestinationEntity)

    @Query("DELETE FROM travel_destinations WHERE id = :id")
    suspend fun deleteDestinationById(id: String)
}

@Dao
interface JourneyDao {
    @Query("SELECT * FROM journeys ORDER BY startedAt DESC")
    fun getAllJourneys(): Flow<List<JourneyEntity>>

    @Query("SELECT * FROM journeys WHERE status = 'ACTIVE' OR status = 'APPROACHING' OR status = 'ALARMING' LIMIT 1")
    fun getActiveJourney(): Flow<JourneyEntity?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertJourney(journey: JourneyEntity)

    @Update
    suspend fun updateJourney(journey: JourneyEntity)

    @Query("DELETE FROM journeys WHERE id = :id")
    suspend fun deleteJourney(id: String)

    @Query("DELETE FROM journeys")
    suspend fun clearHistory()
}

@Dao
interface SavedPlaceDao {
    @Query("SELECT * FROM saved_places ORDER BY name ASC")
    fun getAllPlaces(): Flow<List<SavedPlaceEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPlace(place: SavedPlaceEntity)

    @Delete
    suspend fun deletePlace(place: SavedPlaceEntity)

    @Query("DELETE FROM saved_places WHERE id = :id")
    suspend fun deletePlaceById(id: String)
}

@Dao
interface BelongingDao {
    @Query("SELECT * FROM belonging_items ORDER BY name ASC")
    fun getAllBelongings(): Flow<List<BelongingEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertBelonging(item: BelongingEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(items: List<BelongingEntity>)

    @Update
    suspend fun updateBelonging(item: BelongingEntity)

    @Query("UPDATE belonging_items SET isChecked = :checked")
    suspend fun updateAllChecked(checked: Boolean)

    @Query("DELETE FROM belonging_items WHERE id = :id")
    suspend fun deleteBelonging(id: String)
}

@Database(
    entities = [
        JourneyEntity::class,
        SavedPlaceEntity::class,
        BelongingEntity::class,
        TravelDestinationEntity::class,
        TodoItem::class,
        PackItem::class,
        Reminder::class
    ],
    version = 4,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun travelDestinationDao(): TravelDestinationDao
    abstract fun journeyDao(): JourneyDao
    abstract fun savedPlaceDao(): SavedPlaceDao
    abstract fun belongingDao(): BelongingDao
    abstract fun todoDao(): TodoDao
    abstract fun packItemDao(): PackItemDao
    abstract fun reminderDao(): ReminderDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getInstance(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "travelwake_db"
                )
                    .fallbackToDestructiveMigration()
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
