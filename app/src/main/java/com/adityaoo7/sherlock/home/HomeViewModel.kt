package com.adityaoo7.sherlock.home

import androidx.lifecycle.*
import com.adityaoo7.sherlock.R
import com.adityaoo7.sherlock.data.LoginAccount
import com.adityaoo7.sherlock.data.Result
import com.adityaoo7.sherlock.data.repository.AccountsRepository
import com.adityaoo7.sherlock.services.EncryptionService
import kotlinx.coroutines.launch

class HomeViewModel(
    accountsRepository: AccountsRepository,
    private val encryptionService: EncryptionService
) : ViewModel() {

    private val _items: LiveData<List<LoginAccount>> = accountsRepository
        .observeAccounts()
        .switchMap {
            decryptAccounts(it)
        }

    val items: LiveData<List<LoginAccount>> = _items

    private fun decryptAccounts(accountsResult: Result<List<LoginAccount>>): LiveData<List<LoginAccount>> {
        val result = MutableLiveData<List<LoginAccount>>()
        if (accountsResult is Result.Success) {
            viewModelScope.launch {
                val decryptedAccounts = ArrayList<LoginAccount>()
                accountsResult.data.forEach { account ->
                    val resultDecryptedAccount = encryptionService.decrypt(account)
                    if (resultDecryptedAccount is Result.Success) {
                        decryptedAccounts.add(resultDecryptedAccount.data)
                    } else {
                        decryptedAccounts.clear()
                        _snackbarText.value = R.string.account_decrypt_failed
                        return@forEach
                    }
                }
                result.value = decryptedAccounts
            }
        } else {
            result.value = emptyList()
            _snackbarText.value = R.string.loading_accounts_error
        }
        return result
    }

    private val _dataLoading = MutableLiveData<Boolean>()
    val dataLoading: LiveData<Boolean> = _dataLoading

    private val _snackbarText = MutableLiveData<Int>()
    val snackbarText: LiveData<Int> = _snackbarText

    private val _createNewAccount = MutableLiveData<Boolean>()
    val createNewAccount: LiveData<Boolean> = _createNewAccount

    fun doneCreatingNewAccount() {
        _createNewAccount.value = false
    }

    fun addNewAccount() {
        _createNewAccount.value = true
    }

    private val _openExistingAccount = MutableLiveData<String?>()
    val openExistingAccount: LiveData<String?> = _openExistingAccount

    fun doneOpeningExistingAccount() {
        _openExistingAccount.value = null
    }

    fun openAccount(accountId: String) {
        _openExistingAccount.value = accountId
    }
}