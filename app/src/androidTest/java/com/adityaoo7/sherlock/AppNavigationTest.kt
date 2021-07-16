package com.adityaoo7.sherlock

import androidx.test.core.app.ApplicationProvider.getApplicationContext
import androidx.test.espresso.IdlingRegistry
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.LargeTest
import com.adityaoo7.sherlock.data.repository.AccountsRepository
import com.adityaoo7.sherlock.util.EspressoIdlingResource
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
@LargeTest
class AppNavigationTest {

    private lateinit var accountsRepository: AccountsRepository

    @Before
    fun setUp() {
        accountsRepository = ServiceLocator.provideAccountsRepository(getApplicationContext())
    }

    @After
    fun tearDown() {
        ServiceLocator.resetRepository()
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
    fun clickAddNewAccountAndSaveNewAccount_returnsToHomeScreenAndDisplaysNewAccount() {

    }
}