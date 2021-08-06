package com.adityaoo7.sherlock.services

import kotlinx.coroutines.runBlocking
import javax.crypto.SecretKey
import javax.crypto.SecretKeyFactory
import javax.crypto.spec.PBEKeySpec

object HashingService {

    private var newPassword: String? = null
    private var newSalt: String? = null

    fun hashPassword(password: String, salt: String) {
        newPassword = password
        newSalt = salt
        KEY = null
    }

    private fun generateKey() {
        val saltBytes = newSalt?.toByteArray()
            ?: throw Exception("Unintended behavior: Salt not found while generating key")
        val iterations = 1_00_000
        val keyLength = 256
        val passwordChars = newPassword?.toCharArray()
            ?: throw Exception("Unintended behavior: Password not found while generating key")

        val secretKeyFactory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA512")
        val keySpec = PBEKeySpec(passwordChars, saltBytes, iterations, keyLength)
        val key = secretKeyFactory.generateSecret(keySpec)

        KEY = key
    }

    @Volatile
    private var KEY: SecretKey? = null

    fun getKey(): SecretKey {
        if (KEY == null) {
            runBlocking {
                generateKey()
            }
        }
        return KEY!!
    }
}