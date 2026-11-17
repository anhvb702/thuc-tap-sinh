package com.loitu.yourrefri.feature.backup.data

import com.loitu.yourrefri.feature.pantry.data.PANTRY_UNITS
import com.loitu.yourrefri.feature.pantry.data.PantryCompartment
import com.loitu.yourrefri.feature.recipe.data.MealSlot
import kotlinx.datetime.LocalDate
import kotlinx.serialization.SerializationException
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

object BackupCodec {
    const val FORMAT = "yourrefri-backup"
    const val LEGACY_VERSION = 1
    const val VERSION = 2
    private const val CUSTOM_RECIPE_PREFIX = "custom-"
    private const val MAX_CUSTOM_RECIPE_STEPS = 8

    private val json = Json {
        ignoreUnknownKeys = false
        encodeDefaults = true
    }

    fun encode(snapshot: PantryBackupSnapshot): String {
        validate(snapshot)
        return try {
            json.encodeToString(snapshot)
        } catch (error: SerializationException) {
            throw IllegalArgumentException("Invalid backup snapshot", error)
        }
    }

    fun decode(json: String): PantryBackupSnapshot {
        val snapshot = try {
            this.json.decodeFromString<PantryBackupSnapshot>(json)
        } catch (error: Exception) {
            if (error is IllegalArgumentException) throw error
            throw IllegalArgumentException("Invalid backup JSON", error)
        }
        validate(snapshot)
        return snapshot
    }

