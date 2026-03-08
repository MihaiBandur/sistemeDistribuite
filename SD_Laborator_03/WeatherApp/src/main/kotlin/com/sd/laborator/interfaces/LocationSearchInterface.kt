package com.sd.laborator.interfaces

interface LocationSearchInterface {
    fun getLocation(locationName: String): Pair<Double, Double>?
}