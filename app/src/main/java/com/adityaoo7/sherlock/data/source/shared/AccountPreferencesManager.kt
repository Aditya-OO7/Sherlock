package com.adityaoo7.sherlock.data.source.shared

import android.content.Context
import android.content.SharedPreferences
import com.adityaoo7.sherlock.R
import com.adityaoo7.sherlock.data.LoginAccount
import com.adityaoo7.sherlock.data.Result
import com.adityaoo7.sherlock.data.source.SharedPreferencesManager

class AccountPreferencesManager(
    private val applicationContext: Context,
    preferencesFileKey: String
) : SharedPreferencesManager {

    private val sharedPref: SharedPreferences = applicationContext.getSharedPreferences(
        preferencesFileKey,
        Context.MODE_PRIVATE
    )

    override fun putVerificationAccount(account: LoginAccount) {
        with(sharedPref.edit()) {
            putString(applicationContext.getString(R.string.pref_acc_id_key), account.id)
            putString(applicationContext.getString(R.string.pref_acc_name_key), account.name)
            putString(
                applicationContext.getString(R.string.pref_acc_user_name_key),
                account.userName
            )
            putString(
                applicationContext.getString(R.string.pref_acc_password_key),
                account.password
            )
            putString(applicationContext.getString(R.string.pref_acc_uri_key), account.uri)
            putString(applicationContext.getString(R.string.pref_acc_note_key), account.note)
            apply()
        }
    }

    override fun getVerificationAccount(): Result<LoginAccount> {
        val account = LoginAccount()

        val accId =
            sharedPref.getString(applicationContext.getString(R.string.pref_acc_id_key), null)

        if (accId == null) {
            return Result.Error(Exception("Account Not Found!"))
        } else {
            account.id = accId
            account.name = sharedPref.getString(
                applicationContext.getString(R.string.pref_acc_name_key),
                null
            ) ?: "-"

            account.userName =
                sharedPref.getString(
                    applicationContext.getString(R.string.pref_acc_user_name_key),
                    null
                ) ?: ""

            account.password = sharedPref.getString(
                applicationContext.getString(R.string.pref_acc_password_key),
                null
            ) ?: ""

            account.uri = sharedPref.getString(
                applicationContext.getString(R.string.pref_acc_uri_key),
                null
            ) ?: "-"

            account.note = sharedPref.getString(
                applicationContext.getString(R.string.pref_acc_note_key),
                null
            ) ?: "-"

            return Result.Success(account)
        }
    }

    override fun putSalt(salt: String) {
        with(sharedPref.edit()) {
            putString(applicationContext.getString(R.string.pref_salt_key), salt)
            apply()
        }
    }

    override fun getSalt(): Result<String> {
        val salt = sharedPref.getString(
            applicationContext.getString(R.string.pref_salt_key),
            null
        )
        return if (salt != null) {
            Result.Success(salt)
        } else {
            Result.Error(Exception("Salt Not found!"))
        }
    }

    override fun putIsRegistered(state: Boolean) {
        with(sharedPref.edit()) {
            putBoolean(applicationContext.getString(R.string.pref_auth_state_key), state)
            apply()
        }
    }

    override fun getIsRegistered(): Result<Boolean> {
        val authState = sharedPref.getBoolean(
            applicationContext.getString(R.string.pref_auth_state_key),
            false
        )

        return Result.Success(authState)
    }
}