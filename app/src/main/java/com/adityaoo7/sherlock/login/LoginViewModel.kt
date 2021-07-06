package com.adityaoo7.sherlock.login

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.adityaoo7.sherlock.data.Result
import com.adityaoo7.sherlock.data.source.SharedPreferencesManager
import com.adityaoo7.sherlock.services.EncryptionService
import com.adityaoo7.sherlock.services.HashingService
import com.adityaoo7.sherlock.util.ValidationUtil
import com.adityaoo7.sherlock.util.VerificationAccount

class LoginViewModel(private val sharedPrefsManager: SharedPreferencesManager) : ViewModel() {

    private val TAG = LoginViewModel::class.java.simpleName

    val password = MutableLiveData<String>()

    private val _passwordValidation = MutableLiveData<Boolean>()
    val passwordValidation: LiveData<Boolean> = _passwordValidation

    private fun matchVerificationAccount(): Boolean {
        val account = sharedPrefsManager.getVerificationAccount()

        return if (account is Result.Success) {
            val decryptedAccount = EncryptionService().decryptAccount(account.data)

            if (decryptedAccount is Result.Success) {
                decryptedAccount.data == VerificationAccount.instance
            } else {
                false
            }
        } else {
            false
        }
    }

    fun onPasswordSubmit() {
        val validPassword = ValidationUtil.validatePassword(password.value)

        if (validPassword) {
            val salt = sharedPrefsManager.getSalt()
            if (salt is Result.Success) {

                HashingService.hashPassword(password.value!!, salt.data)
                val verifiedUser = matchVerificationAccount()

                if (verifiedUser) {
                    _passwordValidation.value = true
                    _navigateToHomeScreen.value = true
                } else {
                    _passwordValidation.value = false
                    _navigateToHomeScreen.value = false
                }
            } else {
                _passwordValidation.value = false
                _navigateToHomeScreen.value = false
            }
        } else {
            _passwordValidation.value = false
            _navigateToHomeScreen.value = false
        }
    }

    private val _navigateToHomeScreen = MutableLiveData<Boolean>()
    val navigateToHomeScreen: LiveData<Boolean> = _navigateToHomeScreen

    fun doneNavigating() {
        _navigateToHomeScreen.value = false
    }
}