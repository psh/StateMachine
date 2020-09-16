package com.tinder

import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.then
import org.assertj.core.api.Assertions
import org.assertj.core.api.Assertions.*
import org.junit.Test
import org.junit.experimental.runners.Enclosed
import org.junit.runner.RunWith

@RunWith(Enclosed::class)
class ObjectStateMachineTest {

    class WithInitialState {

        private val onTransitionListener1 = mock<(Transition<State, Event, SideEffect>) -> Unit>()
        private val onTransitionListener2 = mock<(Transition<State, Event, SideEffect>) -> Unit>()
        private val onStateAExitListener1 = mock<State.(Event) -> Unit>()
        private val onStateAExitListener2 = mock<State.(Event) -> Unit>()
        private val onStateCEnterListener1 = mock<State.(Event) -> Unit>()
        private val onStateCEnterListener2 = mock<State.(Event) -> Unit>()
        private val stateMachine = StateMachine.create<State, Event, SideEffect> {
            initialState(State.A)
            state<State.A> {
                onExit(onStateAExitListener1)
                onExit(onStateAExitListener2)
                on<Event.E1> { transitionTo(State.B) }
                on<Event.E2> { transitionTo(State.C) }
                on<Event.E4> { transitionTo(State.D) }
            }
            state<State.B> {
                on<Event.E3> { transitionTo(State.C, SideEffect.SE1) }
            }
            state<State.C> {
                onEnter(onStateCEnterListener1)
                onEnter(onStateCEnterListener2)
                on<Event.E4> { dontTransition() }
            }
            onTransition(onTransitionListener1)
            onTransition(onTransitionListener2)
        }

        @Test
        fun state_shouldReturnInitialState() {
            // When
            val state = stateMachine.state

            // Then
            assertThat(state).isEqualTo(State.A)
        }

        @Test
        fun transition_givenValidEvent_shouldReturnTransition() {
            // When
            val transitionFromStateAToStateB = stateMachine.transition(Event.E1)

            // Then
            assertThat(transitionFromStateAToStateB).isEqualTo(
                Transition.Valid(State.A, Event.E1, State.B, null)
            )

            // When
            val transitionFromStateBToStateC = stateMachine.transition(Event.E3)

            // Then
            assertThat(transitionFromStateBToStateC).isEqualTo(
                Transition.Valid(State.B, Event.E3, State.C, SideEffect.SE1)
            )
        }

        @Test
        fun transition_givenValidEvent_shouldCreateAndSetNewState() {
            // When
            stateMachine.transition(Event.E1)

            // Then
            assertThat(stateMachine.state).isEqualTo(State.B)

            // When
            stateMachine.transition(Event.E3)

            // Then
            assertThat(stateMachine.state).isEqualTo(State.C)
        }

        @Test
        fun transition_givenValidEvent_shouldTriggerOnStateChangeListener() {
            // When
            stateMachine.transition(Event.E1)

            // Then
            then(onTransitionListener1).should().invoke(
                Transition.Valid(State.A, Event.E1, State.B, null)
            )

            // When
            stateMachine.transition(Event.E3)

            // Then
            then(onTransitionListener2).should().invoke(Transition.Valid(State.B, Event.E3, State.C, SideEffect.SE1))

            // When
            stateMachine.transition(Event.E4)

            // Then
            then(onTransitionListener2).should().invoke(Transition.Valid(State.C, Event.E4, State.C, null))
        }

        @Test
        fun transition_givenValidEvent_shouldTriggerOnEnterListeners() {
            // When
            stateMachine.transition(Event.E2)

            // Then
            then(onStateCEnterListener1).should().invoke(State.C, Event.E2)
            then(onStateCEnterListener2).should().invoke(State.C, Event.E2)
        }

        @Test
        fun transition_givenValidEvent_shouldTriggerOnExitListeners() {
            // When
            stateMachine.transition(Event.E2)

            // Then
            then(onStateAExitListener1).should().invoke(State.A, Event.E2)
            then(onStateAExitListener2).should().invoke(State.A, Event.E2)
        }

        @Test
        fun transition_givenInvalidEvent_shouldReturnInvalidTransition() {
            // When
            val fromState = stateMachine.state
            val transition = stateMachine.transition(Event.E3)

            // Then
            assertThat(transition).isEqualTo(
                Transition.Invalid<State, Event, SideEffect>(State.A, Event.E3)
            )
            assertThat(stateMachine.state).isEqualTo(fromState)
        }

        @Test
        fun transition_givenUndeclaredState_shouldThrowIllegalStateException() {
            // Then
            assertThatIllegalStateException().isThrownBy {
                stateMachine.transition(Event.E4)
            }
        }
    }

    class WithoutInitialState {

        @Test
        fun create_givenNoInitialState_shouldThrowIllegalArgumentException() {
            // Then
            assertThatIllegalArgumentException().isThrownBy {
                StateMachine.create<State, Event, SideEffect> {}
            }
        }
    }

    private companion object {
        private sealed class State {
            object A : State()
            object B : State()
            object C : State()
            object D : State()
        }

        private sealed class Event {
            object E1 : Event()
            object E2 : Event()
            object E3 : Event()
            object E4 : Event()
        }

        private sealed class SideEffect {
            object SE1 : SideEffect()
        }
    }
}
