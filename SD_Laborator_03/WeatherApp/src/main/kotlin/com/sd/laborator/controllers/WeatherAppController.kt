package com.sd.laborator.controllers

import com.sd.laborator.interfaces.LocationSearchInterface
import com.sd.laborator.interfaces.WeatherForecastInterface
import com.sd.laborator.interfaces.filterRegionsInterface
import com.sd.laborator.pojo.WeatherForecastData
import com.sd.laborator.services.TimeService
import com.sd.laborator.services.filterRegionsService
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Controller
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestMethod
import org.springframework.web.bind.annotation.ResponseBody

@Controller
class WeatherAppController {

    @Autowired
    private lateinit var filterRegionsService: filterRegionsInterface

    @RequestMapping("/getforecast/{location}", method = [RequestMethod.GET], produces = ["text/plain;charset=UTF-8"])
    @ResponseBody
    fun getForecast(@PathVariable location: String): String{
        return  filterRegionsService.filterRegions(location)
    }
}