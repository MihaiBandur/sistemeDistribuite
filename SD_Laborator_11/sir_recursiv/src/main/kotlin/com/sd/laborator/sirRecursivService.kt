package com.sd.laborator

import jakarta.inject.Singleton
@Singleton
class sirRecursivService {

    val MAX_SIZE: Int = 100

    fun calculateTerm(n: Int): Long{
        if(n == 0){
            return 1L
        }

        val prev = calculateTerm(n - 1)

        return prev + 2 * prev / n
    }
}