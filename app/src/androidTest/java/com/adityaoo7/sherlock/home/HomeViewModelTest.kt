package com.adityaoo7.sherlock.home

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.adityaoo7.sherlock.MainCoroutineRuleAndroid
import com.adityaoo7.sherlock.data.LoginAccount
import com.adityaoo7.sherlock.data.Result
import com.adityaoo7.sherlock.data.repository.FakeTestRepository
import com.adityaoo7.sherlock.getOrAwaitValue
import com.adityaoo7.sherlock.services.EncryptionService
import com.adityaoo7.sherlock.services.HashingService
import kotlinx.coroutines.ExperimentalCoroutinesApi
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.`is`
import org.hamcrest.Matchers.nullValue
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith


@RunWith(AndroidJUnit4::class)
class HomeViewModelTest {

    private lateinit var homeViewModel: HomeViewModel
    private lateinit var accountsRepository: FakeTestRepository

    private lateinit var accountsList: List<LoginAccount>

    @get:Rule
    var instantTaskExecutorRule = InstantTaskExecutorRule()

    @ExperimentalCoroutinesApi
    @get:Rule
    var mainCoroutineRule = MainCoroutineRuleAndroid()

    @Before
    fun setUp() {

        HashingService.hashPassword("Passowrd@123", "SomeSalt")

        accountsRepository = FakeTestRepository()

        val account1 = LoginAccount(
            "Account Name",
            "user@name",
            "password",
            "https://linktoservice.com",
            "note about this account"
        )
        val account2 = LoginAccount(
            "Account2",
            "user@name2",
            "password2",
            "https://linktoservice2.com",
            "note about this account2"
        )

        accountsList = listOf(account1, account2)

        val resultAccount1 = EncryptionService().encryptAccount(account1)
        val resultAccount2 = EncryptionService().encryptAccount(account2)

        val encryptedAccount1 = (resultAccount1 as Result.Success).data
        val encryptedAccount2 = (resultAccount2 as Result.Success).data

        accountsRepository.addAccounts(encryptedAccount1, encryptedAccount2)

        homeViewModel = HomeViewModel(accountsRepository)
    }

    @Test
    fun getAllAccounts_returnsAllAccounts() {
        // When :
        val result = homeViewModel.items.getOrAwaitValue()

        // Then :
        assertThat(result, `is`(accountsList))
    }

    @Test
    fun addNewAccount_setCreateNewAccountTrue() {
        // When :
        homeViewModel.addNewAccount()

        val result = homeViewModel.createNewAccount.getOrAwaitValue()

        // Then :
        assertThat(result, `is`(true))
    }

    @Test
    fun openAccount_returnsAccountId() {
        // When :
        homeViewModel.openAccount(accountsList[0].id)

        val result = homeViewModel.openExistingAccount.getOrAwaitValue()

        // Then :
        assertThat(result, `is`(accountsList[0].id))
    }

    @Test
    fun doneCreatingAccount_setCreateNewAccountFalse() {
        // Given :
        homeViewModel.addNewAccount()

        // When :
        homeViewModel.doneCreatingNewAccount()

        val result = homeViewModel.createNewAccount.getOrAwaitValue()

        // Then :
        assertThat(result, `is`(false))
    }

    @Test
    fun doneOpeningAccount_setOpenExistingAccountFalse() {
        // Given :
        homeViewModel.openAccount(accountsList[0].id)

        // When :
        homeViewModel.doneOpeningExistingAccount()

        val result = homeViewModel.openExistingAccount.getOrAwaitValue()

        // Then :
        assertThat(result, `is`(nullValue()))
    }
}