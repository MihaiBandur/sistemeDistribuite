package com.sd.laborator.presentation.controllers

import com.sd.laborator.business.interfaces.IUserService
import com.sd.laborator.business.services.UserService
import com.sd.laborator.presentation.utils.ControllerUtils
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

data class RegisterRequest(
    val username: String = "",
    val password: String = "",
    val firstName: String = "",
    val lastName: String = ""
)

data class LoginRequest(
    val username: String = "",
    val password: String = ""
)

@RestController
@RequestMapping("/users")
class UserController{
    @Autowired
    private  lateinit var userService: IUserService


    @PostMapping("/register")
    fun register(@RequestBody body: RegisterRequest): ResponseEntity<Any>{
        val response = userService.registerUser(
            body.username, body.password, body.firstName, body.lastName

        )
        return ControllerUtils.makeResponse(response)
    }
    @PostMapping("/login")
    fun login(@RequestBody body: LoginRequest): ResponseEntity<Any>{
        val response = userService.loginUser(body.username, body.password)
        return ControllerUtils.makeResponse(response)
    }
}