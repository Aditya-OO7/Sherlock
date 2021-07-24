package com.adityaoo7.sherlock.authentication

import android.os.Bundle
import androidx.fragment.app.testing.launchFragmentInContainer
import androidx.navigation.NavController
import androidx.navigation.Navigation
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.IdlingRegistry
import androidx.test.espresso.action.ViewActions.*
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.intent.Intents
import androidx.test.espresso.intent.Intents.intended
import androidx.test.espresso.intent.matcher.IntentMatchers.hasComponent
import androidx.test.espresso.matcher.ViewMatchers.*
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.MediumTest
import com.adityaoo7.sherlock.MainActivity
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

@RunWith(AndroidJUnit4::class)
@MediumTest
@ExperimentalCoroutinesApi
class AuthenticationFragmentTest {

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
    fun isNotRegistered_setsRegisterScreen() {
        // When :
        launchFragmentInContainer<AuthenticationFragment>(Bundle(), R.style.Theme_Sherlock)

        // Then :
        onView(
            allOf(
                withId(R.id.heading_text_view),
                withText(R.string.register_heading_text_view)
            )
        ).check(matches(isDisplayed()))

        onView(withId(R.id.re_enter_password_edit_text)).check(
            matches(
                withEffectiveVisibility(
                    Visibility.VISIBLE
                )
            )
        )

        onView(
            allOf(
                withId(R.id.submit_button),
                withText(R.string.register_button_text)
            )
        ).check(matches(isDisplayed()))

    }

    @Test
    fun isRegistered_setsLoginScreen() {
        // Given :
        sharedPreferencesManager.putIsRegistered(true)

        // When :
        launchFragmentInContainer<AuthenticationFragment>(Bundle(), R.style.Theme_Sherlock)

        // Then :
        onView(
            allOf(
                withId(R.id.heading_text_view),
                withText(R.string.login_heading_text_view)
            )
        ).check(matches(isDisplayed()))

        onView(withId(R.id.re_enter_password_edit_text)).check(
            matches(
                withEffectiveVisibility(
                    Visibility.GONE
                )
            )
        )

        onView(
            allOf(
                withId(R.id.submit_button),
                withText(R.string.login_button_text)
            )
        ).check(matches(isDisplayed()))

    }

    @Test
    fun clickRegisterWithEmptyPasswordFields_returnsSnackbarTextWrongPasswordPattern() {
        // When :
        launchFragmentInContainer<AuthenticationFragment>(Bundle(), R.style.Theme_Sherlock)

        onView(withId(R.id.submit_button))
            .perform(click())

        // Then :
        onView(withText(R.string.wrong_password_pattern))
            .check(matches(isDisplayed()))
    }

    @Test
    fun incorrectConfirmPasswordSubmit_returnsSnackbarTextPasswordNotMatch() {
        // When :
        launchFragmentInContainer<AuthenticationFragment>(Bundle(), R.style.Theme_Sherlock)

        onView(withId(R.id.enter_password_edit_text)).perform(
            typeText("Password@123"),
            closeSoftKeyboard()
        )
        onView(withId(R.id.re_enter_password_edit_text)).perform(
            typeText("Password@123Wrong"),
            closeSoftKeyboard()
        )

        onView(withId(R.id.submit_button)).perform(click())

        // Then :
        onView(withText(R.string.password_not_match)).check(matches(isDisplayed()))
    }

    @Test
    fun successfulRegistration_savesVerificationAccountAndSetsLoginScreen() {
        // When :
        launchFragmentInContainer<AuthenticationFragment>(Bundle(), R.style.Theme_Sherlock)

        // Enter valid password and confirm password and click register
        onView(withId(R.id.enter_password_edit_text)).perform(
            typeText("Password@123"),
            closeSoftKeyboard()
        )
        onView(withId(R.id.re_enter_password_edit_text)).perform(
            typeText("Password@123"),
            closeSoftKeyboard()
        )

        onView(withId(R.id.submit_button)).perform(click())

        // Then :
        onView(withText(R.string.register_success)).check(matches(isDisplayed()))

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
        onView(
            allOf(
                withId(R.id.heading_text_view),
                withText(R.string.login_heading_text_view)
            )
        ).check(matches(isDisplayed()))

        onView(withId(R.id.re_enter_password_edit_text)).check(
            matches(
                withEffectiveVisibility(
                    Visibility.GONE
                )
            )
        )

        onView(
            allOf(
                withId(R.id.submit_button),
                withText(R.string.login_button_text)
            )
        ).check(matches(isDisplayed()))
    }

