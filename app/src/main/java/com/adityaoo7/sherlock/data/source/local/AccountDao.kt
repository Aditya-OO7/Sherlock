package com.adityaoo7.sherlock.data.source.local

import androidx.lifecycle.LiveData
import androidx.room.*
import com.adityaoo7.sherlock.data.LoginAccount

@Dao
interface AccountDao {

    @Query("SELECT * FROM accounts")
    fun observeAccounts(): LiveData<List<LoginAccount>>

    @Query("SELECT * FROM accounts WHERE accountID = :accountID")
    fun observeAccountById(accountID: String): LiveData<LoginAccount>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAccount(account: LoginAccount)

    @Update
    suspend fun updateAccount(account: LoginAccount): Int

    @Query("SELECT * FROM accounts WHERE accountID = :accountID")
    suspend fun getAccountById(accountID: String): LoginAccount?

    @Query("DELETE FROM accounts WHERE accountID = :accountID")
    suspend fun deleteAccountById(accountID: String): Int
}