package com.fordmx.cluster.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.fordmx.cluster.data.model.TirePressure
import com.fordmx.cluster.data.model.WarningFlags
import com.fordmx.cluster.presentation.screen.WarnPill

@Composable
fun WarningBar(
    warnings: WarningFlags,
    tirePressure: TirePressure,
    engineTemp: Float,
    modifier: Modifier = Modifier
) {
    val pulse = rememberInfiniteTransition(label = "warn_pulse")
    val blink by pulse.animateFloat(
        initialValue  = 1f,
        targetValue   = 0.15f,
        animationSpec = infiniteRepeatable(
            animation  = tween(500),
            repeatMode = RepeatMode.Reverse
        ),
        label = "blink"
    )
    val turnBlink by pulse.animateFloat(
        initialValue  = 1f,
        targetValue   = 0f,
        animationSpec = infiniteRepeatable(
            animation  = tween(400),
            repeatMode = RepeatMode.Reverse
        ),
        label = "turn"
    )

    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(5.dp)
    ) {
        // Turn left signal
        if (warnings.turnLeft) {
            Text(
                text  = "◀",
                color = Color(0xFF22CC22).copy(alpha = turnBlink),
                fontSize = 13.sp
            )
        }

        // Parking brake
        if (warnings.handbrakeOn) {
            WarnPill(text = "P", color = Color(0xFFEF4444), alpha = 1f)
        }

        // ABS
        if (warnings.absActive) {
            WarnPill(text = "ABS", color = Color(0xFFEF4444), alpha = blink)
        }

        // Traction control
        if (warnings.tractionActive) {
            WarnPill(text = "TCS", color = Color(0xFFF59E0B), alpha = blink)
        }

        // Hazard lights
        if (warnings.hazardOn) {
            Text(
                text  = "◀▶",
                color = Color(0xFFF59E0B).copy(alpha = blink),
                fontSize = 11.sp
            )
        }

        // Seatbelt driver
        if (!warnings.seatbeltDriver) {
            WarnPill(text = "BELT!", color = Color(0xFFEF4444), alpha = blink)
        }

        // Tire pressure
        if (tirePressure.hasWarning) {
            WarnPill(
                text  = "TYRE ${tirePressure.criticalTire}",
                color = Color(0xFFF59E0B),
                alpha = blink
            )
        }

        // Engine overtemp
        if (engineTemp > 100f || warnings.engineTempHigh) {
            WarnPill(text = "TEMP!", color = Color(0xFFEF4444), alpha = blink)
        }

        // Low fuel
        if (warnings.lowFuel) {
            WarnPill(text = "FUEL!", color = Color(0xFFEF4444), alpha = blink)
        }

        // Headlights
        if (warnings.headlightsOn) {
            WarnPill(text = "LIGHTS", color = Color(0xFF3399FF), alpha = 1f)
        }

        // Turn right signal
        if (warnings.turnRight) {
            Text(
                text  = "▶",
                color = Color(0xFF22CC22).copy(alpha = turnBlink),
                fontSize = 13.sp
            )
        }
    }
}