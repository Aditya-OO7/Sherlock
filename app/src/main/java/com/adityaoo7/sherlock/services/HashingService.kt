package com.adityaoo7.sherlock.services

import android.util.Log
import javax.crypto.SecretKey
import javax.crypto.SecretKeyFactory
import javax.crypto.spec.PBEKeySpec

object HashingService {

    private lateinit var salt: String
    private lateinit var password: String

    fun hashPassword(password: String,salt: String) {
        this.salt = salt
        this.password = password
    }

    @Volatile
    private var KEY: SecretKey? = null

    fun getKey(): SecretKey {
        val checkKey = KEY
        if (checkKey != null) {
            return checkKey
        }

        return synchronized(this) {
            val checkKeyAgain = KEY
            if (checkKeyAgain != null) {
                checkKeyAgain
            } else {
                Log.d("HashingService", "New Key created")
                val saltBytes = salt.toByteArray()
                val iterations = 1_00_000
                val keyLength = 256
                val passwordChars = password.toCharArray()

                val secretKeyFactory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA512")
                val keySpec = PBEKeySpec(passwordChars, saltBytes, iterations, keyLength)
                val key = secretKeyFactory.generateSecret(keySpec)

                KEY = key

                key
            }
        }
    }
}