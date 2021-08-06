package com.adityaoo7.sherlock.services

import android.util.Base64
import com.adityaoo7.sherlock.data.LoginAccount
import com.adityaoo7.sherlock.data.Result
import java.lang.Exception
import javax.crypto.Cipher
import javax.crypto.spec.GCMParameterSpec

const val SIZE_OF_IV = 12

object AccountEncryptionService : EncryptionService {

    override suspend fun encrypt(account: LoginAccount): Result<LoginAccount> {
        val key = HashingService.getKey()
        val cipher = Cipher.getInstance("AES/GCM/NoPadding")
        val encryptedAccount = LoginAccount(id = account.id)
        val listOfItemsToEncrypt =
            listOf(account.name, account.userName, account.password, account.uri, account.note)

        val listOfEncryptedItems = Array(5) { "" }
        var i = 0

        listOfItemsToEncrypt.forEach { item ->
            cipher.init(Cipher.ENCRYPT_MODE, key)
            val cipherText = cipher.doFinal(item.toByteArray())
            val iv = cipher.iv
            val total = iv + cipherText

            val cipherTextStr = Base64.encodeToString(total, Base64.NO_WRAP)
            listOfEncryptedItems[i] = cipherTextStr
            i++
        }

        encryptedAccount.name = listOfEncryptedItems[0]
        encryptedAccount.userName = listOfEncryptedItems[1]
        encryptedAccount.password = listOfEncryptedItems[2]
        encryptedAccount.uri = listOfEncryptedItems[3]
        encryptedAccount.note = listOfEncryptedItems[4]

        return Result.Success(encryptedAccount)
    }

    override suspend fun decrypt(account: LoginAccount): Result<LoginAccount> {
        val key = HashingService.getKey()
        val cipher = Cipher.getInstance("AES/GCM/NoPadding")
        val decryptedAccount = LoginAccount(id = account.id)
        val listOfItemsToDecrypt =
            listOf(account.name, account.userName, account.password, account.uri, account.note)

        val listOfDecryptedItems = Array(5) { "" }

        try {
            var i = 0
            listOfItemsToDecrypt.forEach { item ->
                val itemByteArray = Base64.decode(item, Base64.NO_WRAP)

                val iv = itemByteArray.copyOf(SIZE_OF_IV)
                val cipherText = itemByteArray.copyOfRange(SIZE_OF_IV, itemByteArray.size)

                val gcmParameterSpec = GCMParameterSpec(128, iv)

                cipher.init(Cipher.DECRYPT_MODE, key, gcmParameterSpec)

                val plainText = cipher.doFinal(cipherText)

                listOfDecryptedItems[i] = String(plainText)
                i++
            }
        } catch (e: Exception) {
            return Result.Error(e)
        }

        decryptedAccount.name = listOfDecryptedItems[0]
        decryptedAccount.userName = listOfDecryptedItems[1]
        decryptedAccount.password = listOfDecryptedItems[2]
        decryptedAccount.uri = listOfDecryptedItems[3]
        decryptedAccount.note = listOfDecryptedItems[4]

        return Result.Success(decryptedAccount)
    }
}