package com.example.travelwake.data.repository

import com.example.travelwake.data.local.BelongingDao
import com.example.travelwake.data.local.TravelDestinationDao
import com.example.travelwake.data.model.BelongingEntity
import com.example.travelwake.data.model.TravelDestinationEntity
import kotlinx.coroutines.flow.Flow

class TravelDestinationRepository(private val dao: TravelDestinationDao) {
    val allDestinations: Flow<List<TravelDestinationEntity>> = dao.getAllDestinations()

    fun getDestinationById(id: String): Flow<TravelDestinationEntity?> = dao.getDestinationById(id)

    suspend fun insertDestination(destination: TravelDestinationEntity) {
        dao.insertDestination(destination)
    }

    suspend fun updateDestination(destination: TravelDestinationEntity) {
        dao.updateDestination(destination)
    }

    suspend fun deleteDestination(destination: TravelDestinationEntity) {
        dao.deleteDestination(destination)
    }

    suspend fun deleteDestinationById(id: String) {
        dao.deleteDestinationById(id)
    }
}

class BelongingsRepository(private val dao: BelongingDao) {
    val allBelongings: Flow<List<BelongingEntity>> = dao.getAllBelongings()

    suspend fun insertBelonging(item: BelongingEntity) {
        dao.insertBelonging(item)
    }

    suspend fun insertAll(items: List<BelongingEntity>) {
        dao.insertAll(items)
    }

    suspend fun updateBelonging(item: BelongingEntity) {
        dao.updateBelonging(item)
    }

    suspend fun toggleBelonging(item: BelongingEntity) {
        dao.updateBelonging(item.copy(isChecked = !item.isChecked))
    }

    suspend fun setAllChecked(checked: Boolean) {
        dao.updateAllChecked(checked)
    }

    suspend fun deleteBelonging(id: String) {
        dao.deleteBelonging(id)
    }
}
