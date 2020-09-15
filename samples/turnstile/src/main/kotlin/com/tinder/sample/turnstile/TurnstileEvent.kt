package com.tinder.sample.turnstile

sealed class TurnstileEvent {
    data class InsertCoin(val value: Int) : TurnstileEvent()
    object AdmitPerson : TurnstileEvent()
    object MachineDidFail : TurnstileEvent()
    object MachineRepairDidComplete : TurnstileEvent()
}
