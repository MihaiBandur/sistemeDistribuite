package com.sd.laborator.interfaces

import com.sd.laborator.pojo.WeatherForecastData

interface WeatherForecastInterface {
    fun getForecastData(locationName: String, latitude: Double, longitude: Double): WeatherForecastData
}