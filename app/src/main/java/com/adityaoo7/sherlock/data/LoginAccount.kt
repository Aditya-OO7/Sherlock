package com.adityaoo7.sherlock.data

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.*

@Entity(tableName = "accounts")
data class LoginAccount @JvmOverloads constructor(
    @ColumnInfo(name = "account_name") var name: String = "",
    @ColumnInfo(name = "user_name") var userName: String = "",
    @ColumnInfo(name = "password") var password: String = "",
    @ColumnInfo(name = "uri") var uri: String = "",
    @ColumnInfo(name = "note") var note: String = "",
    @PrimaryKey @ColumnInfo(name = "accountID") var id: String = UUID.randomUUID().toString()
)
