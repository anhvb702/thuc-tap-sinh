package com.loitu.yourrefri.feature.recipe.ui

import com.loitu.yourrefri.feature.recipe.logic.MysteryUnlockStatus

internal const val REWARD_BANNER_TAGLINE = "Chăm nấu tại nhà, mở thêm món mới"

internal fun shouldShowRewardFab(status: MysteryUnlockStatus?): Boolean =
    status != null && !status.allUnlocked

internal fun isRewardFabReady(status: MysteryUnlockStatus?): Boolean =
    status != null && !status.allUnlocked && status.availableSpins > 0

internal fun shouldShowRewardRedDot(status: MysteryUnlockStatus?): Boolean = isRewardFabReady(status)

internal fun shouldSpinRewardWheel(status: MysteryUnlockStatus?): Boolean = isRewardFabReady(status)

internal fun rewardFabProgressLabel(status: MysteryUnlockStatus): String {
    val goal = status.cooksPerUnlock
    if (goal <= 0) return "0/0"
    val cooks = if (isRewardFabReady(status)) goal else status.cooksIntoCurrentCycle.coerceIn(0, goal)
    return "$cooks/$goal"
}

internal fun rewardFabProgressFraction(status: MysteryUnlockStatus): Float = when {
    status.cooksPerUnlock <= 0 -> 0f
    status.availableSpins > 0 -> 1f
    else -> (status.cooksIntoCurrentCycle.toFloat() / status.cooksPerUnlock).coerceIn(0f, 1f)
}

internal fun rewardFabContentDescription(status: MysteryUnlockStatus): String =
    if (isRewardFabReady(status)) {
        "Mở vòng quay món mới, đang có ${status.availableSpins} lượt quay"
    } else {
        "Tiến trình quà nấu tại nhà ${rewardFabProgressLabel(status)}, chạm để xem chi tiết"
    }

internal fun rewardProgressHintMessage(status: MysteryUnlockStatus): String =
    "$REWARD_BANNER_TAGLINE. Nhà mình đã nấu ${rewardFabProgressLabel(status)} bữa. " +
        "Thêm ${status.cooksNeededForNext} bữa nữa là có một lượt quay nhé."
