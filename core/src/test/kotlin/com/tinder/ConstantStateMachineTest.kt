package com.tinder

import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.then
import org.assertj.core.api.Assertions
import org.assertj.core.api.Assertions.*
import org.junit.Test
import org.junit.experimental.runners.Enclosed
import org.junit.runner.RunWith

@RunWith(Enclosed::class)
class ConstantStateMachineTest {

    class WithInitialState {

        private val onTransitionListener1 = mock<(Transition<String, Int, String>) -> Unit>()
        private val onTransitionListener2 = mock<(Transition<String, Int, String>) -> Unit>()
        private val onStateCEnterListener1 = mock<String.(Int) -> Unit>()
        private val onStateCEnterListener2 = mock<String.(Int) -> Unit>()
        private val onStateAExitListener1 = mock<String.(Int) -> Unit>()
        private val onStateAExitListener2 = mock<String.(Int) -> Unit>()
        private val stateMachine = StateMachine.create<String, Int, String> {
            initialState(STATE_A)
            state(STATE_A) {
                onExit(onStateAExitListener1)
                onExit(onStateAExitListener2)
                on(EVENT_1) { transitionTo(STATE_B) }
                on(EVENT_2) { transitionTo(STATE_C) }
                on(EVENT_4) { transitionTo(STATE_D) }
            }
            state(STATE_B) {
                on(EVENT_3) { transitionTo(STATE_C, SIDE_EFFECT_1) }
            }
            state(STATE_C) {
                onEnter(onStateCEnterListener1)
                onEnter(onStateCEnterListener2)
            }
            onTransition(onTransitionListener1)
            onTransition(onTransitionListener2)
        }

        @Test
        fun state_shouldReturnInitialState() {
            // When
            val state = stateMachine.state

            // Then
            assertThat(state).isEqualTo(STATE_A)
        }

        @Test
        fun transition_givenValidEvent_shouldReturnTrue() {
            // When
            val transitionFromStateAToStateB = stateMachine.transition(EVENT_1)

            // Then
            assertThat(transitionFromStateAToStateB).isEqualTo(
                Transition.Valid(STATE_A, EVENT_1, STATE_B, null)
            )

            // When
            val transitionFromStateBToStateC = stateMachine.transition(EVENT_3)

            // Then
            assertThat(transitionFromStateBToStateC).isEqualTo(
                Transition.Valid(STATE_B, EVENT_3, STATE_C, SIDE_EFFECT_1)
            )
        }

        @Test
        fun transition_givenValidEvent_shouldCreateAndSetNewState() {
            // When
            stateMachine.transition(EVENT_1)

            // Then
            assertThat(stateMachine.state).isEqualTo(STATE_B)

            // When
            stateMachine.transition(EVENT_3)

            // Then
            assertThat(stateMachine.state).isEqualTo(STATE_C)
        }

        @Test
        fun transition_givenValidEvent_shouldTriggerOnStateChangeListener() {
            // When
            stateMachine.transition(EVENT_1)

            // Then
            then(onTransitionListener1).should().invoke(
                Transition.Valid(STATE_A, EVENT_1, STATE_B, null)
            )

            // When
            stateMachine.transition(EVENT_3)

            // Then
            then(onTransitionListener2).should().invoke(
                Transition.Valid(STATE_B, EVENT_3, STATE_C, SIDE_EFFECT_1)
            )
        }

        @Test
        fun transition_givenValidEvent_shouldTriggerOnEnterListeners() {
            // When
            stateMachine.transition(EVENT_2)

            // Then
            then(onStateCEnterListener1).should().invoke(STATE_C, EVENT_2)
            then(onStateCEnterListener2).should().invoke(STATE_C, EVENT_2)
        }

        @Test
        fun transition_givenValidEvent_shouldTriggerOnExitListeners() {
            // When
            stateMachine.transition(EVENT_2)

            // Then
            then(onStateAExitListener1).should().invoke(STATE_A, EVENT_2)
            then(onStateAExitListener2).should().invoke(STATE_A, EVENT_2)
        }

        @Test
        fun transition_givenInvalidEvent_shouldReturnInvalidTransition() {
            // When
            val fromState = stateMachine.state
            val transition = stateMachine.transition(EVENT_3)

            // Then
            assertThat(transition).isEqualTo(
                Transition.Invalid<String, Int, String>(STATE_A, EVENT_3)
            )
            assertThat(stateMachine.state).isEqualTo(fromState)
        }

        @Test
        fun transition_givenUndeclaredState_shouldThrowIllegalStateException() {
            // Then
            assertThatIllegalStateException().isThrownBy { stateMachine.transition(EVENT_4) }
        }
    }

    class WithoutInitialState {

        @Test
        fun create_givenNoInitialState_shouldThrowIllegalArgumentException() {
            // Then
            assertThatIllegalArgumentException().isThrownBy {
                StateMachine.create<String, Int, String> {}
            }
        }
    }

    class WithMissingStateDefinition {

        private val stateMachine = StateMachine.create<String, Int, Nothing> {
            initialState(STATE_A)
            state(STATE_A) {
                on(EVENT_1) { transitionTo(STATE_B) }
            }
            // Missing STATE_B definition.
        }

        @Test
        fun transition_givenMissingDestinationStateDefinition_shouldThrowIllegalStateExceptionWithStateName() {
            // Then
            assertThatIllegalStateException()
                .isThrownBy { stateMachine.transition(EVENT_1) }
                .withMessage("Missing definition for state ${STATE_B.javaClass.simpleName}!")
        }
    }

    private companion object {
        private const val STATE_A = "a"
        private const val STATE_B = "b"
        private const val STATE_C = "c"
        private const val STATE_D = "d"

        private const val EVENT_1 = 1
        private const val EVENT_2 = 2
        private const val EVENT_3 = 3
        private const val EVENT_4 = 4

        private const val SIDE_EFFECT_1 = "alpha"
    }
}
