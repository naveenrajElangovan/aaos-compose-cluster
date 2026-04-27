package com.fordmx.cluster.presentation.mvi

import com.fordmx.cluster.data.model.*

data class ClusterState(
    val speedKmh: Float          = 0f,
    val rpm: Float               = 0f,
    val gear: String             = "P",
    val fuelPercent: Float       = 75f,
    val engineTemp: Float        = 20f,
    val odometerKm: Float        = 5230f,
    val doorState: DoorState     = DoorState(),
    val warnings: WarningFlags   = WarningFlags(),
    val tirePressure: TirePressure = TirePressure(),
    val isConnected: Boolean     = false
) {
    // ── Derived from real values — never stale ─────────────────
    val isLowFuel: Boolean
        get() = fuelPercent < 15f

    val isEngineOverheat: Boolean
        get() = engineTemp > 100f

    val isDoorOpenWhileMoving: Boolean
        get() = doorState.anyOpen && speedKmh > 5f

    val isCriticalWarning: Boolean
        get() = isEngineOverheat || isDoorOpenWhileMoving || !warnings.seatbeltDriver

    val fuelBarFraction: Float
        get() = (fuelPercent / 100f).coerceIn(0f, 1f)

    val engineTempFraction: Float
        get() = ((engineTemp - 20f) / 90f).coerceIn(0f, 1f)

    // ── Real RPM correlated to speed ──────────────────────────
    val expectedRpm: Float
        get() = if (rpm > 0f) rpm else speedToIdleRpm(speedKmh)

    private fun speedToIdleRpm(speedKmh: Float): Float {
        return if (speedKmh <= 0f) 0f else 900f
    }
}