package com.tinder

class GraphBuilder<STATE : Any, EVENT : Any, SIDE_EFFECT : Any>(
    graph: Graph<STATE, EVENT, SIDE_EFFECT>? = null
) {
    private var initialState = graph?.initialState
    private val stateDefinitions = LinkedHashMap(graph?.stateDefinitions ?: emptyMap())
    private val onTransitionListeners = ArrayList(graph?.onTransitionListeners ?: emptyList())
    private val onEnterListeners =
        mutableListOf<(STATE, EVENT) -> Unit>().apply { graph?.let { addAll(graph.onEnterListeners) } }
    private val onExitListeners =
        mutableListOf<(STATE, EVENT) -> Unit>().apply { graph?.let { addAll(graph.onExitListeners) } }

    fun initialState(initialState: STATE) {
        this.initialState = initialState
    }

    fun <S : STATE> state(
        stateMatcher: Matcher<STATE, S>,
        init: StateDefinitionBuilder<S>.() -> Unit
    ) {
        stateDefinitions[stateMatcher] = StateDefinitionBuilder<S>().apply(init).build()
    }

    inline fun <reified S : STATE> state(noinline init: StateDefinitionBuilder<S>.() -> Unit) {
        state(Matcher.any(), init)
    }

    inline fun <reified S : STATE> state(state: S, noinline init: StateDefinitionBuilder<S>.() -> Unit) {
        state(Matcher.eq(state), init)
    }

    fun onTransition(listener: (Transition<STATE, EVENT, SIDE_EFFECT>) -> Unit) {
        onTransitionListeners.add(listener)
    }

    fun onEnter(listener: (STATE, EVENT) -> Unit) {
        onEnterListeners.add { state, cause -> listener(state, cause) }
    }

    fun onExit(listener: (STATE, EVENT) -> Unit) {
        onExitListeners.add { state, cause -> listener(state, cause) }
    }

    fun build(): Graph<STATE, EVENT, SIDE_EFFECT> {
        return Graph(
            requireNotNull(initialState),
            stateDefinitions.toMap(),
            onTransitionListeners.toList(),
            onEnterListeners,
            onExitListeners
        )
    }

    inner class StateDefinitionBuilder<S : STATE> {

        private val stateDefinition = State<STATE, EVENT, SIDE_EFFECT>()

        inline fun <reified E : EVENT> any(): Matcher<EVENT, E> = Matcher.any()

        inline fun <reified R : EVENT> eq(value: R): Matcher<EVENT, R> = Matcher.eq(value)

        fun <E : EVENT> on(
            eventMatcher: Matcher<EVENT, E>,
            createTransitionTo: S.(E) -> State.TransitionTo<STATE, SIDE_EFFECT>
        ) {
            stateDefinition.transitions[eventMatcher] = { state, event ->
                @Suppress("UNCHECKED_CAST")
                createTransitionTo((state as S), event as E)
            }
        }

        inline fun <reified E : EVENT> on(
            noinline createTransitionTo: S.(E) -> State.TransitionTo<STATE, SIDE_EFFECT>
        ) = on(any(), createTransitionTo)

        inline fun <reified E : EVENT> on(
            event: E,
            noinline createTransitionTo: S.(E) -> State.TransitionTo<STATE, SIDE_EFFECT>
        ) = on(eq(event), createTransitionTo)

        fun onEnter(listener: S.(EVENT) -> Unit) = with(stateDefinition) {
            onEnterListeners.add { state, cause ->
                @Suppress("UNCHECKED_CAST")
                listener(state as S, cause)
            }
        }

        fun onExit(listener: S.(EVENT) -> Unit) = with(stateDefinition) {
            onExitListeners.add { state, cause ->
                @Suppress("UNCHECKED_CAST")
                listener(state as S, cause)
            }
        }

        fun build() = stateDefinition

        @Suppress("UNUSED") // The unused warning is probably a compiler bug.
        fun S.transitionTo(state: STATE, sideEffect: SIDE_EFFECT? = null) =
            State.TransitionTo(state, sideEffect)

        @Suppress("UNUSED") // The unused warning is probably a compiler bug.
        fun S.dontTransition(sideEffect: SIDE_EFFECT? = null) = transitionTo(this, sideEffect)
    }
}