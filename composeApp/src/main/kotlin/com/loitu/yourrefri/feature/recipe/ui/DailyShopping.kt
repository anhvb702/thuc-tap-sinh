package com.loitu.yourrefri.feature.recipe.ui

import android.content.Context
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.unit.dp
import com.loitu.yourrefri.feature.pantry.data.PantryCompartment
import com.loitu.yourrefri.feature.pantry.logic.PantryGridState
import com.loitu.yourrefri.feature.pantry.logic.cellsOf
import com.loitu.yourrefri.feature.pantry.logic.customFoodId
import com.loitu.yourrefri.feature.recipe.data.Recipe
import com.loitu.yourrefri.feature.recipe.data.Slot
import org.json.JSONArray
import org.json.JSONObject

data class DailyShoppingItem(val id: String, val name: String, val checked: Boolean = true, val compartment: PantryCompartment = PantryCompartment.CHILL)

internal fun mergeShoppingItems(existing: List<DailyShoppingItem>, additions: List<DailyShoppingItem>): List<DailyShoppingItem> =
    (existing + additions).distinctBy { it.id }

internal fun shoppingChecklist(items: List<DailyShoppingItem>): String =
    PantryCompartment.entries.mapNotNull { compartment ->
        val names = items.filter { it.checked && it.compartment == compartment }.map { "☐ ${it.name}" }
        if (names.isEmpty()) null else (listOf(if (compartment == PantryCompartment.CHILL) "Ngăn mát" else "Ngăn đông") + names).joinToString("\n")
    }.joinToString("\n\n")

internal fun editLiteRecipe(recipe: Recipe, title: String, ids: List<String>): Recipe =
    recipe.copy(title = title.trim(), required = ids.distinct().map { Slot(listOf(it)) })

object DailyShoppingStore {
    private const val FILE = "daily_shopping"
    private const val KEY = "items"

    fun read(context: Context): List<DailyShoppingItem> = runCatching {
        val array = JSONArray(context.getSharedPreferences(FILE, Context.MODE_PRIVATE).getString(KEY, "[]"))
        (0 until array.length()).map { index ->
            val row = array.getJSONObject(index)
            DailyShoppingItem(row.getString("id"), row.getString("name"), row.optBoolean("checked", true),
                runCatching { PantryCompartment.valueOf(row.optString("compartment", "CHILL")) }.getOrDefault(PantryCompartment.CHILL))
        }.distinctBy { it.id }
    }.getOrDefault(emptyList())

    fun write(context: Context, items: List<DailyShoppingItem>) {
        val rows = JSONArray()
        items.forEach { item -> rows.put(JSONObject().put("id", item.id).put("name", item.name).put("checked", item.checked).put("compartment", item.compartment.name)) }
        context.getSharedPreferences(FILE, Context.MODE_PRIVATE).edit().putString(KEY, rows.toString()).apply()
    }

    fun addIds(context: Context, ids: List<String>, names: Map<String, String>) {
        write(context, mergeShoppingItems(read(context), ids.mapNotNull { id -> names[id]?.let { DailyShoppingItem(id, it) } }))
    }
}

@Composable
internal fun DailyShoppingSection(state: PantryGridState, onStock: ((List<DailyShoppingItem>) -> Unit)?) {
    val context = LocalContext.current
    val clipboard = LocalClipboardManager.current
    var items by remember { mutableStateOf(DailyShoppingStore.read(context)) }
    var customName by remember { mutableStateOf("") }
    var showCatalog by remember { mutableStateOf(false) }
    fun save(next: List<DailyShoppingItem>) { items = next; DailyShoppingStore.write(context, next) }
    val selected = items.filter { it.checked }
    Text("Đi chợ hằng ngày", style = MaterialTheme.typography.titleLarge)
    if (items.isEmpty()) Text("Mình thêm vài món cần mua nhé.")
    items.forEach { item ->
        Column(Modifier.fillMaxWidth().padding(vertical = 4.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Checkbox(checked = item.checked, onCheckedChange = { checked -> save(items.map { if (it.id == item.id) it.copy(checked = checked) else it }) })
                Text(item.name, Modifier.weight(1f))
                TextButton(onClick = { save(items.filterNot { it.id == item.id }) }) { Text("Xóa") }
            }
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                PantryCompartment.entries.forEach { compartment ->
                    FilterChip(selected = item.compartment == compartment, onClick = { save(items.map { if (it.id == item.id) it.copy(compartment = compartment) else it }) }, label = { Text(if (compartment == PantryCompartment.CHILL) "Ngăn mát" else "Ngăn đông") })
                }
            }
        }
    }
    TextButton(onClick = { showCatalog = !showCatalog }, modifier = Modifier.heightIn(min = 48.dp)) { Text(if (showCatalog) "Ẩn danh sách nguyên liệu" else "Thêm từ danh sách nguyên liệu") }
    if (showCatalog) {
        val soon = cellsOf(state).filter { it.have && it.daysLeft != null && it.daysLeft in 0..2 }.map { it.ingredientId }.toSet()
        state.ingredients.sortedByDescending { it.id in soon }.forEach { ingredient ->
            Row(Modifier.fillMaxWidth().heightIn(min = 48.dp), verticalAlignment = Alignment.CenterVertically) {
                Text(ingredient.nameVi + if (ingredient.id in soon) " · nên dùng trước" else "", Modifier.weight(1f))
                TextButton(onClick = { save(mergeShoppingItems(items, listOf(DailyShoppingItem(ingredient.id, ingredient.nameVi)))) }, enabled = items.none { it.id == ingredient.id }) { Text("Thêm") }
            }
        }
    }
    OutlinedTextField(value = customName, onValueChange = { customName = it }, label = { Text("Khác") }, modifier = Modifier.fillMaxWidth(), singleLine = true)
    TextButton(onClick = {
        customFoodId(customName)?.let { id -> save(mergeShoppingItems(items, listOf(DailyShoppingItem(id, customName.trim())))); customName = "" }
    }, enabled = customFoodId(customName) != null, modifier = Modifier.heightIn(min = 48.dp)) { Text("Thêm món khác") }
    TextButton(onClick = {
        val text = shoppingChecklist(items)
        clipboard.setText(AnnotatedString(text))
        shareToZaloOrChooser(context, text)
    }, enabled = selected.isNotEmpty(), modifier = Modifier.heightIn(min = 48.dp)) { Text("Gửi qua Zalo") }
    Button(onClick = { onStock?.invoke(selected); save(items.filterNot { it.checked }) }, enabled = onStock != null && selected.isNotEmpty(), modifier = Modifier.fillMaxWidth().heightIn(min = 48.dp)) { Text("Đã mua xong · Cất vào tủ lạnh") }
}
