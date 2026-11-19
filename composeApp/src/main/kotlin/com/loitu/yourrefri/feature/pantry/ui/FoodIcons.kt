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
enum class FoodGlyph { TOMATO, EGG, MEAT, FISH, MILK, LEAF, TOFU, SPICE, BOWL }

fun foodGlyphFor(ingredientId: String, group: String? = null): FoodGlyph = when (ingredientId) {
    "thit-heo", "thit-heo-xay", "thit-bo", "thit-ga" -> FoodGlyph.MEAT
    "ca-basa", "ca-hoi", "tom" -> FoodGlyph.FISH
    "ca-chua", "ca-rot", "khoai-tay", "bi-xanh", "hanh-tay", "chuoi", "tao", "cam" -> FoodGlyph.TOMATO
    "trung-ga" -> FoodGlyph.EGG
    "sua-tuoi", "sua-chua" -> FoodGlyph.MILK
    "dau-phu" -> FoodGlyph.TOFU
    else -> when (group) {
        "rau-la", "rau-cu" -> FoodGlyph.LEAF
        "thit" -> FoodGlyph.MEAT
        "ca" -> FoodGlyph.FISH
        "trai-cay" -> FoodGlyph.TOMATO
        "sua-trung" -> FoodGlyph.MILK
        "gia-vi" -> FoodGlyph.SPICE
        "khac" -> FoodGlyph.BOWL
        else -> FoodGlyph.LEAF
    }
}

fun foodIconFor(ingredientId: String, group: String? = null): ImageVector = when (foodGlyphFor(ingredientId, group)) {
    FoodGlyph.TOMATO -> AppIcons.Tomato
    FoodGlyph.EGG -> AppIcons.Egg
    FoodGlyph.MEAT -> AppIcons.Meat
    FoodGlyph.FISH -> AppIcons.Fish
    FoodGlyph.MILK -> AppIcons.Milk
    FoodGlyph.LEAF -> AppIcons.Leaf
    FoodGlyph.TOFU -> AppIcons.Tofu
    FoodGlyph.SPICE -> AppIcons.Spice
    FoodGlyph.BOWL -> AppIcons.Bowl
}

/**
 * Phân loại khu vực bảo quản: Ngăn đông hay Ngăn mát.
 */
fun isFreezerItem(ingredientId: String): Boolean = when (ingredientId) {
    "thit-heo", "thit-heo-xay", "thit-bo", "thit-ga", "ca-basa", "ca-hoi", "tom" -> true
    else -> false
}
