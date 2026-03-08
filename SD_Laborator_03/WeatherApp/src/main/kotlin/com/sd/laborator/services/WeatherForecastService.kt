package com.sd.laborator.services

import com.sd.laborator.interfaces.TimeServiceInterface
import com.sd.laborator.interfaces.WeatherForecastInterface
import com.sd.laborator.pojo.WeatherForecastData
import org.json.JSONObject
import org.springframework.stereotype.Service
import java.net.URL
import kotlin.math.roundToInt

@Service
class WeatherForecastService (private val timeService: TimeServiceInterface) : WeatherForecastInterface {
    override fun getForecastData(locationName: String, latitude: Double, longitude: Double): WeatherForecastData {
        val  forecastDataURL = URL("https://api.open-meteo.com/v1/forecast?latitude=$latitude&longitude=$longitude&current=temperature_2m,relative_humidity_2m,apparent_temperature,precipitation_probability,cloud_cover,visibility,wind_speed_10m,wind_direction_10m")

        val rawResponse: String = forecastDataURL.readText()
        val responseRootObject = JSONObject(rawResponse)

        val currentData = responseRootObject.getJSONObject("current")

        return WeatherForecastData(
            location = locationName,
            date = timeService.getCurrentTime(),
            temperature = currentData.getDouble("temperature_2m"),
            apparentTemperature = currentData.getDouble("apparent_temperature"),
            relativeHumidity = currentData.getInt("relative_humidity_2m"),
            precipitationProbability = currentData.optInt("precipitation_probability", 0),
            cloudCover = currentData.getInt("cloud_cover"),
            visibility = currentData.getDouble("visibility"),
            windSpeed = currentData.getDouble("wind_speed_10m"),
            windDirection = currentData.getDouble("wind_direction_10m")
        )
    }
}