package com.adityaoo7.sherlock.services

import androidx.test.ext.junit.runners.AndroidJUnit4
import com.adityaoo7.sherlock.data.LoginAccount
import com.adityaoo7.sherlock.data.Result
import com.adityaoo7.sherlock.data.succeeded
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runBlockingTest
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.`is`
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
@ExperimentalCoroutinesApi
class AccountEncryptionServiceTest {

    @Before
    fun setUp() = runBlockingTest {
        HashingService.hashPassword("TestPassword", "SomeSalt")
    }

    @Test
    fun encryptAccountAndDecryptAccount_accountEncryptedAndDecryptedSuccessfully() =
        runBlockingTest {
            // Given :
            val account = LoginAccount(
                "Account Name",
                "user@name",
                "password",
                "https://linktoservice.com",
                "note about this account"
            )

            // When :
            val encryptedAccount = AccountEncryptionService.encrypt(account)
            val result = AccountEncryptionService.decrypt((encryptedAccount as Result.Success).data)

            // Then :
            assertThat(result.succeeded, `is`(true))
            result as Result.Success
            assertThat(result.data.id, `is`(account.id))
            assertThat(result.data.name, `is`(account.name))
            assertThat(result.data.userName, `is`(account.userName))
            assertThat(result.data.password, `is`(account.password))
            assertThat(result.data.uri, `is`(account.uri))
            assertThat(result.data.note, `is`(account.note))
        }
}