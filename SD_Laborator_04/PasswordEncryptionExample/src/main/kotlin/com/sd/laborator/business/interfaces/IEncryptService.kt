package com.sd.laborator.business.interfaces

interface IEncryptService {
    fun encrypt(data: String): String
    fun decrypt(encryptData: String): String
}