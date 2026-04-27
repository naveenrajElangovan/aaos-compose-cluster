package com.fordmx.cluster.data.model

data class WarningFlags(
    val absActive: Boolean = false,
    val tractionActive: Boolean = false,
    val engineTempHigh: Boolean = false,
    val lowFuel: Boolean = false,
    val seatbeltDriver: Boolean = true,
    val seatbeltPassenger: Boolean = false,
    val handbrakeOn: Boolean = true,
    val hazardOn: Boolean = false,
    val headlightsOn: Boolean = false,
    val turnLeft: Boolean = false,
    val turnRight: Boolean = false
) {
    val hasCritical: Boolean
        get() = absActive || tractionActive || engineTempHigh || !seatbeltDriver

    val hasWarning: Boolean
        get() = lowFuel || handbrakeOn || hazardOn
}