package com.loitu.yourrefri.feature.recipe.data

import app.cash.sqldelight.db.SqlDriver
import app.cash.sqldelight.driver.jdbc.sqlite.JdbcSqliteDriver
import com.loitu.yourrefri.feature.recipe.logic.DishStyle
import com.loitu.yourrefri.feature.recipe.logic.FamilyTasteProfile
import com.loitu.yourrefri.feature.recipe.logic.TasteFlavor
import com.loitu.yourrefri.shared.db.YourRefriDatabase
import com.loitu.yourrefri.shared.db.createDatabase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals

class TasteProfileRepositoryTest {
    private lateinit var driver: SqlDriver
    private lateinit var db: YourRefriDatabase

    @BeforeTest fun setUp() {
        driver = JdbcSqliteDriver(JdbcSqliteDriver.IN_MEMORY)
        YourRefriDatabase.Schema.create(driver)
        db = createDatabase(driver)
    }

    @AfterTest fun tearDown() = driver.close()

    private fun repository() = SqlTasteProfileRepository(db, Dispatchers.Unconfined)

    @Test fun startsWithEmptyProfile() = runTest {
        assertEquals(FamilyTasteProfile(), repository().observe().first())
    }

    @Test fun saveReadAndObserveRoundTrip() = runTest {
        val profile = FamilyTasteProfile(
            flavors = setOf(TasteFlavor.SPICY, TasteFlavor.LIGHT),
            dishStyles = setOf(DishStyle.SOUP),
            avoidIngredientIds = setOf("fish", "milk"),
            hasYoungKids = true,
        )
        repository().save(profile)

        assertEquals(profile, repository().observe().first())
        assertEquals(profile, repository().observe().first())
    }

    @Test fun unknownEnumNamesAreIgnored() = runTest {
        db.tasteProfileEntityQueries.upsertTasteProfile(1L, "LIGHT,FUTURE", "SOUP,NEW", "milk", 0L)

        assertEquals(
            FamilyTasteProfile(
                flavors = setOf(TasteFlavor.LIGHT),
                dishStyles = setOf(DishStyle.SOUP),
                avoidIngredientIds = setOf("milk"),
            ),
            repository().observe().first(),
        )
    }
}
