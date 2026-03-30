// business/services/UserService.kt
package com.sd.laborator.business.services

import com.sd.laborator.business.interfaces.IAuthService
import com.sd.laborator.business.interfaces.IEncryptService
import com.sd.laborator.business.interfaces.IUserService
import com.sd.laborator.business.models.MatchPasswordModel
import com.sd.laborator.business.models.MyCustomResponse
import com.sd.laborator.business.models.User
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service

@Service
class UserService : IUserService {

    @Autowired
    private lateinit var authService: IAuthService

    @Autowired
    private lateinit var encryptionService: IEncryptService

    private val users = mutableListOf<User>()
    private var currentId: Long = 1

    override fun registerUser(
        username: String,
        password: String,
        firstName: String,
        lastName: String
    ): MyCustomResponse<Any> {
        return try {
            if (users.any { it.username == username }) {
                return MyCustomResponse(
                    successfulOperation = false,
                    code = 409,
                    data = Unit,
                    error = "Username already exists."
                )
            }

            val combinedPassword = username + password
            val response = authService.encodePassword(combinedPassword, "bcrypt")
            if (!response.successfulOperation) return response

            val hash = response.data as String
            val encFirstName = encryptionService.encrypt(firstName)
            val encLastName = encryptionService.encrypt(lastName)

            val newUser = User(currentId++, username, hash, encFirstName, encLastName)
            users.add(newUser)

            MyCustomResponse(
                successfulOperation = true,
                code = 201,
                data = mapOf("id" to newUser.id, "username" to newUser.username)
            )
        } catch (e: Exception) {
            MyCustomResponse(successfulOperation = false, code = 500, data = Unit, error = e.message)
        }
    }

    override fun loginUser(username: String, password: String): MyCustomResponse<Any> {
        return try {
            val user = users.find { it.username == username }
                ?: return MyCustomResponse(
                    successfulOperation = false,
                    code = 401,
                    data = Unit,
                    error = "Invalid username or password."
                )

            val combinedPassword = username + password
            val matchModel = MatchPasswordModel(
                password = combinedPassword,
                hashPassword = user.passwordHash,
                encodingId = "bcrypt"
            )
            val matchResponse = authService.matchPassword(matchModel)
            if (!matchResponse.successfulOperation) return matchResponse

            @Suppress("UNCHECKED_CAST")
            val matched = (matchResponse.data as? Map<String, Boolean>)?.get("areMatched") ?: false

            if (!matched) {
                return MyCustomResponse(
                    successfulOperation = false,
                    code = 401,
                    data = Unit,
                    error = "Invalid username or password."
                )
            }

            MyCustomResponse(
                successfulOperation = true,
                code = 200,
                data = mapOf("id" to user.id, "username" to user.username)
            )
        } catch (e: Exception) {
            MyCustomResponse(successfulOperation = false, code = 500, data = Unit, error = e.message)
        }
    }

    fun findUserByUsername(username: String): User? = users.find { it.username == username }
}