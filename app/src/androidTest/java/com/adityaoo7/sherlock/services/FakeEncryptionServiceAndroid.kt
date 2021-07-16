package com.adityaoo7.sherlock.services

import com.adityaoo7.sherlock.data.LoginAccount
import com.adityaoo7.sherlock.data.Result

object FakeEncryptionServiceAndroid : EncryptionService {

    private var shouldReturnError = false

    fun setShouldReturnError(value: Boolean) {
        shouldReturnError = value
    }

    private var shouldSendDifferentAccount = false
    fun setShouldReturnDifferentAccount(value: Boolean) {
        shouldSendDifferentAccount = value
    }

    override suspend fun encrypt(account: LoginAccount): Result<LoginAccount> {
        return if (shouldReturnError) {
            Result.Error(Exception("Test Exception"))
        } else {
            Result.Success(account)
        }
    }

    override suspend fun decrypt(account: LoginAccount): Result<LoginAccount> {
        return when {
            shouldReturnError -> {
                Result.Error(Exception("Test Exception"))
            }
            shouldSendDifferentAccount -> {
                Result.Success(LoginAccount())
            }
            else -> {
                Result.Success(account)
            }
        }
    }
}