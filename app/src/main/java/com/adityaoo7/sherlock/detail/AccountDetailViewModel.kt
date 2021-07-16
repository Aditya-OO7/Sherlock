package com.adityaoo7.sherlock.detail

import androidx.lifecycle.*
import com.adityaoo7.sherlock.R
import com.adityaoo7.sherlock.data.LoginAccount
import com.adityaoo7.sherlock.data.Result
import com.adityaoo7.sherlock.data.repository.AccountsRepository
import com.adityaoo7.sherlock.data.succeeded
import com.adityaoo7.sherlock.services.EncryptionService
import kotlinx.coroutines.launch

class AccountDetailViewModel(
    private val repository: AccountsRepository,
    private val encryptionService: EncryptionService
) : ViewModel() {

    private val _accountId = MutableLiveData<String>()

    private val _account = _accountId.switchMap { id ->
        repository.observeAccount(id).map { getResult(it) }
    }
    val account: LiveData<LoginAccount?> = _account

    private val _snackbarText = MutableLiveData<Int?>()
    val snackbarText: LiveData<Int?> = _snackbarText

    private val _dataLoading = MutableLiveData(false)
    val dataLoading: LiveData<Boolean> = _dataLoading

    private val _editAccount = MutableLiveData(false)
    val editAccount: LiveData<Boolean> = _editAccount

    private val _deleteAccount = MutableLiveData(false)
    val deleteAccount: LiveData<Boolean> = _deleteAccount

    fun deleteThisAccount() = viewModelScope.launch {
        _accountId.value?.let { id ->
            repository.deleteAccount(id)
            _deleteAccount.value = true
        }
    }

    fun doneDeletingAccount() {
        _deleteAccount.value = false
    }

    fun editAccount() {
        _editAccount.value = true
    }

    fun doneNavigatingEditScreen() {
        _editAccount.value = false
    }

    fun doneShowingSnackbar() {
        _snackbarText.value = null
    }

    private fun getResult(result: Result<LoginAccount>): LoginAccount? {

        var account: LoginAccount? = null

        if (result.succeeded) {
            viewModelScope.launch {
                val decryptedResult = encryptionService.decrypt((result as Result.Success).data)
                account = if (decryptedResult.succeeded) {
                    (decryptedResult as Result.Success).data
                } else {
                    _snackbarText.postValue(R.string.account_decrypt_failed)
                    null
                }
            }
        } else {
            _snackbarText.postValue(R.string.no_account_found)
            account = null
        }

        return account
    }

    fun start(accountId: String) {
        if (_dataLoading.value == true || accountId == _accountId.value) {
            return
        }
        _accountId.value = accountId
    }
}

@Suppress("UNCHECKED_CAST")
class AccountDetailViewModelFactory(
    private val repository: AccountsRepository,
    private val encryptionService: EncryptionService
) : ViewModelProvider.NewInstanceFactory() {
    override fun <T : ViewModel?> create(modelClass: Class<T>) =
        (AccountDetailViewModel(repository, encryptionService) as T)
}