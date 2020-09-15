package com.tinder.sample.matter

sealed class MatterEvent {
    object OnMelted : MatterEvent()
    object OnFrozen : MatterEvent()
    object OnVaporized : MatterEvent()
    object OnCondensed : MatterEvent()
}