    private fun validate(snapshot: PantryBackupSnapshot) {
        require(snapshot.format == FORMAT) { "Unsupported backup format" }
        require(snapshot.version == LEGACY_VERSION || snapshot.version == VERSION) { "Unsupported backup version" }
        if (snapshot.version == LEGACY_VERSION) {
            require(snapshot.weeklyMeals.isEmpty()) { "Version 1 cannot contain weekly meals" }
            require(snapshot.mealToggles.isEmpty()) { "Version 1 cannot contain meal toggles" }
            require(snapshot.weeklySettings == null) { "Version 1 cannot contain weekly settings" }
            require(snapshot.favorites.isEmpty()) { "Version 1 cannot contain favorites" }
            require(snapshot.cookMeals.isEmpty()) { "Version 1 cannot contain cook meals" }
            require(snapshot.compartments.isEmpty()) { "Version 1 cannot contain compartments" }
            require(snapshot.customRecipes.isEmpty()) { "Version 1 cannot contain custom recipes" }
            require(snapshot.leftovers.isEmpty()) { "Version 1 cannot contain leftovers" }
            require(snapshot.extraShoppingItems.isEmpty()) { "Version 1 cannot contain extra shopping items" }
        }

        validateUniqueIds("pantry", snapshot.pantry.map { it.ingredientId })
        snapshot.pantry.forEach { LocalDate.parse(it.markedAt) }

        validateUniqueIds("shelfOverrides", snapshot.shelfOverrides.map { it.ingredientId })
        snapshot.shelfOverrides.forEach { require(it.shelfDays >= 0) { "shelfDays must be non-negative" } }

        validateUniqueIds("quantities", snapshot.quantities.map { it.ingredientId })
        snapshot.quantities.forEach { require(it.quantity > 1) { "quantity must be greater than one" } }

        snapshot.units.forEach { require(it.ingredientId.isNotBlank()) { "units id must not be blank" } }
        validateUniqueIds("units", snapshot.units.map { it.ingredientId })
        snapshot.units.forEach { require(it.unit in PANTRY_UNITS) { "Unsupported pantry unit" } }

        validateUniqueIds("cookLogs", snapshot.cookLogs.map { it.id.toString() })
        snapshot.cookLogs.forEach { log ->
            require(log.id > 0) { "Cook log id must be positive" }
            require(log.recipeId.isNotEmpty()) { "recipeId must not be empty" }
            LocalDate.parse(log.cookedOn)
            require(log.savedIngredientIds.all { it.isNotEmpty() && ',' !in it }) {
                "Saved ingredient ids must be non-empty and contain no commas"
            }
        }

        val cookLogIds = snapshot.cookLogs.mapTo(HashSet()) { it.id }
        validateUniqueIds("cookMeals", snapshot.cookMeals.map { it.cookLogId.toString() })
        snapshot.cookMeals.forEach { meal ->
            require(meal.cookLogId in cookLogIds) { "Cook meal must belong to a cook log" }
            require(meal.meal in MealSlot.entries.map { it.name }) { "Unsupported meal slot" }
        }

        validateUniqueIds("favorites", snapshot.favorites.map { it.recipeId })
        snapshot.favorites.forEach {
            require(it.recipeId.isNotBlank()) { "Favorite recipe id must not be blank" }
            require(it.createdAtEpochMs >= 0) { "Favorite createdAt must be non-negative" }
        }

        val toggleKeys = HashSet<Pair<String, String>>()
        snapshot.mealToggles.forEach { toggle ->
            LocalDate.parse(toggle.day)
            require(toggle.mealSlot in MealSlot.entries.map { it.name }) { "Unsupported meal slot" }
            require(toggleKeys.add(toggle.day to toggle.mealSlot)) { "Meal toggle day and slot must be unique" }
        }

        val weeklyKeys = HashSet<Pair<String, String>>()
        snapshot.weeklyMeals.forEach { meal ->
            LocalDate.parse(meal.day)
            require(meal.meal in MealSlot.entries.map { it.name }) { "Unsupported meal slot" }
            require(meal.recipeId.isNotBlank()) { "Weekly recipe id must not be blank" }
            require(meal.portions >= 1) { "Weekly portions must be positive" }
            require(weeklyKeys.add(meal.day to meal.meal)) { "Weekly day and meal must be unique" }
        }

        snapshot.weeklySettings?.let { settings ->
            LocalDate.parse(settings.weekStart)
            require(settings.goalId.isNotBlank()) { "Weekly goal id must not be blank" }
            validateUniqueIds("weekly preferences", settings.preferenceIds)
            validateUniqueIds("weekly unchecked ids", settings.uncheckedIds)
            val swapDays = HashSet<Int>()
            settings.swappedOffsets.forEach { swap ->
                require(swap.dayIndex in 0..6) { "Weekly swap day must be between 0 and 6" }
                require(swap.offset >= 0) { "Weekly swap offset must be non-negative" }
                require(swapDays.add(swap.dayIndex)) { "Weekly swap day must be unique" }
            }
        }

        val compartmentNames = PantryCompartment.entries.map { it.name }
        validateUniqueIds("compartments", snapshot.compartments.map { it.ingredientId })
        snapshot.compartments.forEach {
            require(it.ingredientId.isNotBlank()) { "Compartment ingredientId must not be blank" }
            require(it.compartment in compartmentNames) { "Unsupported pantry compartment" }
        }

        validateUniqueIds("customRecipes", snapshot.customRecipes.map { it.recipeId })
        snapshot.customRecipes.forEach { recipe ->
            require(recipe.recipeId.isNotBlank()) { "Custom recipe id must not be blank" }
            require(recipe.recipeId.startsWith(CUSTOM_RECIPE_PREFIX)) {
                "Custom recipe id must use the $CUSTOM_RECIPE_PREFIX prefix"
            }
            require(recipe.title.isNotBlank()) { "Custom recipe title must not be blank" }
            // Cùng ràng buộc với Recipe: tối đa 8 bước, không bước rỗng.
            require(recipe.steps.size <= MAX_CUSTOM_RECIPE_STEPS) { "Custom recipe must have at most 8 steps" }
            require(recipe.steps.all(String::isNotBlank)) { "Custom recipe steps must not be blank" }
            require(recipe.required.isNotEmpty()) { "Custom recipe must have at least one required slot" }
            require(recipe.required.all { slot -> slot.isNotEmpty() && slot.all(String::isNotBlank) }) {
                "Custom recipe required slots must be non-empty"
            }
            require(recipe.optional.all { slot -> slot.isNotEmpty() && slot.all(String::isNotBlank) }) {
                "Custom recipe optional slots must be non-empty"
            }
            require(recipe.minutes == null || recipe.minutes in 1..1440) {
                "Custom recipe minutes must be between 1 and 1440"
            }
        }

        validateUniqueIds("leftovers", snapshot.leftovers.map { it.id })
        snapshot.leftovers.forEach { leftover ->
            require(leftover.id.isNotBlank()) { "Leftover id must not be blank" }
            require(leftover.title.isNotBlank()) { "Leftover title must not be blank" }
            require(leftover.compartment in compartmentNames) { "Unsupported pantry compartment" }
            require(leftover.shelfDays >= 0) { "Leftover shelfDays must be non-negative" }
            requireEpochDay(leftover.markedAtEpochDays, "Leftover markedAtEpochDays")
        }

        validateUniqueIds("extraShoppingItems", snapshot.extraShoppingItems.map { it.ingredientId })
        snapshot.extraShoppingItems.forEach {
            require(it.ingredientId.isNotBlank()) { "Extra shopping ingredientId must not be blank" }
            requireEpochDay(it.addedAtEpochDays, "Extra shopping addedAtEpochDays")
        }
    }

    // Repository đọc lại bằng toInt() rồi LocalDate.fromEpochDays, nên giá trị phải vừa Int và là ngày hợp lệ.
    private fun requireEpochDay(value: Long, name: String) {
        require(value in Int.MIN_VALUE.toLong()..Int.MAX_VALUE.toLong()) { "$name is out of range" }
        try {
            LocalDate.fromEpochDays(value.toInt())
        } catch (error: Exception) {
            throw IllegalArgumentException("$name is not a valid date", error)
        }
    }

    private fun validateUniqueIds(table: String, ids: List<String>) {
        val seen = HashSet<String>()
        ids.forEach { id ->
            require(id.isNotEmpty()) { "$table id must not be empty" }
            require(seen.add(id)) { "$table id must be unique" }
        }
    }
}
