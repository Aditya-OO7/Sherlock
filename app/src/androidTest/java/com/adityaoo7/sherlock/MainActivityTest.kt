package com.adityaoo7.sherlock

import androidx.recyclerview.widget.RecyclerView
import androidx.test.core.app.ActivityScenario
import androidx.test.core.app.launchActivity
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.IdlingRegistry
import androidx.test.espresso.action.ViewActions
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.assertion.ViewAssertions
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.contrib.RecyclerViewActions
import androidx.test.espresso.matcher.ViewMatchers.*
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.MediumTest
import com.adityaoo7.sherlock.data.LoginAccount
import com.adityaoo7.sherlock.data.source.local.FakeAndroidTestRepository
import com.adityaoo7.sherlock.data.source.shared.FakeSharedPreferencesManagerAndroid
import com.adityaoo7.sherlock.services.FakeEncryptionServiceAndroid
import com.adityaoo7.sherlock.util.EspressoIdlingResource
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
@MediumTest
class MainActivityTest {

    private lateinit var repository: FakeAndroidTestRepository
    private lateinit var encryptionService: FakeEncryptionServiceAndroid
    private lateinit var sharedPreferencesManager: FakeSharedPreferencesManagerAndroid

    @Before
    fun setUp() {
        repository = FakeAndroidTestRepository()
        encryptionService = FakeEncryptionServiceAndroid
        sharedPreferencesManager = FakeSharedPreferencesManagerAndroid()

        ServiceLocator.accountsRepository = repository
        ServiceLocator.encryptionService = encryptionService
        ServiceLocator.sharedPreferencesManager = sharedPreferencesManager

        repository.addAccounts(
            LoginAccount(
                "ACCOUNT 1",
                "USER NAME 1",
                "PASSWORD1",
                "https//:uri.com",
                "NOTE 1",
                "id1"
            ),
            LoginAccount(
                "ACCOUNT 2",
                "USER NAME 2",
                "PASSWORD2",
                "https//:uri2.com",
                "NOTE 2",
                "id2"
            )
        )
        sharedPreferencesManager.init(false, LoginAccount(), "")
    }

    @After
    fun tearDown() {
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
    fun navigateToResetPassword_NavigatesToResetPassword() {

        ActivityScenario.launch(MainActivity::class.java)

        onView(withId(R.id.reset_password)).perform(click())

        onView(withId(R.id.reset_password_title)).check(matches(isDisplayed()))
    }

    @Test
    fun clickAddNewAccountAndSaveNewAccount_returnsToHomeScreenAndDisplaysNewAccount() {

        launchActivity<MainActivity>()

        onView(withId(R.id.add_account_fab)).perform(click())

        onView(withId(R.id.add_edit_layout)).check(matches(isDisplayed()))

        onView(withId(R.id.account_name_edit_text)).perform(
            ViewActions.typeText("New Account")
        )

        onView(withId(R.id.user_name_edit_text)).perform(
            ViewActions.typeText("MyAccount")
        )
        onView(withId(R.id.password_edit_text)).perform(
            ViewActions.typeText("Password@123"),
            ViewActions.closeSoftKeyboard()
        )
        onView(withId(R.id.uri_edit_text)).perform(
            ViewActions.typeText("https://newaccounturi.com"),
            ViewActions.closeSoftKeyboard()
        )
        onView(withId(R.id.note_edit_text)).perform(
            ViewActions.typeText("Note for this new account. You can enter note for your account"),
            ViewActions.closeSoftKeyboard()
        )

        onView(withId(R.id.save_account_button)).perform(click())

        onView(withId(R.id.home_layout)).check(matches(isDisplayed()))

        onView(withText("New Account")).check(matches(isDisplayed()))
    }

    @Test
    fun clickExistingAccountAndDeleteAccount_returnsToHomeScreenAndExistingAccountIsNotDisplayed() {
        launchActivity<MainActivity>()

        onView(withId(R.id.accounts_list))
            .perform(
                RecyclerViewActions.actionOnItem<RecyclerView.ViewHolder>(
                    hasDescendant(withText("ACCOUNT 1")), click()
                )
            )

        onView(withId(R.id.account_details_layout)).check(matches(isDisplayed()))
        onView(withId(R.id.account_name_text_view)).check(matches(withText("ACCOUNT 1")))

        onView(withId(R.id.delete_account_button)).perform(click())

        onView(withId(R.id.home_layout)).check(matches(isDisplayed()))
        onView(withText("ACCOUNT 1")).check(ViewAssertions.doesNotExist())
    }

    @Test
    fun editExistingAccountAndSavedUpdatedAccount_returnsToAddEditScreenAndAccountIsUpdated() {
        launchActivity<MainActivity>()

        onView(withId(R.id.accounts_list))
            .perform(
                RecyclerViewActions.actionOnItem<RecyclerView.ViewHolder>(
                    hasDescendant(withText("ACCOUNT 1")), click()
                )
            )
        onView(withId(R.id.account_details_layout)).check(matches(isDisplayed()))

        onView(withId(R.id.edit_account_button)).perform(click())

        onView(withId(R.id.add_edit_layout)).check(matches(isDisplayed()))
        onView(withId(R.id.account_name_edit_text)).check(matches(withText("ACCOUNT 1")))

        onView(withId(R.id.account_name_edit_text)).perform(ViewActions.clearText())

        onView(withId(R.id.account_name_edit_text)).perform(
            ViewActions.typeText("Updated Account"),
            ViewActions.closeSoftKeyboard()
        )

        onView(withId(R.id.save_account_button)).perform(click())

        onView(withId(R.id.account_details_layout)).check(matches(isDisplayed()))
        onView(withId(R.id.account_name_text_view)).check(matches(withText("Updated Account")))
    }
}