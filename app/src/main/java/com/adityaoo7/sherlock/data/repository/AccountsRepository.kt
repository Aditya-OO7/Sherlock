package com.adityaoo7.sherlock.data.repository

import androidx.lifecycle.LiveData
import com.adityaoo7.sherlock.data.LoginAccount
import com.adityaoo7.sherlock.data.Result

interface AccountsRepository {

    fun observeAccounts(): LiveData<Result<List<LoginAccount>>>
    fun observeAccount(accountID: String): LiveData<Result<LoginAccount>>

    suspend fun saveAccount(account: LoginAccount)
    suspend fun updateAccount(account: LoginAccount)
    suspend fun deleteAccount(accountID: String)

    suspend fun getAccount(accountID: String): Result<LoginAccount>
}