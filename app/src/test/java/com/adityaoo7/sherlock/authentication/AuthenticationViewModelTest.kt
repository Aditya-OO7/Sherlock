package com.adityaoo7.sherlock.authentication

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.adityaoo7.sherlock.R
import com.adityaoo7.sherlock.data.LoginAccount
import com.adityaoo7.sherlock.data.Result
import com.adityaoo7.sherlock.data.source.shared.FakeSharedPreferencesManager
import com.adityaoo7.sherlock.data.succeeded
import com.adityaoo7.sherlock.services.FakeEncryptionService
import com.adityaoo7.sherlock.util.VerificationAccount
import com.adityaoo7.sherlock.util.getOrAwaitValue
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.`is`
import org.hamcrest.Matchers.nullValue
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test


class AuthenticationViewModelTest {

    private lateinit var authenticationViewModel: AuthenticationViewModel
    private lateinit var sharedPreferencesManager: FakeSharedPreferencesManager
    private lateinit var encryptionService: FakeEncryptionService

    @get:Rule
    var instantTaskExecutorRule = InstantTaskExecutorRule()

    @Before
    fun setUp() {
        sharedPreferencesManager = FakeSharedPreferencesManager()
        encryptionService = FakeEncryptionService

        sharedPreferencesManager.init(false, LoginAccount(), "")

        authenticationViewModel = AuthenticationViewModel(
            sharedPreferencesManager,
            encryptionService
        )
    }

    @After
    fun tearDown() {
        encryptionService.setShouldReturnDifferentAccount(false)
    }

    @Test
    fun onInvalidPasswordPatternSubmit_snackbarTextReturnsWrongPasswordPattern() {
        // Given :
        authenticationViewModel.password.value = "Password"

        // When :
        authenticationViewModel.onPasswordSubmit()
        val result = authenticationViewModel.snackbarText.getOrAwaitValue()

        // Then :
        assertThat(result, `is`(R.string.wrong_password_pattern))
    }
    @Test
    fun onAuthenticationStateError_setsSnackbarTextAuthProcessError() {
        // Given :
        sharedPreferencesManager.setIsRegisteredShouldReturnError(true)
        authenticationViewModel.password.value = "Password@123"
        
        // When :
        authenticationViewModel.onPasswordSubmit()

        val result = authenticationViewModel.snackbarText.getOrAwaitValue()

        // Then :
        assertThat(result, `is`(R.string.auth_process_error))
    }

    @Test
    fun onRegister_storesVerificationAccount() {
        // Given :
        sharedPreferencesManager.putIsRegistered(false)
        authenticationViewModel.password.value = "Password@123"

        // When :
        authenticationViewModel.onPasswordSubmit()
        val result = sharedPreferencesManager.getVerificationAccount()

        // Then :
        assertThat(result.succeeded, `is`(true))
        result as Result.Success
        assertThat(result.data, `is`(VerificationAccount.instance))
    }

    @Test
    fun onRegister_storesAuthenticationStateREGISTERED() {
        // Given :
        sharedPreferencesManager.putIsRegistered(false)
        authenticationViewModel.password.value = "Password@123"

        // When :
        authenticationViewModel.onPasswordSubmit()
        val result = authenticationViewModel.isRegistered.getOrAwaitValue()

        // Then :
        assertThat(result, `is`(true))
    }

    @Test
    fun onRegister_setsSnackbarTextRegisterSuccess() {
        // Given :
        sharedPreferencesManager.putIsRegistered(false)
        authenticationViewModel.password.value = "Password@123"

        // When :
        authenticationViewModel.onPasswordSubmit()
        val result = authenticationViewModel.snackbarText.getOrAwaitValue()

        // Then :
        assertThat(result, `is`(R.string.register_success))
    }

    @Test
    fun onLoginAndSaltNotFound_setsSnackbarTextAuthProcessError() {
        // Given :
        sharedPreferencesManager.putIsRegistered(true)
        authenticationViewModel.password.value = "Password@123"
        sharedPreferencesManager.setSaltError(true)

        // When :
        authenticationViewModel.onPasswordSubmit()
        val result = authenticationViewModel.snackbarText.getOrAwaitValue()

        // Then :
        assertThat(result, `is`(R.string.auth_process_error))
    }

    @Test
    fun onLoginAndIncorrectPassword_setsSnackbarTextIncorrectPassword() {
        // Given :
        sharedPreferencesManager.putIsRegistered(true)
        authenticationViewModel.password.value = "Password@432"
        encryptionService.setShouldReturnDifferentAccount(true)

        // When :
        authenticationViewModel.onPasswordSubmit()
        val result = authenticationViewModel.snackbarText.getOrAwaitValue()

        // Then :
        assertThat(result, `is`(R.string.incorrect_password))
    }

    @Test
    fun onLogin_setsSnackbarTextAuthSuccess() {
        // Given :
        sharedPreferencesManager.putIsRegistered(true)
        authenticationViewModel.password.value = "Password@123"
        sharedPreferencesManager.putVerificationAccount(VerificationAccount.instance)

        // When :
        authenticationViewModel.onPasswordSubmit()
        val result = authenticationViewModel.snackbarText.getOrAwaitValue()

        // Then :
        assertThat(result, `is`(R.string.auth_success))
    }

    @Test
    fun onLogin_setsNavigateToHomeScreenTrue() {
        // Given :
        sharedPreferencesManager.putIsRegistered(true)
        authenticationViewModel.password.value = "Password@123"
        sharedPreferencesManager.putVerificationAccount(VerificationAccount.instance)

        // When :
        authenticationViewModel.onPasswordSubmit()
        val result = authenticationViewModel.navigateToHomeScreen.getOrAwaitValue()

        // Then :
        assertThat(result, `is`(true))
    }

    @Test
    fun onRegisterAndOnLogin_setsSnackbarTextAuthSuccess() {
        // Given :
        authenticationViewModel.password.value = "Password@123"
        sharedPreferencesManager.putIsRegistered(false)

        // When :
        authenticationViewModel.onPasswordSubmit()
        authenticationViewModel.onPasswordSubmit()
        val result = authenticationViewModel.snackbarText.getOrAwaitValue()

        // Then :
        assertThat(result, `is`(R.string.auth_success))
    }

    @Test
    fun doneShowingSnackbar_setsSnackbarTextNull() {
        // Given :
        authenticationViewModel.password.value = "Password"
        authenticationViewModel.onPasswordSubmit()

        // When :
        authenticationViewModel.doneShowingSnackbar()
        val result = authenticationViewModel.snackbarText.getOrAwaitValue()

        // Then :
        assertThat(result, `is`(nullValue()))
    }

    @Test
    fun doneNavigating_setsNavigateToHomeScreenFalse() {
        // Given :
        sharedPreferencesManager.putIsRegistered(false)
        authenticationViewModel.password.value = "Password@123"
        authenticationViewModel.onPasswordSubmit()

        // When :
        authenticationViewModel.doneNavigating()
        val result = authenticationViewModel.navigateToHomeScreen.getOrAwaitValue()

        // Then :
        assertThat(result, `is`(false))
    }
}