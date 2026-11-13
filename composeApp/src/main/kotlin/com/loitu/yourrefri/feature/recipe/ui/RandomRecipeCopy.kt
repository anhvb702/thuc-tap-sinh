package com.loitu.yourrefri.feature.recipe.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.loitu.yourrefri.feature.pantry.data.Ingredient
import com.loitu.yourrefri.feature.recipe.data.Recipe
import com.loitu.yourrefri.feature.recipe.logic.DietRestriction
import com.loitu.yourrefri.feature.recipe.logic.RandomPick
import com.loitu.yourrefri.feature.recipe.logic.filterRecipesByDiet
import com.loitu.yourrefri.ui.theme.AppTheme

internal object RandomRecipeCopy {
    const val button = "Nấu ngẫu nhiên"
    const val needMore = "Món này cần thêm vài thứ nữa nè"
    const val outsideDiet = "Món này ngoài chế độ ăn của bạn"
    const val tryAnother = "Thử món khác"
    const val noOther = "Chưa có món khác để thử"
    const val back = "Quay lại"
    const val open = "Xem món này"
}

/** Nút "Nấu ngẫu nhiên" bỏ qua chế độ ăn; hàm này chỉ để báo nhẹ khi món rút ra nằm ngoài chế độ đó. */
internal fun isOutsideDiet(recipe: Recipe, restriction: DietRestriction, ingredients: List<Ingredient>): Boolean =
    filterRecipesByDiet(listOf(recipe), restriction, ingredients).isEmpty()

/** Thẻ kết quả sau khi rút món; `pick == null` nghĩa là không còn món nào khác. */
@Composable
internal fun RandomRecipeCard(
    pick: RandomPick?,
    outsideDiet: Boolean,
    onOpen: () -> Unit,
    onTryAnother: () -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val colors = AppTheme.colors
    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(colors.ctaSoft)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        if (pick == null) {
            Text(RandomRecipeCopy.noOther, style = MaterialTheme.typography.bodyLarge, color = colors.ctaText)
        } else {
            Text(
                pick.recipe.title,
                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                color = colors.ctaText,
            )
            if (!pick.canCookNow) Text(RandomRecipeCopy.needMore, style = MaterialTheme.typography.bodyMedium, color = colors.ctaText)
            if (outsideDiet) Text(RandomRecipeCopy.outsideDiet, style = MaterialTheme.typography.bodyMedium, color = colors.ctaText)
            Button(
                onClick = onOpen,
                modifier = Modifier.fillMaxWidth().heightIn(min = 52.dp),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(containerColor = colors.primary, contentColor = colors.onPrimary),
            ) { Text(RandomRecipeCopy.open, fontWeight = FontWeight.Bold) }
        }
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            TextButton(onClick = onTryAnother, modifier = Modifier.heightIn(min = 48.dp)) {
                Text(RandomRecipeCopy.tryAnother, color = colors.accentText, fontWeight = FontWeight.SemiBold)
            }
            TextButton(onClick = onBack, modifier = Modifier.heightIn(min = 48.dp)) {
                Text(RandomRecipeCopy.back, color = colors.muted)
            }
        }
    }
}
