package com.sd.laborator.services

import com.sd.laborator.interfaces.CartesianProductOperation
import com.sd.laborator.interfaces.UnionOperation
import org.springframework.stereotype.Service

@Service
class CartesianStepTwoService(
    private val cartesianOperation: CartesianProductOperation,
    private val unionStep: UnionStepService
) {
    fun process(aXb: Set<Pair<Int, Int>>, A: Set<Int>, B: Set<Int>){
        val bXb = cartesianOperation.executeOperation(B, B)

        unionStep.process(aXb, bXb, A, B)
    }
}