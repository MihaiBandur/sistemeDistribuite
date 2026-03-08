package com.sd.laborator.services

import com.sd.laborator.interfaces.filterRegionsInterface
import com.sd.laborator.interfaces.LocationSearchInterface
import org.springframework.stereotype.Service
import java.util.Locale

@Service
class filterRegionsService(private  val locationSearchService: LocationSearchInterface): filterRegionsInterface {
    private val blacklistRegions = listOf("US", "UK", )

    override fun getNodeRegion(): String {
        return System.getProperty("user.country").toString() ?: Locale.getDefault().country
    }

    override fun isAllowedRegion(): Boolean {
        return !blacklistRegions.contains(getNodeRegion())
    }
}