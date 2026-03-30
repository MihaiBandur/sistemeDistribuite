package com.sd.laborator.presentation.controllers

import com.sd.laborator.business.interfaces.IExpenseService
import com.sd.laborator.business.models.Expense
import com.sd.laborator.presentation.utils.ControllerUtils
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*


@RestController
@RequestMapping("/expenses")

class ExpenseController{
    @Autowired
    private lateinit var  expenseService: IExpenseService

    @PostMapping
    fun addExpense(@RequestBody expense: Expense): ResponseEntity<Any>{
        val  response = expenseService.addExpense(expense)
        return ControllerUtils.makeResponse(response)
    }

    @GetMapping
    fun getExpenses(@RequestParam userId: Long): ResponseEntity<Any>{
        val response = expenseService.getExpensesByUserId(userId)
        return ControllerUtils.makeResponse(response)
    }
}

