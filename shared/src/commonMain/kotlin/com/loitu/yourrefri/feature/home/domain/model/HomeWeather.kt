package com.loitu.yourrefri.feature.home.domain.model

enum class WeatherCity(
    val id: String,
    val labelVi: String,
    val latitude: Double,
    val longitude: Double,
) {
    HANOI("hanoi", "Hà Nội", 21.0285, 105.8542),
    HCM("hcm", "TP.HCM", 10.8231, 106.6297),
    ;

    fun next(): WeatherCity = if (this == HANOI) HCM else HANOI
}

data class HomeWeather(
    val city: WeatherCity,
    val temperatureC: Int,
    val conditionVi: String,
)

fun mapWmoWeatherCodeToVi(code: Int): String = when (code) {
    0 -> "Trời quang"
    1, 2 -> "Ít mây"
    3 -> "Nhiều mây"
    45, 48 -> "Có sương mù"
    51, 53, 55, 56, 57 -> "Mưa phùn"
    61, 63, 65, 66, 67 -> "Có mưa"
    80, 81, 82 -> "Mưa rào"
    95, 96, 99 -> "Có dông"
    else -> "Thời tiết ôn hòa"
}

fun formatHomeWeatherLine(city: WeatherCity, weather: HomeWeather?, loading: Boolean): String = when {
    weather != null && weather.city == city ->
        "${city.labelVi} · ${weather.temperatureC}°C · ${weather.conditionVi}"
    loading -> "Đang xem thời tiết ${city.labelVi}..."
    else -> "${city.labelVi} · Thời tiết chưa sẵn sàng"
}
