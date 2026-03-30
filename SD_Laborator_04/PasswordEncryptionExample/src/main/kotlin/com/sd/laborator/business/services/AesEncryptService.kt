package com.sd.laborator.business.services

import com.sd.laborator.business.interfaces.IEncryptService
import org.springframework.beans.factory.annotation.Value
import org.springframework.core.io.ClassPathResource
import org.springframework.stereotype.Service
import java.security.KeyStore
import java.util.Base64
import javax.crypto.Cipher
import javax.crypto.SecretKey
import javax.crypto.spec.SecretKeySpec
@Service

class AesEncryptService: IEncryptService {
    private  val secretKey: SecretKey

    init {
        val keystorePassword = "changeit".toCharArray()
        val keyPassword = "changeit".toCharArray()
        val keyAlias = "myAesKey"

        val keyStoreFile = ClassPathResource("keystore.p12").inputStream

        val keyStore = KeyStore.getInstance("PKCS12")
        keyStore.load(keyStoreFile, keystorePassword)

        secretKey =keyStore.getKey(keyAlias, keyPassword) as SecretKey
    }
    override fun encrypt(data: String): String {
        val cipher = Cipher.getInstance("AES/ECB/PKCS5Padding")
        cipher.init(Cipher.ENCRYPT_MODE, secretKey)
        val encryptedBytes = cipher.doFinal(data.toByteArray(Charsets.UTF_8))
        return Base64.getEncoder().encodeToString(encryptedBytes)
    }

    override fun decrypt(encryptData: String): String {
        val cipher = Cipher.getInstance("AES/ECB/PKCS5Padding")
        cipher.init(Cipher.DECRYPT_MODE, secretKey)
        val decryptedBytes = cipher.doFinal(Base64.getDecoder().decode(encryptData))
        return String(decryptedBytes, Charsets.UTF_8)
    }

}