// business/services/ExpenseService.kt
package com.sd.laborator.business.services

import com.sd.laborator.business.interfaces.IExpenseService
import com.sd.laborator.business.models.Expense
import com.sd.laborator.business.models.MyCustomResponse
import org.springframework.stereotype.Service

@Service
class ExpenseService : IExpenseService {

    private val expensesByUser = mutableMapOf<Long, MutableList<Expense>>()
    private var currentId: Long = 1

    override fun addExpense(expense: Expense): MyCustomResponse<Any> {
        return try {
            expense.id = currentId++
            expensesByUser
                .getOrPut(expense.userId) { mutableListOf() }
                .add(expense)

            MyCustomResponse(successfulOperation = true, code = 201, data = expense)
        } catch (e: Exception) {
            MyCustomResponse(successfulOperation = false, code = 500, data = Unit, error = e.message)
        }
    }

    override fun getExpensesByUserId(userId: Long): MyCustomResponse<Any> {
        val list = expensesByUser[userId] ?: emptyList<Expense>()
        return MyCustomResponse(successfulOperation = true, code = 200, data = list)
    }
}