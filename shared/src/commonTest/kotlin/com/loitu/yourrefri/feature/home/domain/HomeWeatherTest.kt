package com.loitu.yourrefri.feature.home.domain

import com.loitu.yourrefri.feature.home.domain.model.HomeWeather
import com.loitu.yourrefri.feature.home.domain.model.WeatherCity
import com.loitu.yourrefri.feature.home.domain.model.formatHomeWeatherLine
import com.loitu.yourrefri.feature.home.domain.model.mapWmoWeatherCodeToVi
import kotlin.test.Test
import kotlin.test.assertEquals

class HomeWeatherTest {
    @Test
    fun mapsWmoWeatherCodesToVietnameseConditions() {
        assertEquals("Trời quang", mapWmoWeatherCodeToVi(0))
        assertEquals("Ít mây", mapWmoWeatherCodeToVi(2))
        assertEquals("Nhiều mây", mapWmoWeatherCodeToVi(3))
        assertEquals("Có sương mù", mapWmoWeatherCodeToVi(45))
        assertEquals("Mưa phùn", mapWmoWeatherCodeToVi(55))
        assertEquals("Có mưa", mapWmoWeatherCodeToVi(65))
        assertEquals("Mưa rào", mapWmoWeatherCodeToVi(80))
        assertEquals("Có dông", mapWmoWeatherCodeToVi(95))
        assertEquals("Thời tiết ôn hòa", mapWmoWeatherCodeToVi(999))
    }

    @Test
    fun nextCyclesBetweenSupportedCities() {
        assertEquals(WeatherCity.HCM, WeatherCity.HANOI.next())
        assertEquals(WeatherCity.HANOI, WeatherCity.HCM.next())
    }

    @Test
    fun formatsWeatherLineForLoadedLoadingAndUnavailableStates() {
        val weather = HomeWeather(WeatherCity.HANOI, 28, "Ít mây")

        assertEquals("Hà Nội · 28°C · Ít mây", formatHomeWeatherLine(WeatherCity.HANOI, weather, loading = false))
        assertEquals("Đang xem thời tiết TP.HCM...", formatHomeWeatherLine(WeatherCity.HCM, null, loading = true))
        assertEquals("TP.HCM · Thời tiết chưa sẵn sàng", formatHomeWeatherLine(WeatherCity.HCM, weather, loading = false))
    }
}
