package com.adityaoo7.sherlock.home

import androidx.lifecycle.*
import com.adityaoo7.sherlock.R
import com.adityaoo7.sherlock.data.LoginAccount
import com.adityaoo7.sherlock.data.Result
import com.adityaoo7.sherlock.data.repository.AccountsRepository
import com.adityaoo7.sherlock.data.succeeded
import com.adityaoo7.sherlock.services.EncryptionService
import kotlinx.coroutines.launch

class HomeViewModel(
    accountsRepository: AccountsRepository,
    private val encryptionService: EncryptionService
) : ViewModel() {

    private val _items: LiveData<List<LoginAccount>> = accountsRepository
        .observeAccounts()
        .switchMap { decryptAccounts(it) }

    val items: LiveData<List<LoginAccount>> = _items

    val empty: LiveData<Boolean> = Transformations.map(_items) { accountsList ->
        accountsList.isEmpty()
    }

    private fun decryptAccounts(result: Result<List<LoginAccount>>): LiveData<List<LoginAccount>> {
        val accountsList = MutableLiveData<List<LoginAccount>>()
        if (result.succeeded) {
            _dataLoading.postValue(true)

            viewModelScope.launch {
                val decryptedAccounts = ArrayList<LoginAccount>()

                (result as Result.Success).data.forEach { account ->
                    val resultDecryptedAccount = encryptionService.decrypt(account)

                    if (resultDecryptedAccount.succeeded) {
                        decryptedAccounts.add((resultDecryptedAccount as Result.Success).data)
                    } else {
                        decryptedAccounts.clear()
                        _snackbarText.postValue(R.string.account_decrypt_failed)
                        return@forEach
                    }
                }

                accountsList.postValue(decryptedAccounts)
                _dataLoading.postValue(false)
            }

        } else {
            accountsList.postValue(emptyList())
            _snackbarText.postValue(R.string.loading_accounts_error)
        }
        return accountsList
    }

    private val _dataLoading = MutableLiveData(false)
    val dataLoading: LiveData<Boolean> = _dataLoading

    private val _snackbarText = MutableLiveData<Int?>()
    val snackbarText: LiveData<Int?> = _snackbarText

    fun doneShowingSnackbar() {
        _snackbarText.value = null
    }

    private val _createNewAccount = MutableLiveData(false)
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

@Suppress("UNCHECKED_CAST")
class HomeViewModelFactory(
    private val repository: AccountsRepository,
    private val encryptionService: EncryptionService
) : ViewModelProvider.NewInstanceFactory() {
    override fun <T : ViewModel?> create(modelClass: Class<T>) =
        (HomeViewModel(repository, encryptionService) as T)
}