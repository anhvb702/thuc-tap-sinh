package com.loitu.yourrefri.shared.db

import app.cash.sqldelight.driver.jdbc.sqlite.JdbcSqliteDriver
import kotlin.test.Test
import kotlin.test.assertEquals

class DatabaseMigrationTest {
    @Test
    fun versionSevenDatabase_migratesAllRepairTables() {
        val driver = JdbcSqliteDriver(JdbcSqliteDriver.IN_MEMORY)
        try {
            YourRefriDatabase.Schema.create(driver)
            listOf(
                "PantryCompartmentEntity",
                "CustomRecipeEntity",
                "WeeklyMealPlanSettingsEntity",
                "ExtraShoppingItemEntity",
                "LeftoverEntity",
            ).forEach { table -> driver.execute(null, "DROP TABLE $table", 0) }
            driver.execute(null, "PRAGMA user_version = 7", 0)

            YourRefriDatabase.Schema.migrate(driver, oldVersion = 7, newVersion = 8)
            val database = createDatabase(driver)

            assertEquals(0, database.pantryCompartmentEntityQueries.selectAll().executeAsList().size)
            assertEquals(0, database.customRecipeEntityQueries.selectAll().executeAsList().size)
            assertEquals(0, database.weeklyMealPlanSettingsEntityQueries.selectAll().executeAsList().size)
            assertEquals(0, database.extraShoppingItemEntityQueries.selectAll().executeAsList().size)
            assertEquals(0, database.leftoverEntityQueries.selectAll().executeAsList().size)
        } finally {
            driver.close()
        }
    }
}
