package com.adityaoo7.sherlock.data.source.local

import android.annotation.SuppressLint
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.map
import com.adityaoo7.sherlock.data.LoginAccount
import com.adityaoo7.sherlock.data.Result
import com.adityaoo7.sherlock.data.repository.AccountsRepository
import kotlinx.coroutines.runBlocking

class FakeAndroidTestRepository : AccountsRepository {

    private val TAG = FakeAndroidTestRepository::class.java.simpleName

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

    private suspend fun refreshAccounts() {
        observableAccounts.postValue(getAccounts())
    }

    override fun observeAccounts(): LiveData<Result<List<LoginAccount>>> {
        runBlocking {
            refreshAccounts()
        }
        return observableAccounts
    }

    override fun observeAccount(accountID: String): LiveData<Result<LoginAccount>> {
        runBlocking {
            refreshAccounts()
        }
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

        refreshAccounts()
    }

    override suspend fun saveAccounts(accounts: List<LoginAccount>) {
        accounts.forEach { account ->
            accountsServiceData[account.id] = account
        }
        refreshAccounts()
    }

    override suspend fun updateAccount(account: LoginAccount) {
        accountsServiceData[account.id] = account
        refreshAccounts()
    }

    override suspend fun deleteAccount(accountID: String) {
        accountsServiceData.remove(accountID)
        refreshAccounts()
    }

    override suspend fun deleteAccounts() {
        accountsServiceData.clear()
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

    override suspend fun getAccounts(): Result<List<LoginAccount>> {
        if (shouldReturnError) {
            return Result.Error(Exception("Test Exception"))
        }
        return Result.Success(accountsServiceData.values.toList())
    }
}