package com.sd.laborator

import java.util.*
import jakarta.inject.Singleton

@Singleton
class EratosteneSieveService {
    val MAX_SIZE = 1000001

    private val isPrime = Vector<Boolean>(MAX_SIZE)
    private val SPF = Vector<Int>(MAX_SIZE)
    fun findPrimesLessThan(n: Int): List<Int>{
        val prime: MutableList<Int> = ArrayList()
        for(i in 2 until n){
            if(isPrime[i]) {
                prime.add(i)

                SPF[i] = i
            }
                var j = 0
                while (j < prime.size && i * prime[j] < n && prime[j] <= SPF[i]){
                    isPrime[i * prime[j]] = false
                    SPF[i * prime[j]] = prime[j]
                    j++
                }
            }
        return  prime
    }
    init {
        for (i in 0 until MAX_SIZE){
            isPrime.add(true)
            SPF.add(2)
        }
        isPrime[0] = false
        isPrime[1] = false
    }
}