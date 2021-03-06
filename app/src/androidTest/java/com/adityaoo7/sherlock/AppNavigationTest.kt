package com.adityaoo7.sherlock

import android.app.Activity
import androidx.appcompat.widget.Toolbar
import androidx.recyclerview.widget.RecyclerView
import androidx.test.core.app.ActivityScenario
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.IdlingRegistry
import androidx.test.espresso.action.ViewActions.*
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.contrib.RecyclerViewActions
import androidx.test.espresso.matcher.ViewMatchers.*
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.LargeTest
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
@LargeTest
class AppNavigationTest {

    private lateinit var accountsRepository: FakeAndroidTestRepository
    private lateinit var encryptionService: FakeEncryptionServiceAndroid
    private lateinit var sharedPreferencesManager: FakeSharedPreferencesManagerAndroid

    @Before
    fun setUp() {
        accountsRepository = FakeAndroidTestRepository()
        encryptionService = FakeEncryptionServiceAndroid
        sharedPreferencesManager = FakeSharedPreferencesManagerAndroid()

        ServiceLocator.accountsRepository = accountsRepository
        ServiceLocator.encryptionService = encryptionService
        ServiceLocator.sharedPreferencesManager = sharedPreferencesManager

        accountsRepository.addAccounts(
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
    fun checkUpButton_doubleUpButtonWorks() {
        val scenario = ActivityScenario.launch(MainActivity::class.java)

        onView(withId(R.id.accounts_list))
            .perform(
                RecyclerViewActions.actionOnItem<RecyclerView.ViewHolder>(
                    hasDescendant(withText("ACCOUNT 1")), click()
                )
            )

        onView(withId(R.id.edit_account_button)).perform(click())

        onView(withContentDescription(scenario.getToolbarNavigationContentDescription()))
            .perform(click())

        onView(withId(R.id.account_details_layout)).check(matches(isDisplayed()))

        onView(withContentDescription(scenario.getToolbarNavigationContentDescription()))
            .perform(click())

        onView(withId(R.id.home_layout)).check(matches(isDisplayed()))
    }
}

fun <T : Activity> ActivityScenario<T>.getToolbarNavigationContentDescription(): String {
    var description = ""
    onActivity {
        description = it.findViewById<Toolbar>(R.id.toolbar).navigationContentDescription as String
    }
    return description
}