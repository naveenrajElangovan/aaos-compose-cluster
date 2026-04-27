package com.fordmx.cluster.ui.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.Canvas
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.dp
import kotlin.math.cos
import kotlin.math.sin

@Composable
fun SpeedometerGauge(speedKmh: Float, modifier: Modifier = Modifier) {
    val animatedSpeed by animateFloatAsState(
        targetValue = speedKmh,
        animationSpec = spring(dampingRatio = 0.6f, stiffness = 80f),
        label = "speed"
    )

    Canvas(modifier = modifier) {
        val strokeWidth = 14.dp.toPx()
        val radius = (size.minDimension / 2f) - strokeWidth
        val center = Offset(size.width / 2f, size.height / 2f)
        val startAngle = 150f
        val sweepRange = 240f
        val speedFraction = (animatedSpeed / 220f).coerceIn(0f, 1f)

        // Background track
        drawArc(
            color = Color(0xFF1F2937),
            startAngle = startAngle,
            sweepAngle = sweepRange,
            useCenter = false,
            topLeft = Offset(center.x - radius, center.y - radius),
            size = Size(radius * 2, radius * 2),
            style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
        )

        // Speed fill arc — blue → orange → red
        val speedSweep = speedFraction * sweepRange
        if (speedSweep > 0.5f) {
            val arcColor = when {
                speedFraction < 0.6f -> Color(0xFF1A6EF5)
                speedFraction < 0.85f -> Color(0xFFF59E0B)
                else -> Color(0xFFEF4444)
            }
            drawArc(
                color = arcColor,
                startAngle = startAngle,
                sweepAngle = speedSweep,
                useCenter = false,
                topLeft = Offset(center.x - radius, center.y - radius),
                size = Size(radius * 2, radius * 2),
                style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
            )
        }

        // Tick marks — every 10 km/h
        val tickCount = 22
        for (i in 0..tickCount) {
            val fraction = i.toFloat() / tickCount
            val angleDeg = startAngle + fraction * sweepRange
            val angleRad = Math.toRadians(angleDeg.toDouble())
            val isMajor = i % 2 == 0
            val tickOuter = radius - strokeWidth / 2f - 4.dp.toPx()
            val tickInner = tickOuter - if (isMajor) 14.dp.toPx() else 8.dp.toPx()
            val tickColor = if (fraction > speedFraction)
                Color(0xFF374151) else Color.White.copy(alpha = 0.6f)

            drawLine(
                color = tickColor,
                start = Offset(
                    center.x + tickOuter * cos(angleRad).toFloat(),
                    center.y + tickOuter * sin(angleRad).toFloat()
                ),
                end = Offset(
                    center.x + tickInner * cos(angleRad).toFloat(),
                    center.y + tickInner * sin(angleRad).toFloat()
                ),
                strokeWidth = if (isMajor) 2.dp.toPx() else 1.dp.toPx(),
                cap = StrokeCap.Round
            )
        }

        // Needle shadow
        val needleAngleRad = Math.toRadians((startAngle + speedFraction * sweepRange).toDouble())
        val needleLength = radius - strokeWidth - 8.dp.toPx()
        val needleTip = Offset(
            center.x + needleLength * cos(needleAngleRad).toFloat(),
            center.y + needleLength * sin(needleAngleRad).toFloat()
        )
        drawLine(
            color = Color.Black.copy(alpha = 0.4f),
            start = center + Offset(2f, 2f),
            end = needleTip + Offset(2f, 2f),
            strokeWidth = 4.dp.toPx(),
            cap = StrokeCap.Round
        )
        // Needle
        drawLine(
            color = Color.White,
            start = center,
            end = needleTip,
            strokeWidth = 3.dp.toPx(),
            cap = StrokeCap.Round
        )
        // Center hub
        drawCircle(color = Color(0xFF1A6EF5), radius = 6.dp.toPx(), center = center)
        drawCircle(color = Color.White, radius = 3.dp.toPx(), center = center)
    }
}