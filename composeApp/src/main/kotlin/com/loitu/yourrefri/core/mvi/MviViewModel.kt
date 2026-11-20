package com.loitu.yourrefri.core.mvi

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update

abstract class MviViewModel<S : Any, I : Any, E : Any>(initialState: S) : ViewModel() {
    private val _state = MutableStateFlow(initialState)
    private val _effects = Channel<E>(Channel.BUFFERED)

    val state: StateFlow<S> = _state.asStateFlow()
    val effects: Flow<E> = _effects.receiveAsFlow()

    abstract fun onIntent(intent: I)

    protected val currentState: S get() = _state.value

    protected fun updateState(reducer: S.() -> S) {
        _state.update(reducer)
    }

    protected fun sendEffect(effect: E) {
        _effects.trySend(effect)
    }
}
