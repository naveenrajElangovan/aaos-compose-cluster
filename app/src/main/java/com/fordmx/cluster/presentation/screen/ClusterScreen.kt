package com.fordmx.cluster.presentation.screen

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
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.fordmx.cluster.data.model.DoorState
import com.fordmx.cluster.presentation.mvi.ClusterEffect
import com.fordmx.cluster.presentation.mvi.ClusterState
import com.fordmx.cluster.presentation.viewmodel.ClusterViewModel
import com.fordmx.cluster.ui.components.*
import kotlinx.coroutines.flow.collectLatest

@Composable
fun ClusterScreen(viewModel: ClusterViewModel) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    LaunchedEffect(Unit) {
        viewModel.effect.collectLatest { effect ->
            when (effect) {
                is ClusterEffect.DoorOpenWhileMoving -> { }
                is ClusterEffect.LowFuelAlert        -> { }
                is ClusterEffect.EngineOverheat      -> { }
                is ClusterEffect.SeatbeltUnbuckled   -> { }
                is ClusterEffect.TirePressureLow     -> { }
            }
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF060806))
    ) {
        SweepArcs(false, Modifier.fillMaxHeight().width(300.dp).align(Alignment.CenterStart))
        SweepArcs(true,  Modifier.fillMaxHeight().width(300.dp).align(Alignment.CenterEnd))

        Column(Modifier.fillMaxSize()) {
            TopWarningBar(state)
            Row(
                modifier = Modifier.weight(1f).fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                LeftGaugePanel(state,  Modifier.weight(1f).fillMaxHeight())
                CenterPanel(state,     Modifier.width(190.dp).fillMaxHeight())
                RightGaugePanel(state, Modifier.weight(1f).fillMaxHeight())
            }
            BottomStatusBar(state)
        }

        // ── Full-screen flash overlays ─────────────────────────────────────────
        if (state.isDoorOpenWhileMoving) {
            DoorWarningOverlay(
                doorState = state.doorState,
                speedKmh  = state.speedKmh,
                modifier  = Modifier.align(Alignment.TopCenter).padding(top = 32.dp)
            )
        }
        if (state.warnings.absActive) {
            FlashOverlay("ABS ACTIVE", Color(0xFFEF4444),
                Modifier.align(Alignment.BottomStart).padding(start = 8.dp, bottom = 44.dp))
        }
        if (state.warnings.tractionActive) {
            FlashOverlay("TCS ACTIVE", Color(0xFFF59E0B),
                Modifier.align(Alignment.BottomStart).padding(start = 8.dp, bottom = 68.dp))
        }
        if (state.isEngineOverheat) {
            FlashOverlay("ENGINE TEMP HIGH!", Color(0xFFEF4444),
                Modifier.align(Alignment.TopEnd).padding(end = 8.dp, top = 32.dp))
        }
        if (state.isLowFuel) {
            FlashOverlay("LOW FUEL!", Color(0xFFF59E0B),
                Modifier.align(Alignment.BottomEnd).padding(end = 8.dp, bottom = 44.dp))
        }
        if (state.isCriticalWarning) {
            // Red border flash on critical warning
            val pulse = rememberInfiniteTransition(label = "crit")
            val a by pulse.animateFloat(
                0.6f, 0f,
                infiniteRepeatable(tween(300), RepeatMode.Reverse), label = "ca"
            )
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .border(3.dp, Color(0xFFEF4444).copy(alpha = a))
            )
        }
    }
}

