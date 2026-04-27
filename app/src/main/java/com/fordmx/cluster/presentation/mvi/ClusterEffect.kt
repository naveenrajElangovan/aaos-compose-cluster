package com.fordmx.cluster.presentation.mvi

sealed class ClusterEffect {
    data class DoorOpenWhileMoving(val speedKmh: Float) : ClusterEffect()
    object LowFuelAlert : ClusterEffect()
    object EngineOverheat : ClusterEffect()
    object SeatbeltUnbuckled : ClusterEffect()
    data class TirePressureLow(val tire: String, val psi: Float) : ClusterEffect()
}