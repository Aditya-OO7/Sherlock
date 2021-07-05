package com.adityaoo7.sherlock.services

import com.adityaoo7.sherlock.data.LoginAccount
import com.adityaoo7.sherlock.data.Result
import com.adityaoo7.sherlock.data.succeeded
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.`is`
import org.junit.Before
import org.junit.Test


class EncryptionServiceTest {

    private lateinit var encryptionService: EncryptionService
    private lateinit var account: LoginAccount

    @Before
    fun setUp() {
        HashingService.hashPassword("TestPassword", "SomeSalt")
        encryptionService = EncryptionService()

        account = LoginAccount(
            "Account Name",
            "user@name",
            "password",
            "https://linktoservice.com",
            "note about this account"
        )
    }

    @Test
    fun encryptAccountAndDecryptAccount_accountEncryptedAndDecryptedSuccessfully() {

        val encryptedAccount = encryptionService.encryptAccount(account)
        val result = encryptionService.decryptAccount((encryptedAccount as Result.Success).data)

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