package com.tinder.sample.matter

import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.then
import com.tinder.StateMachine
import com.tinder.Transition
import com.tinder.sample.matter.MatterEvent.*
import com.tinder.sample.matter.MatterSideEffect.*
import com.tinder.sample.matter.MatterState.*
import org.assertj.core.api.Assertions.assertThat
import org.junit.Before
import org.junit.Test

class MatterStateMachineTest {

    @Before
    fun setUp() {
        logger = mock()
    }

    @Test
    fun initialState_shouldBeSolid() {
        // Then
        assertThat(stateMachine.state).isEqualTo(Solid)
    }

    @Test
    fun givenStateIsSolid_onMelted_shouldTransitionToLiquidStateAndLog() {
        // Given
        val stateMachine = givenStateIs(Solid)

        // When
        val transition = stateMachine.transition(OnMelted)

        // Then
        assertThat(stateMachine.state).isEqualTo(Liquid)
        assertThat(transition).isEqualTo(
                Transition.Valid(Solid, OnMelted, Liquid, LogMelted)
        )
        then(logger).should().log(ON_MELTED_MESSAGE)
    }

    @Test
    fun givenStateIsLiquid_onFroze_shouldTransitionToSolidStateAndLog() {
        // Given
        val stateMachine = givenStateIs(Liquid)

        // When
        val transition = stateMachine.transition(OnFrozen)

        // Then
        assertThat(stateMachine.state).isEqualTo(Solid)
        assertThat(transition).isEqualTo(
                Transition.Valid(Liquid, OnFrozen, Solid, LogFrozen)
        )
        then(logger).should().log(ON_FROZEN_MESSAGE)
    }

    @Test
    fun givenStateIsLiquid_onVaporized_shouldTransitionToGasStateAndLog() {
        // Given
        val stateMachine = givenStateIs(Liquid)

        // When
        val transition = stateMachine.transition(OnVaporized)

        // Then
        assertThat(stateMachine.state).isEqualTo(Gas)
        assertThat(transition).isEqualTo(
                Transition.Valid(Liquid, OnVaporized, Gas, LogVaporized)
        )
        then(logger).should().log(ON_VAPORIZED_MESSAGE)
    }

    @Test
    fun givenStateIsGas_onCondensed_shouldTransitionToLiquidStateAndLog() {
        // Given
        val stateMachine = givenStateIs(Gas)

        // When
        val transition = stateMachine.transition(OnCondensed)

        // Then
        assertThat(stateMachine.state).isEqualTo(Liquid)
        assertThat(transition).isEqualTo(
                Transition.Valid(Gas, OnCondensed, Liquid, LogCondensed)
        )
        then(logger).should().log(ON_CONDENSED_MESSAGE)
    }

    private fun givenStateIs(state: MatterState): StateMachine<MatterState, MatterEvent, MatterSideEffect> {
        return stateMachine.with { initialState(state) }
    }
}