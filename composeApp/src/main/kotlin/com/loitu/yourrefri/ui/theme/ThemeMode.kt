package com.loitu.yourrefri.ui.theme

import android.content.Context

/** Lựa chọn giao diện của người dùng. Mặc định theo máy để không bất ngờ với ai đã bật chế độ tối hệ thống. */
enum class ThemeMode(val storageValue: String, val label: String) {
    SYSTEM("system", "Theo máy"),
    LIGHT("light", "Sáng"),
    DARK("dark", "Tối");

    fun isDark(systemDark: Boolean): Boolean = when (this) {
        SYSTEM -> systemDark
        LIGHT -> false
        DARK -> true
    }

    companion object {
        fun fromStorage(value: String?): ThemeMode = entries.firstOrNull { it.storageValue == value } ?: SYSTEM
    }
}

object ThemeModeCopy {
    const val title = "Giao diện"
    const val subtitle = "Nền tối dễ nhìn hơn khi nấu buổi tối."
}

/** Lưu lựa chọn trên máy (SharedPreferences), không gửi đi đâu. */
object ThemePrefs {
    private const val FILE = "yourrefri_ui"
    private const val KEY = "theme_mode"

    fun read(context: Context): ThemeMode =
        ThemeMode.fromStorage(context.getSharedPreferences(FILE, Context.MODE_PRIVATE).getString(KEY, null))

    fun write(context: Context, mode: ThemeMode) {
        context.getSharedPreferences(FILE, Context.MODE_PRIVATE).edit().putString(KEY, mode.storageValue).apply()
    }
}
