package com.loitu.yourrefri.feature.home.presentation

import com.loitu.yourrefri.core.mvi.MviViewModel
import com.loitu.yourrefri.feature.home.data.HomeWeatherRepository
import com.loitu.yourrefri.feature.home.domain.usecase.BuildHomeDashboardUseCase
import com.loitu.yourrefri.feature.home.domain.usecase.PickHomeRandomRecipeUseCase
import com.loitu.yourrefri.feature.home.domain.model.WeatherCity
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch

class HomeViewModel(
    private val buildHomeDashboard: BuildHomeDashboardUseCase = BuildHomeDashboardUseCase(),
    private val pickRandomRecipe: PickHomeRandomRecipeUseCase = PickHomeRandomRecipeUseCase(),
    private val weatherRepository: HomeWeatherRepository? = null,
) : MviViewModel<HomeUiState, HomeIntent, HomeEffect>(HomeUiState()) {
    override fun onIntent(intent: HomeIntent) {
        when (intent) {
            is HomeIntent.SyncInputs -> {
                latestPantryState = intent.pantryState
                updateState {
                copy(
                    dashboard = buildHomeDashboard(
                        state = intent.pantryState,
                        recipes = intent.recipes,
                        meal = intent.meal,
                        tasteProfile = intent.tasteProfile,
                    ),
                    meal = intent.meal,
                )
                }
            }
            HomeIntent.OpenRandomWheel, HomeIntent.SpinRandomAgain -> openRandomWheel()
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
        val cookable = currentState.dashboard.cookableRecipes
        if (cookable.isEmpty()) {
            updateState { copy(showRandomWheel = false, showNoCookableDialog = true) }
            return
        }
        val pick = pickRandomRecipe(
            cookable = cookable,
            state = latestPantryState ?: return,
            excludeId = currentState.randomPick?.recipe?.id,
        ) ?: return
        updateState { copy(randomPick = pick, showRandomWheel = true, showNoCookableDialog = false) }
    }

    private var latestPantryState: com.loitu.yourrefri.feature.pantry.logic.PantryGridState? = null
}
