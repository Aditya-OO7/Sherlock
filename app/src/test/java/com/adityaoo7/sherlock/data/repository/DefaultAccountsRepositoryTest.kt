package com.adityaoo7.sherlock.data.repository

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.adityaoo7.sherlock.MainCoroutineRule
import com.adityaoo7.sherlock.data.LoginAccount
import com.adityaoo7.sherlock.data.Result
import com.adityaoo7.sherlock.data.getOrAwaitValue
import com.adityaoo7.sherlock.data.source.FakeDataSource
import com.adityaoo7.sherlock.data.succeeded
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runBlockingTest
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.`is`
import org.hamcrest.Matchers.instanceOf
import org.junit.Before
import org.junit.Rule
import org.junit.Test

@ExperimentalCoroutinesApi
class DefaultAccountsRepositoryTest {

    private lateinit var localAccountsDataSource: FakeDataSource
    private lateinit var account1:LoginAccount
    private lateinit var account2:LoginAccount
    private lateinit var account3:LoginAccount

    private lateinit var accountsRepository: DefaultAccountsRepository

    @ExperimentalCoroutinesApi
    @get:Rule
    var mainCoroutineRule = MainCoroutineRule()

    @get:Rule
    var instantTaskExecutorRule = InstantTaskExecutorRule()

    @Before
    fun createRepository() {
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
        localAccountsDataSource = FakeDataSource(mutableListOf(account1, account2))
        accountsRepository = DefaultAccountsRepository(
            localAccountsDataSource,
            Dispatchers.Main
        )
    }

    @Test
    fun observeAccounts_returnsAllAccounts() = mainCoroutineRule.runBlockingTest {
        val result = accountsRepository.observeAccounts().getOrAwaitValue()

        assertThat(result.succeeded, `is`(true))
        result as Result.Success
        assertThat(result.data, `is`(listOf(account1, account2)))
    }

    @Test
    fun observeAccount_returnsAccount() = mainCoroutineRule.runBlockingTest {
        val result = accountsRepository.observeAccount(account1.id).getOrAwaitValue()

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
    fun getAccount_returnsAccount() = mainCoroutineRule.runBlockingTest {

        val result = accountsRepository.getAccount(account1.id)

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
    fun getAccountThatIsNotPresent_returnsAccountNotFoundError() = mainCoroutineRule.runBlockingTest {

        val result = accountsRepository.getAccount(account3.id)

        assertThat(result, `is`(instanceOf(Result.Error::class.java)))
    }


    @Test
    fun saveAccountAndGetAccount_returnsSavedAccount() = mainCoroutineRule.runBlockingTest {
        accountsRepository.saveAccount(account3)
        val result = accountsRepository.getAccount(account3.id)

        assertThat(result.succeeded, `is`(true))
        result as Result.Success
        assertThat(result.data.id, `is`(account3.id))
        assertThat(result.data.name, `is`(account3.name))
        assertThat(result.data.userName, `is`(account3.userName))
        assertThat(result.data.password, `is`(account3.password))
        assertThat(result.data.uri, `is`(account3.uri))
        assertThat(result.data.note, `is`(account3.note))
    }

    @Test
    fun updateAccountAndGetAccount_returnsUpdatedAccount() = mainCoroutineRule.runBlockingTest {
        val updatedAccount = LoginAccount(
            "New Account Name",
            "changed_Username",
            "passwordChanged",
            "https://linktoserviceChanged.com",
            "Something new added to note",
            account1.id
        )
        accountsRepository.updateAccount(updatedAccount)

        val result = accountsRepository.getAccount(account1.id)

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
    fun deleteAccountAndGetAccount_returnsAccountNotFoundError() = mainCoroutineRule.runBlockingTest {

        accountsRepository.deleteAccount(account2.id)
        val result = accountsRepository.getAccount(account2.id)

        assertThat(result, `is`(instanceOf(Result.Error::class.java)))
    }
}