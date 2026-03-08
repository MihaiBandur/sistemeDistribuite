package com.sd.laborator.services

import com.sd.laborator.interfaces.LocationSearchInterface
import com.sd.laborator.interfaces.WeatherForecastInterface
import com.sd.laborator.interfaces.dirijorInterfaces
import com.sd.laborator.interfaces.filterRegionsInterface

import org.springframework.stereotype.Service

@Service
class dirijorService(
    private var regionsService: filterRegionsInterface,
    private  var locationSearchService: LocationSearchInterface,
    private var weatherForecastService: WeatherForecastInterface
): dirijorInterfaces {
    override fun resolveRequest(location: String): String {
        if (!regionsService.isAllowedRegion()){
            return "Acest nod nu poate da informatii despre zona  ${regionsService.getNodeRegion()}."
        }
        val coordinates = locationSearchService.getLocation(location)?: return "Nu s-au putut gasi informatii despre locatia \"$location\"!"

        val weatherData = weatherForecastService.getForecastData(
            location,
            coordinates.first,
            coordinates.second
        )

       return weatherData.toString()
    }
}