package com.adityaoo7.sherlock.data.repository

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.map
import com.adityaoo7.sherlock.data.LoginAccount
import com.adityaoo7.sherlock.data.Result
import kotlinx.coroutines.runBlocking

class FakeTestRepository : AccountsRepository {

    var accountsServiceData: LinkedHashMap<String, LoginAccount> = LinkedHashMap()

    private val observableAccounts = MutableLiveData<Result<List<LoginAccount>>>()

    private var shouldReturnError = false

    fun setReturnError(value: Boolean) {
        shouldReturnError = value
    }

    fun addAccounts(vararg accounts: LoginAccount) {
        for (account in accounts) {
            accountsServiceData[account.id] = account
        }

        runBlocking { refreshAccounts() }
    }

    private fun refreshAccounts() {
        observableAccounts.value = getAccounts()
    }

    override fun observeAccounts(): LiveData<Result<List<LoginAccount>>> {
        refreshAccounts()
        return observableAccounts
    }

    private fun getAccounts(): Result<List<LoginAccount>> {
        if (shouldReturnError) {
            return Result.Error(Exception("Test Exception"))
        }
        return Result.Success(accountsServiceData.values.toList())
    }

    override fun observeAccount(accountID: String): LiveData<Result<LoginAccount>> {
        refreshAccounts()
        return observableAccounts.map { accounts ->
            when (accounts) {
                is Result.Loading -> Result.Loading
                is Result.Error -> Result.Error(accounts.exception)
                is Result.Success -> {
                    val account = accounts.data.firstOrNull { it.id == accountID }
                        ?: return@map Result.Error(
                            Exception("Not Found")
                        )
                    Result.Success(account)
                }
            }
        }
    }

    override suspend fun saveAccount(account: LoginAccount) {
        accountsServiceData[account.id] = account
    }

    override suspend fun updateAccount(account: LoginAccount) {
        accountsServiceData[account.id] = account
    }

    override suspend fun deleteAccount(accountID: String) {
        accountsServiceData.remove(accountID)
        refreshAccounts()
    }

    override suspend fun getAccount(accountID: String): Result<LoginAccount> {
        if (shouldReturnError) {
            return Result.Error(Exception("Test Exception"))
        }
        accountsServiceData[accountID]?.let {
            return Result.Success(it)
        }
        return Result.Error(Exception("Could not find account"))
    }
}