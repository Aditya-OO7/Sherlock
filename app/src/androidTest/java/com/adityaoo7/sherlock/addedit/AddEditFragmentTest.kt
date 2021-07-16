package com.adityaoo7.sherlock.addedit

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
import com.adityaoo7.sherlock.services.FakeEncryptionServiceAndroid
import com.adityaoo7.sherlock.util.EspressoIdlingResource
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runBlockingTest
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mockito.mock
import org.mockito.Mockito.verify

@RunWith(AndroidJUnit4::class)
@MediumTest
@ExperimentalCoroutinesApi
class AddEditFragmentTest {

    private lateinit var repository: FakeAndroidTestRepository
    private lateinit var encryptionService: FakeEncryptionServiceAndroid

    @Before
    fun setUp() {
        repository = FakeAndroidTestRepository()
        encryptionService = FakeEncryptionServiceAndroid

        ServiceLocator.accountsRepository = repository
        ServiceLocator.encryptionService = encryptionService
    }

    @After
    fun tearDown() = runBlockingTest {
        ServiceLocator.resetRepository()
        encryptionService.setShouldReturnError(false)
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
    fun retrieveExistingAccountError_returnsSnackbarTextNoAccountFound() = runBlockingTest {
        // Given :
        val account = LoginAccount(
            "Existing Account",
            "Existing user name",
            "ExistingPassword",
            "https://existinguri.com",
            "This is Existing Note saved to Database"
        )
        repository.saveAccount(account)
        repository.setReturnError(true)

        // When :
        val bundle = AddEditFragmentArgs(account.id).toBundle()
        launchFragmentInContainer<AddEditFragment>(bundle, R.style.Theme_Sherlock)

        // Then :
        onView(withText(R.string.no_account_found)).check(matches(isDisplayed()))
    }

    @Test
    fun retrievedAccountDecryptionError_returnsSnackbarTextAccountDecryptFailed() =
        runBlockingTest {
            // Given :
            val account = LoginAccount(
                "Existing Account",
                "Existing user name",
                "ExistingPassword",
                "https://existinguri.com",
                "This is Existing Note saved to Database"
            )
            repository.saveAccount(account)
            encryptionService.setShouldReturnError(true)

            // When :
            val bundle = AddEditFragmentArgs(account.id).toBundle()
            launchFragmentInContainer<AddEditFragment>(bundle, R.style.Theme_Sherlock)

            // Then :
            onView(withText(R.string.account_decrypt_failed)).check(matches(isDisplayed()))
        }

    @Test
    fun saveEmptyAccount_returnsSnackbarTextEmptyAccount() {

        // When :
        val bundle = AddEditFragmentArgs(null).toBundle()
        launchFragmentInContainer<AddEditFragment>(bundle, R.style.Theme_Sherlock)

        onView(withId(R.id.save_account_button)).perform(click())

        // Then :
        onView(withText(R.string.empty_account)).check(matches(isDisplayed()))
    }

    @Test
    fun createAndSaveAccountEncryptionError_returnsSnackbarTextAccountEncryptFailed() {
        // When :
        val bundle = AddEditFragmentArgs(null).toBundle()
        launchFragmentInContainer<AddEditFragment>(bundle, R.style.Theme_Sherlock)

        encryptionService.setShouldReturnError(true)

        onView(withId(R.id.account_name_edit_text)).perform(
            typeText("New Account"),
            closeSoftKeyboard()
        )

        onView(withId(R.id.user_name_edit_text)).perform(
            typeText("MyAccount"),
            closeSoftKeyboard()
        )
        onView(withId(R.id.password_edit_text)).perform(
            typeText("Password@123"),
            closeSoftKeyboard()
        )

        onView(withId(R.id.toggle_view_password_button)).perform(click())
        onView(withId(R.id.toggle_view_password_button)).perform(click())
        onView(withId(R.id.toggle_view_password_button)).perform(click())

        onView(withId(R.id.uri_edit_text)).perform(
            typeText("https://newaccounturi.com"),
            closeSoftKeyboard()
        )
        onView(withId(R.id.note_edit_text)).perform(
            typeText("Note for this new account. You can enter note for your account"),
            closeSoftKeyboard()
        )

        onView(withId(R.id.save_account_button)).perform(click())

        // Then :
        onView(withText(R.string.account_encrypt_failed)).check(matches(isDisplayed()))
    }

    @Test
    fun createAndSaveAccount_returnsSnackbarTextSaveSuccessAndNavigatesToHomeScreen() {
        // When :
        val bundle = AddEditFragmentArgs(null).toBundle()
        val scenario = launchFragmentInContainer<AddEditFragment>(bundle, R.style.Theme_Sherlock)
        val navController = mock(NavController::class.java)

        scenario.onFragment {
            Navigation.setViewNavController(it.view!!, navController)
        }

        onView(withId(R.id.account_name_edit_text)).perform(
            typeText("New Account")
        )

        onView(withId(R.id.user_name_edit_text)).perform(
            typeText("MyAccount")
        )
        onView(withId(R.id.password_edit_text)).perform(
            typeText("Password@123"),
            closeSoftKeyboard()
        )
        onView(withId(R.id.uri_edit_text)).perform(
            typeText("https://newaccounturi.com"),
            closeSoftKeyboard()
        )
        onView(withId(R.id.note_edit_text)).perform(
            typeText("Note for this new account. You can enter note for your account"),
            closeSoftKeyboard()
        )

        onView(withId(R.id.save_account_button)).perform(click())

        // Then :
        onView(withText(R.string.save_success)).check(matches(isDisplayed()))

        verify(navController).navigate(
            AddEditFragmentDirections.actionAddEditFragmentToHomeFragment()
        )
    }

    @Test
    fun loadExistingAccount_displaysExistingAccountDetails() = runBlockingTest {
        // Given :
        val account = LoginAccount(
            "Existing Account",
            "Existing user name",
            "ExistingPassword",
            "https://existinguri.com",
            "This is Existing Note saved to Database"
        )
        repository.saveAccount(account)

        // When :
        val bundle = AddEditFragmentArgs(account.id).toBundle()
        launchFragmentInContainer<AddEditFragment>(bundle, R.style.Theme_Sherlock)

        // Then :
        onView(withId(R.id.account_name_edit_text)).check(matches(withText(account.name)))
        onView(withId(R.id.user_name_edit_text)).check(matches(withText(account.userName)))
        onView(withId(R.id.password_edit_text)).check(matches(withText(account.password)))
        onView(withId(R.id.uri_edit_text)).check(matches(withText(account.uri)))
        onView(withId(R.id.note_edit_text)).check(matches(withText(account.note)))
    }

    @Test
    fun updateExistingAccountEncryptionError_returnsSnackbarTextAccountEncryptionFailed() =
        runBlockingTest {
            // Given :
            val account = LoginAccount(
                "Existing Account",
                "Existing user name",
                "ExistingPassword",
                "https://existinguri.com",
                "This is Existing Note saved to Database"
            )
            repository.saveAccount(account)

            // When :
            val bundle = AddEditFragmentArgs(account.id).toBundle()
            launchFragmentInContainer<AddEditFragment>(bundle, R.style.Theme_Sherlock)

            encryptionService.setShouldReturnError(true)

            onView(withId(R.id.user_name_edit_text)).perform(clearText())
            onView(withId(R.id.password_edit_text)).perform(clearText())

            onView(withId(R.id.user_name_edit_text)).perform(
                typeText("Updated User Name"),
                closeSoftKeyboard()
            )
            onView(withId(R.id.password_edit_text)).perform(
                typeText("UpdatedPassword"),
                closeSoftKeyboard()
            )

            onView(withId(R.id.save_account_button)).perform(click())

            // Then :
            onView(withText(R.string.account_encrypt_failed)).check(matches(isDisplayed()))
        }

    @Test
    fun updateExistingAccount_returnsSnackbarTextSaveSuccess() = runBlockingTest {
        // Given :
        val account = LoginAccount(
            "Existing Account",
            "Existing user name",
            "ExistingPassword",
            "https://existinguri.com",
            "This is Existing Note saved to Database"
        )
        repository.saveAccount(account)

        // When :
        val bundle = AddEditFragmentArgs(account.id).toBundle()
        val scenario = launchFragmentInContainer<AddEditFragment>(bundle, R.style.Theme_Sherlock)
        val navController = mock(NavController::class.java)

        scenario.onFragment {
            Navigation.setViewNavController(it.view!!, navController)
        }

        onView(withId(R.id.user_name_edit_text)).perform(clearText())
        onView(withId(R.id.password_edit_text)).perform(clearText())

        onView(withId(R.id.user_name_edit_text)).perform(
            typeText("Updated User Name"),
            closeSoftKeyboard()
        )
        onView(withId(R.id.password_edit_text)).perform(
            typeText("UpdatedPassword"),
            closeSoftKeyboard()
        )

        onView(withId(R.id.save_account_button)).perform(click())

        // Then :
        onView(withText(R.string.save_success)).check(matches(isDisplayed()))

        verify(navController).navigate(
            AddEditFragmentDirections.actionAddEditFragmentToHomeFragment()
        )
    }
}