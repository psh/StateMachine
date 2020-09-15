package com.tinder.sample.matter

import com.tinder.StateMachine
import com.tinder.sample.matter.MatterEvent.*
import com.tinder.sample.matter.MatterState.*

var logger: Logger = object : Logger {
    override fun log(message: String) = Unit
}

sealed class MatterState {
    object Solid : MatterState()
    object Liquid : MatterState()
    object Gas : MatterState()
}

sealed class MatterEvent {
    object OnMelted : MatterEvent()
    object OnFrozen : MatterEvent()
    object OnVaporized : MatterEvent()
    object OnCondensed : MatterEvent()
}

sealed class MatterSideEffect

val stateMachine = StateMachine.create<MatterState, MatterEvent, MatterSideEffect> {
    initialState(Solid)

    onEnter { state, cause ->
        logger.log("Global Enter ${state::class.java.simpleName} by ${cause::class.java.simpleName}")
    }

    onExit { state, cause ->
        logger.log("Global Exit ${state::class.java.simpleName} by ${cause::class.java.simpleName}")
    }

    state<Solid> {
        onEnter { logger.log("Enter Solid") }
        onExit { logger.log("Exit Solid") }
        on<OnMelted> { transitionTo(Liquid) }
    }

    state<Liquid> {
        onEnter { logger.log("Enter Liquid") }
        onExit { logger.log("Exit Liquid") }
        on<OnFrozen> { transitionTo(Solid) }
        on<OnVaporized> { transitionTo(Gas) }
    }

    state<Gas> {
        onEnter { logger.log("Enter Gas") }
        onExit { logger.log("Exit Gas") }
        on<OnCondensed> { transitionTo(Liquid) }
    }
}