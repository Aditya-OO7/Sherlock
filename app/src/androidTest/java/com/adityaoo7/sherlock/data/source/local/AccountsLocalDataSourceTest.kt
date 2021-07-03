package com.adityaoo7.sherlock.data.source.local

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider.getApplicationContext
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.MediumTest
import com.adityaoo7.sherlock.data.LoginAccount
import com.adityaoo7.sherlock.data.Result
import com.adityaoo7.sherlock.data.succeeded
import com.adityaoo7.sherlock.getOrAwaitValue
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.runBlocking
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.`is`
import org.hamcrest.Matchers.instanceOf
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@ExperimentalCoroutinesApi
@RunWith(AndroidJUnit4::class)
@MediumTest
class AccountsLocalDataSourceTest {

    private lateinit var localDataSource: AccountsLocalDataSource
    private lateinit var database: Database
    private lateinit var account1: LoginAccount
    private lateinit var account2: LoginAccount
    private lateinit var account3: LoginAccount

    @get:Rule
    var instantTaskExecutorRule = InstantTaskExecutorRule()

    @Before
    fun setUp() {
        database = Room.inMemoryDatabaseBuilder(
            getApplicationContext(),
            Database::class.java
        ).allowMainThreadQueries().build()

        localDataSource = AccountsLocalDataSource(database.accountDao(), Dispatchers.Main)
        account1 = LoginAccount(
            "Account Name",
            "user@name",
            "password",
            "https://linktoservice.com",
            "note about this account"
        )
        account2 = LoginAccount(
            "Account2",
            "user@name2",
            "password2",
            "https://linktoservice2.com",
            "note about this account2"
        )
        account3 = LoginAccount(
            "Account3",
            "user@name3",
            "password3",
            "https://linktoservice3.com",
            "note about this account3"
        )
    }

    @After
    fun cleanUp() = database.close()

    @Test
    fun saveAccount_retrieveAccount() = runBlocking {
        // Given :
        localDataSource.saveAccount(account1)

        // When :
        val result = localDataSource.getAccount(account1.id)

        // Then :
        assertThat(result.succeeded, `is`(true))
        result as Result.Success
        assertThat(result.data.id, `is`(account1.id))
        assertThat(result.data.name, `is`(account1.name))
        assertThat(result.data.userName, `is`(account1.userName))
        assertThat(result.data.password, `is`(account1.password))
        assertThat(result.data.uri, `is`(account1.uri))
        assertThat(result.data.note, `is`(account1.note))
    }

    @Test
    fun updateAccount_retrieveAccount() = runBlocking {
        // Given :
        localDataSource.saveAccount(account1)

        // When :
        val updatedAccount = LoginAccount(
            "New Account Name",
            "changed_Username",
            "passwordChanged",
            "https://linktoserviceChanged.com",
            "Something new added to note",
            account1.id
        )
        localDataSource.updateAccount(updatedAccount)
        val result = localDataSource.getAccount(account1.id)

        // Then :
        assertThat(result.succeeded, `is`(true))
        result as Result.Success
        assertThat(result.data.id, `is`(updatedAccount.id))
        assertThat(result.data.name, `is`(updatedAccount.name))
        assertThat(result.data.userName, `is`(updatedAccount.userName))
        assertThat(result.data.password, `is`(updatedAccount.password))
        assertThat(result.data.uri, `is`(updatedAccount.uri))
        assertThat(result.data.note, `is`(updatedAccount.note))
    }

    @Test
    fun saveAccount_observeAccount() = runBlocking {
        // Given :
        localDataSource.saveAccount(account1)

        // When :
        val result = localDataSource.observeAccount(account1.id).getOrAwaitValue()

        assertThat(result.succeeded, `is`(true))
        result as Result.Success
        assertThat(result.data.id, `is`(account1.id))
        assertThat(result.data.name, `is`(account1.name))
        assertThat(result.data.userName, `is`(account1.userName))
        assertThat(result.data.password, `is`(account1.password))
        assertThat(result.data.uri, `is`(account1.uri))
        assertThat(result.data.note, `is`(account1.note))
    }

    @Test
    fun saveMultipleAccounts_observeAllAccounts() = runBlocking {
        // Given :
        localDataSource.saveAccount(account1)
        localDataSource.saveAccount(account2)
        localDataSource.saveAccount(account3)

        // When :
        val result = localDataSource.observeAccounts().getOrAwaitValue()

        // Then :
        assertThat(result.succeeded, `is`(true))
        result as Result.Success
        assertThat(result.data, `is`(listOf(account1, account2, account3)))
    }

    @Test
    fun deleteAccount_ObserveAccountsReturnsEmptyListOfAccounts() = runBlocking {
        // Given :
        localDataSource.saveAccount(account1)

        // When :
        localDataSource.deleteAccount(account1.id)
        val result = localDataSource.observeAccounts().getOrAwaitValue()

        // Then :
        assertThat(result.succeeded, `is`(true))
        result as Result.Success
        assertThat(result.data, `is`(emptyList()))
    }

    @Test
    fun deleteAccountAndGetAccount_returnsAccountNotFoundError() = runBlocking {
        // Given :
        localDataSource.saveAccount(account1)

        // When :
        localDataSource.deleteAccount(account1.id)
        val result = localDataSource.getAccount(account1.id)

        // Then :
        assertThat(result, `is`(instanceOf(Result.Error::class.java)) )
    }
}