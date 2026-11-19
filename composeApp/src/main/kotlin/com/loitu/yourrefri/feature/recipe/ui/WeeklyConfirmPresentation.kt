package com.loitu.yourrefri.feature.recipe.ui

internal const val WEEKLY_CONFIRM_LABEL = "Xác nhận thực đơn tuần"

/** Sau khi xác nhận thực đơn, luôn mở tab Đi chợ cuối tuần. */
internal fun confirmTargetMode(plan: List<*>): RecipeScreenMode = RecipeScreenMode.SHOPPING

/** Nút xác nhận: ghi nhận thực đơn rồi mở tab Đi chợ, kể cả khi thực đơn rỗng. */
internal fun handleWeeklyConfirm(
    plan: List<*>,
    onConfirmWeek: () -> Unit,
    onNavigate: (RecipeScreenMode) -> Unit,
) {
    onConfirmWeek()
    onNavigate(confirmTargetMode(plan))
}
