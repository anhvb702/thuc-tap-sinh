package com.loitu.yourrefri.feature.pantry.data

import kotlinx.coroutines.flow.Flow
import kotlinx.datetime.LocalDate

interface PantryRepository {
    fun observe(): Flow<List<PantryItem>>
    fun observeShelfOverrides(): Flow<Map<String, Int>>
    suspend fun setHave(ingredientId: String, have: Boolean, markedAt: LocalDate? = null)
    suspend fun setShelfDays(ingredientId: String, days: Int?)
}
