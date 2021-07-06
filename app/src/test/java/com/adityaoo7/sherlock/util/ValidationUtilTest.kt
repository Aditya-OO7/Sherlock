package com.adityaoo7.sherlock.util

import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.`is`
import org.junit.Test

class ValidationUtilTest {

    private val validationUtil = ValidationUtil

    @Test
    fun nullPassword_returnsFalse() {
        // When :
        val result = validationUtil.validatePassword(null)

        // Then :
        assertThat(result, `is`(false))
    }

    @Test
    fun emptyOrLessThanEightCharactersPassword_returnsFalse() {
        // When :
        val result1 = validationUtil.validatePassword("")
        val result2 = validationUtil.validatePassword("Hello")

        // Then :
        assertThat(result1, `is`(false))
        assertThat(result2, `is`(false))
    }

    @Test
    fun noUpperCaseLetter_returnsFalse() {
        // When :
        val result = validationUtil.validatePassword("hello@12345")

        // Then :
        assertThat(result, `is`(false))
    }

    @Test
    fun noNumericCharacter_returnsFalse() {
        // When :
        val result = validationUtil.validatePassword("Hello@World")

        // Then :
        assertThat(result, `is`(false))
    }

    @Test
    fun noSpecialCharacter_returnsFalse() {
        // When :
        val result = validationUtil.validatePassword("Hello12345")

        // Then :
        assertThat(result, `is`(false))
    }

    @Test
    fun correctPasswordPattern_returnTrue() {
        // When :
        val result = validationUtil.validatePassword("HelloWorld@2021")

        // Then :
        assertThat(result, `is`(true))
    }
}