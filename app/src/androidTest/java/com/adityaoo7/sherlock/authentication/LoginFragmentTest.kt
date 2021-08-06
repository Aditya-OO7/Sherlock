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
import com.adityaoo7.sherlock.data.source.shared.FakeSharedPreferencesManagerAndroid
import com.adityaoo7.sherlock.services.FakeEncryptionServiceAndroid
import com.adityaoo7.sherlock.util.EspressoIdlingResource
import com.adityaoo7.sherlock.util.MainCoroutineRuleAndroid
import com.adityaoo7.sherlock.util.VerificationAccount
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runBlockingTest
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mockito.mock

@RunWith(AndroidJUnit4::class)
@MediumTest
@ExperimentalCoroutinesApi
class LoginFragmentTest {

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
    fun successfulLogin_navigatesToHomeScreen() {
        // Given :
        sharedPreferencesManager.putIsRegistered(true)
        sharedPreferencesManager.putSalt("Some Salt")
        sharedPreferencesManager.putVerificationAccount(VerificationAccount.instance)

        // When :
        Intents.init()
        val scenario = launchFragmentInContainer<LoginFragment>(Bundle(), R.style.Theme_Sherlock)
        val navController = mock(NavController::class.java)
        scenario.onFragment {
            Navigation.setViewNavController(it.view!!, navController)
        }
        ServiceLocator.encryptionService = FakeEncryptionServiceAndroid

        onView(withId(R.id.login_password_edit_text)).perform(
            typeText("Password@123"),
            closeSoftKeyboard()
        )
        onView(withId(R.id.login_button)).perform(click())

        // Then :
        intended(hasComponent(MainActivity::class.java.name))

        Intents.release()
    }

    @Test
    fun clickLoginAndSaltNotFound_returnsSnackbarTextAuthProcessError() {
        // Given :
        sharedPreferencesManager.putIsRegistered(true)
        sharedPreferencesManager.setSaltError(true)

        // When :
        launchFragmentInContainer<LoginFragment>(Bundle(), R.style.Theme_Sherlock)

        onView(withId(R.id.login_password_edit_text)).perform(
            typeText("Password@123"),
            closeSoftKeyboard()
        )
        onView(withId(R.id.login_button)).perform(click())

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
        launchFragmentInContainer<LoginFragment>(Bundle(), R.style.Theme_Sherlock)
        onView(withId(R.id.login_password_edit_text)).perform(
            typeText("Password@123"),
            closeSoftKeyboard()
        )
        onView(withId(R.id.login_button)).perform(click())

        // Then :
        onView(withText(R.string.incorrect_password)).check(matches(isDisplayed()))
    }
}