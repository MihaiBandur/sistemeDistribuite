package com.sd.laborator.business.interfaces

import com.sd.laborator.business.models.Expense
import com.sd.laborator.business.models.MyCustomResponse

interface IExpenseService {
    fun addExpense(expense: Expense): MyCustomResponse<Any>
    fun getExpensesByUserId(userId: Long): MyCustomResponse<Any>
}