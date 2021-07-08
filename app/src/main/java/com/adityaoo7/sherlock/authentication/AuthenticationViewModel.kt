package com.adityaoo7.sherlock.authentication

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.adityaoo7.sherlock.R
import com.adityaoo7.sherlock.data.Result
import com.adityaoo7.sherlock.data.source.SharedPreferencesManager
import com.adityaoo7.sherlock.data.succeeded
import com.adityaoo7.sherlock.services.EncryptionService
import com.adityaoo7.sherlock.services.HashingService
import com.adityaoo7.sherlock.util.ValidationUtil
import com.adityaoo7.sherlock.util.VerificationAccount
import java.nio.charset.StandardCharsets
import java.security.SecureRandom

class AuthenticationViewModel(
    private val sharedPreferencesManager: SharedPreferencesManager,
    private val encryptionService: EncryptionService
) : ViewModel() {

    private val _isRegistered = MutableLiveData<Boolean?>()
    val isRegistered: LiveData<Boolean?> = _isRegistered

    init {
        getIsRegistered()
    }

    private fun getIsRegistered() {
        val result = sharedPreferencesManager.getIsRegistered()
        if (result is Result.Success) {
            _isRegistered.value = result.data
        } else {
            _isRegistered.value = null
        }
    }

    val password = MutableLiveData<String>()

    private val _snackbarText = MutableLiveData<Int?>()
    val snackbarText: LiveData<Int?> = _snackbarText

    fun doneShowingSnackbar() {
        _snackbarText.value = null
    }

    private val _navigateToHomeScreen = MutableLiveData<Boolean>()
    val navigateToHomeScreen: LiveData<Boolean> = _navigateToHomeScreen

    fun doneNavigating() {
        _navigateToHomeScreen.value = false
    }

    private fun saveVerificationAccount() {
        val encryptedVerificationAccount =
            encryptionService.encrypt(VerificationAccount.instance)
        if (encryptedVerificationAccount.succeeded && encryptedVerificationAccount is Result.Success) {
            sharedPreferencesManager.putVerificationAccount(encryptedVerificationAccount.data)
        }
    }

    private fun matchVerificationAccount(): Boolean {
        val account = sharedPreferencesManager.getVerificationAccount()

        return if (account is Result.Success) {
            val decryptedAccount = encryptionService.decrypt(account.data)
            if (decryptedAccount is Result.Success) {
                decryptedAccount.data == VerificationAccount.instance
            } else {
                false
            }
        } else {
            false
        }
    }

    private fun getSalt(): String {
        val random = SecureRandom()
        val saltBytes = ByteArray(64)
        random.nextBytes(saltBytes)
        return String(saltBytes, StandardCharsets.UTF_8)
    }

    fun onPasswordSubmit() {
        getIsRegistered()
        val validPassword = ValidationUtil.validatePassword(password.value)
        if (validPassword) {
            when(_isRegistered.value) {
                true -> { login() }
                false -> { register() }
                else -> {
                    _snackbarText.value = R.string.auth_process_error
                }
            }
        } else {
            _snackbarText.value = R.string.wrong_password_pattern
        }
    }

    private fun login() {
        val salt = sharedPreferencesManager.getSalt()
        if (salt is Result.Success) {

            HashingService.hashPassword(password.value!!, salt.data)
            val verifiedUser = matchVerificationAccount()

            if (verifiedUser) {
                _snackbarText.value = R.string.auth_success
                _navigateToHomeScreen.value = true
            } else {
                _snackbarText.value = R.string.incorrect_password
            }
        } else {
            _snackbarText.value = R.string.auth_process_error
        }
    }

    private fun register() {
        HashingService.hashPassword(password.value!!, getSalt())
        saveVerificationAccount()

        sharedPreferencesManager.putIsRegistered(true)
        getIsRegistered()
        _snackbarText.value = R.string.register_success
    }
}