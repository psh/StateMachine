package com.tinder

import java.util.concurrent.atomic.AtomicReference

class StateMachine<STATE : Any, EVENT : Any, SIDE_EFFECT : Any> private constructor(
        private val graph: Graph<STATE, EVENT, SIDE_EFFECT>
) {

    private val stateRef = AtomicReference(graph.initialState)

    val state: STATE
        get() = stateRef.get()

    fun transition(event: EVENT): Transition<STATE, EVENT, SIDE_EFFECT> {
        val transition = synchronized(this) {
            val fromState = stateRef.get()
            val transition = fromState.getTransition(event)
            if (transition is Transition.Valid) {
                stateRef.set(transition.toState)
            }
            transition
        }
        transition.notifyOnTransition()
        if (transition is Transition.Valid) {
            with(transition) {
                with(fromState) {
                    notifyOnExit(event)
                }
                with(toState) {
                    notifyOnEnter(event)
                }
            }
        }
        return transition
    }

    fun with(init: GraphBuilder<STATE, EVENT, SIDE_EFFECT>.() -> Unit): StateMachine<STATE, EVENT, SIDE_EFFECT> {
        return create(graph.copy(initialState = state), init)
    }

    private fun STATE.getTransition(event: EVENT): Transition<STATE, EVENT, SIDE_EFFECT> {
        for ((eventMatcher, createTransitionTo) in getDefinition().transitions) {
            if (eventMatcher.matches(event)) {
                val (toState, sideEffect) = createTransitionTo(this, event)
                return Transition.Valid(this, event, toState, sideEffect)
            }
        }
        return Transition.Invalid(this, event)
    }

    private fun STATE.getDefinition() = graph.stateDefinitions
            .filter { it.key.matches(this) }
            .map { it.value }
            .firstOrNull() ?: error("Missing definition for state ${this.javaClass.simpleName}!")

    private fun STATE.notifyOnEnter(cause: EVENT) {
        graph.onEnterListeners.forEach {
            it(this, cause)
        }
        getDefinition().onEnterListeners.forEach {
            it(this, cause)
        }
    }

    private fun STATE.notifyOnExit(cause: EVENT) {
        graph.onExitListeners.forEach {
            it(this, cause)
        }
        getDefinition().onExitListeners.forEach {
            it(this, cause)
        }
    }

    private fun Transition<STATE, EVENT, SIDE_EFFECT>.notifyOnTransition() {
        graph.onTransitionListeners.forEach { it(this) }
    }

    companion object {
        fun <STATE : Any, EVENT : Any, SIDE_EFFECT : Any> create(
                init: GraphBuilder<STATE, EVENT, SIDE_EFFECT>.() -> Unit
        ): StateMachine<STATE, EVENT, SIDE_EFFECT> {
            return create(null, init)
        }

        private fun <STATE : Any, EVENT : Any, SIDE_EFFECT : Any> create(
                graph: Graph<STATE, EVENT, SIDE_EFFECT>?,
                init: GraphBuilder<STATE, EVENT, SIDE_EFFECT>.() -> Unit
        ): StateMachine<STATE, EVENT, SIDE_EFFECT> {
            return StateMachine(GraphBuilder(graph).apply(init).build())
        }
    }
}

fun <S : Any, E : Any, SE : Any> graph(init: GraphBuilder<S, E, SE>.() -> Unit) = StateMachine.create(init)