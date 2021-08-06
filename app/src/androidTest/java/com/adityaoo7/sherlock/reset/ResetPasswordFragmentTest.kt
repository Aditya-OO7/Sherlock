package com.adityaoo7.sherlock.reset

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
import com.adityaoo7.sherlock.data.source.local.FakeAndroidTestRepository
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
import org.mockito.Mockito.verify

@RunWith(AndroidJUnit4::class)
@MediumTest
@ExperimentalCoroutinesApi
class ResetPasswordFragmentTest {

    private lateinit var repository: FakeAndroidTestRepository
    private lateinit var sharedPreferencesManagerAndroid: FakeSharedPreferencesManagerAndroid
    private lateinit var encryptionService: FakeEncryptionServiceAndroid

    private lateinit var account1: LoginAccount
    private lateinit var account2: LoginAccount

    @get:Rule
    var mainCoroutineRuleAndroid = MainCoroutineRuleAndroid()

    @Before
    fun setup() {
        repository = FakeAndroidTestRepository()
        sharedPreferencesManagerAndroid = FakeSharedPreferencesManagerAndroid()
        encryptionService = FakeEncryptionServiceAndroid

        ServiceLocator.accountsRepository = repository
        ServiceLocator.sharedPreferencesManager = sharedPreferencesManagerAndroid
        ServiceLocator.encryptionService = encryptionService

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

        repository.addAccounts(account1, account2)
    }

    @After
    fun tearDown() = runBlockingTest {
        ServiceLocator.resetRepository()
        ServiceLocator.resetPreferencesManager()
        encryptionService.setShouldReturnError(false)
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
    fun enterCorrectOldPasswordAndNewPassword_navigatesToHomeScreen() {
        // Given :
        sharedPreferencesManagerAndroid.putSalt("Some Salt")
        sharedPreferencesManagerAndroid.putVerificationAccount(VerificationAccount.instance)

        // When :
        val scenario =
            launchFragmentInContainer<ResetPasswordFragment>(Bundle(), R.style.Theme_Sherlock)
        val navController = mock(NavController::class.java)

        scenario.onFragment {
            Navigation.setViewNavController(it.view!!, navController)
        }

        onView(withId(R.id.old_password_edit_text)).perform(
            typeText("OldPassword@123"),
            closeSoftKeyboard()
        )

        onView(withId(R.id.new_password_edit_text)).perform(
            typeText("NewPassword@123"),
            closeSoftKeyboard()
        )
        onView(withId(R.id.confirm_new_password_edit_text)).perform(
            typeText("NewPassword@123"),
            closeSoftKeyboard()
        )

        onView(withId(R.id.reset_password_button)).perform(click())

        // Then :
        verify(navController).navigate(
            ResetPasswordFragmentDirections.actionResetPasswordFragmentToHomeFragment()
        )
    }

    @Test
    fun incorrectOldPassword_returnsSnackbarTextIncorrectPassword() {
        // Given :
        sharedPreferencesManagerAndroid.putSalt("Some Salt")
        sharedPreferencesManagerAndroid.putVerificationAccount(VerificationAccount.instance)
        encryptionService.setShouldReturnDifferentAccount(true)

        // When :
        val scenario =
            launchFragmentInContainer<ResetPasswordFragment>(Bundle(), R.style.Theme_Sherlock)
        val navController = mock(NavController::class.java)

        scenario.onFragment {
            Navigation.setViewNavController(it.view!!, navController)
        }

        onView(withId(R.id.old_password_edit_text)).perform(
            typeText("OldPassword@123"),
            closeSoftKeyboard()
        )

        onView(withId(R.id.reset_password_button)).perform(click())

        // Then :
        onView(withText(R.string.incorrect_password)).check(matches(isDisplayed()))
    }

    @Test
    fun invalidNewPasswordPattern_returnsSnackbarTextWrongPasswordPattern() {
        // Given :
        sharedPreferencesManagerAndroid.putSalt("Some Salt")
        sharedPreferencesManagerAndroid.putVerificationAccount(VerificationAccount.instance)

        // When :
        val scenario =
            launchFragmentInContainer<ResetPasswordFragment>(Bundle(), R.style.Theme_Sherlock)
        val navController = mock(NavController::class.java)

        scenario.onFragment {
            Navigation.setViewNavController(it.view!!, navController)
        }

        onView(withId(R.id.old_password_edit_text)).perform(
            typeText("OldPassword@123"),
            closeSoftKeyboard()
        )

        onView(withId(R.id.new_password_edit_text)).perform(
            typeText("NewPassword"),
            closeSoftKeyboard()
        )
        onView(withId(R.id.confirm_new_password_edit_text)).perform(
            typeText("NewPassword"),
            closeSoftKeyboard()
        )

        onView(withId(R.id.reset_password_button)).perform(click())

        // Then :
        onView(withText(R.string.wrong_password_pattern)).check(matches(isDisplayed()))
    }

    @Test
    fun wrongConfirmPassword_returnsSnackbarTextPasswordNotMatch() {
        // Given :
        sharedPreferencesManagerAndroid.putSalt("Some Salt")
        sharedPreferencesManagerAndroid.putVerificationAccount(VerificationAccount.instance)

        // When :
        val scenario =
            launchFragmentInContainer<ResetPasswordFragment>(Bundle(), R.style.Theme_Sherlock)
        val navController = mock(NavController::class.java)

        scenario.onFragment {
            Navigation.setViewNavController(it.view!!, navController)
        }

        onView(withId(R.id.old_password_edit_text)).perform(
            typeText("OldPassword@123"),
            closeSoftKeyboard()
        )

        onView(withId(R.id.new_password_edit_text)).perform(
            typeText("NewPassword@123"),
            closeSoftKeyboard()
        )
        onView(withId(R.id.confirm_new_password_edit_text)).perform(
            typeText("SomeWrongPassword"),
            closeSoftKeyboard()
        )

        onView(withId(R.id.reset_password_button)).perform(click())

        // Then :
        onView(withText(R.string.password_not_match)).check(matches(isDisplayed()))
    }
}