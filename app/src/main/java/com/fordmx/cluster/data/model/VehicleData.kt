package com.fordmx.cluster.data.model

data class VehicleData(
    val speedKmh: Float = 0f,
    val rpm: Float = 0f,
    val gear: String = "P",
    val fuelPercent: Float = 75f,
    val engineTempCelsius: Float = 20f,
    val odometerKm: Float = 5230f,
    val doorState: DoorState = DoorState(),
    val tirePressure: TirePressure = TirePressure(),
    val warnings: WarningFlags = WarningFlags()
)