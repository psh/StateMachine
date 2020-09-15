package com.tinder.sample.turnstile

sealed class TurnstileCommand {
    object SoundAlarm : TurnstileCommand()
    object CloseDoors : TurnstileCommand()
    object OpenDoors : TurnstileCommand()
    object OrderRepair : TurnstileCommand()
}
