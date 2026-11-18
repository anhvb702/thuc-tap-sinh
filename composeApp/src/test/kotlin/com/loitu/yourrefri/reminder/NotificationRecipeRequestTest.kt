package com.loitu.yourrefri.reminder

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class NotificationRecipeRequestTest {
    @Test fun knownDinnerIdIsRequested() {
        assertEquals("ca-kho", notificationRecipeRequest("ca-kho", listOf("ca-kho", "ga-om")))
    }

    @Test fun dinnerOutsideTodaysSuggestionsStillOpens() {
        // Hàm không nhận danh sách gợi ý; món ngoài 3 gợi ý vẫn được mở.
        assertEquals("ga-om", notificationRecipeRequest("ga-om", listOf("ga-om")))
    }

    @Test fun unknownOrDeletedIdFallsBackToNull() {
        assertNull(notificationRecipeRequest("mon-da-xoa", listOf("ca-kho")))
    }

    @Test fun missingDinnerReturnsNull() {
        assertNull(notificationRecipeRequest(null, listOf("ca-kho")))
    }

    @Test fun extraNameIsStable() {
        assertEquals("recipe_id", NOTIFICATION_RECIPE_EXTRA)
    }
}
