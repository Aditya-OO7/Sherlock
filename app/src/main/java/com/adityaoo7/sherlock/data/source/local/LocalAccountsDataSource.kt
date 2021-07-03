package com.adityaoo7.sherlock.data.source.local

import androidx.lifecycle.LiveData
import androidx.lifecycle.map
import com.adityaoo7.sherlock.data.LoginAccount
import com.adityaoo7.sherlock.data.Result
import com.adityaoo7.sherlock.data.source.AccountsDataSource
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class AccountsLocalDataSource internal constructor(
    private val accountDao: AccountDao,
    private val ioDispatcher: CoroutineDispatcher = Dispatchers.IO
) : AccountsDataSource {
    override fun observeAccounts(): LiveData<Result<List<LoginAccount>>> {
        return accountDao.observeAccounts().map {
            Result.Success(it)
        }
    }

    override fun observeAccount(accountID: String): LiveData<Result<LoginAccount>> {
        return accountDao.observeAccountById(accountID).map {
            Result.Success(it)
        }
    }

    override suspend fun saveAccount(account: LoginAccount) = withContext(ioDispatcher) {
        accountDao.insertAccount(account)
    }

    override suspend fun updateAccount(account: LoginAccount) = withContext<Unit>(ioDispatcher) {
        accountDao.updateAccount(account)
    }

    override suspend fun getAccount(accountID: String): Result<LoginAccount> =
        withContext(ioDispatcher) {
            try {
                val account = accountDao.getAccountById(accountID)
                if (account != null) {
                    return@withContext Result.Success(account)
                } else {
                    return@withContext Result.Error(Exception("Account not found!"))
                }
            } catch (e: Exception) {
                return@withContext Result.Error(e)
            }
        }

    override suspend fun deleteAccount(accountID: String) = withContext<Unit>(ioDispatcher) {
        accountDao.deleteAccountById(accountID)
    }
}