package com.adityaoo7.sherlock.register

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.adityaoo7.sherlock.data.Result
import com.adityaoo7.sherlock.data.source.SharedPreferencesManager
import com.adityaoo7.sherlock.data.succeeded
import com.adityaoo7.sherlock.services.EncryptionService
import com.adityaoo7.sherlock.services.HashingService
import com.adityaoo7.sherlock.util.ValidationUtil
import com.adityaoo7.sherlock.util.VerificationAccount
import java.nio.charset.StandardCharsets
import java.security.SecureRandom

class RegisterViewModel(private val sharedPrefsManager: SharedPreferencesManager) : ViewModel() {

    val password = MutableLiveData<String>()

    private val _passwordValidation = MutableLiveData<Boolean>()
    val passwordValidation: LiveData<Boolean> = _passwordValidation

    private fun saveVerificationAccount() {
        val encryptedVerificationAccount =
            EncryptionService().encryptAccount(VerificationAccount.instance)
        if (encryptedVerificationAccount.succeeded && encryptedVerificationAccount is Result.Success) {
            sharedPrefsManager.putVerificationAccount(encryptedVerificationAccount.data)
        }
    }

    private fun getSalt(): String {
        val random = SecureRandom()
        val saltBytes = ByteArray(64)
        random.nextBytes(saltBytes)
        return String(saltBytes, StandardCharsets.UTF_8)
    }

    fun onPasswordSubmit() {
        val validPassword = ValidationUtil.validatePassword(password.value)
        if (validPassword) {
            HashingService.hashPassword(password.value!!, getSalt())
            saveVerificationAccount()
            _passwordValidation.value = true
            _navigateToLoginScreen.value = true
        } else {
            _navigateToLoginScreen.value = false
            _passwordValidation.value = false
        }
    }

    private val _navigateToLoginScreen = MutableLiveData<Boolean>()
    val navigateToLoginScreen: LiveData<Boolean> = _navigateToLoginScreen

    fun doneNavigating() {
        _navigateToLoginScreen.value = false
    }
}