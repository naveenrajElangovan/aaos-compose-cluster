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
fun RpmGauge(
    rpm: Float,
    modifier: Modifier = Modifier
) {
    val animated by animateFloatAsState(
        targetValue = rpm,
        animationSpec = spring(dampingRatio = 0.7f, stiffness = 90f),
        label = "rpm"
    )

    Canvas(modifier = modifier) {
        val cx = size.width / 2f
        val cy = size.height / 2f
        val outerR  = size.minDimension / 2f - 8.dp.toPx()
        val trackW  = 14.dp.toPx()
        val startAngle = 135f
        val sweep = 270f
        val maxRpm = 8000f
        val redlineF = 6500f / maxRpm
        val fraction = (animated / maxRpm).coerceIn(0f, 1f)

        // Outer shadow
        drawArc(
            color = Color(0xFF0C160C),
            startAngle = startAngle, sweepAngle = sweep, useCenter = false,
            topLeft = Offset(cx - outerR, cy - outerR),
            size = Size(outerR * 2, outerR * 2),
            style = Stroke(width = trackW + 6.dp.toPx(), cap = StrokeCap.Round)
        )

        // Background track
        drawArc(
            color = Color(0xFF0F1F0F),
            startAngle = startAngle, sweepAngle = sweep, useCenter = false,
            topLeft = Offset(cx - outerR, cy - outerR),
            size = Size(outerR * 2, outerR * 2),
            style = Stroke(width = trackW, cap = StrokeCap.Round)
        )

        // Redline zone background (always visible, dim red)
        drawArc(
            color = Color(0xFF3A0A0A),
            startAngle = startAngle + redlineF * sweep,
            sweepAngle = (1f - redlineF) * sweep,
            useCenter = false,
            topLeft = Offset(cx - outerR, cy - outerR),
            size = Size(outerR * 2, outerR * 2),
            style = Stroke(width = trackW, cap = StrokeCap.Butt)
        )

        // RPM fill
        if (fraction > 0f) {
            val normalFill = fraction.coerceAtMost(redlineF)
            // Normal zone — green
            drawArc(
                color = Color(0xFF22CC22),
                startAngle = startAngle,
                sweepAngle = normalFill * sweep,
                useCenter = false,
                topLeft = Offset(cx - outerR, cy - outerR),
                size = Size(outerR * 2, outerR * 2),
                style = Stroke(width = trackW, cap = StrokeCap.Round)
            )
            // Redline zone — red
            if (fraction > redlineF) {
                drawArc(
                    color = Color(0xFFEF4444),
                    startAngle = startAngle + redlineF * sweep,
                    sweepAngle = (fraction - redlineF) * sweep,
                    useCenter = false,
                    topLeft = Offset(cx - outerR, cy - outerR),
                    size = Size(outerR * 2, outerR * 2),
                    style = Stroke(width = trackW, cap = StrokeCap.Round)
                )
            }
        }

        // Tick marks
        val tickCount = 16
        val tickOuterR = outerR - trackW / 2f - 3.dp.toPx()
        for (i in 0..tickCount) {
            val frac     = i.toFloat() / tickCount
            val angleDeg = startAngle + frac * sweep
            val rad      = Math.toRadians(angleDeg.toDouble())
            val isMajor  = i % 2 == 0
            val isRed    = frac >= redlineF
            val len      = if (isMajor) 12.dp.toPx() else 7.dp.toPx()
            val inner    = tickOuterR - len
            val color = when {
                isRed && frac <= fraction -> Color(0xFFEF4444)
                !isRed && frac <= fraction -> Color.White.copy(alpha = 0.7f)
                isRed -> Color(0xFF4A1A1A)
                else  -> Color(0xFF1A3A1A)
            }
            drawLine(
                color = color,
                start = Offset(cx + tickOuterR * cos(rad).toFloat(), cy + tickOuterR * sin(rad).toFloat()),
                end   = Offset(cx + inner * cos(rad).toFloat(),      cy + inner * sin(rad).toFloat()),
                strokeWidth = if (isMajor) 2.dp.toPx() else 1.2f.dp.toPx(),
                cap = StrokeCap.Round
            )
        }

        // Redline marker line
        val redlineAngle = startAngle + redlineF * sweep
        val redlineRad   = Math.toRadians(redlineAngle.toDouble())
        drawLine(
            color = Color(0xFFEF4444).copy(alpha = 0.9f),
            start = Offset(cx + (tickOuterR - 16.dp.toPx()) * cos(redlineRad).toFloat(),
                cy + (tickOuterR - 16.dp.toPx()) * sin(redlineRad).toFloat()),
            end   = Offset(cx + (tickOuterR + 4.dp.toPx()) * cos(redlineRad).toFloat(),
                cy + (tickOuterR + 4.dp.toPx()) * sin(redlineRad).toFloat()),
            strokeWidth = 3.dp.toPx(),
            cap = StrokeCap.Round
        )

        // Needle
        val needleAngle = startAngle + fraction * sweep
        val needleRad   = Math.toRadians(needleAngle.toDouble())
        val needleLen   = outerR - trackW - 16.dp.toPx()
        val tailLen     = 14.dp.toPx()
        val tipX  = cx + needleLen * cos(needleRad).toFloat()
        val tipY  = cy + needleLen * sin(needleRad).toFloat()
        val tailX = cx - tailLen * cos(needleRad).toFloat()
        val tailY = cy - tailLen * sin(needleRad).toFloat()

        drawLine(
            color = Color.Black.copy(alpha = 0.5f),
            start = Offset(tailX + 2f, tailY + 2f),
            end   = Offset(tipX  + 2f, tipY  + 2f),
            strokeWidth = 5.dp.toPx(), cap = StrokeCap.Round
        )
        drawLine(
            color = Color.White,
            start = Offset(tailX, tailY), end = Offset(tipX, tipY),
            strokeWidth = 3.5f.dp.toPx(), cap = StrokeCap.Round
        )
        val needleColor = if (fraction > redlineF) Color(0xFFEF4444) else Color(0xFF22EE22)
        drawLine(
            color = needleColor,
            start = Offset(cx, cy), end = Offset(tipX, tipY),
            strokeWidth = 1.5f.dp.toPx(), cap = StrokeCap.Round
        )

        // Center hub
        drawCircle(color = Color(0xFF0A140A), radius = 10.dp.toPx(), center = Offset(cx, cy))
        drawCircle(color = needleColor,       radius = 7.dp.toPx(),  center = Offset(cx, cy))
        drawCircle(color = Color.White,       radius = 3.5f.dp.toPx(), center = Offset(cx, cy))
    }
}