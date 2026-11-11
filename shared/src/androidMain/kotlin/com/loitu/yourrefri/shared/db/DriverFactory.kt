package com.loitu.yourrefri.shared.db

import android.content.Context
import app.cash.sqldelight.db.SqlDriver
import app.cash.sqldelight.driver.android.AndroidSqliteDriver

class DriverFactory(private val context: Context) {
    fun create(): SqlDriver = AndroidSqliteDriver(YourRefriDatabase.Schema, context, "yourrefri.db")
}
