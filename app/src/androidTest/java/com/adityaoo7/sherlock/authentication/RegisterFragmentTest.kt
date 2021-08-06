package com.adityaoo7.sherlock.authentication

import android.os.Bundle
import androidx.fragment.app.testing.launchFragmentInContainer
import androidx.navigation.NavController
import androidx.navigation.Navigation
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.IdlingRegistry
import androidx.test.espresso.action.ViewActions.*
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.*
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.MediumTest
import com.adityaoo7.sherlock.R
import com.adityaoo7.sherlock.ServiceLocator
import com.adityaoo7.sherlock.data.LoginAccount
import com.adityaoo7.sherlock.data.Result
import com.adityaoo7.sherlock.data.source.shared.FakeSharedPreferencesManagerAndroid
import com.adityaoo7.sherlock.data.succeeded
import com.adityaoo7.sherlock.services.FakeEncryptionServiceAndroid
import com.adityaoo7.sherlock.util.EspressoIdlingResource
import com.adityaoo7.sherlock.util.MainCoroutineRuleAndroid
import com.adityaoo7.sherlock.util.VerificationAccount
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runBlockingTest
import org.hamcrest.Matchers.*
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mockito.mock
import org.mockito.Mockito.verify

@RunWith(AndroidJUnit4::class)
@MediumTest
@ExperimentalCoroutinesApi
class RegisterFragmentTest {

    private lateinit var sharedPreferencesManager: FakeSharedPreferencesManagerAndroid
    private lateinit var encryptionService: FakeEncryptionServiceAndroid

    @ExperimentalCoroutinesApi
    @get:Rule
    var mainCoroutineRuleAndroid = MainCoroutineRuleAndroid()

    @Before
    fun setUp() {
        sharedPreferencesManager = FakeSharedPreferencesManagerAndroid()
        encryptionService = FakeEncryptionServiceAndroid

        sharedPreferencesManager.init(false, LoginAccount(), "")

        ServiceLocator.sharedPreferencesManager = sharedPreferencesManager
        ServiceLocator.encryptionService = encryptionService
    }

    @After
    fun tearDown() = runBlockingTest {
        ServiceLocator.resetPreferencesManager()
        encryptionService.setShouldReturnDifferentAccount(false)
    }

    @Before
    fun registerIdlingResource() {
        IdlingRegistry.getInstance().register(EspressoIdlingResource.countingIdlingResource)
    }

    @After
    fun unregisterIdlingResource() {
        IdlingRegistry.getInstance().unregister(EspressoIdlingResource.countingIdlingResource)
    }

    @Test
    fun successfulRegistration_navigatesToLoginScreen() {
        // When :
        val scenario = launchFragmentInContainer<RegisterFragment>(Bundle(), R.style.Theme_Sherlock)
        val navController = mock(NavController::class.java)
        scenario.onFragment {
            Navigation.setViewNavController(it.view!!, navController)
        }

        // Enter valid password and confirm password and click register
        onView(withId(R.id.enter_password_edit_text)).perform(
            typeText("Password@123"),
            closeSoftKeyboard()
        )
        onView(withId(R.id.re_enter_password_edit_text)).perform(
            typeText("Password@123"),
            closeSoftKeyboard()
        )

        onView(withId(R.id.register_button)).perform(click())

        // Then :
        // check verification account is saved successfully
        val resultSavedVerificationAccount = sharedPreferencesManager.getVerificationAccount()

        assertThat(resultSavedVerificationAccount.succeeded, `is`(true))
        resultSavedVerificationAccount as Result.Success
        assertThat(resultSavedVerificationAccount.data, `is`(VerificationAccount.instance))

        // check isRegistered state is saved successfully
        val resultIsRegistered = sharedPreferencesManager.getIsRegistered()

        assertThat(resultIsRegistered.succeeded, `is`(true))
        resultIsRegistered as Result.Success
        assertThat(resultIsRegistered.data, `is`(true))

        // check login screen is displayed
        verify(navController).navigate(
            RegisterFragmentDirections.actionRegisterFragmentToLoginFragment()
        )
    }

    @Test
    fun registerWithInvalidPasswordPattern_returnsSnackbarTextWrongPasswordPattern() {
        // When :
        launchFragmentInContainer<RegisterFragment>(Bundle(), R.style.Theme_Sherlock)

        onView(withId(R.id.register_button))
            .perform(click())

        // Then :
        onView(withText(R.string.wrong_password_pattern))
            .check(matches(isDisplayed()))
    }

    @Test
    fun enterMainPasswordAndConfirmPasswordDifferent_returnsSnackbarTextPasswordNotMatch() {
        // When :
        launchFragmentInContainer<RegisterFragment>(Bundle(), R.style.Theme_Sherlock)

        onView(withId(R.id.enter_password_edit_text)).perform(
            typeText("Password@123"),
            closeSoftKeyboard()
        )
        onView(withId(R.id.re_enter_password_edit_text)).perform(
            typeText("Password@123Wrong"),
            closeSoftKeyboard()
        )

        onView(withId(R.id.register_button)).perform(click())

        // Then :
        onView(withText(R.string.password_not_match)).check(matches(isDisplayed()))
    }
}