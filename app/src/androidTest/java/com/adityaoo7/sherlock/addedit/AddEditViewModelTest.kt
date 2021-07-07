package com.adityaoo7.sherlock.addedit

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.MediumTest
import com.adityaoo7.sherlock.MainCoroutineRuleAndroid
import com.adityaoo7.sherlock.R
import com.adityaoo7.sherlock.data.LoginAccount
import com.adityaoo7.sherlock.data.Result
import com.adityaoo7.sherlock.data.repository.FakeTestRepository
import com.adityaoo7.sherlock.getOrAwaitValue
import com.adityaoo7.sherlock.services.EncryptionService
import com.adityaoo7.sherlock.services.HashingService
import kotlinx.coroutines.ExperimentalCoroutinesApi
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.*
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
@MediumTest
class AddEditViewModelTest {

    private lateinit var addEditViewModel: AddEditViewModel
    private lateinit var accountsRepository: FakeTestRepository

    private lateinit var account1: LoginAccount
    private lateinit var account2: LoginAccount

    @get:Rule
    var instantTaskExecutorRule = InstantTaskExecutorRule()

    @ExperimentalCoroutinesApi
    @get:Rule
    var mainCoroutineRule = MainCoroutineRuleAndroid()

    @Before
    fun setUp() {
        HashingService.hashPassword("Password@123", "SomeSalt")

        accountsRepository = FakeTestRepository()

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

        val resultAccount1 = EncryptionService().encryptAccount(account1)
        val resultAccount2 = EncryptionService().encryptAccount(account2)

        val encryptedAccount1 = (resultAccount1 as Result.Success).data
        val encryptedAccount2 = (resultAccount2 as Result.Success).data

        accountsRepository.addAccounts(encryptedAccount1, encryptedAccount2)

        addEditViewModel = AddEditViewModel(accountsRepository)
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
    fun saveNewAccount_storesAccount() {
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
        val resultNavigateToHomeScreen = addEditViewModel.navigateToHomeScreen.getOrAwaitValue()
        val resultSnackbarText = addEditViewModel.snackbarText.getOrAwaitValue()

        // Then :
        assertThat(lastCount, `is`(greaterThan(initialCount)))
        assertThat(resultNavigateToHomeScreen, `is`(true))
        assertThat(resultSnackbarText, `is`(R.string.save_success))
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
        val resultNavigateToHomeScreen = addEditViewModel.navigateToHomeScreen.getOrAwaitValue()
        val resultSnackbarText = addEditViewModel.snackbarText.getOrAwaitValue()

        val resultDecryptedSavedAccount =
            EncryptionService().decryptAccount(accountsRepository.accountsServiceData[account2.id]!!)
        val resultSavedAccount = (resultDecryptedSavedAccount as Result.Success).data

        // Then :
        assertThat(resultSavedAccount.id, `is`(updatedAccount.id))
        assertThat(resultSavedAccount.name, `is`(updatedAccount.name))
        assertThat(resultSavedAccount.userName, `is`(updatedAccount.userName))
        assertThat(resultSavedAccount.password, `is`(updatedAccount.password))
        assertThat(resultSavedAccount.uri, `is`(updatedAccount.uri))
        assertThat(resultSavedAccount.note, `is`(updatedAccount.note))

        assertThat(lastCount, `is`(equalTo(initialCount)))
        assertThat(resultNavigateToHomeScreen, `is`(true))
        assertThat(resultSnackbarText, `is`(R.string.save_success))
    }

    @Test
    fun saveAccountWithNullData_setSnackBarTextEventEmptyMessage() {
        // When :
        addEditViewModel.start(null)
        addEditViewModel.saveAccount()

        val resultSnackbarText = addEditViewModel.snackbarText.getOrAwaitValue()

        // Then :
        assertThat(resultSnackbarText, `is`(R.string.empty_account))
    }

    @Test
    fun doneShowingSnackbar_snackbarTextReturnsNull() {
        // Given :
        addEditViewModel.start(null)
        addEditViewModel.saveAccount()

        // When :
        addEditViewModel.doneShowingSnackbar()

        val resultSnackbarText = addEditViewModel.snackbarText.getOrAwaitValue()

        // Then :
        assertThat(resultSnackbarText, `is`(nullValue()))
    }

    @Test
    fun doneNavigating_navigateToHomeScreenReturnsFalse() {
        // Given :
        addEditViewModel.start(null)
        addEditViewModel.saveAccount()

        // When :
        addEditViewModel.doneNavigating()

        val resultNavigateToHomeScreen = addEditViewModel.navigateToHomeScreen.getOrAwaitValue()

        // Then :
        assertThat(resultNavigateToHomeScreen, `is`(false))
    }
}