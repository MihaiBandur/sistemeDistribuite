package com.sd.laborator.pojo

data class WeatherForecastData (
    var location: String,
    var date: String,
    var temperature: Double,
    val apparentTemperature: Double,
    var relativeHumidity: Int,
    var precipitationProbability: Int,
    var cloudCover: Int,
    var visibility: Double,
    var windSpeed: Double,
    var windDirection: Double
)
{
    override fun toString(): String {
        return """
            Date meteo pentru $location la data de $date:
            - Temperatura: $temperature °C (Se simte ca $apparentTemperature °C)
            - Umiditate: $relativeHumidity %
            - Probabilitate precipitații: $precipitationProbability %
            - Acoperire nori: $cloudCover %
            - Vizibilitate: $visibility m
            - Viteza vântului: $windSpeed km/h
            - Direcția vântului: $windDirection °
        """.trimIndent()
    }
}
