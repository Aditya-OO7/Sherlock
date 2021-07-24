package com.adityaoo7.sherlock.home

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.adityaoo7.sherlock.util.MainCoroutineRule
import com.adityaoo7.sherlock.R
import com.adityaoo7.sherlock.data.LoginAccount
import com.adityaoo7.sherlock.data.repository.FakeTestRepository
import com.adityaoo7.sherlock.services.FakeEncryptionService
import com.adityaoo7.sherlock.util.getOrAwaitValue
import kotlinx.coroutines.ExperimentalCoroutinesApi
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.`is`
import org.hamcrest.Matchers.nullValue
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test

class HomeViewModelTest {

    private lateinit var homeViewModel: HomeViewModel
    private lateinit var accountsRepository: FakeTestRepository
    private lateinit var encryptionService: FakeEncryptionService

    private lateinit var accountsList: List<LoginAccount>

    @get:Rule
    var instantTaskExecutorRule = InstantTaskExecutorRule()

    @ExperimentalCoroutinesApi
    @get:Rule
    var mainCoroutineRule = MainCoroutineRule()

    @Before
    fun setUp() {
        accountsRepository = FakeTestRepository()

        encryptionService = FakeEncryptionService

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

        accountsRepository.addAccounts(account1, account2)

        homeViewModel = HomeViewModel(accountsRepository, encryptionService)
    }

    @After
    fun tearDown() {
        encryptionService.setShouldReturnError(false)
    }

    @Test
    fun getAllAccounts_returnAllAccounts() {
        // When :
        val result = homeViewModel.items.getOrAwaitValue()

        // Then :
        assertThat(result, `is`(accountsList))
    }

    @Test
    fun getAllAccountsAndObserveAccountsError_returnsItemsEmptyList() {
        // Given :
        accountsRepository.setReturnError(true)

        // When :
        val newHomeViewModel = HomeViewModel(accountsRepository, encryptionService)
        val result = newHomeViewModel.items.getOrAwaitValue()

        // Then :
        assertThat(result, `is`(emptyList()))
    }

    @Test
    fun getAllAccountsAndObserveAccountsError_setsSnackbarTextLoadingAccountsError() {
        // Given :
        accountsRepository.setReturnError(true)

        // When :
        val newHomeViewModel = HomeViewModel(accountsRepository, encryptionService)

        // subscription to items live data is needed to activate switched map
        newHomeViewModel.items.getOrAwaitValue()
        val result = newHomeViewModel.snackbarText.getOrAwaitValue()

        // Then :
        assertThat(result, `is`(R.string.loading_accounts_error))
    }

    @Test
    fun getAllAccountsAndDecryptionError_returnsItemsEmptyList() {
        // Given :
        val newHomeViewModel = HomeViewModel(accountsRepository, encryptionService)

        // When :
        encryptionService.setShouldReturnError(true)

        val result = newHomeViewModel.items.getOrAwaitValue()

        // Then :
        assertThat(result, `is`(emptyList()))
    }

    @Test
    fun getAllAccountsAndDecryptionError_setsSnackbarTextAccountDecryptFailed() {
        // Given :
        encryptionService.setShouldReturnError(true)

        // When :
        val newHomeViewModel = HomeViewModel(accountsRepository, encryptionService)

        // subscription to items live data is needed to activate switched map
        newHomeViewModel.items.getOrAwaitValue()
        val result = newHomeViewModel.snackbarText.getOrAwaitValue()

        // Then :
        assertThat(result, `is`(R.string.account_decrypt_failed))
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
    fun doneCreatingAccount_setsCreateNewAccountFalse() {
        // Given :
        homeViewModel.addNewAccount()

        // When :
        homeViewModel.doneCreatingNewAccount()

        val result = homeViewModel.createNewAccount.getOrAwaitValue()

        // Then :
        assertThat(result, `is`(false))
    }

    @Test
    fun doneOpeningAccount_setsOpenExistingAccountFalse() {
        // Given :
        homeViewModel.openAccount(accountsList[0].id)

        // When :
        homeViewModel.doneOpeningExistingAccount()

        val result = homeViewModel.openExistingAccount.getOrAwaitValue()

        // Then :
        assertThat(result, `is`(nullValue()))
    }

    @Test
    fun navigateToResetPassword_setsResetPasswordTrue() {
        // When :
        homeViewModel.resetPassword()
        val result = homeViewModel.resetPassword.getOrAwaitValue()

        // Then :
        assertThat(result, `is`(true))
    }

    @Test
    fun doneNavigatingResetPassword_setsResetPasswordFalse() {
        // Given :
        homeViewModel.resetPassword()

        // When :
        homeViewModel.doneNavigatingResetPassword()

        val result = homeViewModel.resetPassword.getOrAwaitValue()

        // Then :
        assertThat(result, `is`(false))
    }
}