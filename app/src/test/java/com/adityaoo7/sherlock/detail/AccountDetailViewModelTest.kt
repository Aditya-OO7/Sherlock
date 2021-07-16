package com.adityaoo7.sherlock.detail

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.adityaoo7.sherlock.R
import com.adityaoo7.sherlock.data.LoginAccount
import com.adityaoo7.sherlock.data.repository.FakeTestRepository
import com.adityaoo7.sherlock.services.FakeEncryptionService
import com.adityaoo7.sherlock.util.MainCoroutineRule
import com.adityaoo7.sherlock.util.getOrAwaitValue
import kotlinx.coroutines.ExperimentalCoroutinesApi
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.`is`
import org.hamcrest.Matchers.notNullValue
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test


class AccountDetailViewModelTest {

    private lateinit var accountDetailViewModel: AccountDetailViewModel
    private lateinit var repository: FakeTestRepository
    private lateinit var encryptionService: FakeEncryptionService

    private lateinit var account: LoginAccount

    @get:Rule
    var instantTaskExecutorRule = InstantTaskExecutorRule()

    @ExperimentalCoroutinesApi
    @get:Rule
    var mainCoroutineRule = MainCoroutineRule()

    @Before
    fun setUp() {
        repository = FakeTestRepository()
        encryptionService = FakeEncryptionService

        account = LoginAccount(
            "Account Name",
            "user@name",
            "password",
            "https://linktoservice.com",
            "note about this account"
        )
        repository.addAccounts(account)

        accountDetailViewModel = AccountDetailViewModel(repository, encryptionService)
    }

    @After
    fun tearDown() {
        encryptionService.setShouldReturnError(false)
    }

    @Test
    fun startWithExistingAccount_loadsExistingAccount() {
        // When :
        accountDetailViewModel.start(account.id)

        val result = accountDetailViewModel.account.getOrAwaitValue()

        // Then :
        assertThat(result, `is`(notNullValue()))
        assertThat(result, `is`(account))
    }

    @Test
    fun startWithExistingAccountAndAccountNotFound_setsSnackbarTextNoAccountFound() {
        // Given :
        repository.setReturnError(true)

        // When :
        accountDetailViewModel.start(account.id)

        // subscription to account live data is needed to activate switched map
        accountDetailViewModel.account.getOrAwaitValue()

        val result = accountDetailViewModel.snackbarText.getOrAwaitValue()

        // Then :
        assertThat(result, `is`(R.string.no_account_found))
    }

    @Test
    fun startWithExistingAccountAndDecryptionError_setsSnackbarTextAccountDecryptError() {
        // Given :
        encryptionService.setShouldReturnError(true)

        // When :
        accountDetailViewModel.start(account.id)

        // subscription to account live data is needed to activate switched map
        accountDetailViewModel.account.getOrAwaitValue()

        val result = accountDetailViewModel.snackbarText.getOrAwaitValue()

        // Then :
        assertThat(result, `is`(R.string.account_decrypt_failed))
    }

    @Test
    fun editAccount_setsEditAccountTrue() {
        // When :
        accountDetailViewModel.start(account.id)

        accountDetailViewModel.editAccount()

        val result = accountDetailViewModel.editAccount.getOrAwaitValue()

        // Then :
        assertThat(result, `is`(true))
    }

    @Test
    fun deleteAccount_deletesAccountAndSetsDeleteAccountTrue() {
        // When :
        accountDetailViewModel.start(account.id)

        accountDetailViewModel.deleteThisAccount()

        val result = accountDetailViewModel.deleteAccount.getOrAwaitValue()

        // Then :
        assertThat(result, `is`(true))
        assertThat(repository.accountsServiceData, `is`(emptyMap()))
    }
}