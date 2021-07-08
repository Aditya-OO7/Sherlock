package com.adityaoo7.sherlock.services

import com.adityaoo7.sherlock.data.LoginAccount
import com.adityaoo7.sherlock.data.Result

interface EncryptionService {

    fun encrypt(account: LoginAccount): Result<LoginAccount>

    fun decrypt(account: LoginAccount): Result<LoginAccount>
}