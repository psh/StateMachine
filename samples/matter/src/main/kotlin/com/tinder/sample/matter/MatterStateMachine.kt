package com.tinder.sample.matter

import com.tinder.StateMachine
import com.tinder.Transition
import com.tinder.sample.matter.MatterEvent.*
import com.tinder.sample.matter.MatterSideEffect.*
import com.tinder.sample.matter.MatterState.Liquid
import com.tinder.sample.matter.MatterState.Solid

const val ON_MELTED_MESSAGE = "I melted"
const val ON_FROZEN_MESSAGE = "I froze"
const val ON_VAPORIZED_MESSAGE = "I vaporized"
const val ON_CONDENSED_MESSAGE = "I condensed"

var logger: Logger = object : Logger {
    override fun log(message: String) = Unit
}

val stateMachine = StateMachine.create<MatterState, MatterEvent, MatterSideEffect> {
    initialState(Solid)

    state<Solid> {
        on<OnMelted> { transitionTo(Liquid, LogMelted) }
    }

    state<Liquid> {
        on<OnFrozen> { transitionTo(Solid, LogFrozen) }
        on<OnVaporized> { transitionTo(MatterState.Gas, LogVaporized) }
    }

    state<MatterState.Gas> {
        on<OnCondensed> { transitionTo(Liquid, LogCondensed) }
    }

    onTransition {
        val validTransition = it as? Transition.Valid ?: return@onTransition
        when (validTransition.sideEffect) {
            LogMelted -> logger.log(ON_MELTED_MESSAGE)
            LogFrozen -> logger.log(ON_FROZEN_MESSAGE)
            LogVaporized -> logger.log(ON_VAPORIZED_MESSAGE)
            LogCondensed -> logger.log(ON_CONDENSED_MESSAGE)
        }
    }
}