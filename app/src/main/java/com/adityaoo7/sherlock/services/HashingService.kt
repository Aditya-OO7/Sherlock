package com.adityaoo7.sherlock.services

import javax.crypto.SecretKey
import javax.crypto.SecretKeyFactory
import javax.crypto.spec.PBEKeySpec

object HashingService {

    fun hashPassword(password: String, salt: String) {

        val saltBytes = salt.toByteArray()
        val iterations = 1_00_000
        val keyLength = 256
        val passwordChars = password.toCharArray()

        val secretKeyFactory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA512")
        val keySpec = PBEKeySpec(passwordChars, saltBytes, iterations, keyLength)
        val key = secretKeyFactory.generateSecret(keySpec)

        KEY = key
    }

    @Volatile
    private var KEY: SecretKey? = null

    fun getKey(): SecretKey {
        return KEY!!
    }
}