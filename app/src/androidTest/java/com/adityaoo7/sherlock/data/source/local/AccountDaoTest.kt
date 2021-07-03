package com.adityaoo7.sherlock.data.source.local

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider.getApplicationContext
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.SmallTest
import com.adityaoo7.sherlock.data.LoginAccount
import com.adityaoo7.sherlock.getOrAwaitValue
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runBlockingTest
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.`is`
import org.hamcrest.Matchers.notNullValue
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@ExperimentalCoroutinesApi
@RunWith(AndroidJUnit4::class)
@SmallTest
class AccountDaoTest {

    @get:Rule
    var instantTaskExecutorRule = InstantTaskExecutorRule()

    private lateinit var database: Database
    private lateinit var account1: LoginAccount
    private lateinit var account2: LoginAccount
    private lateinit var account3: LoginAccount

    @Before
    fun initDB() {
        database = Room.inMemoryDatabaseBuilder(
            getApplicationContext(),
            Database::class.java
        ).allowMainThreadQueries().build()

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
    fun closeDB() = database.close()

    @Test
    fun insertAccountAndGetAccountById() = runBlockingTest {
        // Given :
        database.accountDao().insertAccount(account1)

        // When :
        val result = database.accountDao().getAccountById(account1.id)

        // Then :
        assertThat(result as LoginAccount, notNullValue())
        assertThat(result.id, `is`(account1.id))
        assertThat(result.name, `is`(account1.name))
        assertThat(result.userName, `is`(account1.userName))
        assertThat(result.password, `is`(account1.password))
        assertThat(result.uri, `is`(account1.uri))
        assertThat(result.note, `is`(account1.note))
    }

    @Test
    fun updateAccountAndGetAccountById() = runBlockingTest {
        // Given :
        database.accountDao().insertAccount(account1)

        // When :
        val updatedAccount = LoginAccount(
            "New Account Name",
            "changed_Username",
            "passwordChanged",
            "https://linktoserviceChanged.com",
            "Something new added to note",
            account1.id
        )
        database.accountDao().updateAccount(updatedAccount)
        val result = database.accountDao().getAccountById(account1.id)

        // Then :
        assertThat(result as LoginAccount, notNullValue())
        assertThat(result.id, `is`(updatedAccount.id))
        assertThat(result.name, `is`(updatedAccount.name))
        assertThat(result.userName, `is`(updatedAccount.userName))
        assertThat(result.password, `is`(updatedAccount.password))
        assertThat(result.uri, `is`(updatedAccount.uri))
        assertThat(result.note, `is`(updatedAccount.note))
    }

    @Test
    fun observeAccount_ReturnSingleAccount() = runBlockingTest {
        // Given :
        database.accountDao().insertAccount(account1)

        // When :
        val result = database.accountDao().observeAccountById(account1.id).getOrAwaitValue()

        // Then :
        assertThat(result, notNullValue())
        assertThat(result.id, `is`(account1.id))
        assertThat(result.name, `is`(account1.name))
        assertThat(result.userName, `is`(account1.userName))
        assertThat(result.password, `is`(account1.password))
        assertThat(result.uri, `is`(account1.uri))
        assertThat(result.note, `is`(account1.note))
    }

    @Test
    fun observeAccounts_ReturnsAllAccounts() = runBlockingTest {
        // Given :
        database.accountDao().insertAccount(account1)
        database.accountDao().insertAccount(account2)
        database.accountDao().insertAccount(account3)

        // When :
        val result = database.accountDao().observeAccounts().getOrAwaitValue()

        // Then :
        assertThat(result, `is`(listOf(account1, account2, account3)))
    }

    @Test
    fun deleteAccountAndGetEmptyListOfAccounts() = runBlockingTest {
        // Given :
        database.accountDao().insertAccount(account1)

        // When :
        database.accountDao().deleteAccountById(account1.id)
        val result = database.accountDao().observeAccounts().getOrAwaitValue()

        // Then :
        assertThat(result, `is`(emptyList()))
    }
}