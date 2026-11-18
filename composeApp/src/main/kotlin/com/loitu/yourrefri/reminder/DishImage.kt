package com.loitu.yourrefri.reminder

fun dishImageResName(recipeId: String): String = buildString {
    append("dish_")
    recipeId.lowercase().forEach { character ->
        append(if (character in 'a'..'z' || character in '0'..'9' || character == '_') character else '_')
    }
}