// ── Top warning bar ────────────────────────────────────────────────────────────
@Composable
private fun TopWarningBar(state: ClusterState) {
    val pulse = rememberInfiniteTransition(label = "p")
    val blink by pulse.animateFloat(
        1f, 0.15f,
        infiniteRepeatable(tween(500), RepeatMode.Reverse), label = "b"
    )
    val turnBlink by pulse.animateFloat(
        1f, 0f,
        infiniteRepeatable(tween(400), RepeatMode.Reverse), label = "t"
    )

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(30.dp)
            .background(Color(0xFF040604))
            .padding(horizontal = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(5.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (state.warnings.turnLeft || state.warnings.hazardOn)
                Text("◀", color = Color(0xFF22CC22).copy(alpha = turnBlink), fontSize = 14.sp)
            if (state.warnings.handbrakeOn)
                WarnPill("P", Color(0xFFEF4444), 1f)
            if (state.warnings.absActive)
                WarnPill("ABS", Color(0xFFEF4444), blink)
            if (state.warnings.tractionActive)
                WarnPill("TCS", Color(0xFFF59E0B), blink)
            if (state.warnings.hazardOn)
                Text("◀▶", color = Color(0xFFF59E0B).copy(alpha = blink), fontSize = 12.sp)
            if (!state.warnings.seatbeltDriver)
                WarnPill("BELT!", Color(0xFFEF4444), blink)
        }

        Text(
            text          = "${state.odometerKm.toInt()} km",
            color         = Color(0xFF334433),
            fontSize      = 9.sp,
            letterSpacing = 1.sp
        )

        Row(
            horizontalArrangement = Arrangement.spacedBy(5.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (state.tirePressure.hasWarning)
                WarnPill("TYRE ${state.tirePressure.criticalTire}", Color(0xFFF59E0B), blink)
            if (state.isEngineOverheat)
                WarnPill("TEMP!", Color(0xFFEF4444), blink)
            if (state.isLowFuel)
                WarnPill("FUEL!", Color(0xFFEF4444), blink)
            if (state.warnings.headlightsOn)
                WarnPill("LIGHTS", Color(0xFF3399FF), 1f)
            if (state.warnings.turnRight || state.warnings.hazardOn)
                Text("▶", color = Color(0xFF22CC22).copy(alpha = turnBlink), fontSize = 14.sp)
        }
    }
}

// ── Left speed panel ───────────────────────────────────────────────────────────
@Composable
private fun LeftGaugePanel(state: ClusterState, modifier: Modifier) {
    Box(
        modifier = modifier.background(Color(0x33060806)),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            SpeedometerGauge(
                speedKmh = state.speedKmh,
                modifier = Modifier.size(210.dp)
            )
            Spacer(Modifier.height(6.dp))
            Text(
                text       = "${state.speedKmh.toInt()}",
                color      = if (state.speedKmh > 120f) Color(0xFFEF4444)
                else Color(0xFF22DD22),
                fontSize   = 48.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = FontFamily.Monospace
            )
            Text(
                text          = "km/h",
                color         = Color(0xFF448844),
                fontSize      = 12.sp,
                letterSpacing = 2.sp
            )
        }
    }
}

// ── Center info panel ──────────────────────────────────────────────────────────
@Composable
private fun CenterPanel(state: ClusterState, modifier: Modifier) {
    Column(
        modifier = modifier
            .background(Color(0xFF040604))
            .padding(horizontal = 8.dp, vertical = 6.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceEvenly
    ) {
        // Large gear + underline
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text       = state.gear,
                color      = when (state.gear) {
                    "R"  -> Color(0xFFEF4444)
                    "P"  -> Color(0xFFF59E0B)
                    "D","S" -> Color.White
                    else -> Color(0xFF22CC22)
                },
                fontSize   = 26.sp,
                fontWeight = FontWeight.Light
            )
            Box(Modifier.width(30.dp).height(2.dp).background(Color(0xFF22CC22)))
        }

        // Gear selector — uses GearSelector component
        GearSelector(
            currentGear = state.gear,
            gears       = listOf("P", "R", "N", "D", "S")
        )

        // Trip info
        Column(modifier = Modifier.fillMaxWidth()) {
            Text(
                "CURRENT TRIP",
                color = Color(0xFF444444), fontSize = 8.sp, letterSpacing = 2.sp,
                modifier = Modifier.align(Alignment.CenterHorizontally)
            )
            Spacer(Modifier.height(3.dp))
            listOf("Trip" to "17.5 km", "Timer" to "0:23", "Avg." to "16.5 km/L")
                .forEach { (l, v) ->
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text(l, color = Color(0xFF333333), fontSize = 10.sp)
                        Text(v, color = Color(0xFF777777), fontSize = 10.sp)
                    }
                }
        }

        // Fuel ring — uses fuelPercent
        FuelRing(
            fuelPercent = state.fuelPercent,
            modifier    = Modifier.size(62.dp)
        )

        // Door car diagram — uses doorState + speedKmh
        MiniCarDoors(
            doorState = state.doorState,
            speedKmh  = state.speedKmh
        )

        // Engine temp + odometer line
        Text(
            text     = "${state.engineTemp.toInt()}°C — ${state.odometerKm.toInt()} km",
            color    = if (state.isEngineOverheat) Color(0xFFEF4444) else Color(0xFF444444),
            fontSize = 9.sp,
            fontWeight = if (state.isEngineOverheat) FontWeight.Bold else FontWeight.Normal
        )
    }
}

