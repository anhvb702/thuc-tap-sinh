package com.loitu.yourrefri.feature.pantry.ui

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

/**
 * Phân loại khu vực bảo quản: Ngăn đông hay Ngăn mát.
 */
fun isFreezerItem(ingredientId: String): Boolean = when (ingredientId) {
    "thit-heo", "thit-heo-xay", "thit-bo", "thit-ga", "ca-basa", "ca-hoi", "tom" -> true
    else -> false
}
