package com.adityaoo7.sherlock

import android.content.Context
import androidx.annotation.VisibleForTesting
import androidx.room.Room
import com.adityaoo7.sherlock.data.LoginAccount
import com.adityaoo7.sherlock.data.repository.AccountsRepository
import com.adityaoo7.sherlock.data.repository.DefaultAccountsRepository
import com.adityaoo7.sherlock.data.source.AccountsDataSource
import com.adityaoo7.sherlock.data.source.SharedPreferencesManager
import com.adityaoo7.sherlock.data.source.local.AccountsLocalDataSource
import com.adityaoo7.sherlock.data.source.local.Database
import com.adityaoo7.sherlock.data.source.shared.AccountPreferencesManager
import com.adityaoo7.sherlock.services.AccountEncryptionService
import com.adityaoo7.sherlock.services.EncryptionService

object ServiceLocator {

    private var database: Database? = null

    @Volatile
    var accountsRepository: AccountsRepository? = null
        @VisibleForTesting set

    @Volatile
    var sharedPreferencesManager: SharedPreferencesManager? = null
        @VisibleForTesting set

    @Volatile
    var encryptionService: EncryptionService? = null
        @VisibleForTesting set

    private val lock = Any()

    @VisibleForTesting
    fun resetRepository() {
        synchronized(lock) {
            database?.apply {
                clearAllTables()
                close()
            }
            database = null
            accountsRepository = null
        }
    }

    @VisibleForTesting
    fun resetPreferencesManager() {
        synchronized(lock) {
            sharedPreferencesManager?.apply {
                putIsRegistered(false)
                putVerificationAccount(LoginAccount())
                putSalt("")
            }
            sharedPreferencesManager = null
        }
    }

    fun provideAccountsRepository(context: Context): AccountsRepository {
        synchronized(this) {
            return accountsRepository ?: createAccountsRepository(context)
        }
    }

    private fun createAccountsRepository(context: Context): AccountsRepository {
        val newRepository = DefaultAccountsRepository(createLocalAccountsDataSource(context))
        accountsRepository = newRepository

        return newRepository
    }

    private fun createLocalAccountsDataSource(context: Context): AccountsDataSource {
        val database = database ?: createDatabase(context)
        return AccountsLocalDataSource(database.accountDao())
    }

    private fun createDatabase(context: Context): Database {
        val result = Room.databaseBuilder(
            context.applicationContext,
            Database::class.java, "Accounts.db"
        ).build()
        database = result
        return result
    }

    fun provideSharedPreferencesManager(context: Context): SharedPreferencesManager {
        synchronized(this) {
            return sharedPreferencesManager ?: createSharedPreferencesManager(context)
        }
    }

    private fun createSharedPreferencesManager(context: Context): SharedPreferencesManager {
        val newPreferencesManager = AccountPreferencesManager(
            context.applicationContext,
            context.getString(R.string.preference_file_key)
        )

        sharedPreferencesManager = newPreferencesManager

        return newPreferencesManager
    }

    fun provideEncryptionService(): EncryptionService {
        synchronized(this) {
            return encryptionService ?: AccountEncryptionService.also {
                encryptionService = it
            }
        }
    }
}