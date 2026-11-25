package com.loitu.yourrefri.feature.settings.ui

import com.loitu.yourrefri.feature.recipe.logic.DietGoal
import com.loitu.yourrefri.feature.recipe.logic.DietRestriction
import com.loitu.yourrefri.feature.recipe.logic.FamilyPreferenceTag
import com.loitu.yourrefri.feature.recipe.logic.FamilyTasteProfile

/** Chữ và nhãn rút gọn của màn Cài đặt; hàm thuần để test được. */
internal object SettingsPresentation {
    const val title = "Cài đặt"
    const val reminderTitle = "Nhắc gợi ý món ăn mỗi ngày"
    const val reminderTime = "Ngày thường lúc 16:00"
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
    const val reminderGroupTitle = "Nhắc nhở"
    const val tasteGroupTitle = "Khẩu vị & thói quen"
    const val tasteSavedFeedback = "Đã lưu"
    const val tasteResetLabel = "Đặt lại"
    const val favoriteGroupTitle = "Nhóm món ưa thích"
    const val tasteCloseLabel = "Đóng"
    const val backupGroupTitle = "Sao lưu & khôi phục"
    const val transferTitle = "Chuyển sang điện thoại mới"
    const val onDeviceBadge = "Chỉ trên máy bạn"
    const val bottomPaddingDp = 148

    val themeModeOrder: List<com.loitu.yourrefri.ui.theme.ThemeMode> = listOf(
        com.loitu.yourrefri.ui.theme.ThemeMode.LIGHT,
        com.loitu.yourrefri.ui.theme.ThemeMode.DARK,
        com.loitu.yourrefri.ui.theme.ThemeMode.SYSTEM,
    )

    fun toggleChipLabel(enabled: Boolean): String = if (enabled) "Bật" else "Tắt"

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

internal enum class TasteSheetSection(val title: String) {
    DIET(SettingsPresentation.dietTitle),
    FAVORITE_GROUPS(SettingsPresentation.favoriteGroupTitle),
    GOAL(SettingsPresentation.goalTitle),
    FLAVORS("Vị thích"),
    DISH_STYLES("Món thích"),
    AVOIDED("Không ăn"),
    KIDS("Nhà có trẻ nhỏ"),
}

internal val TASTE_SHEET_SECTIONS = TasteSheetSection.entries

internal data class TasteHabitSnapshot(
    val diet: DietRestriction = DietRestriction.NONE,
    val preferences: Set<FamilyPreferenceTag> = emptySet(),
    val goal: DietGoal = DietGoal.BALANCED,
    val taste: FamilyTasteProfile = FamilyTasteProfile(),
)

internal fun resetTasteHabitDefaults(): TasteHabitSnapshot = TasteHabitSnapshot()

internal fun dispatchTasteHabitReset(
    onDiet: (DietRestriction) -> Unit,
    onPreferences: (Set<FamilyPreferenceTag>) -> Unit,
    onGoal: (DietGoal) -> Unit,
    onTaste: (FamilyTasteProfile) -> Unit,
): String {
    val defaults = resetTasteHabitDefaults()
    onDiet(defaults.diet)
    onPreferences(defaults.preferences)
    onGoal(defaults.goal)
    onTaste(defaults.taste)
    return SettingsPresentation.tasteSavedFeedback
}

internal fun settingsTasteHabitSummary(
    diet: DietRestriction, preferences: Set<FamilyPreferenceTag>, goal: DietGoal, taste: FamilyTasteProfile,
): String = buildList {
    if (diet != DietRestriction.NONE) add(diet.labelVi)
    if (preferences.isNotEmpty()) add(SettingsPresentation.preferencesSummary(preferences))
    if (goal != DietGoal.BALANCED) add(SettingsPresentation.goalSummary(goal))
    if (taste != FamilyTasteProfile()) add(tasteProfileSummaryVi(taste))
}.joinToString(" · ").ifEmpty { "Ăn đa dạng · Cân bằng" }

internal fun applyImmediateTasteChange(
    current: TasteHabitSnapshot,
    transform: (TasteHabitSnapshot) -> TasteHabitSnapshot,
    onSave: (TasteHabitSnapshot) -> Unit,
): Pair<TasteHabitSnapshot, String> {
    val next = transform(current)
    onSave(next)
    return next to SettingsPresentation.tasteSavedFeedback
}
