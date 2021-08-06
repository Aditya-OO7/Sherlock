package com.adityaoo7.sherlock.services

import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.*
import org.junit.Test
import javax.crypto.SecretKey

class HashingServiceTest {

    @Test
    fun getKey_returnsKey() {
        HashingService.hashPassword("TestPassword", "SomeSalt")

        // When :
        val result = HashingService.getKey()

        // Then :
        assertThat(result, `is`(notNullValue()))
        assertThat(result, `is`(instanceOf(SecretKey::class.java)))
    }

    @Test
    fun getKeyTwice_returnsSameKey() {
        HashingService.hashPassword("TestPassword", "SomeSalt")

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