package com.loitu.yourrefri.feature.settings.ui

import com.loitu.yourrefri.feature.recipe.logic.DietGoal
import com.loitu.yourrefri.feature.recipe.logic.FamilyPreferenceTag

/** Chữ và nhãn rút gọn của màn Cài đặt; hàm thuần để test được. */
internal object SettingsPresentation {
    const val title = "Cài đặt"
    const val reminderTitle = "Nhắc gợi ý món ăn mỗi ngày"
    const val reminderTime = "Ngày thường lúc 17:30"
    const val exportTitle = "Xuất dữ liệu (Backup)"
    const val exportSubtitle = "Lưu file JSON trên thiết bị"
    const val importTitle = "Nhập dữ liệu (Restore)"
    const val importSubtitle = "Phục hồi từ file JSON"
    const val onDeviceLine = "Lưu trữ trên thiết bị 100%, không cần tài khoản"
    const val dietTooStrictNotice = "Chế độ ăn này đang lọc hết món, mình nới ra giúp nhé"
    const val doneLabel = "Xong"
    const val tasteTitle = "Khẩu vị gia đình"
    const val goalTitle = "Ưu tiên gợi ý"
    const val dietTitle = "Chế độ ăn"

    private const val EMPTY_PREFERENCES = "Thích đa dạng"
    private const val MAX_NAMED = 2

    private fun FamilyPreferenceTag.shortLabel(): String = when (this) {
        FamilyPreferenceTag.KID_FAVORITE -> "Dễ ăn"
        FamilyPreferenceTag.SEAFOOD -> "Cá & Tôm"
        FamilyPreferenceTag.VEGGIE_TOFU -> "Rau, Đậu & Nấm"
        FamilyPreferenceTag.MEAT -> "Thịt"
    }

    fun preferencesSummary(selected: Set<FamilyPreferenceTag>): String {
        val ordered = FamilyPreferenceTag.entries.filter { it in selected }
        return when {
            ordered.isEmpty() -> EMPTY_PREFERENCES
            ordered.size <= MAX_NAMED -> ordered.joinToString(", ") { it.shortLabel() }
            else -> "${ordered.size} lựa chọn"
        }
    }

    fun goalSummary(goal: DietGoal): String = when (goal) {
        DietGoal.BALANCED -> "Cân bằng"
        DietGoal.LIGHT -> "Nhiều rau"
        DietGoal.RICH_PROTEIN -> "Đủ đạm"
        DietGoal.QUICK -> "Nấu nhanh"
    }

    fun togglePreference(current: Set<FamilyPreferenceTag>, tag: FamilyPreferenceTag): Set<FamilyPreferenceTag> =
        if (tag in current) current - tag else current + tag

    fun versionLine(versionName: String): String? =
        versionName.trim().takeIf { it.isNotEmpty() }?.let { "Phiên bản $it" }
}
