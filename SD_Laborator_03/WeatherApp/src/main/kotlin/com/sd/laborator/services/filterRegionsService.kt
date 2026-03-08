package com.sd.laborator.services

import com.sd.laborator.interfaces.filterRegionsInterface
import com.sd.laborator.interfaces.LocationSearchInterface
import org.springframework.stereotype.Service
import java.util.Locale

@Service
class filterRegionsService(private  val locationSearchService: LocationSearchInterface): filterRegionsInterface {
    private val blacklistRegions = listOf("US", "UK")

    override fun filterRegions(location: String): String {
        val currentZone = System.getProperty("user.country") ?: Locale.getDefault().country

        if (blacklistRegions.contains(currentZone)){
            return "Nu este permis accesul pentru zona $currentZone."
        }

        return locationSearchService.processLocationAndPassFurther(location)
    }
}