package com.fordmx.cluster.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun FuelRing(fuelPercent: Float, modifier: Modifier = Modifier) {
    val animated by animateFloatAsState(
        targetValue = fuelPercent,
        animationSpec = spring(dampingRatio = 0.8f, stiffness = 60f),
        label = "fuel"
    )
    val isLow = fuelPercent < 20f
    val inf = rememberInfiniteTransition(label = "pulse")
    val pulse by inf.animateFloat(
        initialValue = 1f, targetValue = 0.2f,
        animationSpec = infiniteRepeatable(
            animation = tween(600, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ), label = "p"
    )
    val fraction = (animated / 100f).coerceIn(0f, 1f)
    val col = when {
        fuelPercent < 15f -> Color(0xFFEF4444)
        fuelPercent < 30f -> Color(0xFFF59E0B)
        else              -> Color(0xFF22CC22)
    }
    val effCol = if (isLow) col.copy(alpha = pulse) else col

    Box(modifier = modifier, contentAlignment = Alignment.Center) {
        Canvas(modifier = Modifier.matchParentSize()) {
            val sw = 7.dp.toPx()
            val r  = size.minDimension / 2f - sw
            val c  = Offset(size.width / 2f, size.height / 2f)
            drawArc(
                color = Color(0xFF0F1F0F),
                startAngle = 135f, sweepAngle = 270f, useCenter = false,
                topLeft = Offset(c.x - r, c.y - r), size = Size(r * 2, r * 2),
                style = Stroke(width = sw, cap = StrokeCap.Round)
            )
            if (fraction > 0f) {
                drawArc(
                    color = effCol,
                    startAngle = 135f, sweepAngle = fraction * 270f, useCenter = false,
                    topLeft = Offset(c.x - r, c.y - r), size = Size(r * 2, r * 2),
                    style = Stroke(width = sw, cap = StrokeCap.Round)
                )
            }
        }
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = if (isLow) "LOW" else "${fuelPercent.toInt()}%",
                color = if (isLow) Color(0xFFEF4444).copy(alpha = pulse) else Color(0xFF22CC22),
                fontSize = if (isLow) 9.sp else 11.sp,
                fontWeight = FontWeight.Bold
            )
            Text(text = "⛽", fontSize = 9.sp)
        }
    }
}