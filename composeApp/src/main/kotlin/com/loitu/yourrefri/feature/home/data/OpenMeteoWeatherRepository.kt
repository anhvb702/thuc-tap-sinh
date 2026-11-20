package com.loitu.yourrefri.feature.home.data

import com.loitu.yourrefri.feature.home.domain.model.HomeWeather
import com.loitu.yourrefri.feature.home.domain.model.WeatherCity
import com.loitu.yourrefri.feature.home.domain.model.mapWmoWeatherCodeToVi
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL
import kotlin.math.roundToInt

interface HomeWeatherRepository {
    suspend fun fetchCurrent(city: WeatherCity): HomeWeather?
}

class OpenMeteoWeatherRepository(
    private val ioDispatcher: CoroutineDispatcher = Dispatchers.IO,
) : HomeWeatherRepository {
    override suspend fun fetchCurrent(city: WeatherCity): HomeWeather? = withContext(ioDispatcher) {
        runCatching {
            val connection = URL(
                "https://api.open-meteo.com/v1/forecast?latitude=${city.latitude}&longitude=${city.longitude}" +
                    "&current=temperature_2m,weather_code&timezone=auto",
            ).openConnection() as HttpURLConnection
            try {
                connection.connectTimeout = 3500
                connection.readTimeout = 3500
                connection.requestMethod = "GET"
                val current = JSONObject(connection.inputStream.bufferedReader().use { it.readText() })
                    .getJSONObject("current")
                HomeWeather(
                    city = city,
                    temperatureC = current.getDouble("temperature_2m").roundToInt(),
                    conditionVi = mapWmoWeatherCodeToVi(current.getInt("weather_code")),
                )
            } finally {
                connection.disconnect()
            }
        }.getOrNull()
    }
}
