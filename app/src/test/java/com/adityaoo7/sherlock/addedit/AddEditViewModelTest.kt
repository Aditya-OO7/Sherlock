package com.adityaoo7.sherlock.addedit

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.adityaoo7.sherlock.util.MainCoroutineRule
import com.adityaoo7.sherlock.R
import com.adityaoo7.sherlock.data.LoginAccount
import com.adityaoo7.sherlock.data.repository.FakeTestRepository
import com.adityaoo7.sherlock.services.FakeEncryptionService
import com.adityaoo7.sherlock.util.getOrAwaitValue
import kotlinx.coroutines.ExperimentalCoroutinesApi
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.*
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test


class AddEditViewModelTest {

    private lateinit var addEditViewModel: AddEditViewModel
    private lateinit var accountsRepository: FakeTestRepository
    private lateinit var encryptionService: FakeEncryptionService

    private lateinit var account1: LoginAccount
    private lateinit var account2: LoginAccount

    @get:Rule
    var instantTaskExecutorRule = InstantTaskExecutorRule()

    @ExperimentalCoroutinesApi
    @get:Rule
    var mainCoroutineRule = MainCoroutineRule()

    @Before
    fun setUp() {

        accountsRepository = FakeTestRepository()
        encryptionService = FakeEncryptionService

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

        accountsRepository.addAccounts(account1, account2)

        addEditViewModel = AddEditViewModel(accountsRepository, encryptionService)
    }

    @After
    fun tearDown() {
        encryptionService.setShouldReturnError(false)
    }

    @Test
    fun startWithExistingAccount_loadsExistingAccount() {
        // When :
        addEditViewModel.start(account1.id)

        val resultName = addEditViewModel.name.getOrAwaitValue()
        val resultUserName = addEditViewModel.userName.getOrAwaitValue()
        val resultPassword = addEditViewModel.password.getOrAwaitValue()
        val resultUri = addEditViewModel.uri.getOrAwaitValue()
        val resultNote = addEditViewModel.note.getOrAwaitValue()

        // Then :
        assertThat(resultName, `is`(account1.name))
        assertThat(resultUserName, `is`(account1.userName))
        assertThat(resultPassword, `is`(account1.password))
        assertThat(resultUri, `is`(account1.uri))
        assertThat(resultNote, `is`(account1.note))
    }

    @Test
    fun startWithExistingAccountAndAccountNotFound_setsSnackbarTextNoAccountFound() {
        // Given :
        accountsRepository.setReturnError(true)

        // When :
        addEditViewModel.start(account1.id)

        val result = addEditViewModel.snackbarText.getOrAwaitValue()

        // Then :
        assertThat(result, `is`(R.string.no_account_found))
    }

    @Test
    fun startWithExistingAccountAndDecryptionError_setsSnackbarTextAccountDecryptFailed() {
        // Given :
        encryptionService.setShouldReturnError(true)

        // When :
        addEditViewModel.start(account1.id)

        val result = addEditViewModel.snackbarText.getOrAwaitValue()

        // Then :
        assertThat(result, `is`(R.string.account_decrypt_failed))
    }

    @Test
    fun saveNewAccount_storesAccountAndSetsNavigateToScreenTrue() {
        // When :
        addEditViewModel.start(null)
        val initialCount = accountsRepository.accountsServiceData.size

        addEditViewModel.name.value = "Account4"
        addEditViewModel.userName.value = "user@name4"
        addEditViewModel.password.value = "password4"
        addEditViewModel.uri.value = "https://linktoservice4.com"
        addEditViewModel.note.value = "note about this account4"

        addEditViewModel.saveAccount()

        val lastCount = accountsRepository.accountsServiceData.size
        val result = addEditViewModel.navigateToHomeScreen.getOrAwaitValue()

        // Then :
        assertThat(lastCount, `is`(greaterThan(initialCount)))
        assertThat(result, `is`(true))
    }


    @Test
    fun saveNewAccount_storesAccountAndSetsSnackbarTextSaveSuccess() {
        // When :
        addEditViewModel.start(null)
        val initialCount = accountsRepository.accountsServiceData.size

        addEditViewModel.name.value = "Account4"
        addEditViewModel.userName.value = "user@name4"
        addEditViewModel.password.value = "password4"
        addEditViewModel.uri.value = "https://linktoservice4.com"
        addEditViewModel.note.value = "note about this account4"

        addEditViewModel.saveAccount()

        val lastCount = accountsRepository.accountsServiceData.size
        val result = addEditViewModel.snackbarText.getOrAwaitValue()

        // Then :
        assertThat(lastCount, `is`(greaterThan(initialCount)))
        assertThat(result, `is`(R.string.save_success))
    }

    @Test
    fun saveNewAccountAndEncryptionError_setsSnackbarTextAccountEncryptionFailed() {
        // Given :
        encryptionService.setShouldReturnError(true)

        // When :
        addEditViewModel.start(null)

        addEditViewModel.name.value = "Account4"
        addEditViewModel.userName.value = "user@name4"
        addEditViewModel.password.value = "password4"
        addEditViewModel.uri.value = "https://linktoservice4.com"
        addEditViewModel.note.value = "note about this account4"

        addEditViewModel.saveAccount()

        val result = addEditViewModel.snackbarText.getOrAwaitValue()

        // Then :
        assertThat(result, `is`(R.string.account_encrypt_failed))
    }

