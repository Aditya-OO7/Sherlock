package com.adityaoo7.sherlock.login

import android.content.Context
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.test.core.app.ApplicationProvider.getApplicationContext
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.MediumTest
import com.adityaoo7.sherlock.R
import com.adityaoo7.sherlock.data.LoginAccount
import com.adityaoo7.sherlock.data.Result
import com.adityaoo7.sherlock.data.source.shared.AccountPreferencesManager
import com.adityaoo7.sherlock.getOrAwaitValue
import com.adityaoo7.sherlock.services.EncryptionService
import com.adityaoo7.sherlock.services.HashingService
import com.adityaoo7.sherlock.util.VerificationAccount
import org.hamcrest.MatcherAssert
import org.hamcrest.Matchers
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
@MediumTest
class LoginViewModelTest {

    private lateinit var loginViewModel: LoginViewModel
    private lateinit var sharedPreferencesManager: AccountPreferencesManager
    private lateinit var encryptedVerificationAccount: LoginAccount

    @get:Rule
    var instantTaskExecutorRule = InstantTaskExecutorRule()

    @Before
    fun setUp() {
        val context: Context = getApplicationContext()
        sharedPreferencesManager =
            AccountPreferencesManager(context, context.getString(R.string.test_preference_file_key))

        loginViewModel = LoginViewModel(sharedPreferencesManager)

        HashingService.hashPassword("Password@123", "SomeSalt")

        val resultEncryptedAccount = EncryptionService().encryptAccount(VerificationAccount.instance)
        encryptedVerificationAccount = (resultEncryptedAccount as Result.Success).data

        sharedPreferencesManager.putVerificationAccount(encryptedVerificationAccount)
        sharedPreferencesManager.putSalt("SomeSalt")

    }

    @After
    fun tearDown() {
        sharedPreferencesManager.clearAll()
    }

    @Test
    fun onValidPasswordSubmit_navigateToHomeScreenAndPasswordValidationReturnsTrue() {
        // When :
        loginViewModel.password.value = "Password@123"
        loginViewModel.onPasswordSubmit()

        val resultNavigateToHomeScreen = loginViewModel.navigateToHomeScreen.getOrAwaitValue()
        val resultPasswordValidation = loginViewModel.passwordValidation.getOrAwaitValue()

        // Then :
        MatcherAssert.assertThat(resultPasswordValidation, Matchers.`is`(true))
        MatcherAssert.assertThat(resultNavigateToHomeScreen, Matchers.`is`(true))
    }

    @Test
    fun onInvalidPasswordSubmit_navigateToHomeScreenAndPasswordValidationReturnsFalse() {
        // When :
        loginViewModel.password.value = "SomeWrongPassword@123"
        loginViewModel.onPasswordSubmit()

        val resultNavigateToHomeScreen = loginViewModel.navigateToHomeScreen.getOrAwaitValue()
        val resultPasswordValidation = loginViewModel.passwordValidation.getOrAwaitValue()

        // Then :
        MatcherAssert.assertThat(resultPasswordValidation, Matchers.`is`(false))
        MatcherAssert.assertThat(resultNavigateToHomeScreen, Matchers.`is`(false))
    }

    @Test
    fun doneNavigating_NavigateToLoginScreenReturnsFalse() {
        // Given :
        loginViewModel.password.value = "Password@123"
        loginViewModel.onPasswordSubmit()

        // When :
        loginViewModel.doneNavigating()
        val result = loginViewModel.navigateToHomeScreen.getOrAwaitValue()

        // Then :
        MatcherAssert.assertThat(result, Matchers.`is`(false))
    }
}