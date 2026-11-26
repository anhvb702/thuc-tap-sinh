package com.loitu.yourrefri.feature.home.presentation

import com.loitu.yourrefri.core.mvi.MviViewModel
import com.loitu.yourrefri.feature.home.data.HomeWeatherRepository
import com.loitu.yourrefri.feature.home.domain.usecase.BuildHomeDashboardUseCase
import com.loitu.yourrefri.feature.home.domain.usecase.PickHomeRandomRecipeUseCase
import com.loitu.yourrefri.feature.home.domain.model.WeatherCity
import com.loitu.yourrefri.feature.recipe.ui.randomWheelPool
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch

class HomeViewModel(
    private val buildHomeDashboard: BuildHomeDashboardUseCase = BuildHomeDashboardUseCase(),
    private val pickRandomRecipe: PickHomeRandomRecipeUseCase = PickHomeRandomRecipeUseCase(),
    private val weatherRepository: HomeWeatherRepository? = null,
) : MviViewModel<HomeUiState, HomeIntent, HomeEffect>(HomeUiState()) {
    private var lastSyncedInputs: HomeIntent.SyncInputs? = null

    override fun onIntent(intent: HomeIntent) {
        when (intent) {
            is HomeIntent.SyncInputs -> {
                if (intent == lastSyncedInputs) return
                val dashboard = buildHomeDashboard(
                    state = intent.pantryState,
                    recipes = intent.recipes,
                    meal = intent.meal,
                    tasteProfile = intent.tasteProfile,
                )
                lastSyncedInputs = intent
                updateState { copy(dashboard = dashboard, meal = intent.meal) }
            }
            HomeIntent.OpenRandomWheel, HomeIntent.SpinRandomAgain -> openRandomWheel()
            is HomeIntent.SetRandomOnlyReady -> {
                updateState { copy(randomOnlyReady = intent.onlyReady) }
                if (currentState.showRandomWheel) openRandomWheel()
            }
            HomeIntent.DismissRandomWheel -> updateState { copy(showRandomWheel = false) }
            HomeIntent.DismissNoCookableDialog -> updateState { copy(showNoCookableDialog = false) }
            is HomeIntent.SelectRandomRecipe -> {
                updateState { copy(showRandomWheel = false) }
                sendEffect(HomeEffect.OpenRecipeDetail(intent.recipeId))
            }
            HomeIntent.LoadWeather -> {
                if (weatherRepository == null || currentState.weatherLoading || currentState.weather?.city == currentState.weatherCity) return
                loadWeatherFor(currentState.weatherCity)
            }
            HomeIntent.ToggleWeatherCity -> {
                val nextCity = currentState.weatherCity.next()
                updateState { copy(weatherCity = nextCity, weather = null) }
                if (weatherRepository != null) loadWeatherFor(nextCity)
            }
        }
    }

    private fun loadWeatherFor(city: WeatherCity) {
        val repository = weatherRepository ?: return
        updateState { copy(weatherLoading = true) }
        viewModelScope.launch {
            val fetched = repository.fetchCurrent(city)
            if (currentState.weatherCity == city) {
                updateState { copy(weather = fetched, weatherLoading = false) }
            }
        }
    }

    private fun openRandomWheel() {
        val inputs = lastSyncedInputs
        val pool = randomWheelPool(
            cookableRecipes = currentState.dashboard.cookableRecipes,
            allRecipes = inputs?.recipes.orEmpty(),
            onlyReadyIngredients = currentState.randomOnlyReady,
        )
        if (pool.isEmpty()) {
            updateState { copy(showRandomWheel = false, showNoCookableDialog = true) }
            return
        }
        val pick = pickRandomRecipe(
            cookable = pool,
            state = inputs?.pantryState ?: return,
            excludeId = currentState.randomPick?.recipe?.id,
            onlyCookable = currentState.randomOnlyReady,
        ) ?: return
        updateState { copy(randomPick = pick, showRandomWheel = true, showNoCookableDialog = false) }
    }
}
