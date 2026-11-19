package com.loitu.yourrefri.feature.settings.ui

import com.loitu.yourrefri.ui.theme.ThemeMode
import kotlin.test.Test
import kotlin.test.assertEquals

/** Logic thuần của giao diện Cài đặt theo thiết kế Figma: nhãn chip Bật/Tắt và thứ tự chế độ theme. */
class SettingsFigmaPresentationTest {
    @Test fun toggleChipLabel_onAndOff() {
        assertEquals("Bật", SettingsPresentation.toggleChipLabel(true))
        assertEquals("Tắt", SettingsPresentation.toggleChipLabel(false))
    }

    @Test fun themeModeOrder_followsFigmaLightDarkSystem() {
        assertEquals(listOf(ThemeMode.LIGHT, ThemeMode.DARK, ThemeMode.SYSTEM), SettingsPresentation.themeModeOrder)
    }

    @Test fun themeModeOrder_coversEveryMode() {
        assertEquals(ThemeMode.entries.toSet(), SettingsPresentation.themeModeOrder.toSet())
    }

    @Test fun groupTitles_matchFigmaSections() {
        assertEquals("Nhắc nhở", SettingsPresentation.reminderGroupTitle)
        assertEquals("Khẩu vị & thói quen", SettingsPresentation.tasteGroupTitle)
        assertEquals("Sao lưu & khôi phục", SettingsPresentation.backupGroupTitle)
        assertEquals("Chuyển sang điện thoại mới", SettingsPresentation.transferTitle)
        assertEquals("Chỉ trên máy bạn", SettingsPresentation.onDeviceBadge)
    }
}
