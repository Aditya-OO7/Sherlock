package com.adityaoo7.sherlock.authentication

import androidx.lifecycle.*
import com.adityaoo7.sherlock.R
import com.adityaoo7.sherlock.data.Result
import com.adityaoo7.sherlock.data.source.SharedPreferencesManager
import com.adityaoo7.sherlock.data.succeeded
import com.adityaoo7.sherlock.services.EncryptionService
import com.adityaoo7.sherlock.services.HashingService
import com.adityaoo7.sherlock.util.ValidationUtil
import com.adityaoo7.sherlock.util.VerificationAccount
import kotlinx.coroutines.launch
import java.nio.charset.StandardCharsets
import java.security.SecureRandom

class AuthenticationViewModel(
    private val sharedPreferencesManager: SharedPreferencesManager,
    private val encryptionService: EncryptionService
) : ViewModel() {

    private val _isRegistered = MutableLiveData<Boolean>()
    val isRegistered: LiveData<Boolean> = _isRegistered

    init {
        getIsRegistered()
    }

    private fun getIsRegistered() {
        val result = sharedPreferencesManager.getIsRegistered()
        if (result.succeeded) {
            val registerState = (result as Result.Success).data
            _isRegistered.postValue(registerState)
        } else {
            _snackbarText.postValue(R.string.register_state_not_found)
        }
    }

    val password = MutableLiveData<String>()

    val confirmPassword = MutableLiveData<String>()

    private val _dataLoading = MutableLiveData(false)
    val dataLoading: LiveData<Boolean> = _dataLoading

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

    private suspend fun saveVerificationAccount() {
        val result = encryptionService.encrypt(VerificationAccount.instance)
        if (result.succeeded) {
            sharedPreferencesManager.putVerificationAccount((result as Result.Success).data)
        }
    }

    private suspend fun matchVerificationAccount(): Boolean {
        val result = sharedPreferencesManager.getVerificationAccount()

        return if (result.succeeded) {
            val resultOfDecryption = encryptionService.decrypt((result as Result.Success).data)
            if (resultOfDecryption.succeeded) {
                (resultOfDecryption as Result.Success).data == VerificationAccount.instance
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
            when (_isRegistered.value) {
                true -> {
                    login()
                }
                false -> {
                    register()
                }
                else -> {
                    _snackbarText.value = R.string.auth_process_error
                }
            }
        } else {
            _snackbarText.value = R.string.wrong_password_pattern
        }
    }

    private fun login() {

        _dataLoading.value = true

        viewModelScope.launch {
            val result = sharedPreferencesManager.getSalt()

            if (result.succeeded) {
                HashingService.hashPassword(password.value!!, (result as Result.Success).data)

                val verifiedUser = matchVerificationAccount()

                if (verifiedUser) {
                    _snackbarText.postValue(R.string.auth_success)
                    _navigateToHomeScreen.postValue(true)
                } else {
                    _snackbarText.postValue(R.string.incorrect_password)
                }
            } else {
                _snackbarText.postValue(R.string.auth_process_error)
            }
            _dataLoading.postValue(false)
        }
    }

    private fun register() {
        if (password.value != confirmPassword.value) {
            _snackbarText.value = R.string.password_not_match
            return
        }

        _dataLoading.value = true

        viewModelScope.launch {
            val salt = getSalt()
            HashingService.hashPassword(password.value!!, salt)

            saveVerificationAccount()

            sharedPreferencesManager.apply {
                putIsRegistered(true)
                putSalt(salt)
            }
            _snackbarText.postValue(R.string.register_success)
            getIsRegistered()
            _dataLoading.postValue(false)
        }
    }
}

@Suppress("UNCHECKED_CAST")
class AuthenticationViewModelFactory(
    private val sharedPreferencesManager: SharedPreferencesManager,
    private val encryptionService: EncryptionService
) : ViewModelProvider.NewInstanceFactory() {
    override fun <T : ViewModel?> create(modelClass: Class<T>) =
        (AuthenticationViewModel(sharedPreferencesManager, encryptionService) as T)
}