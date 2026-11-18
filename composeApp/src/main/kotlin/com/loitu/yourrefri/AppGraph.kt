package com.loitu.yourrefri

import android.content.Context
import com.loitu.yourrefri.feature.backup.data.PantryBackupRepositoryImpl
import com.loitu.yourrefri.feature.pantry.data.Ingredient
import com.loitu.yourrefri.feature.pantry.data.LeftoverRepositoryImpl
import com.loitu.yourrefri.feature.pantry.data.PantryRepositoryImpl
import com.loitu.yourrefri.feature.pantry.data.ShelfTable
import com.loitu.yourrefri.feature.recipe.data.CustomRecipeRepositoryImpl
import com.loitu.yourrefri.feature.recipe.data.ExtraShoppingRepositoryImpl
import com.loitu.yourrefri.feature.recipe.data.FavoriteRecipeRepositoryImpl
import com.loitu.yourrefri.feature.recipe.data.Recipe
import com.loitu.yourrefri.feature.recipe.data.RecipeBundle
import com.loitu.yourrefri.feature.recipe.data.WeeklyMealPlanRepositoryImpl
import com.loitu.yourrefri.shared.db.DriverFactory
import com.loitu.yourrefri.shared.db.createDatabase
import kotlinx.coroutines.Dispatchers
import kotlinx.datetime.TimeZone
import kotlin.time.Clock

@OptIn(kotlin.time.ExperimentalTime::class)
internal object AppGraph {
    @Volatile private var cached: AppDependencies? = null

    fun get(context: Context): AppDependencies =
        cached ?: synchronized(this) { cached ?: build(context.applicationContext).also { cached = it } }

    private fun build(context: Context): AppDependencies {
        val driver = DriverFactory(context).create()
        val db = createDatabase(driver)
        val repo = PantryRepositoryImpl(
            db = db,
            clock = Clock.System,
            timeZone = TimeZone.currentSystemDefault(),
            ioContext = Dispatchers.IO,
        )
        val json = context.assets.open("shelf_table.json").bufferedReader().use { it.readText() }
        val parsedIngredients = ShelfTable.parse(json)
        val recipeJson = context.assets.open("recipes.json").bufferedReader().use { it.readText() }
        return AppDependencies(
            pantryRepository = repo,
            ingredients = parsedIngredients,
            backupRepository = PantryBackupRepositoryImpl(db, Dispatchers.IO),
            recipes = RecipeBundle.parse(recipeJson),
            customRecipeRepository = CustomRecipeRepositoryImpl(db, Dispatchers.IO),
            weeklyMealPlanRepository = WeeklyMealPlanRepositoryImpl(db, Dispatchers.IO),
            favoriteRepository = FavoriteRecipeRepositoryImpl(db, Dispatchers.IO, Clock.System),
            cookLogRepository = com.loitu.yourrefri.feature.cooklog.data.CookLogRepositoryImpl(db, Dispatchers.IO),
            leftoverRepository = LeftoverRepositoryImpl(db, Dispatchers.IO),
            extraShoppingRepository = ExtraShoppingRepositoryImpl(db, Dispatchers.IO),
        )
    }
}

internal data class AppDependencies(
    val pantryRepository: PantryRepositoryImpl,
    val ingredients: List<Ingredient>,
    val backupRepository: PantryBackupRepositoryImpl,
    val recipes: List<Recipe>,
    val customRecipeRepository: CustomRecipeRepositoryImpl,
    val weeklyMealPlanRepository: WeeklyMealPlanRepositoryImpl,
    val favoriteRepository: FavoriteRecipeRepositoryImpl,
    val cookLogRepository: com.loitu.yourrefri.feature.cooklog.data.CookLogRepositoryImpl,
    val leftoverRepository: LeftoverRepositoryImpl,
    val extraShoppingRepository: ExtraShoppingRepositoryImpl,
)