    @Test
    fun saveUpdatedAccount_storesAccount() {
        // Given :
        val updatedAccount = LoginAccount(
            "Updated Account",
            "user@nameUpdated",
            "passwordUpdated",
            "https://linktoserviceupdated.com",
            "note about this account updated",
            account2.id
        )

        // When :
        addEditViewModel.start(account2.id)
        val initialCount = accountsRepository.accountsServiceData.size

        addEditViewModel.name.value = updatedAccount.name
        addEditViewModel.userName.value = updatedAccount.userName
        addEditViewModel.password.value = updatedAccount.password
        addEditViewModel.uri.value = updatedAccount.uri
        addEditViewModel.note.value = updatedAccount.note

        addEditViewModel.saveAccount()

        val lastCount = accountsRepository.accountsServiceData.size

        val result = accountsRepository.accountsServiceData[account2.id] ?: LoginAccount()

        // Then :
        assertThat(lastCount, `is`(equalTo(initialCount)))

        assertThat(result.id, `is`(updatedAccount.id))
        assertThat(result.name, `is`(updatedAccount.name))
        assertThat(result.userName, `is`(updatedAccount.userName))
        assertThat(result.password, `is`(updatedAccount.password))
        assertThat(result.uri, `is`(updatedAccount.uri))
        assertThat(result.note, `is`(updatedAccount.note))

    }


    @Test
    fun saveUpdatedAccount_setsNavigateToHomeScreenTrue() {
        // Given :
        val updatedAccount = LoginAccount(
            "Updated Account",
            "user@nameUpdated",
            "passwordUpdated",
            "https://linktoserviceupdated.com",
            "note about this account updated",
            account2.id
        )

        // When :
        addEditViewModel.start(account2.id)
        val initialCount = accountsRepository.accountsServiceData.size

        addEditViewModel.name.value = updatedAccount.name
        addEditViewModel.userName.value = updatedAccount.userName
        addEditViewModel.password.value = updatedAccount.password
        addEditViewModel.uri.value = updatedAccount.uri
        addEditViewModel.note.value = updatedAccount.note

        addEditViewModel.saveAccount()

        val lastCount = accountsRepository.accountsServiceData.size
        val result = addEditViewModel.navigateToHomeScreen.getOrAwaitValue()

        // Then :
        assertThat(lastCount, `is`(equalTo(initialCount)))
        assertThat(result, `is`(true))
    }


    @Test
    fun saveUpdatedAccount_setsSnackbarTextSaveSuccess() {
        // Given :
        val updatedAccount = LoginAccount(
            "Updated Account",
            "user@nameUpdated",
            "passwordUpdated",
            "https://linktoserviceupdated.com",
            "note about this account updated",
            account2.id
        )

        // When :
        addEditViewModel.start(account2.id)
        val initialCount = accountsRepository.accountsServiceData.size

        addEditViewModel.name.value = updatedAccount.name
        addEditViewModel.userName.value = updatedAccount.userName
        addEditViewModel.password.value = updatedAccount.password
        addEditViewModel.uri.value = updatedAccount.uri
        addEditViewModel.note.value = updatedAccount.note

        addEditViewModel.saveAccount()

        val lastCount = accountsRepository.accountsServiceData.size
        val result = addEditViewModel.snackbarText.getOrAwaitValue()

        // Then :
        assertThat(lastCount, `is`(equalTo(initialCount)))
        assertThat(result, `is`(R.string.save_success))
    }

    @Test
    fun saveUpdatedAccountAndEncryptionError_setsSnackbarTextAccountEncryptionError() {
        // Given :
        val updatedAccount = LoginAccount(
            "Updated Account",
            "user@nameUpdated",
            "passwordUpdated",
            "https://linktoserviceupdated.com",
            "note about this account updated",
            account2.id
        )
        encryptionService.setShouldReturnError(true)

        // When :
        addEditViewModel.start(account2.id)

        addEditViewModel.name.value = updatedAccount.name
        addEditViewModel.userName.value = updatedAccount.userName
        addEditViewModel.password.value = updatedAccount.password
        addEditViewModel.uri.value = updatedAccount.uri
        addEditViewModel.note.value = updatedAccount.note

        addEditViewModel.saveAccount()

        val result = addEditViewModel.snackbarText.getOrAwaitValue()

        // Then :
        assertThat(result, `is`(R.string.account_encrypt_failed))
    }

    @Test
    fun saveAccountWithNullData_setSnackBarTextEventEmptyMessage() {
        // When :
        addEditViewModel.start(null)
        addEditViewModel.saveAccount()

        val result = addEditViewModel.snackbarText.getOrAwaitValue()

        // Then :
        assertThat(result, `is`(R.string.empty_account))
    }

    @Test
    fun doneShowingSnackbar_snackbarTextReturnsNull() {
        // Given :
        addEditViewModel.start(null)
        addEditViewModel.saveAccount()

        // When :
        addEditViewModel.doneShowingSnackbar()

        val result = addEditViewModel.snackbarText.getOrAwaitValue()

        // Then :
        assertThat(result, `is`(nullValue()))
    }

    @Test
    fun doneNavigating_navigateToHomeScreenReturnsFalse() {
        // Given :
        addEditViewModel.start(null)
        addEditViewModel.saveAccount()

        // When :
        addEditViewModel.doneNavigating()

        val result = addEditViewModel.navigateToHomeScreen.getOrAwaitValue()

        // Then :
        assertThat(result, `is`(false))
    }
}