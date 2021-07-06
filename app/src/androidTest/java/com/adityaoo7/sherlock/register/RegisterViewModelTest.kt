package com.adityaoo7.sherlock.register

import android.content.Context
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.test.core.app.ApplicationProvider.getApplicationContext
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.MediumTest
import com.adityaoo7.sherlock.R
import com.adityaoo7.sherlock.data.LoginAccount
import com.adityaoo7.sherlock.data.Result
import com.adityaoo7.sherlock.data.source.shared.AccountPreferencesManager
import com.adityaoo7.sherlock.data.succeeded
import com.adityaoo7.sherlock.getOrAwaitValue
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.*
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
@MediumTest
class RegisterViewModelTest {
    private lateinit var registerViewModel: RegisterViewModel
    private lateinit var sharedPreferencesManager: AccountPreferencesManager

    @get:Rule
    var instantTaskExecutorRule = InstantTaskExecutorRule()

    @Before
    fun setUp() {
        val context: Context = getApplicationContext()
        sharedPreferencesManager =
            AccountPreferencesManager(context, context.getString(R.string.test_preference_file_key))

        registerViewModel = RegisterViewModel(sharedPreferencesManager)
    }

    @After
    fun tearDown() {
        sharedPreferencesManager.clearAll()
    }

    @Test
    fun onPasswordSubmit_SavesVerificationAccountAndNavigateToLoginScreenAndPasswordValidationReturnsTrue() {
        // When :
        registerViewModel.password.value = "Password@1234"
        registerViewModel.onPasswordSubmit()

        val resultEncryptedVerificationAccount = sharedPreferencesManager.getVerificationAccount()
        val resultNavigateToLoginScreen = registerViewModel.navigateToLoginScreen.getOrAwaitValue()
        val resultPasswordValidation = registerViewModel.passwordValidation.getOrAwaitValue()

        // Then :
        // Check if stored verification account :
        // - is not null
        // - is instance of [LoginAccount]
        assertThat(resultEncryptedVerificationAccount.succeeded, `is`(true))
        resultEncryptedVerificationAccount as Result.Success
        assertThat(resultEncryptedVerificationAccount.data, `is`(instanceOf(LoginAccount::class.java)))

        assertThat(resultPasswordValidation, `is`(true))
        assertThat(resultNavigateToLoginScreen, `is`(true))
    }

    @Test
    fun onInvalidPasswordSubmit_NavigateToLoginScreenAndPasswordValidationReturnsFalse() {
        // When :
        registerViewModel.password.value = "Password"
        registerViewModel.onPasswordSubmit()

        val resultPasswordValidation = registerViewModel.passwordValidation.getOrAwaitValue()
        val resultNavigateToLoginScreen = registerViewModel.navigateToLoginScreen.getOrAwaitValue()

        // Then :
        assertThat(resultPasswordValidation, `is`(false))
        assertThat(resultNavigateToLoginScreen, `is`(false))
    }

    @Test
    fun doneNavigatingShouldMakeNavigateToLoginScreenFalse_returnsFalse() {
        // Given :
        registerViewModel.password.value = "Password"
        registerViewModel.onPasswordSubmit()

        // When :
        registerViewModel.doneNavigating()
        val result = registerViewModel.navigateToLoginScreen.getOrAwaitValue()

        // Then :
        assertThat(result, `is`(false))
    }
}