package com.adityaoo7.sherlock.data.source.shared

import com.adityaoo7.sherlock.data.LoginAccount
import com.adityaoo7.sherlock.data.Result
import com.adityaoo7.sherlock.data.source.SharedPreferencesManager

class FakeSharedPreferencesManager : SharedPreferencesManager {

    private var isRegistered:Boolean? = null
    private var verificationAccount:LoginAccount? = null
    private var saltStr:String? = null

    fun init(state: Boolean, vAccount:LoginAccount, salt:String) {
        isRegistered = state
        verificationAccount = vAccount
        saltStr = salt
    }

    private var isRegisteredShouldReturnError = false
    fun setIsRegisteredShouldReturnError(value: Boolean) {
        isRegisteredShouldReturnError = value
    }

    private var saltShouldReturnError = false
    fun setSaltError(value: Boolean) {
        saltShouldReturnError = value
    }

    override fun putVerificationAccount(account: LoginAccount) {
        verificationAccount = account
    }

    override fun getVerificationAccount(): Result<LoginAccount> {
        return if (verificationAccount != null) {
            Result.Success(verificationAccount!!)
        } else {
            Result.Error(Exception("Verification Account not found"))
        }
    }

    override fun putSalt(salt: String) {
        saltStr = salt
    }

    override fun getSalt(): Result<String> {
        return if (saltShouldReturnError) {
            Result.Error(Exception("Test Exception"))
        } else {
            if (saltStr != null) {
                Result.Success(saltStr!!)
            } else {
                Result.Error(Exception("Salt not found"))
            }
        }
    }

    override fun putIsRegistered(state: Boolean) {
        isRegistered = state
    }

    override fun getIsRegistered(): Result<Boolean> {
        return if (isRegisteredShouldReturnError) {
            Result.Error(Exception("Test Exception"))
        } else {
            if (isRegistered != null) {
                Result.Success(isRegistered!!)
            } else {
                Result.Error(Exception("isRegistered state not found"))
            }
        }
    }
}