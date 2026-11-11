package com.loitu.yourrefri.shared.db

import app.cash.sqldelight.ColumnAdapter
import app.cash.sqldelight.db.SqlDriver

object IdListAdapter : ColumnAdapter<List<String>, String> {
    override fun decode(databaseValue: String): List<String> =
        if (databaseValue.isEmpty()) emptyList() else databaseValue.split(',')

    override fun encode(value: List<String>): String {
        require(value.all { it.isNotEmpty() && ',' !in it })
        return value.joinToString(",")
    }
}

fun createDatabase(driver: SqlDriver): YourRefriDatabase = YourRefriDatabase(
    driver,
    CookLogEntityAdapter = CookLogEntity.Adapter(savedIngredientIdsAdapter = IdListAdapter),
)
