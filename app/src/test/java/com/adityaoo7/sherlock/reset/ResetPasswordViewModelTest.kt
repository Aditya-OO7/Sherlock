package com.adityaoo7.sherlock.reset

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.adityaoo7.sherlock.R
import com.adityaoo7.sherlock.data.LoginAccount
import com.adityaoo7.sherlock.data.repository.FakeTestRepository
import com.adityaoo7.sherlock.data.source.shared.FakeSharedPreferencesManager
import com.adityaoo7.sherlock.services.FakeEncryptionService
import com.adityaoo7.sherlock.util.MainCoroutineRule
import com.adityaoo7.sherlock.util.VerificationAccount
import com.adityaoo7.sherlock.util.getOrAwaitValue
import kotlinx.coroutines.ExperimentalCoroutinesApi
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.`is`
import org.hamcrest.Matchers.nullValue
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test

@ExperimentalCoroutinesApi
class ResetPasswordViewModelTest {

    private lateinit var resetPasswordViewModel: ResetPasswordViewModel
    private lateinit var repository: FakeTestRepository
    private lateinit var sharedPreferencesManager: FakeSharedPreferencesManager
    private lateinit var encryptionService: FakeEncryptionService

    @get:Rule
    var instantTaskExecutorRule = InstantTaskExecutorRule()

    @ExperimentalCoroutinesApi
    @get:Rule
    var mainCoroutineRule = MainCoroutineRule()

    @Before
    fun setUp() {
        repository = FakeTestRepository()
        sharedPreferencesManager = FakeSharedPreferencesManager()
        encryptionService = FakeEncryptionService

        sharedPreferencesManager.init(false, LoginAccount(), "")

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

        repository.addAccounts(account1, account2)

        resetPasswordViewModel =
            ResetPasswordViewModel(repository, encryptionService, sharedPreferencesManager)
    }

    @After
    fun tearDown() {
        encryptionService.setShouldReturnError(false)
        encryptionService.setShouldReturnDifferentAccount(false)
    }

    @Test
    fun resetPassword_setsNavigateToHomeScreenTrue() {
        // Given :
        sharedPreferencesManager.putSalt("Some Salt")
        sharedPreferencesManager.putVerificationAccount(VerificationAccount.instance)

        // When :
        resetPasswordViewModel.oldPassword.value = "Password@old123"
        resetPasswordViewModel.newPassword.value = "Password@new123"
        resetPasswordViewModel.confirmPassword.value = "Password@new123"

        resetPasswordViewModel.onPasswordSubmit()

        val result = resetPasswordViewModel.navigateToHomeScreen.getOrAwaitValue()

        // Then :
        assertThat(result, `is`(true))
    }

    @Test
    fun onPasswordSubmitAndWrongPasswordPattern_setsSnackbarTextWrongPasswordPattern() {
        // When :
        resetPasswordViewModel.oldPassword.value = "SomeWeakPassword"

        resetPasswordViewModel.onPasswordSubmit()
        val result = resetPasswordViewModel.snackbarText.getOrAwaitValue()

        // Then :
        assertThat(result, `is`(R.string.wrong_password_pattern))
    }

    @Test
    fun processingResetPassword_setsDataLoadingTrue() {
        // Given :
        sharedPreferencesManager.putSalt("Some Salt")
        sharedPreferencesManager.putVerificationAccount(VerificationAccount.instance)

        // When :
        resetPasswordViewModel.oldPassword.value = "Password@old123"
        resetPasswordViewModel.newPassword.value = "Password@new123"
        resetPasswordViewModel.confirmPassword.value = "Password@new123"

        mainCoroutineRule.pauseDispatcher()

        resetPasswordViewModel.onPasswordSubmit()

        // Then :
        assertThat(resetPasswordViewModel.dataLoading.getOrAwaitValue(), `is`(true))

        mainCoroutineRule.resumeDispatcher()

        assertThat(resetPasswordViewModel.dataLoading.getOrAwaitValue(), `is`(false))
    }

    @Test
    fun saltNotFound_setsSnackbarTextAuthProcessError() {
        // Given :
        sharedPreferencesManager.putSalt("Some Salt")
        sharedPreferencesManager.putVerificationAccount(VerificationAccount.instance)
        sharedPreferencesManager.setSaltError(true)

        // When :
        resetPasswordViewModel.oldPassword.value = "Password@old123"
        resetPasswordViewModel.newPassword.value = "Password@new123"
        resetPasswordViewModel.confirmPassword.value = "Password@new123"

        resetPasswordViewModel.onPasswordSubmit()

        val result = resetPasswordViewModel.snackbarText.getOrAwaitValue()

        // Then :
        assertThat(result, `is`(R.string.auth_process_error))
    }

