package com.sd.laborator.business.interfaces

import com.sd.laborator.business.models.User
import com.sd.laborator.business.models.MyCustomResponse

interface IUserService {
    fun registerUser(username: String, password: String, firstName: String, lastName: String): MyCustomResponse<Any>
    fun loginUser(username: String, password: String): MyCustomResponse<Any>
}