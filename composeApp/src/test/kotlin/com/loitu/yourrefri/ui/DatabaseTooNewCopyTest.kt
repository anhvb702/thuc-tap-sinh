package com.loitu.yourrefri.ui

import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class DatabaseTooNewCopyTest {
    @Test
    fun bodyAsksForUpdateAndPromisesDataStaysSafe() {
        assertTrue(DatabaseTooNewCopy.body.contains("cập nhật"))
        assertTrue(DatabaseTooNewCopy.body.contains("giữ nguyên"))
    }

    @Test
    fun copyAvoidsBannedWords() {
        val all = DatabaseTooNewCopy.title + " " + DatabaseTooNewCopy.body
        listOf("lãng phí", "bỏ lỡ", "hết hạn", "bạn quên").forEach { banned ->
            assertFalse(all.contains(banned, ignoreCase = true), "chứa từ cấm: $banned")
        }
    }
}
