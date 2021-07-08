package com.adityaoo7.sherlock.data.source

import com.adityaoo7.sherlock.data.LoginAccount
import com.adityaoo7.sherlock.data.Result

interface SharedPreferencesManager {

    fun putVerificationAccount(account: LoginAccount)
    fun getVerificationAccount(): Result<LoginAccount>

    fun putSalt(salt: String)
    fun getSalt(): Result<String>

    fun putIsRegistered(state: Boolean)
    fun getIsRegistered(): Result<Boolean>
}