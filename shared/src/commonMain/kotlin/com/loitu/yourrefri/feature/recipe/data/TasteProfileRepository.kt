package com.loitu.yourrefri.feature.recipe.data

import app.cash.sqldelight.coroutines.asFlow
import app.cash.sqldelight.coroutines.mapToList
import com.loitu.yourrefri.feature.recipe.logic.DishStyle
import com.loitu.yourrefri.feature.recipe.logic.FamilyTasteProfile
import com.loitu.yourrefri.feature.recipe.logic.TasteFlavor
import com.loitu.yourrefri.shared.db.YourRefriDatabase
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext

interface TasteProfileRepository {
    fun observe(): Flow<FamilyTasteProfile>
    suspend fun save(profile: FamilyTasteProfile)
}

class SqlTasteProfileRepository(
    private val db: YourRefriDatabase,
    private val ioDispatcher: CoroutineDispatcher,
) : TasteProfileRepository {
    override fun observe(): Flow<FamilyTasteProfile> =
        db.tasteProfileEntityQueries.selectTasteProfile().asFlow().mapToList(ioDispatcher).map { rows ->
            rows.firstOrNull()?.let { row ->
                decode(row.flavors, row.dish_styles, row.avoid_ingredient_ids, row.has_young_kids)
            } ?: FamilyTasteProfile()
        }

    override suspend fun save(profile: FamilyTasteProfile) {
        withContext(ioDispatcher) {
            require(profile.avoidIngredientIds.all { it.isNotBlank() && ',' !in it })
            db.tasteProfileEntityQueries.upsertTasteProfile(
                id = 1L,
                flavors = profile.flavors.joinToString(",") { it.name },
                dish_styles = profile.dishStyles.joinToString(",") { it.name },
                avoid_ingredient_ids = profile.avoidIngredientIds.sorted().joinToString(","),
                has_young_kids = if (profile.hasYoungKids) 1L else 0L,
            )
        }
    }

    private fun decode(
        flavors: String,
        dishStyles: String,
        avoidIngredientIds: String,
        hasYoungKids: Long,
    ): FamilyTasteProfile =
        FamilyTasteProfile(
            flavors = decodeEnums(flavors) { TasteFlavor.entries.firstOrNull { value -> value.name == it } },
            dishStyles = decodeEnums(dishStyles) { DishStyle.entries.firstOrNull { value -> value.name == it } },
            avoidIngredientIds = decodeIds(avoidIngredientIds),
            hasYoungKids = hasYoungKids != 0L,
        )

    private fun decodeIds(value: String): Set<String> =
        if (value.isEmpty()) emptySet() else value.split(',').filter(String::isNotEmpty).toSet()

    private fun <T> decodeEnums(value: String, decode: (String) -> T?): Set<T> =
        if (value.isEmpty()) emptySet() else value.split(',').mapNotNull(decode).toSet()
}
