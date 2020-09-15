package com.tinder.sample.matter

sealed class MatterState {
    object Solid : MatterState()
    object Liquid : MatterState()
    object Gas : MatterState()
}
