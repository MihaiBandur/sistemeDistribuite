package com.sd.laborator.services

import com.sd.laborator.interfaces.CartesianProductOperation
import org.springframework.stereotype.Service

@Service
class CartesianStepOneService(
    private val cartesianOperation: CartesianProductOperation,
    private val stepTwo: CartesianStepTwoService
){
    fun process(A: Set<Int>, B: Set<Int>){
        val aXb = cartesianOperation.executeOperation(A, B);

        stepTwo.process(aXb, A, B)
    }
}