// ── Right RPM panel ────────────────────────────────────────────────────────────
@Composable
private fun RightGaugePanel(state: ClusterState, modifier: Modifier) {
    Box(
        modifier = modifier.background(Color(0x33060806)),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // Uses expectedRpm — falls back to derived value if raw rpm is 0
            RpmGauge(
                rpm      = state.expectedRpm,
                modifier = Modifier.size(210.dp)
            )
            Spacer(Modifier.height(6.dp))
            Text(
                text       = "%.1f".format(state.expectedRpm / 1000f),
                color      = when {
                    state.expectedRpm >= 6500f -> Color(0xFFEF4444)
                    state.expectedRpm >= 5000f -> Color(0xFFF59E0B)
                    else                       -> Color(0xFF22DD22)
                },
                fontSize   = 48.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = FontFamily.Monospace
            )
            Text(
                text          = "×1000 rpm",
                color         = Color(0xFF448844),
                fontSize      = 11.sp,
                letterSpacing = 1.sp
            )
        }
    }
}

// ── Bottom status bar ──────────────────────────────────────────────────────────
@Composable
private fun BottomStatusBar(state: ClusterState) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(38.dp)
            .background(Color(0xFF040604))
            .padding(horizontal = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        // Fuel bar — uses fuelBarFraction + isLowFuel
        BarIndicator(
            startLabel = "E",
            endLabel   = "F",
            fraction   = state.fuelBarFraction,
            color      = if (state.isLowFuel) Color(0xFFEF4444) else Color(0xFF229922),
            note       = "${state.fuelPercent.toInt()}%"
        )

        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text      = if (state.warnings.handbrakeOn) "P BRAKE" else "AUTO HOLD",
                color     = if (state.warnings.handbrakeOn) Color(0xFFEF4444)
                else Color(0xFF223322),
                fontSize  = 8.sp,
                letterSpacing = 1.sp
            )
            if (state.tirePressure.hasWarning) {
                Text(
                    "TYRE ${state.tirePressure.criticalTire} LOW",
                    color = Color(0xFFF59E0B), fontSize = 7.sp
                )
            }
            // Show critical warning indicator
            if (state.isCriticalWarning) {
                Text("⚠ ALERT", color = Color(0xFFEF4444), fontSize = 7.sp,
                    fontWeight = FontWeight.Bold)
            }
        }

        // Temp bar — uses engineTempFraction + isEngineOverheat
        BarIndicator(
            startLabel = "C",
            endLabel   = "H",
            fraction   = state.engineTempFraction,
            color      = if (state.isEngineOverheat) Color(0xFFEF4444) else Color(0xFF229922),
            note       = "${state.engineTemp.toInt()}°C",
            reversed   = true
        )
    }
}