    @Test
    fun incorrectPassword_setsSnackbarTextIncorrectPassword() {
        // Given :
        sharedPreferencesManager.putSalt("Some Salt")
        sharedPreferencesManager.putVerificationAccount(VerificationAccount.instance)
        encryptionService.setShouldReturnDifferentAccount(true)

        // When :
        resetPasswordViewModel.oldPassword.value = "Password@old123"
        resetPasswordViewModel.newPassword.value = "Password@new123"
        resetPasswordViewModel.confirmPassword.value = "Password@new123"

        resetPasswordViewModel.onPasswordSubmit()

        val result = resetPasswordViewModel.snackbarText.getOrAwaitValue()

        // Then :
        assertThat(result, `is`(R.string.incorrect_password))
    }

    @Test
    fun noAccountsFoundFromRepositoryToRetrieve_setsSnackbarTextNoAccountFound() {
        // Given :
        sharedPreferencesManager.putSalt("Some Salt")
        sharedPreferencesManager.putVerificationAccount(VerificationAccount.instance)
        repository.setReturnError(true)

        // When :
        resetPasswordViewModel.oldPassword.value = "Password@old123"
        resetPasswordViewModel.newPassword.value = "Password@new123"
        resetPasswordViewModel.confirmPassword.value = "Password@new123"

        resetPasswordViewModel.onPasswordSubmit()

        val result = resetPasswordViewModel.snackbarText.getOrAwaitValue()

        // Then :
        assertThat(result, `is`(R.string.no_account_found))
    }

    @Test
    fun registerWithWrongPasswordPattern_setsSnackbarTextWrongPasswordPattern() {
        // Given :
        sharedPreferencesManager.putSalt("Some Salt")
        sharedPreferencesManager.putVerificationAccount(VerificationAccount.instance)

        // When :
        resetPasswordViewModel.oldPassword.value = "Password@old123"
        resetPasswordViewModel.newPassword.value = "Password"
        resetPasswordViewModel.confirmPassword.value = "Password"

        resetPasswordViewModel.onPasswordSubmit()

        val result = resetPasswordViewModel.snackbarText.getOrAwaitValue()

        // Then :
        assertThat(result, `is`(R.string.wrong_password_pattern))
    }

    @Test
    fun registerWithWrongConfirmPassword_setsSnackbarTextPasswordNotMatch() {
        // Given :
        sharedPreferencesManager.putSalt("Some Salt")
        sharedPreferencesManager.putVerificationAccount(VerificationAccount.instance)

        // When :
        resetPasswordViewModel.oldPassword.value = "Password@old123"
        resetPasswordViewModel.newPassword.value = "Password@new123"
        resetPasswordViewModel.confirmPassword.value = "Password@123"

        resetPasswordViewModel.onPasswordSubmit()

        val result = resetPasswordViewModel.snackbarText.getOrAwaitValue()

        // Then :
        assertThat(result, `is`(R.string.password_not_match))
    }

    @Test
    fun doneNavigating() {
        // Given :
        sharedPreferencesManager.putSalt("Some Salt")
        sharedPreferencesManager.putVerificationAccount(VerificationAccount.instance)

        // When :
        resetPasswordViewModel.oldPassword.value = "Password@old123"
        resetPasswordViewModel.newPassword.value = "Password@new123"
        resetPasswordViewModel.confirmPassword.value = "Password@new123"

        resetPasswordViewModel.onPasswordSubmit()

        resetPasswordViewModel.doneNavigating()
        val result = resetPasswordViewModel.navigateToHomeScreen.getOrAwaitValue()

        // Then :
        assertThat(result, `is`(false))
    }

    @Test
    fun doneShowingSnackbar() {
        // When :
        resetPasswordViewModel.oldPassword.value = "WrongPasswordPattern"

        resetPasswordViewModel.onPasswordSubmit()

        resetPasswordViewModel.doneShowingSnackbar()
        val result = resetPasswordViewModel.snackbarText.getOrAwaitValue()

        // Then :
        assertThat(result, `is`(nullValue()))
    }
}