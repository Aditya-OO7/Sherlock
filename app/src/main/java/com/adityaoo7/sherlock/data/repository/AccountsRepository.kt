package com.adityaoo7.sherlock.data.repository

import androidx.lifecycle.LiveData
import com.adityaoo7.sherlock.data.LoginAccount
import com.adityaoo7.sherlock.data.Result

interface AccountsRepository {

    fun observeAccounts(): LiveData<Result<List<LoginAccount>>>
    fun observeAccount(accountID: String): LiveData<Result<LoginAccount>>

    suspend fun saveAccount(account: LoginAccount)
    suspend fun saveAccounts(accounts: List<LoginAccount>)

    suspend fun updateAccount(account: LoginAccount)

    suspend fun deleteAccount(accountID: String)
    suspend fun deleteAccounts()

    suspend fun getAccount(accountID: String): Result<LoginAccount>
    suspend fun getAccounts(): Result<List<LoginAccount>>
}