package com.loitu.yourrefri.feature.home.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.loitu.yourrefri.feature.pantry.logic.PantryCell
import com.loitu.yourrefri.feature.pantry.logic.PantryGridState
import com.loitu.yourrefri.feature.pantry.logic.cellsOf
import com.loitu.yourrefri.feature.pantry.logic.statusText
import com.loitu.yourrefri.feature.home.logic.homeGreeting
import com.loitu.yourrefri.feature.home.logic.pantryPreviewNames
import com.loitu.yourrefri.feature.recipe.data.Recipe
import com.loitu.yourrefri.feature.recipe.logic.RecommendationReason
import com.loitu.yourrefri.feature.recipe.logic.recommendRecipes
import com.loitu.yourrefri.feature.recipe.ui.recommendationColor
import com.loitu.yourrefri.feature.recipe.ui.recommendationLabel
import com.loitu.yourrefri.feature.pantry.ui.foodEmojiFor
import com.loitu.yourrefri.ui.theme.DeepCharcoal
import com.loitu.yourrefri.ui.theme.MutedGray
import com.loitu.yourrefri.ui.theme.SageGreen
import com.loitu.yourrefri.ui.theme.SageGreenStrong
import com.loitu.yourrefri.ui.theme.SageGreenText
import com.loitu.yourrefri.ui.theme.SoftGreen
import com.loitu.yourrefri.ui.theme.SoftGreenLight
import com.loitu.yourrefri.ui.theme.Terracotta
import com.loitu.yourrefri.ui.theme.TerracottaText
import com.loitu.yourrefri.ui.theme.WarmBeige
import com.loitu.yourrefri.ui.theme.WarmIvory

// Nút hành động chính: nền xanh sage đậm, chữ trắng (WCAG AA >= 4.5).
internal data class ActionStyle(val container: Color, val content: Color)

internal val weeklyPlanActionStyle = ActionStyle(container = SageGreenStrong, content = Color.White)

