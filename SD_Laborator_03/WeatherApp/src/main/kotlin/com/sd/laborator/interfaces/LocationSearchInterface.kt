package com.sd.laborator.interfaces


interface LocationSearchInterface {
    fun getLocation(location: String): Pair<Double, Double>?
}