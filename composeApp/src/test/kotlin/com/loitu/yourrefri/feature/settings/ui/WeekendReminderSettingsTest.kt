package com.loitu.yourrefri.feature.settings.ui

import com.loitu.yourrefri.feature.notice.logic.WeekendReminderDay
import com.loitu.yourrefri.feature.settings.presentation.SettingsBackupPort
import com.loitu.yourrefri.feature.settings.presentation.SettingsIntent
import com.loitu.yourrefri.feature.settings.presentation.SettingsPreferencesPort
import com.loitu.yourrefri.feature.settings.presentation.SettingsViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import kotlinx.datetime.LocalTime
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

@OptIn(ExperimentalCoroutinesApi::class)
class WeekendReminderSettingsTest {
    private val dispatcher = StandardTestDispatcher()
    private val preferences = RecordingPreferences()

    @BeforeTest fun setUp() = Dispatchers.setMain(dispatcher)
    @AfterTest fun tearDown() = Dispatchers.resetMain()

    @Test fun weekendDayOf_defaultsToSundayUnlessSaturdayWasStored() {
        assertEquals(WeekendReminderDay.SATURDAY, weekendDayOf("SATURDAY"))
        for (storedName in listOf("SUNDAY", null, "invalid")) {
            assertEquals(WeekendReminderDay.SUNDAY, weekendDayOf(storedName))
        }
    }

    @Test fun weekendSummary_showsSelectedDayAndFormattedTime() {
        val time = LocalTime(8, 5)
        assertEquals("Thứ 7 lúc 08:05", ReminderCopy.weekendSummary(WeekendReminderDay.SATURDAY, time))
        assertEquals("Chủ Nhật lúc 08:05", ReminderCopy.weekendSummary(WeekendReminderDay.SUNDAY, time))
    }

    @Test fun weekdayTimeRow_requiresBothReminderSwitches() {
        assertFalse(shouldShowWeekdayTimeRow(false, false))
        assertFalse(shouldShowWeekdayTimeRow(false, true))
        assertFalse(shouldShowWeekdayTimeRow(true, false))
        assertTrue(shouldShowWeekdayTimeRow(true, true))
    }

    @Test fun setWeekendDay_persistsAndUpdatesState() = runTest {
        val viewModel = createViewModel()
        assertEquals(WeekendReminderDay.SUNDAY, viewModel.state.value.weekendDay)
        viewModel.onIntent(SettingsIntent.SetWeekendDay(WeekendReminderDay.SATURDAY))
        runCurrent()

        assertEquals(listOf(WeekendReminderDay.SATURDAY), preferences.weekendDayWrites)
        assertEquals(WeekendReminderDay.SATURDAY, viewModel.state.value.weekendDay)
        assertEquals(WeekendReminderDay.SATURDAY, createViewModel().state.value.weekendDay)
    }

    @Test fun setWeekdayEnabled_persistsAndUpdatesStateInBothDirections() = runTest {
        val viewModel = createViewModel()
        assertFalse(viewModel.state.value.weekdayEnabled)
        viewModel.onIntent(SettingsIntent.SetWeekdayEnabled(true))
        runCurrent()
        assertTrue(viewModel.state.value.weekdayEnabled)
        assertTrue(createViewModel().state.value.weekdayEnabled)

        viewModel.onIntent(SettingsIntent.SetWeekdayEnabled(false))
        runCurrent()
        assertFalse(viewModel.state.value.weekdayEnabled)
        assertEquals(listOf(true, false), preferences.weekdayEnabledWrites)
    }

    private fun createViewModel() = SettingsViewModel(UnusedBackupPort, preferences, dispatcher)

    private class RecordingPreferences : SettingsPreferencesPort {
        val weekendDayWrites = mutableListOf<WeekendReminderDay>()
        val weekdayEnabledWrites = mutableListOf<Boolean>()

        override fun readWeekendDay() = weekendDayWrites.lastOrNull() ?: super.readWeekendDay()
        override fun setWeekendDay(day: WeekendReminderDay) {
            weekendDayWrites.add(day)
        }

        override fun readWeekdayEnabled() = weekdayEnabledWrites.lastOrNull() ?: super.readWeekdayEnabled()
        override fun setWeekdayEnabled(enabled: Boolean) {
            weekdayEnabledWrites.add(enabled)
        }
    }

    private object UnusedBackupPort : SettingsBackupPort {
        override suspend fun exportJson(): String = error("Unexpected backup export")
        override fun validateJson(json: String): Unit = error("Unexpected backup validation")
        override suspend fun restoreJson(json: String): Unit = error("Unexpected backup restore")
    }
}
