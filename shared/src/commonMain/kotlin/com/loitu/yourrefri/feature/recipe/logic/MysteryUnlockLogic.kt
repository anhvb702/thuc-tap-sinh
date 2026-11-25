package com.loitu.yourrefri.feature.recipe.logic

import com.loitu.yourrefri.feature.recipe.data.Recipe

/** Số bữa nấu tại nhà để có một lượt quay món bí mật. */
const val COOKS_PER_MYSTERY_UNLOCK = 5

data class MysteryUnlockStatus(
    val totalCooks: Int,
    val cooksPerUnlock: Int,
    val cooksIntoCurrentCycle: Int,
    val cooksNeededForNext: Int,
    val unlockedMysteryCount: Int,
    val remainingLockedCount: Int,
    val availableSpins: Int,
    val allUnlocked: Boolean,
)

fun computeMysteryUnlockStatus(
    totalCookLogs: Int,
    recipes: List<Recipe>,
    unlockedIds: Set<String>,
    cooksPerUnlock: Int = COOKS_PER_MYSTERY_UNLOCK,
): MysteryUnlockStatus {
    require(cooksPerUnlock > 0) { "cooksPerUnlock phải > 0" }
    val mysteryRecipes = recipes.filter { it.isMystery }
    val unlockedMysteryCount = mysteryRecipes.count { it.id in unlockedIds }
    val remainingLockedCount = mysteryRecipes.size - unlockedMysteryCount
    val safeCooks = totalCookLogs.coerceAtLeast(0)
    val earnedSpins = safeCooks / cooksPerUnlock
    val availableSpins = (earnedSpins - unlockedMysteryCount).coerceAtLeast(0).coerceAtMost(remainingLockedCount)
    val allUnlocked = mysteryRecipes.isNotEmpty() && remainingLockedCount == 0
    val consumedCooks = unlockedMysteryCount * cooksPerUnlock
    val cooksIntoCurrentCycle = if (safeCooks < consumedCooks) 0 else safeCooks % cooksPerUnlock
    val cooksNeededForNext = when {
        allUnlocked -> 0
        safeCooks < consumedCooks -> (unlockedMysteryCount + 1) * cooksPerUnlock - safeCooks
        else -> cooksPerUnlock - cooksIntoCurrentCycle
    }
    return MysteryUnlockStatus(
        totalCooks = safeCooks,
        cooksPerUnlock = cooksPerUnlock,
        cooksIntoCurrentCycle = cooksIntoCurrentCycle,
        cooksNeededForNext = cooksNeededForNext,
        unlockedMysteryCount = unlockedMysteryCount,
        remainingLockedCount = remainingLockedCount,
        availableSpins = availableSpins,
        allUnlocked = allUnlocked,
    )
}

/** Món được đưa vào gợi ý/thực đơn: món thường, hoặc món bí mật đã mở. */
fun availableRecipesForPlanning(recipes: List<Recipe>, unlockedIds: Set<String>): List<Recipe> =
    recipes.filter { !it.isMystery || it.id in unlockedIds }

fun lockedMysteryRecipes(recipes: List<Recipe>, unlockedIds: Set<String>): List<Recipe> =
    recipes.filter { it.isMystery && it.id !in unlockedIds }

/** Chọn một món bí mật chưa mở; `randomIndex(n)` trả về chỉ số trong [0, n). */
fun pickMysteryRecipeToUnlock(
    recipes: List<Recipe>,
    unlockedIds: Set<String>,
    randomIndex: (Int) -> Int,
): Recipe? {
    val locked = lockedMysteryRecipes(recipes, unlockedIds)
    if (locked.isEmpty()) return null
    return locked[randomIndex(locked.size).coerceIn(0, locked.lastIndex)]
}
