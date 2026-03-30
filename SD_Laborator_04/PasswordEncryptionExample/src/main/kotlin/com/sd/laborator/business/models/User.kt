package com.sd.laborator.business.models

data class User(
    var id: Long? = null,
    var username: String,
    var passwordHash: String,
    var encryptedFirstName: String,
    var encryptedLastName: String
)