package com.loitu.yourrefri.di

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.loitu.yourrefri.feature.backup.data.PantryBackupRepositoryImpl
import com.loitu.yourrefri.feature.cooklog.data.CookLogRepository
import com.loitu.yourrefri.feature.cooklog.data.CookLogRepositoryImpl
import com.loitu.yourrefri.feature.cooklog.ui.CookHistoryViewModel
import com.loitu.yourrefri.feature.home.presentation.HomeViewModel
import com.loitu.yourrefri.feature.pantry.data.Ingredient
import com.loitu.yourrefri.feature.pantry.data.PantryRepository
import com.loitu.yourrefri.feature.pantry.data.PantryRepositoryImpl
import com.loitu.yourrefri.feature.pantry.data.ShelfTable
import com.loitu.yourrefri.feature.pantry.ui.PantryGridViewModel
import com.loitu.yourrefri.feature.recipe.data.FavoriteRecipeRepository
import com.loitu.yourrefri.feature.recipe.data.FavoriteRecipeRepositoryImpl
import com.loitu.yourrefri.feature.recipe.data.Recipe
import com.loitu.yourrefri.feature.recipe.data.RecipeBundle
import com.loitu.yourrefri.feature.recipe.data.SqlTasteProfileRepository
import com.loitu.yourrefri.feature.recipe.data.TasteProfileRepository
import com.loitu.yourrefri.feature.recipe.data.WeeklyMealPlanRepository
import com.loitu.yourrefri.feature.recipe.data.WeeklyMealPlanRepositoryImpl
import com.loitu.yourrefri.feature.recipe.ui.FavoriteRecipesViewModel
import com.loitu.yourrefri.feature.recipe.ui.WeeklyMealPlanViewModel
import com.loitu.yourrefri.feature.recipe.presentation.RecipeViewModel
import com.loitu.yourrefri.shared.db.DriverFactory
import com.loitu.yourrefri.shared.db.createDatabase
import kotlinx.coroutines.Dispatchers
import kotlinx.datetime.TimeZone
import kotlin.time.Clock
import kotlin.time.ExperimentalTime

interface AppContainer {
    val pantryRepository: PantryRepository
    val weeklyMealPlanRepository: WeeklyMealPlanRepository
    val tasteProfileRepository: TasteProfileRepository
    val favoriteRepository: FavoriteRecipeRepository
    val cookLogRepository: CookLogRepository
    val backupRepository: PantryBackupRepositoryImpl
    val ingredients: List<Ingredient>
    val recipes: List<Recipe>

    fun createPantryGridViewModel(): PantryGridViewModel
    fun pantryGridViewModelFactory(): ViewModelProvider.Factory
    fun homeViewModelFactory(): ViewModelProvider.Factory
    fun recipeViewModelFactory(): ViewModelProvider.Factory
    fun weeklyMealPlanViewModelFactory(): ViewModelProvider.Factory
    fun favoriteRecipesViewModelFactory(): ViewModelProvider.Factory
    fun cookHistoryViewModelFactory(): ViewModelProvider.Factory
}

@OptIn(ExperimentalTime::class)
class DefaultAppContainer(context: Context) : AppContainer {
    private val appContext = context.applicationContext
    private val db = createDatabase(DriverFactory(appContext).create())
    private val clock = Clock.System
    private val timeZone = TimeZone.currentSystemDefault()

    override val pantryRepository: PantryRepository = PantryRepositoryImpl(db, clock, timeZone, Dispatchers.IO)
    override val weeklyMealPlanRepository: WeeklyMealPlanRepository = WeeklyMealPlanRepositoryImpl(db, Dispatchers.IO)
    override val tasteProfileRepository: TasteProfileRepository = SqlTasteProfileRepository(db, Dispatchers.IO)
    override val favoriteRepository: FavoriteRecipeRepository = FavoriteRecipeRepositoryImpl(db, Dispatchers.IO, clock)
    override val cookLogRepository: CookLogRepository = CookLogRepositoryImpl(db, Dispatchers.IO)
    override val backupRepository = PantryBackupRepositoryImpl(db, Dispatchers.IO)
    override val ingredients: List<Ingredient> = appContext.assets.open("shelf_table.json").bufferedReader().use {
        ShelfTable.parse(it.readText())
    }
    override val recipes: List<Recipe> = appContext.assets.open("recipes.json").bufferedReader().use {
        RecipeBundle.parse(it.readText())
    }

    override fun createPantryGridViewModel() = PantryGridViewModel(pantryRepository, ingredients, clock, timeZone)

    override fun pantryGridViewModelFactory() = singleViewModelFactory {
        PantryGridViewModel(pantryRepository, ingredients, clock, timeZone)
    }

    override fun homeViewModelFactory() = singleViewModelFactory { HomeViewModel() }

    override fun recipeViewModelFactory() = singleViewModelFactory { RecipeViewModel() }

    override fun weeklyMealPlanViewModelFactory() = singleViewModelFactory {
        WeeklyMealPlanViewModel(weeklyMealPlanRepository, clock, timeZone)
    }

    override fun favoriteRecipesViewModelFactory() = singleViewModelFactory {
        FavoriteRecipesViewModel(favoriteRepository)
    }

    override fun cookHistoryViewModelFactory() = singleViewModelFactory {
        CookHistoryViewModel(cookLogRepository, clock, timeZone)
    }
}

object AppGraph {
    @Volatile private var cached: AppContainer? = null

    fun get(context: Context): AppContainer =
        cached ?: synchronized(this) { cached ?: DefaultAppContainer(context.applicationContext).also { cached = it } }
}

inline fun <reified VM : ViewModel> singleViewModelFactory(
    crossinline creator: () -> VM,
): ViewModelProvider.Factory = object : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        require(modelClass.isAssignableFrom(VM::class.java))
        @Suppress("UNCHECKED_CAST")
        return creator() as T
    }
}
