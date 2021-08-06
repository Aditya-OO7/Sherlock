package com.adityaoo7.sherlock.authentication

import androidx.test.core.app.launchActivity
import androidx.test.espresso.Espresso
import androidx.test.espresso.IdlingRegistry
import androidx.test.espresso.action.ViewActions
import androidx.test.espresso.assertion.ViewAssertions
import androidx.test.espresso.matcher.ViewMatchers
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.MediumTest
import com.adityaoo7.sherlock.R
import com.adityaoo7.sherlock.ServiceLocator
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
class AuthenticationActivityTest {

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
    fun registerAndLogin_navigatesToHomeScreen() {
        launchActivity<AuthenticationActivity>()

        Espresso.onView(ViewMatchers.withId(R.id.enter_password_edit_text)).perform(
            ViewActions.typeText("Password@123"),
            ViewActions.closeSoftKeyboard()
        )
        Espresso.onView(ViewMatchers.withId(R.id.re_enter_password_edit_text)).perform(
            ViewActions.typeText("Password@123"),
            ViewActions.closeSoftKeyboard()
        )
        Espresso.onView(ViewMatchers.withId(R.id.register_button)).perform(ViewActions.click())

        Espresso.onView(ViewMatchers.withId(R.id.login_layout))
            .check(ViewAssertions.matches(ViewMatchers.isDisplayed()))

        Espresso.onView(ViewMatchers.withId(R.id.login_button)).perform(ViewActions.click())

        Espresso.onView(ViewMatchers.withId(R.id.home_layout))
            .check(ViewAssertions.matches(ViewMatchers.isDisplayed()))
    }
}