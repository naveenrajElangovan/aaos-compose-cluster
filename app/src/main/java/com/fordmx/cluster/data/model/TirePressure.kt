package com.fordmx.cluster.data.model

data class TirePressure(
    val frontLeft: Float = 32f,
    val frontRight: Float = 32f,
    val rearLeft: Float = 32f,
    val rearRight: Float = 32f
) {
    val hasWarning: Boolean
        get() = frontLeft < 26f || frontRight < 26f ||
                rearLeft < 26f  || rearRight < 26f

    val criticalTire: String?
        get() = when {
            frontLeft < 26f  -> "FL"
            frontRight < 26f -> "FR"
            rearLeft < 26f   -> "RL"
            rearRight < 26f  -> "RR"
            else -> null
        }

    val lowest: Float
        get() = minOf(frontLeft, frontRight, rearLeft, rearRight)
}