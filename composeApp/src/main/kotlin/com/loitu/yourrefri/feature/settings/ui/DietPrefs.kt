package com.loitu.yourrefri.feature.settings.ui

import android.content.Context
import com.loitu.yourrefri.feature.recipe.logic.DietRestriction

/** Lưu chế độ ăn trên máy (SharedPreferences), không gửi đi đâu. */
object DietPrefs {
    private const val FILE = "yourrefri_ui"
    private const val KEY = "diet_restriction"

    fun read(context: Context): DietRestriction =
        DietRestriction.fromStorage(context.getSharedPreferences(FILE, Context.MODE_PRIVATE).getString(KEY, null))

    fun write(context: Context, restriction: DietRestriction) {
        context.getSharedPreferences(FILE, Context.MODE_PRIVATE).edit().putString(KEY, restriction.storageValue).apply()
    }
}
