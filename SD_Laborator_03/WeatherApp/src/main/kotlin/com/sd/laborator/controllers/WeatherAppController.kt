package com.sd.laborator.controllers



import com.sd.laborator.interfaces.dirijorInterfaces
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Controller
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestMethod
import org.springframework.web.bind.annotation.ResponseBody

@Controller
class WeatherAppController {

    @Autowired
    private lateinit var dirijor: dirijorInterfaces

    @RequestMapping("/getforecast/{location}", method = [RequestMethod.GET], produces = ["text/plain;charset=UTF-8"])
    @ResponseBody
    fun getForecast(@PathVariable location: String): String{
        return dirijor.resolveRequest(location)
    }
}