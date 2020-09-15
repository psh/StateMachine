package com.tinder.sample.matter

import com.nhaarman.mockitokotlin2.inOrder
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

class GlobalNotificationStateMachineTest {

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
        stateMachine.transition(OnMelted)

        // Then
        assertThat(stateMachine.state).isEqualTo(Liquid)
        logger.inOrder {
            verify().log("Global Exit Solid by OnMelted")
            verify().log("Exit Solid")
            verify().log("Global Enter Liquid by OnMelted")
            verify().log("Enter Liquid")
        }
    }

    @Test
    fun givenStateIsLiquid_onFroze_shouldTransitionToSolidStateAndLog() {
        // Given
        val stateMachine = givenStateIs(Liquid)

        // When
        stateMachine.transition(OnFrozen)

        // Then
        assertThat(stateMachine.state).isEqualTo(Solid)
        logger.inOrder {
            verify().log("Global Exit Liquid by OnFrozen")
            verify().log("Exit Liquid")
            verify().log("Global Enter Solid by OnFrozen")
            verify().log("Enter Solid")
        }
    }

    @Test
    fun givenStateIsLiquid_onVaporized_shouldTransitionToGasStateAndLog() {
        // Given
        val stateMachine = givenStateIs(Liquid)

        // When
        stateMachine.transition(OnVaporized)

        // Then
        assertThat(stateMachine.state).isEqualTo(Gas)
        logger.inOrder {
            verify().log("Global Exit Liquid by OnVaporized")
            verify().log("Exit Liquid")
            verify().log("Global Enter Gas by OnVaporized")
            verify().log("Enter Gas")
        }
    }

    @Test
    fun givenStateIsGas_onCondensed_shouldTransitionToLiquidStateAndLog() {
        // Given
        val stateMachine = givenStateIs(Gas)

        // When
        stateMachine.transition(OnCondensed)

        // Then
        assertThat(stateMachine.state).isEqualTo(Liquid)
        logger.inOrder {
            verify().log("Global Exit Gas by OnCondensed")
            verify().log("Exit Gas")
            verify().log("Global Enter Liquid by OnCondensed")
            verify().log("Enter Liquid")
        }
    }

    private fun givenStateIs(state: MatterState): StateMachine<MatterState, MatterEvent, MatterSideEffect> {
        return stateMachine.with { initialState(state) }
    }
}