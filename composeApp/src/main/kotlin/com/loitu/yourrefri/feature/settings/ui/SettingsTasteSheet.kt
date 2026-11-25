package com.loitu.yourrefri.feature.settings.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.liveRegion
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import com.loitu.yourrefri.feature.recipe.logic.*
import com.loitu.yourrefri.ui.theme.AppIcons
import com.loitu.yourrefri.ui.theme.AppTheme
import kotlinx.coroutines.delay

private val AVOIDED_INGREDIENT_OPTIONS = listOf(
    "tom" to "Tôm", "ca-basa" to "Cá", "thit-bo" to "Thịt bò",
    "dau-phu" to "Đậu phụ", "nam" to "Nấm", "sua-tuoi" to "Sữa",
)

private fun shortDishLabel(style: DishStyle): String =
    style.labelVi.removePrefix("Món ").removeSuffix("/rán").replaceFirstChar { it.uppercaseChar() }

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
internal fun SettingsTasteSheet(
    dietRestriction: DietRestriction, onDietRestrictionChange: (DietRestriction) -> Unit,
    weeklyPreferences: Set<FamilyPreferenceTag>, onWeeklyPreferencesChange: (Set<FamilyPreferenceTag>) -> Unit,
    weeklyGoal: DietGoal, onWeeklyGoalChange: (DietGoal) -> Unit,
    tasteProfile: FamilyTasteProfile, onTasteProfileChange: (FamilyTasteProfile) -> Unit,
    onDismiss: () -> Unit,
) {
    val colors = AppTheme.colors
    val current = TasteHabitSnapshot(dietRestriction, weeklyPreferences, weeklyGoal, tasteProfile)
    var feedback by remember { mutableStateOf("") }
    var savedTick by remember { mutableIntStateOf(0) }
    LaunchedEffect(savedTick) {
        if (savedTick > 0) {
            delay(2000)
            feedback = ""
        }
    }
    val touchTarget = Modifier.heightIn(min = 48.dp)
    fun change(transform: (TasteHabitSnapshot) -> TasteHabitSnapshot, save: (TasteHabitSnapshot) -> Unit) {
        val (_, message) = applyImmediateTasteChange(current, transform, save)
        feedback = message
        savedTick++
    }
    fun changeTaste(transform: (FamilyTasteProfile) -> FamilyTasteProfile) {
        change({ it.copy(taste = transform(it.taste)) }, { onTasteProfileChange(it.taste) })
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
        shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp),
        containerColor = colors.background, contentColor = colors.text,
    ) {
        Column(Modifier.padding(horizontal = 20.dp)) {
            Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                Column(Modifier.weight(1f)) {
                    Text(SettingsPresentation.tasteGroupTitle, style = MaterialTheme.typography.titleLarge)
                    if (feedback.isNotEmpty()) {
                        Text(
                            feedback,
                            modifier = Modifier.semantics { liveRegion = LiveRegionMode.Polite },
                            style = MaterialTheme.typography.bodySmall,
                            color = colors.accentText,
                        )
                    }
                }
                IconButton(onClick = onDismiss, modifier = Modifier.size(48.dp)) {
                    Icon(AppIcons.Close, SettingsPresentation.tasteCloseLabel, tint = colors.muted)
                }
            }
            Column(Modifier.weight(1f, fill = false).verticalScroll(rememberScrollState()), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                TASTE_SHEET_SECTIONS.forEach { section ->
                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        if (section != TasteSheetSection.KIDS) Text(section.title, style = MaterialTheme.typography.titleSmall)
                        FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            when (section) {
                                TasteSheetSection.DIET -> DietRestriction.entries.forEach { option ->
                                    SelectChip(option.labelVi, option == current.diet, onClick = {
                                        change({ it.copy(diet = option) }, { onDietRestrictionChange(it.diet) })
                                    })
                                }
                                TasteSheetSection.FAVORITE_GROUPS -> FamilyPreferenceTag.entries.forEach { tag ->
                                    TasteToggleChip(tag.labelVi, tag in current.preferences) {
                                        change({ it.copy(preferences = SettingsPresentation.togglePreference(it.preferences, tag)) }, { onWeeklyPreferencesChange(it.preferences) })
                                    }
                                }
                                TasteSheetSection.GOAL -> DietGoal.entries.forEach { goal ->
                                    SelectChip(goal.labelVi, goal == current.goal, onClick = {
                                        change({ it.copy(goal = goal) }, { onWeeklyGoalChange(it.goal) })
                                    })
                                }
                                TasteSheetSection.FLAVORS -> TasteFlavor.entries.forEach { flavor ->
                                    TasteToggleChip(flavor.labelVi, flavor in current.taste.flavors) { changeTaste { toggleFlavor(it, flavor) } }
                                }
                                TasteSheetSection.DISH_STYLES -> DishStyle.entries.forEach { style ->
                                    TasteToggleChip(shortDishLabel(style), style in current.taste.dishStyles) { changeTaste { toggleDishStyle(it, style) } }
                                }
                                TasteSheetSection.AVOIDED -> AVOIDED_INGREDIENT_OPTIONS.forEach { (id, label) ->
                                    TasteToggleChip(label, id in current.taste.avoidIngredientIds) { changeTaste { toggleAvoidIngredient(it, id) } }
                                }
                                TasteSheetSection.KIDS -> ChipRow(section.title) {
                                    ToggleChip(current.taste.hasYoungKids, section.title) { enabled ->
                                        changeTaste { it.copy(hasYoungKids = enabled) }
                                    }
                                }
                            }
                        }
                        if (section == TasteSheetSection.DIET) Text(current.diet.hintVi, style = MaterialTheme.typography.bodySmall, color = colors.muted)
                    }
                }
            }
            Row(Modifier.fillMaxWidth().padding(vertical = 8.dp), horizontalArrangement = Arrangement.SpaceBetween) {
                TextButton(modifier = touchTarget, onClick = {
                    feedback = dispatchTasteHabitReset(
                        onDiet = onDietRestrictionChange,
                        onPreferences = onWeeklyPreferencesChange,
                        onGoal = onWeeklyGoalChange,
                        onTaste = onTasteProfileChange,
                    )
                    savedTick++
                }) { Text(SettingsPresentation.tasteResetLabel, color = colors.accentText) }
                Button(onDismiss, touchTarget, shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = colors.primary, contentColor = colors.onPrimary),
                ) { Text(SettingsPresentation.doneLabel) }
            }
        }
    }
}

@Composable
private fun TasteToggleChip(label: String, selected: Boolean, onClick: () -> Unit) {
    val colors = AppTheme.colors
    FilterChip(selected, onClick, { Text(label) }, Modifier.heightIn(min = 48.dp),
        shape = RoundedCornerShape(16.dp),
        colors = FilterChipDefaults.filterChipColors(containerColor = colors.surfaceSoft, labelColor = colors.text,
            selectedContainerColor = colors.primary, selectedLabelColor = colors.onPrimary,
        ),
        border = null,
    )
}
