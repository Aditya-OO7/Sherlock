package com.adityaoo7.sherlock.data.source.shared

import android.content.Context
import androidx.test.core.app.ApplicationProvider.getApplicationContext
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.SmallTest
import com.adityaoo7.sherlock.R
import com.adityaoo7.sherlock.data.LoginAccount
import com.adityaoo7.sherlock.data.Result
import com.adityaoo7.sherlock.data.succeeded
import com.adityaoo7.sherlock.util.VerificationAccount
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.*
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
@SmallTest
class AccountPreferencesManagerTest {

    private lateinit var accountPreferencesManager: AccountPreferencesManager

    @Before
    fun setUp() {
        val context: Context = getApplicationContext()
        accountPreferencesManager = AccountPreferencesManager.getInstance(
            context,
            context.getString(R.string.test_preference_file_key)
        )
    }

    @After
    fun tearDown() {
        accountPreferencesManager.clearAll()
    }

    @Test
    fun putVerificationAccountAndGetVerificationAccount_returns_verificationAccount() {
        // Given :
        val verificationAccount = VerificationAccount.instance

        // When :
        accountPreferencesManager.putVerificationAccount(verificationAccount)
        val result = accountPreferencesManager.getVerificationAccount()

        // Then :
        assertThat(result.succeeded, `is`(true))
        result as Result.Success
        assertThat(result.data, `is`(instanceOf(LoginAccount::class.java)))
        assertThat(result.data.id, `is`(verificationAccount.id))
        assertThat(result.data.name, `is`(verificationAccount.name))
        assertThat(result.data.userName, `is`(verificationAccount.userName))
        assertThat(result.data.password, `is`(verificationAccount.password))
        assertThat(result.data.uri, `is`(verificationAccount.uri))
        assertThat(result.data.note, `is`(verificationAccount.note))
    }

    @Test
    fun putSaltAndGetSalt_returnsSalt() {
        // Given :
        val salt = "Some Test Salt"

        // When :
        accountPreferencesManager.putSalt(salt)
        val result = accountPreferencesManager.getSalt()

        // Then :
        assertThat(result.succeeded, `is`(true))
        result as Result.Success
        assertThat(result.data, `is`(salt))
    }
}