package com.tinder.sample.matter

sealed class MatterSideEffect {
    object LogMelted : MatterSideEffect()
    object LogFrozen : MatterSideEffect()
    object LogVaporized : MatterSideEffect()
    object LogCondensed : MatterSideEffect()
}
