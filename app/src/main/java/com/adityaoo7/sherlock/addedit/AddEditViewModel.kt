package com.adityaoo7.sherlock.addedit

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.adityaoo7.sherlock.R
import com.adityaoo7.sherlock.data.LoginAccount
import com.adityaoo7.sherlock.data.Result
import com.adityaoo7.sherlock.data.repository.AccountsRepository
import com.adityaoo7.sherlock.services.EncryptionService
import kotlinx.coroutines.launch
import java.lang.RuntimeException

class AddEditViewModel(
    private val accountsRepository: AccountsRepository,
    private val encryptionService: EncryptionService
    ): ViewModel() {

    val name = MutableLiveData<String>()
    val userName = MutableLiveData<String>()
    val password = MutableLiveData<String>()
    val uri = MutableLiveData<String>()
    val note = MutableLiveData<String>()

    private var accountId: String? = null

    private var isNewAccount:Boolean = false

    private var isDataLoaded = false

    private val _dataLoading = MutableLiveData<Boolean>()
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

    fun start(accountId: String?) {
        if (_dataLoading.value == true) {
            return
        }
        this.accountId = accountId
        if (accountId == null) {
            isNewAccount = true
            return
        }
        if (isDataLoaded) {
            return
        }

        isNewAccount = false
        _dataLoading.value = true
        viewModelScope.launch {
            accountsRepository.getAccount(accountId).let { result ->
                if (result is Result.Success) {
                    onAccountLoaded(result.data)
                } else {
                    onDataNotAvailable()
                }
            }
        }
    }

    private fun onAccountLoaded(account: LoginAccount) {
        val result = encryptionService.decrypt(account)
        if (result is Result.Success) {
            val decryptedAccount = result.data
            name.value = decryptedAccount.name
            userName.value = decryptedAccount.userName
            password.value = decryptedAccount.password
            uri.value = decryptedAccount.uri
            note.value = decryptedAccount.note
            isDataLoaded = true
        } else {
            isDataLoaded = false
            _snackbarText.value = R.string.account_decrypt_failed
        }

        _dataLoading.value = false
    }

    private fun onDataNotAvailable() {
        _snackbarText.value = R.string.no_account_found
        _dataLoading.value = false
    }

    fun saveAccount() {
        val currentName = name.value
        val currentUserName = userName.value
        val currentPassword = password.value
        val currentUri = uri.value
        val currentNote = note.value

        if (currentUserName == null || currentPassword == null) {
            _snackbarText.value = R.string.empty_account
            return
        }
        val currentAccount = LoginAccount(
            currentName ?: "-",
            currentUserName,
            currentPassword,
            currentUri ?: "-",
            currentNote ?: "-"
        )
        if (currentAccount.isEmpty) {
            _snackbarText.value = R.string.empty_account
        }

        val currentAccountId = accountId
        if (isNewAccount || currentAccountId == null) {
            createAccount(currentAccount)
        } else {
            currentAccount.id = currentAccountId
            updateAccount(currentAccount)
        }
    }

    private fun createAccount(newAccount: LoginAccount) = viewModelScope.launch {
        val result = encryptionService.encrypt(newAccount)
        if (result is Result.Success) {
            val encryptedAccount = result.data
            accountsRepository.saveAccount(encryptedAccount)
            _snackbarText.value = R.string.save_success
            _navigateToHomeScreen.value = true
        } else {
            _snackbarText.value = R.string.account_encrypt_failed
        }
    }

    private fun updateAccount(account: LoginAccount) {
        if (isNewAccount) {
            throw RuntimeException("updateAccount() was called but account is new")
        }
        viewModelScope.launch {
            val result = encryptionService.encrypt(account)
            if (result is Result.Success) {
                val encryptedAccount = result.data
                accountsRepository.updateAccount(encryptedAccount)
                _snackbarText.value = R.string.save_success
                _navigateToHomeScreen.value = true
            } else {
                _snackbarText.value = R.string.account_encrypt_failed
            }
        }
    }
}