    @Test
    fun hashPassword_loading() {
        // Given :
        mainCoroutineRuleAndroid.pauseDispatcher()

        // When :
        launchFragmentInContainer<AuthenticationFragment>(Bundle(), R.style.Theme_Sherlock)

        // Enter valid password and confirm password and click register
        onView(withId(R.id.enter_password_edit_text)).perform(
            typeText("Password@123"),
            closeSoftKeyboard()
        )
        onView(withId(R.id.re_enter_password_edit_text)).perform(
            typeText("Password@123"),
            closeSoftKeyboard()
        )

        onView(withId(R.id.submit_button)).perform(click())

        // Then :
        onView(withId(R.id.auth_progress_bar)).check(matches(isDisplayed()))

        mainCoroutineRuleAndroid.resumeDispatcher()

        onView(withId(R.id.auth_progress_bar)).check(matches(not(isDisplayed())))
    }

    @Test
    fun clickLoginAndSaltNotFound_returnsSnackbarTextAuthProcessError() {
        // Given :
        sharedPreferencesManager.putIsRegistered(true)
        sharedPreferencesManager.setSaltError(true)

        // When :
        launchFragmentInContainer<AuthenticationFragment>(Bundle(), R.style.Theme_Sherlock)

        onView(withId(R.id.enter_password_edit_text)).perform(
            typeText("Password@123"),
            closeSoftKeyboard()
        )
        onView(withId(R.id.submit_button)).perform(click())

        // Then :
        onView(withText(R.string.auth_process_error)).check(matches(isDisplayed()))
    }

    @Test
    fun clickLoginWithIncorrectPassword_returnsSnackbarTextIncorrectPassword() {
        // Given :
        sharedPreferencesManager.putIsRegistered(true)
        sharedPreferencesManager.putSalt("Some Salt")
        encryptionService.setShouldReturnDifferentAccount(true)

        // When :
        launchFragmentInContainer<AuthenticationFragment>(Bundle(), R.style.Theme_Sherlock)

        onView(withId(R.id.enter_password_edit_text)).perform(
            typeText("Password@123"),
            closeSoftKeyboard()
        )
        onView(withId(R.id.submit_button)).perform(click())

        // Then :
        onView(withText(R.string.incorrect_password)).check(matches(isDisplayed()))
    }

    @Test
    fun successfulLogin_navigatesToHomeScreenAndReturnsSnackbarTextAuthSuccess() {
        // Given :
        sharedPreferencesManager.putIsRegistered(true)
        sharedPreferencesManager.putSalt("Some Salt")
        sharedPreferencesManager.putVerificationAccount(VerificationAccount.instance)

        // When :
        Intents.init()
        val scenario =
            launchFragmentInContainer<AuthenticationFragment>(Bundle(), R.style.Theme_Sherlock)

        val navController = mock(NavController::class.java)
        scenario.onFragment {
            Navigation.setViewNavController(it.view!!, navController)
        }
        ServiceLocator.encryptionService = FakeEncryptionServiceAndroid

        onView(withId(R.id.enter_password_edit_text)).perform(
            typeText("Password@123"),
            closeSoftKeyboard()
        )
        onView(withId(R.id.submit_button)).perform(click())

        // Then :
        intended(hasComponent(MainActivity::class.java.name))

        Intents.release()
    }

    @Test
    fun hashLoginPassword_loading() {
        // Given :
        mainCoroutineRuleAndroid.pauseDispatcher()
        sharedPreferencesManager.putIsRegistered(true)
        sharedPreferencesManager.putSalt("Some Salt")

        // When :
        val scenario =
            launchFragmentInContainer<AuthenticationFragment>(Bundle(), R.style.Theme_Sherlock)

        val navController = mock(NavController::class.java)
        scenario.onFragment {
            Navigation.setViewNavController(it.view!!, navController)
        }

        onView(withId(R.id.enter_password_edit_text)).perform(
            typeText("Password@123"),
            closeSoftKeyboard()
        )
        onView(withId(R.id.submit_button)).perform(click())

        // Then :
        onView(withId(R.id.auth_progress_bar)).check(matches(isDisplayed()))

        mainCoroutineRuleAndroid.resumeDispatcher()

        onView(withId(R.id.auth_progress_bar)).check(matches(not(isDisplayed())))
    }

    @Test
    fun registerAndLogin_navigatesToHomeScreen() {
        // When :
        Intents.init()
        val scenario =
            launchFragmentInContainer<AuthenticationFragment>(Bundle(), R.style.Theme_Sherlock)

        val navController = mock(NavController::class.java)
        scenario.onFragment {
            Navigation.setViewNavController(it.view!!, navController)
        }

        // Enter valid password and confirm password, then click register
        onView(withId(R.id.enter_password_edit_text)).perform(
            typeText("Password@123"),
            closeSoftKeyboard()
        )
        onView(withId(R.id.re_enter_password_edit_text)).perform(
            typeText("Password@123"),
            closeSoftKeyboard()
        )

        onView(withId(R.id.submit_button)).perform(click())

        onView(withId(R.id.enter_password_edit_text)).perform(clearText())
        // Enter correct password for login
        onView(withId(R.id.enter_password_edit_text)).perform(
            typeText("Password@123"),
            closeSoftKeyboard()
        )

        onView(withId(R.id.submit_button)).perform(click())

        // Then :
        intended(hasComponent(MainActivity::class.java.name))

        Intents.release()
    }
}