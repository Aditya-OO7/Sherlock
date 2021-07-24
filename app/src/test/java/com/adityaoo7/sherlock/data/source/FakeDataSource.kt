package com.adityaoo7.sherlock.data.source

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.adityaoo7.sherlock.data.LoginAccount
import com.adityaoo7.sherlock.data.Result

class FakeDataSource(
    private var accounts: MutableList<LoginAccount>? = mutableListOf()
) : AccountsDataSource {

    override fun observeAccounts(): LiveData<Result<List<LoginAccount>>> {
        return MutableLiveData(Result.Success(accounts?.toList() ?: emptyList()))
    }

    override fun observeAccount(accountID: String): LiveData<Result<LoginAccount>> {
        val account = MutableLiveData<Result<LoginAccount>>()
        accounts?.forEach { acc ->
            if (acc.id == accountID) {
                account.value = Result.Success(acc)
                return account
            }
        }
        account.value = Result.Error(Exception("Account Not Found!"))
        return account
    }

    override suspend fun saveAccount(account: LoginAccount) {
        accounts?.add(account)
    }

    override suspend fun saveAccounts(accounts: List<LoginAccount>) {
        this.accounts?.addAll(accounts)
    }

    override suspend fun updateAccount(account: LoginAccount) {
        var ac = LoginAccount()
        accounts?.forEach { acc ->
            if (acc.id == account.id) {
                ac = acc
            }
        }
        accounts?.remove(ac)
        accounts?.add(account)
    }

    override suspend fun getAccount(accountID: String): Result<LoginAccount> {
        accounts?.forEach { acc ->
            if (acc.id == accountID) {
                return Result.Success(acc)
            }
        }

        return Result.Error(Exception("Account Not Found!"))
    }

    override suspend fun getAccounts(): Result<List<LoginAccount>> {
        return Result.Success(accounts ?: emptyList())
    }

    override suspend fun deleteAccount(accountID: String) {
        var ac = LoginAccount()
        accounts?.forEach { acc ->
            if (acc.id == accountID) {
                ac = acc
            }
        }
        accounts?.remove(ac)
    }

    override suspend fun deleteAccounts() {
        accounts?.clear()
    }
}