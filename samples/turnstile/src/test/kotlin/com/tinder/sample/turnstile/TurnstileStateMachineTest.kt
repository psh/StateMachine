package com.tinder.sample.turnstile

import com.tinder.StateMachine
import com.tinder.Transition
import com.tinder.sample.turnstile.TurnstileCommand.*
import com.tinder.sample.turnstile.TurnstileEvent.*
import com.tinder.sample.turnstile.TurnstileState.*
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test

class TurnstileStateMachineTest {

    @Test
    fun initialState_shouldBeLocked() {
        // Given
        val stateMachine = stateMachine.with { initialState(Locked(credit = 0)) }

        // Then
        assertThat(stateMachine.state).isEqualTo(Locked(credit = 0))
    }

    @Test
    fun givenStateIsLocked_whenInsertCoin_andCreditLessThanFairPrice_shouldTransitionToLockedState() {
        // When
        val transition = stateMachine.transition(InsertCoin(10))

        // Then
        assertThat(stateMachine.state).isEqualTo(Locked(credit = 10))
        assertThat(transition).isEqualTo(
                Transition.Valid(
                        Locked(credit = 0),
                        InsertCoin(10),
                        Locked(credit = 10),
                        null
                )
        )
    }

    @Test
    fun givenStateIsLocked_whenInsertCoin_andCreditEqualsFairPrice_shouldTransitionToUnlockedStateAndOpenDoors() {
        // Given
        val stateMachine = givenStateIs(Locked(credit = 35))

        // When
        val transition = stateMachine.transition(InsertCoin(15))

        // Then
        assertThat(stateMachine.state).isEqualTo(Unlocked)
        assertThat(transition).isEqualTo(
                Transition.Valid(
                        Locked(credit = 35),
                        InsertCoin(15),
                        Unlocked,
                        OpenDoors
                )
        )
    }

    @Test
    fun givenStateIsLocked_whenInsertCoin_andCreditMoreThanFairPrice_shouldTransitionToUnlockedStateAndOpenDoors() {
        // Given
        val stateMachine = givenStateIs(Locked(credit = 35))

        // When
        val transition = stateMachine.transition(InsertCoin(20))

        // Then
        assertThat(stateMachine.state).isEqualTo(Unlocked)
        assertThat(transition).isEqualTo(
                Transition.Valid(
                        Locked(credit = 35),
                        InsertCoin(20),
                        Unlocked,
                        OpenDoors
                )
        )
    }

    @Test
    fun givenStateIsLocked_whenAdmitPerson_shouldTransitionToLockedStateAndSoundAlarm() {
        // Given
        val stateMachine = givenStateIs(Locked(credit = 35))

        // When
        val transition = stateMachine.transition(AdmitPerson)

        // Then
        assertThat(stateMachine.state).isEqualTo(Locked(credit = 35))
        assertThat(transition).isEqualTo(
                Transition.Valid(
                        Locked(credit = 35),
                        AdmitPerson,
                        Locked(credit = 35),
                        SoundAlarm
                )
        )
    }

    @Test
    fun givenStateIsLocked_whenMachineDidFail_shouldTransitionToBrokenStateAndOrderRepair() {
        // Given
        val stateMachine = givenStateIs(Locked(credit = 15))

        // When
        val transitionToBroken = stateMachine.transition(MachineDidFail)

        // Then
        assertThat(stateMachine.state).isEqualTo(Broken(oldState = Locked(credit = 15)))
        assertThat(transitionToBroken).isEqualTo(
                Transition.Valid(
                        Locked(credit = 15),
                        MachineDidFail,
                        Broken(oldState = Locked(credit = 15)),
                        OrderRepair
                )
        )
    }

    @Test
    fun givenStateIsUnlocked_whenAdmitPerson_shouldTransitionToLockedStateAndCloseDoors() {
        // Given
        val stateMachine = givenStateIs(Unlocked)

        // When
        val transition = stateMachine.transition(AdmitPerson)

        // Then
        assertThat(stateMachine.state).isEqualTo(Locked(credit = 0))
        assertThat(transition).isEqualTo(
                Transition.Valid(
                        Unlocked,
                        AdmitPerson,
                        Locked(credit = 0),
                        CloseDoors
                )
        )
    }

    @Test
    fun givenStateIsBroken_whenMachineRepairDidComplete_shouldTransitionToLockedState() {
        // Given
        val stateMachine = givenStateIs(Broken(oldState = Locked(credit = 15)))

        // When
        val transition = stateMachine.transition(MachineRepairDidComplete)

        // Then
        assertThat(stateMachine.state).isEqualTo(Locked(credit = 15))
        assertThat(transition).isEqualTo(
                Transition.Valid(
                        Broken(oldState = Locked(credit = 15)),
                        MachineRepairDidComplete,
                        Locked(credit = 15),
                        null
                )
        )
    }

    private fun givenStateIs(state: TurnstileState): StateMachine<TurnstileState, TurnstileEvent, TurnstileCommand> =
            stateMachine.with { initialState(state) }
}