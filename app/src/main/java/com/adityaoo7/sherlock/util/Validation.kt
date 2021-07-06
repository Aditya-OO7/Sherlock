package com.adityaoo7.sherlock.util

object ValidationUtil {
    fun validatePassword(password: String?): Boolean {
        val matcher =
            Regex("^(?=.*[0-9])(?=.*[a-z])(?=.*[A-Z])(?=.*[@#$%^&\\-+=()])(?=\\S+$).{8,128}$")

        return password?.let { pass ->
            matcher.matches(pass)
        } ?: false
    }
}