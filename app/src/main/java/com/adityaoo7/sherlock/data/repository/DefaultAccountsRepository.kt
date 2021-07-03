package com.adityaoo7.sherlock.data.repository

import androidx.lifecycle.LiveData
import com.adityaoo7.sherlock.data.LoginAccount
import com.adityaoo7.sherlock.data.Result
import com.adityaoo7.sherlock.data.source.AccountsDataSource
import com.adityaoo7.sherlock.util.wrapEspressoIdlingResource
import kotlinx.coroutines.*

class DefaultAccountsRepository(
    private val localAccountsDataSource: AccountsDataSource,
    private val ioDispatcher: CoroutineDispatcher = Dispatchers.IO
) : AccountsRepository {

    override fun observeAccounts(): LiveData<Result<List<LoginAccount>>> {
        wrapEspressoIdlingResource {
            return localAccountsDataSource.observeAccounts()
        }
    }

    override fun observeAccount(accountID: String): LiveData<Result<LoginAccount>> {
        wrapEspressoIdlingResource {
            return localAccountsDataSource.observeAccount(accountID)
        }
    }

    override suspend fun saveAccount(account: LoginAccount) = withContext<Unit>(ioDispatcher) {
        wrapEspressoIdlingResource {
            coroutineScope {
                launch { localAccountsDataSource.saveAccount(account) }
            }
        }
    }

    override suspend fun updateAccount(account: LoginAccount) = withContext<Unit>(ioDispatcher) {
        wrapEspressoIdlingResource {
            coroutineScope {
                launch { localAccountsDataSource.updateAccount(account) }
            }
        }
    }

    override suspend fun deleteAccount(accountID: String)  = withContext<Unit>(ioDispatcher) {
        wrapEspressoIdlingResource {
            coroutineScope {
                launch { localAccountsDataSource.deleteAccount(accountID) }
            }
        }
    }

    override suspend fun getAccount(accountID: String): Result<LoginAccount>  = withContext(ioDispatcher) {
        wrapEspressoIdlingResource {
            return@withContext localAccountsDataSource.getAccount(accountID)
        }
    }
}