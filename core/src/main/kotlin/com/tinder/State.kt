package com.tinder

class State<STATE : Any, EVENT : Any, SIDE_EFFECT : Any> internal constructor() {
    val onEnterListeners = mutableListOf<(STATE, EVENT) -> Unit>()
    val onExitListeners = mutableListOf<(STATE, EVENT) -> Unit>()
    val transitions = linkedMapOf<Matcher<EVENT, EVENT>, (STATE, EVENT) -> TransitionTo<STATE, SIDE_EFFECT>>()

    data class TransitionTo<out STATE : Any, out SIDE_EFFECT : Any> internal constructor(
            val toState: STATE,
            val sideEffect: SIDE_EFFECT?
    )
}