package com.adityaoo7.sherlock

import android.app.Application
import com.adityaoo7.sherlock.data.repository.AccountsRepository
import com.adityaoo7.sherlock.data.source.SharedPreferencesManager
import com.adityaoo7.sherlock.services.EncryptionService

class SherlockApplication : Application() {

    val accountsRepository: AccountsRepository
        get() = ServiceLocator.provideAccountsRepository(this)

    val sharedPreferencesManager: SharedPreferencesManager
        get() = ServiceLocator.provideSharedPreferencesManager(this)

    val encryptionService: EncryptionService
        get() = ServiceLocator.provideEncryptionService()

}