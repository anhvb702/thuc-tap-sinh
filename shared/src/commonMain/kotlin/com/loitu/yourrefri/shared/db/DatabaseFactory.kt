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

fun createDatabase(driver: SqlDriver): YourRefriDatabase {
    driver.execute(
        identifier = null,
        sql = """CREATE TABLE IF NOT EXISTS WeeklyMealPlanSettingsEntity (
            settingsId INTEGER NOT NULL PRIMARY KEY CHECK (settingsId = 1),
            weekStart TEXT NOT NULL,
            goalId TEXT NOT NULL,
            preferenceIdsJson TEXT NOT NULL,
            swappedOffsetsJson TEXT NOT NULL,
            uncheckedIdsJson TEXT NOT NULL,
            includeLunchMealPrep INTEGER NOT NULL
        )""",
        parameters = 0,
    )
    return YourRefriDatabase(
        driver,
        CookLogEntityAdapter = CookLogEntity.Adapter(savedIngredientIdsAdapter = IdListAdapter),
    )
}
