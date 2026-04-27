package com.fordmx.cluster.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun GearSelector(
    currentGear: String,
    gears: List<String> = listOf("P", "R", "N", "D", "S"),
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(6.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        gears.forEach { gear ->
            val isSelected = gear == currentGear
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .clip(RoundedCornerShape(3.dp))
                    .background(
                        if (isSelected) Color(0xFF0A1A0A) else Color.Transparent
                    )
                    .padding(horizontal = if (isSelected) 5.dp else 2.dp, vertical = 1.dp)
            ) {
                Text(
                    text       = gear,
                    color      = when {
                        isSelected && gear == "R" -> Color(0xFFEF4444)
                        isSelected && gear == "P" -> Color(0xFFF59E0B)
                        isSelected               -> Color.White
                        else                     -> Color(0xFF1A3A1A)
                    },
                    fontSize   = if (isSelected) 17.sp else 11.sp,
                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                )
            }
        }
    }
}