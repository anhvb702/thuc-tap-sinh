package com.loitu.yourrefri.shared.db

import app.cash.sqldelight.db.QueryResult
import app.cash.sqldelight.driver.jdbc.sqlite.JdbcSqliteDriver
import kotlin.test.Test
import kotlin.test.assertEquals

class DatabaseMigrationTest {
    @Test
    fun versionSevenDatabase_migratesAllRepairTables() {
        val driver = JdbcSqliteDriver(JdbcSqliteDriver.IN_MEMORY)
        try {
            // Schema.create tạo schema hiện tại; các bảng sửa chữa chỉ có qua migration 7.sqm.
            YourRefriDatabase.Schema.create(driver)
            driver.execute(null, "PRAGMA user_version = 7", 0)

            YourRefriDatabase.Schema.migrate(driver, oldVersion = 7, newVersion = 8)
            createDatabase(driver)

            // Nhánh này chưa có query cho các bảng này; kiểm tra bảng có mặt sau migration.
            listOf(
                "PantryCompartmentEntity",
                "CustomRecipeEntity",
                "WeeklyMealPlanSettingsEntity",
                "ExtraShoppingItemEntity",
                "LeftoverEntity",
            ).forEach { table ->
                val count = driver.executeQuery(
                    identifier = null,
                    sql = "SELECT COUNT(*) FROM sqlite_master WHERE type = 'table' AND name = '$table'",
                    mapper = { cursor ->
                        cursor.next()
                        QueryResult.Value(cursor.getLong(0))
                    },
                    parameters = 0,
                ).value
                assertEquals(1L, count, "thiếu bảng $table sau migration 7→8")
            }
        } finally {
            driver.close()
        }
    }
}
