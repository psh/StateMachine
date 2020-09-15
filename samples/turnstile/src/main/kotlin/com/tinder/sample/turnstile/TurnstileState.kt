package com.tinder.sample.turnstile

sealed class TurnstileState {
    data class Locked(val credit: Int) : TurnstileState()
    object Unlocked : TurnstileState()
    data class Broken(val oldState: TurnstileState) : TurnstileState()
}