@Composable
fun HomeScreen(
    state: PantryGridState,
    recipes: List<Recipe>,
    onNavigateToPantry: () -> Unit,
    onNavigateToRecipes: () -> Unit,
    onNavigateToRecipe: (String) -> Unit,
    onNavigateToWeeklyRecipes: () -> Unit = onNavigateToRecipes,
    modifier: Modifier = Modifier,
) {
    val cells = cellsOf(state)
    val haveItems = cells.filter { it.have }
    val pantryPreview = pantryPreviewNames(state)
    val urgentItems = haveItems.filter { it.daysLeft in 0..2 }
    val recommendations = recommendRecipes(recipes, state)

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(WarmIvory)
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
    ) {
        // Lời chào theo giờ địa phương được tiêm vào state.
        Text(
            text = homeGreeting(state.hourOfDay),
            style = MaterialTheme.typography.bodyLarge,
            color = MutedGray,
        )
        Button(
            onClick = onNavigateToWeeklyRecipes,
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.buttonColors(
                containerColor = weeklyPlanActionStyle.container,
                contentColor = weeklyPlanActionStyle.content,
            ),
        ) {
            Text("Lập thực đơn tuần & đi chợ 📅", color = weeklyPlanActionStyle.content)
        }
        Text(
            text = "Hôm nay nhà mình thế nào?",
            style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold),
            color = DeepCharcoal,
            modifier = Modifier.padding(top = 2.dp, bottom = 16.dp)
        )

        // Card Tổng quan: Trong tủ đang có bao nhiêu món
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(20.dp))
                .border(1.dp, SoftGreen, RoundedCornerShape(20.dp)),
            colors = CardDefaults.cardColors(containerColor = SoftGreenLight),
            shape = RoundedCornerShape(20.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Tủ lạnh hiện có",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MutedGray
                    )
                    Text(
                        text = "${haveItems.size} nguyên liệu sẵn sàng",
                        style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold),
                        color = DeepCharcoal,
                        modifier = Modifier.padding(top = 2.dp)
                    )
                    Text(
                        text = pantryPreview.takeIf { it.isNotEmpty() }?.joinToString(separator = " · ")
                            ?: "Chưa có món nào trong tủ.",
                        style = MaterialTheme.typography.bodySmall,
                        color = SageGreenText,
                        modifier = Modifier.padding(top = 4.dp),
                    )
                }
                Button(
                    onClick = onNavigateToPantry,
                    colors = ButtonDefaults.buttonColors(containerColor = SageGreenStrong),
                    shape = RoundedCornerShape(12.dp),
                    contentPadding = PaddingValues(horizontal = 14.dp, vertical = 8.dp)
                ) {
                    Text("Xem tủ 🧊", color = Color.White, fontWeight = FontWeight.Medium)
                }
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Mục: Cần ưu tiên sử dụng sớm (0 - 2 ngày)
        Text(
            text = "Cần ưu tiên dùng sớm ⏳",
            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
            color = DeepCharcoal,
            modifier = Modifier.padding(bottom = 8.dp)
        )

        if (urgentItems.isEmpty()) {
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp),
                color = Color.White,
                border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFE5E7EB))
            ) {
                Text(
                    text = "Chưa có món nào cần ưu tiên dùng sớm.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = SageGreenText,
                    modifier = Modifier.padding(14.dp)
                )
            }
        } else {
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(urgentItems, key = { it.ingredientId }) { item ->
                    Card(
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        shape = RoundedCornerShape(14.dp),
                        modifier = Modifier.border(1.dp, Terracotta.copy(alpha = 0.5f), RoundedCornerShape(14.dp))
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(text = foodEmojiFor(item.ingredientId), fontSize = 24.sp)
                            Spacer(modifier = Modifier.width(8.dp))
                            Column {
                                Text(
                                    text = item.nameVi,
                                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
                                    color = DeepCharcoal
                                )
                                Text(
                                    text = statusText(item.daysLeft ?: 0),
                                    style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold),
                                    color = TerracottaText
                                )
                            }
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Mục: Gợi ý 3 món ăn hôm nay
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Gợi ý bữa tối hôm nay 🍲",
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                color = DeepCharcoal
            )
            Text(
                text = "Xem công thức →",
                style = MaterialTheme.typography.bodySmall.copy(color = SageGreenText, fontWeight = FontWeight.Bold),
                modifier = Modifier.clickable(onClick = onNavigateToRecipes)
            )
        }

        Spacer(modifier = Modifier.height(10.dp))

        if (recommendations.isEmpty()) {
            Text(
                "Chưa có công thức để gợi ý.",
                style = MaterialTheme.typography.bodyMedium,
                color = MutedGray,
            )
        } else {
            recommendations.forEach { recommendation ->
                HomeRecipeCard(
                    title = recommendation.recipe.title,
                    tag = recommendationLabel(recommendation.reason),
                    tagColor = recommendationColor(recommendation.reason),
                    time = recommendation.recipe.minutes?.let { "$it phút" } ?: "Thời gian chưa rõ",
                    onClick = { onNavigateToRecipe(recommendation.recipe.id) },
                )
                Spacer(modifier = Modifier.height(10.dp))
            }
        }
    }
}

@Composable
private fun HomeRecipeCard(
    title: String,
    tag: String,
    tagColor: Color,
    time: String,
    onClick: () -> Unit,
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .clickable(onClick = onClick)
            .border(1.dp, Color(0xFFE5E7EB), RoundedCornerShape(16.dp)),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    shape = RoundedCornerShape(6.dp),
                    color = tagColor.copy(alpha = 0.15f)
                ) {
                    Text(
                        text = tag,
                        style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold, fontSize = 14.sp),
                        color = when (tagColor) {
                            Terracotta -> TerracottaText
                            SageGreen -> SageGreenText
                            else -> DeepCharcoal
                        },
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                    )
                }
                Text(
                    text = "⏱️ $time",
                    style = MaterialTheme.typography.bodySmall,
                    color = MutedGray
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                color = DeepCharcoal
            )
        }
    }
}

private fun recommendationColor(reason: RecommendationReason): Color = when (reason) {
    RecommendationReason.USE_SOON -> Terracotta
    RecommendationReason.READY -> SageGreen
    RecommendationReason.QUICK -> WarmBeige
    RecommendationReason.OTHER -> SoftGreen
}
