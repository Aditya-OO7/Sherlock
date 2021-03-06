package com.adityaoo7.sherlock.data.source

import androidx.lifecycle.LiveData
import com.adityaoo7.sherlock.data.LoginAccount
import com.adityaoo7.sherlock.data.Result

interface AccountsDataSource {
    fun observeAccounts(): LiveData<Result<List<LoginAccount>>>

    fun observeAccount(accountID: String): LiveData<Result<LoginAccount>>

    suspend fun saveAccount(account: LoginAccount)

    suspend fun saveAccounts(accounts: List<LoginAccount>)

    suspend fun updateAccount(account: LoginAccount)

    suspend fun getAccount(accountID: String): Result<LoginAccount>

    suspend fun getAccounts(): Result<List<LoginAccount>>

    suspend fun deleteAccount(accountID: String)

    suspend fun deleteAccounts()
}