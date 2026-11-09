package com.loitu.yourrefri.feature.home.logic

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class HomeGreetingTest {
    @Test
    fun greeting_tracksLocalDayPeriods() {
        assertEquals("Chào buổi sáng 🌿", homeGreeting(5))
        assertEquals("Chào buổi sáng 🌿", homeGreeting(11))
        assertEquals("Chào buổi chiều 🌿", homeGreeting(12))
        assertEquals("Chào buổi chiều 🌿", homeGreeting(17))
        assertEquals("Chào buổi tối 🌿", homeGreeting(18))
        assertEquals("Chào buổi tối 🌿", homeGreeting(4))
    }

    @Test
    fun invalidHour_isRejected() {
        assertFailsWith<IllegalArgumentException> { homeGreeting(-1) }
        assertFailsWith<IllegalArgumentException> { homeGreeting(24) }
    }
}