// ── Mini car top-view doors ────────────────────────────────────────────────────
@Composable
fun MiniCarDoors(doorState: DoorState, speedKmh: Float) {
    val pulse = rememberInfiniteTransition(label = "door")
    val blink by pulse.animateFloat(
        1f, 0.2f,
        infiniteRepeatable(tween(400), RepeatMode.Reverse), label = "db"
    )
    val isDanger  = doorState.anyOpen && speedKmh > 5f
    val openColor = if (isDanger)
        Color(0xFFEF4444).copy(alpha = blink)
    else
        Color(0xFFF59E0B).copy(alpha = if (doorState.anyOpen) blink else 0.25f)
    val closedColor = Color(0xFF1A3A1A)
    val bodyColor   = Color(0xFF0F1F0F)

    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        if (doorState.anyOpen) {
            Text(
                text          = if (isDanger) "DOOR OPEN — DANGER" else "DOOR OPEN",
                color         = openColor,
                fontSize      = 8.sp,
                fontWeight    = FontWeight.Bold,
                letterSpacing = 1.sp
            )
            Spacer(Modifier.height(2.dp))
        }
        Box(
            modifier         = Modifier.size(width = 68.dp, height = 86.dp),
            contentAlignment = Alignment.Center
        ) {
            Box(
                Modifier.size(width = 28.dp, height = 56.dp)
                    .clip(RoundedCornerShape(5.dp))
                    .background(bodyColor)
                    .border(1.dp, Color(0xFF1A3A1A), RoundedCornerShape(5.dp))
            )
            Box(Modifier.size(9.dp, 20.dp).offset(x = (-19).dp, y = (-14).dp)
                .clip(RoundedCornerShape(topStart = 3.dp, bottomStart = 3.dp))
                .background(if (doorState.frontLeft) openColor else closedColor))
            Box(Modifier.size(9.dp, 20.dp).offset(x = 19.dp, y = (-14).dp)
                .clip(RoundedCornerShape(topEnd = 3.dp, bottomEnd = 3.dp))
                .background(if (doorState.frontRight) openColor else closedColor))
            Box(Modifier.size(9.dp, 18.dp).offset(x = (-19).dp, y = 13.dp)
                .clip(RoundedCornerShape(topStart = 3.dp, bottomStart = 3.dp))
                .background(if (doorState.rearLeft) openColor else closedColor))
            Box(Modifier.size(9.dp, 18.dp).offset(x = 19.dp, y = 13.dp)
                .clip(RoundedCornerShape(topEnd = 3.dp, bottomEnd = 3.dp))
                .background(if (doorState.rearRight) openColor else closedColor))
            if (doorState.trunk) {
                Box(Modifier.size(22.dp, 7.dp).offset(y = 34.dp)
                    .clip(RoundedCornerShape(bottomStart = 3.dp, bottomEnd = 3.dp))
                    .background(openColor))
            }
        }
        if (doorState.anyOpen) {
            Text(
                text          = doorState.openDoorNames().joinToString("  "),
                color         = openColor,
                fontSize      = 8.sp,
                letterSpacing = 1.sp
            )
        }
    }
}

// ── Shared UI primitives ───────────────────────────────────────────────────────
@Composable
fun WarnPill(text: String, color: Color, alpha: Float) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(3.dp))
            .background(color.copy(alpha = alpha * 0.18f))
            .padding(horizontal = 5.dp, vertical = 2.dp)
    ) {
        Text(text, color = color.copy(alpha = alpha), fontSize = 8.sp,
            fontWeight = FontWeight.Bold, letterSpacing = 0.5.sp)
    }
}

@Composable
fun FlashOverlay(text: String, color: Color, modifier: Modifier) {
    val inf = rememberInfiniteTransition(label = "flash")
    val a by inf.animateFloat(
        1f, 0f,
        infiniteRepeatable(tween(300), RepeatMode.Reverse), label = "fa"
    )
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(4.dp))
            .background(color.copy(alpha = a * 0.15f))
            .border(1.dp, color.copy(alpha = a), RoundedCornerShape(4.dp))
            .padding(horizontal = 8.dp, vertical = 4.dp)
    ) {
        Text(text, color = color.copy(alpha = a), fontSize = 10.sp,
            fontWeight = FontWeight.Bold, letterSpacing = 1.sp)
    }
}

@Composable
fun BarIndicator(
    startLabel: String, endLabel: String,
    fraction: Float, color: Color, note: String,
    reversed: Boolean = false
) {
    Row(
        verticalAlignment     = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(3.dp)
    ) {
        if (reversed) Text(note, color = Color(0xFF555555), fontSize = 9.sp)
        Text(startLabel, color = Color(0xFF333333), fontSize = 9.sp)
        Box(
            Modifier.width(85.dp).height(5.dp)
                .clip(RoundedCornerShape(3.dp))
                .background(Color(0xFF0C160C))
        ) {
            Box(
                Modifier.fillMaxHeight().fillMaxWidth(fraction)
                    .clip(RoundedCornerShape(3.dp)).background(color)
            )
        }
        Text(endLabel, color = Color(0xFF333333), fontSize = 9.sp)
        if (!reversed) Text(note, color = Color(0xFF555555), fontSize = 9.sp)
    }
}