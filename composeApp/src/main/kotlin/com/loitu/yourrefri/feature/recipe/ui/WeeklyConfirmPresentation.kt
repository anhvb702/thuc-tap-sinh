package com.loitu.yourrefri.feature.recipe.ui

internal const val WEEKLY_CONFIRM_LABEL = "Xác nhận thực đơn tuần"

/** Sau khi xác nhận thực đơn: có thực đơn thì mở tab Đi chợ, không có thì ở lại. */
internal fun weeklyConfirmTarget(hasPlan: Boolean): RecipeScreenMode? =
    if (hasPlan) RecipeScreenMode.SHOPPING else null

/** Nút xác nhận: luôn gọi onConfirm; chỉ chuyển tab khi thực đơn không rỗng. */
internal fun handleWeeklyConfirm(
    plan: List<*>,
    onConfirm: () -> Unit,
    onNavigate: (RecipeScreenMode) -> Unit,
) {
    onConfirm()
    weeklyConfirmTarget(plan.isNotEmpty())?.let(onNavigate)
}
