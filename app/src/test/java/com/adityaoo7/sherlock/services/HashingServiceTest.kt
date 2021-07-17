package com.adityaoo7.sherlock.services

import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runBlockingTest
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.*
import org.junit.Before
import org.junit.Test
import javax.crypto.SecretKey

class HashingServiceTest {

    @ExperimentalCoroutinesApi
    @Before
    fun setUp() = runBlockingTest {
        HashingService.hashPassword("TestPassword", "SomeSalt")
    }

    @Test
    fun getKey_returnsKey() {

        // When :
        val result = HashingService.getKey()

        // Then :
        assertThat(result, `is`(notNullValue()))
        assertThat(result, `is`(instanceOf(SecretKey::class.java)))
    }

    @Test
    fun getKeyTwice_returnsSameKey() {

        // When :
        val result1 = HashingService.getKey()
        val result2 = HashingService.getKey()

        // Then :
        assertThat(result1, `is`(notNullValue()))
        assertThat(result1, `is`(instanceOf(SecretKey::class.java)))
        assertThat(result2, `is`(notNullValue()))
        assertThat(result2, `is`(instanceOf(SecretKey::class.java)))
        assertThat(result1, `is`(result2))
    }
}