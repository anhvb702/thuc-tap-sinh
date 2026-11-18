package com.loitu.yourrefri.shared.db

import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class DatabaseVersionTest {
    @Test
    fun databaseNewerThanAppIsTooNew() {
        // Đúng ca đã gặp trên máy: DB phiên bản 8, app bản 7.
        assertTrue(isDatabaseTooNew(dbVersion = 8, appVersion = 7))
    }

    @Test
    fun equalOrOlderDatabaseIsOpenable() {
        assertFalse(isDatabaseTooNew(dbVersion = 7, appVersion = 7))
        assertFalse(isDatabaseTooNew(dbVersion = 0, appVersion = 7))
    }
}
