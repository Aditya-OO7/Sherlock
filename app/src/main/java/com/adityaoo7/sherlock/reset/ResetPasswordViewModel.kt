package com.adityaoo7.sherlock.reset

import androidx.lifecycle.*
import com.adityaoo7.sherlock.R
import com.adityaoo7.sherlock.data.LoginAccount
import com.adityaoo7.sherlock.data.Result
import com.adityaoo7.sherlock.data.repository.AccountsRepository
import com.adityaoo7.sherlock.data.source.SharedPreferencesManager
import com.adityaoo7.sherlock.data.succeeded
import com.adityaoo7.sherlock.services.EncryptionService
import com.adityaoo7.sherlock.services.HashingService
import com.adityaoo7.sherlock.util.ValidationUtil
import com.adityaoo7.sherlock.util.VerificationAccount
import kotlinx.coroutines.launch
import java.nio.charset.StandardCharsets
import java.security.SecureRandom

class ResetPasswordViewModel(
    private val repository: AccountsRepository,
    private val encryptionService: EncryptionService,
    private val sharedPreferencesManager: SharedPreferencesManager
) : ViewModel() {

    val oldPassword = MutableLiveData<String>()
    val newPassword = MutableLiveData<String>()
    val confirmPassword = MutableLiveData<String>()

    private val accounts = mutableListOf<LoginAccount>()

    private val _dataLoading = MutableLiveData(false)
    val dataLoading: LiveData<Boolean> = _dataLoading

    private val _snackbarText = MutableLiveData<Int?>()
    val snackbarText: LiveData<Int?> = _snackbarText

    fun doneShowingSnackbar() {
        _snackbarText.value = null
    }

    private val _navigateToHomeScreen = MutableLiveData(false)
    val navigateToHomeScreen: LiveData<Boolean> = _navigateToHomeScreen

    fun doneNavigating() {
        _navigateToHomeScreen.value = false
    }

    fun onPasswordSubmit() {
        val validPassword = ValidationUtil.validatePassword(oldPassword.value)
        if (validPassword) {
            _dataLoading.value = true
            viewModelScope.launch {
                when (val errorValue = login()) {
                    VERIFIED -> {
                        when (val rErrorValue = retrieveAllAccounts()) {
                            VERIFIED -> {
                                val validNewPassword =
                                    ValidationUtil.validatePassword(newPassword.value)
                                if (validNewPassword) {
                                    when (val registerErrorValue = register()) {
                                        SUCCESSFULLY_REGISTERED -> {
                                            when (val sErrorValue = saveAllAccounts()) {
                                                SUCCESSFULLY_REGISTERED -> {
                                                    _dataLoading.postValue(false)
                                                    _navigateToHomeScreen.postValue(true)
                                                }
                                                else -> {
                                                    _dataLoading.postValue(false)
                                                    _snackbarText.postValue(sErrorValue)
                                                }
                                            }
                                        }
                                        else -> {
                                            _dataLoading.postValue(false)
                                            _snackbarText.postValue(registerErrorValue)
                                        }
                                    }
                                } else {
                                    _dataLoading.postValue(false)
                                    _snackbarText.postValue(R.string.wrong_password_pattern)
                                }
                            }
                            else -> {
                                _dataLoading.postValue(false)
                                _snackbarText.postValue(rErrorValue)
                            }
                        }
                    }
                    else -> {
                        _dataLoading.postValue(false)
                        _snackbarText.postValue(errorValue)
                    }
                }
            }
        } else {
            _snackbarText.value = R.string.wrong_password_pattern
        }
    }

    private suspend fun login(): Int {
        val result = sharedPreferencesManager.getSalt()

        return if (result.succeeded) {
            HashingService.hashPassword(oldPassword.value!!, (result as Result.Success).data)

            val verifiedUser = matchVerificationAccount()

            if (verifiedUser) {
                VERIFIED
            } else {
                R.string.incorrect_password
            }
        } else {
            R.string.auth_process_error
        }
    }

    private suspend fun register(): Int {
        if (newPassword.value != confirmPassword.value) {
            return R.string.password_not_match
        }

        val salt = getSalt()
        HashingService.hashPassword(newPassword.value!!, salt)

        saveVerificationAccount()

        sharedPreferencesManager.apply {
            putIsRegistered(true)
            putSalt(salt)
        }
        return SUCCESSFULLY_REGISTERED
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

    private suspend fun retrieveAllAccounts(): Int {
        val result = repository.getAccounts()
        if (result.succeeded) {
            val encryptedAccounts = (result as Result.Success).data
            encryptedAccounts.forEach { account ->

                val decryptedAccount = getDecryptedAccount(account)
                if (decryptedAccount != null) {
                    accounts.add(decryptedAccount)
                } else {
                    accounts.clear()
                    return R.string.account_decrypt_failed
                }
            }
            return VERIFIED
        } else {
            return R.string.no_account_found
        }
    }

    private suspend fun getDecryptedAccount(account: LoginAccount): LoginAccount? {
        val result = encryptionService.decrypt(account)
        return if (result.succeeded) {
            (result as Result.Success).data
        } else {
            null
        }
    }

    private suspend fun saveAllAccounts(): Int {
        val encryptedAccounts = mutableListOf<LoginAccount>()
        accounts.forEach { account ->
            val result = encryptionService.encrypt(account)
            if (result.succeeded) {
                encryptedAccounts.add((result as Result.Success).data)
            } else {
                return R.string.account_encrypt_failed
            }
        }

        repository.saveAccounts(encryptedAccounts)

        return SUCCESSFULLY_REGISTERED
    }

    private fun getSalt(): String {
        val random = SecureRandom()
        val saltBytes = ByteArray(64)
        random.nextBytes(saltBytes)
        return String(saltBytes, StandardCharsets.UTF_8)
    }

    private suspend fun saveVerificationAccount() {
        val result = encryptionService.encrypt(VerificationAccount.instance)
        if (result.succeeded) {
            sharedPreferencesManager.putVerificationAccount((result as Result.Success).data)
        }
    }
}

private const val VERIFIED = 0
private const val SUCCESSFULLY_REGISTERED = 0

@Suppress("UNCHECKED_CAST")
class ResetPasswordViewModelFactory(
    private val repository: AccountsRepository,
    private val sharedPreferencesManager: SharedPreferencesManager,
    private val encryptionService: EncryptionService
) : ViewModelProvider.NewInstanceFactory() {
    override fun <T : ViewModel?> create(modelClass: Class<T>) =
        (ResetPasswordViewModel(repository, encryptionService, sharedPreferencesManager) as T)
}