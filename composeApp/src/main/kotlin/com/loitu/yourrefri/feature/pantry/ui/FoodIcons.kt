package com.loitu.yourrefri.feature.pantry.ui

import androidx.compose.ui.graphics.vector.ImageVector
import com.loitu.yourrefri.ui.theme.AppIcons

/**
 * Ánh xạ icon thực phẩm thân thiện, trực quan cho người nội trợ.
 */
fun foodEmojiFor(ingredientId: String): String = when (ingredientId) {
    "thit-heo" -> "🥩"
    "thit-heo-xay" -> "🥓"
    "thit-bo" -> "🥩"
    "thit-ga" -> "🍗"
    "ca-basa", "ca-hoi" -> "🐟"
    "tom" -> "🦐"
    "rau-muong", "cai-ngot" -> "🥬"
    "xa-lach" -> "🥗"
    "rau-ngot" -> "🌿"
    "hanh-la" -> "🌱"
    "ca-rot" -> "🥕"
    "khoai-tay" -> "🥔"
    "ca-chua" -> "🍅"
    "bi-xanh" -> "🥒"
    "hanh-tay" -> "🧅"
    "chuoi" -> "🍌"
    "tao" -> "🍎"
    "cam" -> "🍊"
    "trung-ga" -> "🥚"
    "sua-tuoi" -> "🥛"
    "sua-chua" -> "🍶"
    "dau-phu" -> "🧈"
    "nam" -> "🍄"
    else -> "🥦"
}

/** Nhóm biểu tượng nét dùng thay emoji trong giao diện mới. */
enum class FoodGlyph { TOMATO, EGG, MEAT, FISH, MILK, LEAF, TOFU }

fun foodGlyphFor(ingredientId: String): FoodGlyph = when (ingredientId) {
    "thit-heo", "thit-heo-xay", "thit-bo", "thit-ga" -> FoodGlyph.MEAT
    "ca-basa", "ca-hoi", "tom" -> FoodGlyph.FISH
    "ca-chua", "ca-rot", "khoai-tay", "bi-xanh", "hanh-tay", "chuoi", "tao", "cam" -> FoodGlyph.TOMATO
    "trung-ga" -> FoodGlyph.EGG
    "sua-tuoi", "sua-chua" -> FoodGlyph.MILK
    "dau-phu" -> FoodGlyph.TOFU
    else -> FoodGlyph.LEAF
}

fun foodIconFor(ingredientId: String): ImageVector = when (foodGlyphFor(ingredientId)) {
    FoodGlyph.TOMATO -> AppIcons.Tomato
    FoodGlyph.EGG -> AppIcons.Egg
    FoodGlyph.MEAT -> AppIcons.Meat
    FoodGlyph.FISH -> AppIcons.Fish
    FoodGlyph.MILK -> AppIcons.Milk
    FoodGlyph.LEAF -> AppIcons.Leaf
    FoodGlyph.TOFU -> AppIcons.Tofu
}

/**
 * Phân loại khu vực bảo quản: Ngăn đông hay Ngăn mát.
 */
fun isFreezerItem(ingredientId: String): Boolean = when (ingredientId) {
    "thit-heo", "thit-heo-xay", "thit-bo", "thit-ga", "ca-basa", "ca-hoi", "tom" -> true
    else -> false
}
