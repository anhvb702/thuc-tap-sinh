package com.loitu.yourrefri.feature.pantry.data

import kotlinx.coroutines.flow.Flow
import kotlinx.datetime.LocalDate

interface PantryRepository {
    fun observe(): Flow<List<PantryItem>>
    fun observeShelfOverrides(): Flow<Map<String, Int>>
    fun observeQuantities(): Flow<Map<String, Int>>
    fun observeUnits(): Flow<Map<String, String>>
    suspend fun setHave(ingredientId: String, have: Boolean, markedAt: LocalDate? = null)
    suspend fun deleteHave(ingredientId: String)
    suspend fun setShelfDays(ingredientId: String, days: Int?)
    suspend fun setQuantity(ingredientId: String, quantity: Int)
    suspend fun setUnit(ingredientId: String, unit: String?)
}
