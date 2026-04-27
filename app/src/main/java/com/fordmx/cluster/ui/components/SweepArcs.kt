package com.fordmx.cluster.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.unit.dp
import kotlin.math.cos
import kotlin.math.sin

@Composable
fun SweepArcs(
    mirrored: Boolean = false,
    modifier: Modifier = Modifier
) {
    Canvas(modifier = modifier) {
        val sign = if (mirrored) -1f else 1f
        val cx = if (mirrored) size.width else 0f
        val cy = size.height

        // Diagonal slash lines
        val lineCount = 16
        for (i in 0 until lineCount) {
            val frac   = i.toFloat() / lineCount
            val alpha  = (0.9f - frac * 0.72f).coerceAtLeast(0.04f)
            val width  = (18f - frac * 12f).coerceAtLeast(2f)
            val dx     = (20f + i * 17f) * sign
            val ang    = -0.54
            val len    = size.height * 0.82f
            val x1     = cx + dx
            val y1     = cy * 0.85f
            val x2     = x1 + cos(ang).toFloat() * len * sign
            val y2     = y1 - sin(ang).toFloat() * len

            drawLine(
                color = Color(0xFF1DAD1D).copy(alpha = alpha),
                start = androidx.compose.ui.geometry.Offset(x1, y1),
                end   = androidx.compose.ui.geometry.Offset(x2, y2),
                strokeWidth = width,
                cap = StrokeCap.Butt
            )
        }

        // Arc bands
        val arcCount = 14
        for (i in 0 until arcCount) {
            val frac   = i.toFloat() / arcCount
            val alpha  = (0.55f - frac * 0.45f).coerceAtLeast(0.02f)
            val width  = (13f - frac * 9f).coerceAtLeast(1.5f)
            val r      = 90.dp.toPx() + i * 14.dp.toPx()
            val startA = if (!mirrored) Math.PI * 0.72 else Math.PI * 0.52
            val endA   = if (!mirrored) Math.PI * 1.48 else Math.PI * 1.28
            val arcCx  = if (!mirrored) size.width * 0.72f else size.width * 0.28f
            val arcCy  = size.height * 0.78f

            drawArc(
                color = Color(0xFF1DAD1D).copy(alpha = alpha),
                startAngle = Math.toDegrees(startA).toFloat(),
                sweepAngle = Math.toDegrees(endA - startA).toFloat(),
                useCenter = false,
                topLeft = androidx.compose.ui.geometry.Offset(arcCx - r, arcCy - r),
                size = androidx.compose.ui.geometry.Size(r * 2, r * 2),
                style = androidx.compose.ui.graphics.drawscope.Stroke(
                    width = width,
                    cap = StrokeCap.Butt
                )
            )
        }

        // Bright edge line
        val edgeR = 88.dp.toPx()
        val edgeCx = if (!mirrored) size.width * 0.72f else size.width * 0.28f
        val edgeCy = size.height * 0.78f
        val sA = if (!mirrored) Math.PI * 0.72 else Math.PI * 0.52
        val eA = if (!mirrored) Math.PI * 1.48 else Math.PI * 1.28
        drawArc(
            color = Color(0xFF44EE44).copy(alpha = 0.8f),
            startAngle = Math.toDegrees(sA).toFloat(),
            sweepAngle = Math.toDegrees(eA - sA).toFloat(),
            useCenter = false,
            topLeft = androidx.compose.ui.geometry.Offset(edgeCx - edgeR, edgeCy - edgeR),
            size = androidx.compose.ui.geometry.Size(edgeR * 2, edgeR * 2),
            style = androidx.compose.ui.graphics.drawscope.Stroke(
                width = 1.5.dp.toPx(),
                cap = StrokeCap.Round
            )
        )
    }
}