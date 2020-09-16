package com.tinder.sample.turnstile

import com.tinder.graph
import com.tinder.sample.turnstile.TurnstileCommand.*
import com.tinder.sample.turnstile.TurnstileEvent.*
import com.tinder.sample.turnstile.TurnstileState.*

private const val FARE_PRICE = 50

val stateMachine = graph<TurnstileState, TurnstileEvent, TurnstileCommand> {
    initialState(Locked(credit = 0))

    state<Locked> {
        on<InsertCoin> {
            val newCredit = credit + it.value
            if (newCredit >= FARE_PRICE) {
                transitionTo(Unlocked, OpenDoors)
            } else {
                transitionTo(Locked(newCredit))
            }
        }

        on<AdmitPerson> { dontTransition(SoundAlarm) }

        on<MachineDidFail> { transitionTo(TurnstileState.Broken(this), OrderRepair) }
    }

    state<Unlocked> {
        on<AdmitPerson> { transitionTo(Locked(credit = 0), CloseDoors) }
    }

    state<Broken> {
        on<MachineRepairDidComplete> { transitionTo(oldState) }
    }
}