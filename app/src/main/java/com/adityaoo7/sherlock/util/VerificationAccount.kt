package com.adityaoo7.sherlock.util

import com.adityaoo7.sherlock.data.LoginAccount

object VerificationAccount {

    val instance = LoginAccount(
        "verification Account Name",
        "verification@UserName",
        "verificationPassword",
        "https://verificationUri.com",
        "verification Note",
        "verification Account ID",
    )
}