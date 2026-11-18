package com.loitu.yourrefri.feature.recipe.ui

internal const val WEEKLY_CONFIRM_LABEL = "Xác nhận thực đơn tuần"

/** Sau khi xác nhận thực đơn: có thực đơn thì mở tab Đi chợ, không có thì ở lại. */
internal fun weeklyConfirmTarget(hasPlan: Boolean): RecipeScreenMode? =
    if (hasPlan) RecipeScreenMode.SHOPPING else null
