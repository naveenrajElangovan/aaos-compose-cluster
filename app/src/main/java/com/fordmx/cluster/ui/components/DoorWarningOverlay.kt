package com.fordmx.cluster.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.fordmx.cluster.data.model.DoorState

@Composable
fun DoorWarningOverlay(
    doorState: DoorState,
    speedKmh: Float,
    modifier: Modifier = Modifier
) {
    if (!doorState.anyOpen) return

    val isDanger = speedKmh > 5f
    val pulse    = rememberInfiniteTransition(label = "door_pulse")
    val alpha by pulse.animateFloat(
        initialValue = 1f,
        targetValue  = 0.2f,
        animationSpec = infiniteRepeatable(
            animation  = tween(400, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "alpha"
    )

    val borderColor = if (isDanger) Color(0xFFEF4444) else Color(0xFFF59E0B)
    val bgColor     = if (isDanger) Color(0x99200000) else Color(0x99201000)
    val titleText   = if (isDanger) "DOOR OPEN — DANGER!" else "DOOR OPEN"

    Column(
        modifier = modifier
            .clip(RoundedCornerShape(8.dp))
            .background(bgColor.copy(alpha = alpha * 0.85f))
            .border(1.5.dp, borderColor.copy(alpha = alpha), RoundedCornerShape(8.dp))
            .padding(horizontal = 10.dp, vertical = 6.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text       = titleText,
            color      = borderColor.copy(alpha = alpha),
            fontSize   = 10.sp,
            fontWeight = FontWeight.Bold,
            letterSpacing = 1.sp
        )
        Spacer(Modifier.height(4.dp))
        CarTopView(
            doorState    = doorState,
            warningColor = borderColor,
            alpha        = alpha
        )
        Spacer(Modifier.height(3.dp))
        Text(
            text      = doorState.openDoorNames().joinToString("  "),
            color     = borderColor.copy(alpha = alpha),
            fontSize  = 9.sp,
            letterSpacing = 2.sp
        )
    }
}

@Composable
private fun CarTopView(
    doorState: DoorState,
    warningColor: Color,
    alpha: Float
) {
    val openCol   = warningColor.copy(alpha = alpha)
    val closedCol = Color(0xFF1A3A1A)
    val bodyCol   = Color(0xFF0F1F0F)

    Box(
        modifier = Modifier.size(width = 68.dp, height = 86.dp),
        contentAlignment = Alignment.Center
    ) {
        // Car body
        Box(
            Modifier
                .size(width = 28.dp, height = 56.dp)
                .clip(RoundedCornerShape(5.dp))
                .background(bodyCol)
                .border(1.dp, Color(0xFF1A3A1A), RoundedCornerShape(5.dp))
        )
        // Windshield line
        Box(
            Modifier
                .size(width = 20.dp, height = 1.dp)
                .offset(y = (-20).dp)
                .background(Color(0xFF1A3A1A))
        )
        // Rear window line
        Box(
            Modifier
                .size(width = 20.dp, height = 1.dp)
                .offset(y = 20.dp)
                .background(Color(0xFF1A3A1A))
        )

        // FL — front left door
        Box(
            Modifier
                .size(width = 9.dp, height = 20.dp)
                .offset(x = (-20).dp, y = (-14).dp)
                .clip(RoundedCornerShape(topStart = 3.dp, bottomStart = 3.dp))
                .background(if (doorState.frontLeft) openCol else closedCol)
        )
        // FR — front right door
        Box(
            Modifier
                .size(width = 9.dp, height = 20.dp)
                .offset(x = 20.dp, y = (-14).dp)
                .clip(RoundedCornerShape(topEnd = 3.dp, bottomEnd = 3.dp))
                .background(if (doorState.frontRight) openCol else closedCol)
        )
        // RL — rear left door
        Box(
            Modifier
                .size(width = 9.dp, height = 18.dp)
                .offset(x = (-20).dp, y = 12.dp)
                .clip(RoundedCornerShape(topStart = 3.dp, bottomStart = 3.dp))
                .background(if (doorState.rearLeft) openCol else closedCol)
        )
        // RR — rear right door
        Box(
            Modifier
                .size(width = 9.dp, height = 18.dp)
                .offset(x = 20.dp, y = 12.dp)
                .clip(RoundedCornerShape(topEnd = 3.dp, bottomEnd = 3.dp))
                .background(if (doorState.rearRight) openCol else closedCol)
        )
        // Trunk
        if (doorState.trunk) {
            Box(
                Modifier
                    .size(width = 22.dp, height = 7.dp)
                    .offset(y = 34.dp)
                    .clip(RoundedCornerShape(bottomStart = 4.dp, bottomEnd = 4.dp))
                    .background(openCol)
            )
        }
        // Hood
        if (doorState.hood) {
            Box(
                Modifier
                    .size(width = 22.dp, height = 7.dp)
                    .offset(y = (-34).dp)
                    .clip(RoundedCornerShape(topStart = 4.dp, topEnd = 4.dp))
                    .background(openCol)
            )
        }
    }
}