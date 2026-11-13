package com.loitu.yourrefri.feature.backup.data

import kotlin.test.Test
import kotlin.test.assertEquals

class MealToggleBackupCodecTest {
    @Test fun oldBackupWithoutToggleFieldDecodes() {
        val old = """{"format":"yourrefri-backup","version":2,"pantry":[],"shelfOverrides":[],"quantities":[],"cookLogs":[]}"""
        assertEquals(emptyList(), BackupCodec.decode(old).mealToggles)
    }
}
