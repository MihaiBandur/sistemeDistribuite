package com.sd.laborator.business.models

import com.fasterxml.jackson.annotation.JsonCreator
import com.fasterxml.jackson.annotation.JsonProperty

data class Expense(
    var id: Long? = null,
    var userId: Long = 0,
    var amount: Double = 0.0,
    var category: ExpenseCategory = ExpenseCategory.PERSONAL,
    var description: String = ""
)