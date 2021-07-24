package com.adityaoo7.sherlock

import androidx.test.core.app.ActivityScenario
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.*
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.MediumTest
import com.adityaoo7.sherlock.data.source.local.FakeAndroidTestRepository
import com.adityaoo7.sherlock.services.FakeEncryptionServiceAndroid
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
@MediumTest
class MainActivityTest {

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
    fun tearDown() {
        ServiceLocator.resetRepository()
        encryptionService.setShouldReturnError(false)
    }

    @Test
    fun navigateToResetPassword_NavigatesToResetPassword() {

        ActivityScenario.launch(MainActivity::class.java)

        onView(withId(R.id.reset_password)).perform(click())

        onView(withId(R.id.reset_password_button)).check(matches(isDisplayed()))
    }
}