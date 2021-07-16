package com.adityaoo7.sherlock.services

import com.adityaoo7.sherlock.data.LoginAccount
import com.adityaoo7.sherlock.data.Result

interface EncryptionService {

    suspend fun encrypt(account: LoginAccount): Result<LoginAccount>

    suspend fun decrypt(account: LoginAccount): Result<LoginAccount>
}