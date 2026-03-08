package com.sd.laborator.services

import com.sd.laborator.interfaces.LocationSearchInterface
import org.springframework.stereotype.Service
import java.net.URL
import org.json.JSONObject
import java.lang.Exception
import java.net.URLEncoder
import java.nio.charset.StandardCharsets

@Service
class LocationSearchService : LocationSearchInterface {
    override fun getLocation(location: String): Pair<Double, Double>? {
        val encodedLocationName = URLEncoder.encode(location, StandardCharsets.UTF_8.toString())
        val locationSearchURL = URL("https://geocoding-api.open-meteo.com/v1/search?name=$encodedLocationName&count=1&language=en&format=json")

        return try {
            val rawResponse: String = locationSearchURL.readText()
            val responseRootObject = JSONObject(rawResponse)

            if(responseRootObject.has("results")){
                val result = responseRootObject.getJSONArray("results").getJSONObject(0)
                val latitude = result.getDouble("latitude")
                val longitude = result.getDouble("longitude")
                Pair(latitude, longitude)
            } else {
                null
            }
        } catch (e: Exception){
            e.printStackTrace()
            null
        }
    }
}