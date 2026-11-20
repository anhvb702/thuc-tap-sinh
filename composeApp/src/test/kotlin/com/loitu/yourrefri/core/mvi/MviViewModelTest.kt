package com.loitu.yourrefri.core.mvi

import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals

class MviViewModelTest {
    private data class State(val count: Int = 0)
    private sealed interface Intent { data object Increment : Intent }
    private sealed interface Effect { data object Incremented : Effect }

    private class TestViewModel : MviViewModel<State, Intent, Effect>(State()) {
        override fun onIntent(intent: Intent) {
            when (intent) {
                Intent.Increment -> {
                    updateState { copy(count = currentState.count + 1) }
                    sendEffect(Effect.Incremented)
                }
            }
        }
    }

    @Test
    fun exposesInitialState() {
        assertEquals(State(), TestViewModel().state.value)
    }

    @Test
    fun intentUpdatesState() {
        val viewModel = TestViewModel()
        viewModel.onIntent(Intent.Increment)
        assertEquals(State(1), viewModel.state.value)
    }

    @Test
    fun intentEmitsOneShotEffect() = runTest {
        val viewModel = TestViewModel()
        viewModel.onIntent(Intent.Increment)
        assertEquals(Effect.Incremented, viewModel.effects.first())
    }
}
