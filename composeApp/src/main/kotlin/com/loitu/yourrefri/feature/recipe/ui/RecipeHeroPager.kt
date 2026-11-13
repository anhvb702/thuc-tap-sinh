package com.loitu.yourrefri.feature.recipe.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.PagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.loitu.yourrefri.feature.recipe.logic.RecipeRecommendation
import com.loitu.yourrefri.ui.components.RecipeThumb
import com.loitu.yourrefri.ui.theme.AppIcons
import com.loitu.yourrefri.ui.theme.AppTheme

private val HeroPhotoHeight = 140.dp

/** Nút tim 48dp. Đã yêu thích = nền ctaSoft, chữ/biểu tượng ctaText. */
@Composable
internal fun FavoriteHeartButton(
    title: String,
    isFavorite: Boolean,
    onToggle: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val colors = AppTheme.colors
    IconButton(
        onClick = onToggle,
        modifier = modifier
            .size(48.dp)
            .semantics { contentDescription = favoriteToggleDescription(title, isFavorite) },
        colors = IconButtonDefaults.iconButtonColors(
            containerColor = if (isFavorite) colors.ctaSoft else colors.surface,
            contentColor = if (isFavorite) colors.ctaText else colors.muted,
        ),
    ) {
        Icon(AppIcons.Heart, contentDescription = null, modifier = Modifier.size(24.dp))
    }
}

/** Một thẻ nổi bật: ảnh món phủ rộng, nhãn, nút tim; dưới ảnh là tên món, thời gian, mức độ. */
@Composable
internal fun RecipeHeroCard(
    recommendation: RecipeRecommendation,
    index: Int,
    isFavorite: Boolean,
    onToggleFavorite: () -> Unit,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val colors = AppTheme.colors
    val recipe = recommendation.recipe
    val shape = RoundedCornerShape(16.dp)
    Column(
        modifier = modifier
            .fillMaxWidth()
            .heightIn(min = 220.dp)
            .clip(shape)
            .background(colors.surface)
            .border(1.dp, colors.line, shape)
            .clickable(role = Role.Button, onClick = onClick),
    ) {
        Box(modifier = Modifier.fillMaxWidth().height(HeroPhotoHeight)) {
            // size(...) sau fillMaxWidth: chiều rộng bị ép theo khung cha, chiều cao giữ nguyên.
            RecipeThumb(
                recipeId = recipe.id,
                modifier = Modifier.fillMaxWidth(),
                size = HeroPhotoHeight,
                shape = RoundedCornerShape(0.dp),
            )
            Surface(
                modifier = Modifier.align(Alignment.TopStart).padding(12.dp),
                shape = RoundedCornerShape(999.dp),
                color = colors.surface,
            ) {
                Text(
                    text = heroBadgeLabel(index, recommendation.reason),
                    style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.SemiBold),
                    color = colors.accentText,
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                )
            }
            FavoriteHeartButton(
                title = recipe.title,
                isFavorite = isFavorite,
                onToggle = onToggleFavorite,
                modifier = Modifier.align(Alignment.TopEnd).padding(8.dp).clip(CircleShape),
            )
        }
        Column(modifier = Modifier.padding(horizontal = 14.dp, vertical = 12.dp)) {
            Text(
                text = recipe.title,
                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                color = colors.text,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
            )
            Row(
                modifier = Modifier.padding(top = 6.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp),
            ) {
                Icon(AppIcons.Clock, contentDescription = null, tint = colors.muted, modifier = Modifier.size(16.dp))
                Text(heroMinutesLabel(recipe.minutes), style = MaterialTheme.typography.bodyMedium, color = colors.muted)
                Text("·", style = MaterialTheme.typography.bodyMedium, color = colors.muted)
                Text(difficultyLabel(recipe), style = MaterialTheme.typography.bodyMedium, color = colors.muted)
            }
        }
    }
}

/** Pager các thẻ nổi bật kèm chấm phân trang. */
@Composable
internal fun RecipeHeroPager(
    items: List<RecipeRecommendation>,
    pagerState: PagerState,
    favoriteIds: Set<String>,
    onToggleFavorite: (String) -> Unit,
    onOpen: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier.fillMaxWidth()) {
        HorizontalPager(
            state = pagerState,
            modifier = Modifier.fillMaxWidth(),
            pageSpacing = 12.dp,
        ) { page ->
            val item = items[page]
            RecipeHeroCard(
                recommendation = item,
                index = page,
                isFavorite = item.recipe.id in favoriteIds,
                onToggleFavorite = { onToggleFavorite(item.recipe.id) },
                onClick = { onOpen(item.recipe.id) },
            )
        }
        if (items.size > 1) {
            PagerDots(count = items.size, current = pagerState.currentPage, modifier = Modifier.align(Alignment.CenterHorizontally))
        }
    }
}

@Composable
internal fun PagerDots(count: Int, current: Int, modifier: Modifier = Modifier) {
    val colors = AppTheme.colors
    Row(
        modifier = modifier.padding(top = 10.dp).semantics { contentDescription = "Món ${current + 1} trên $count" },
        horizontalArrangement = Arrangement.spacedBy(6.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        repeat(count) { i ->
            val active = i == current
            Box(
                modifier = Modifier
                    .height(8.dp)
                    .width(if (active) 20.dp else 8.dp)
                    .clip(CircleShape)
                    .background(if (active) colors.primary else colors.line),
            )
        }
    }
}
