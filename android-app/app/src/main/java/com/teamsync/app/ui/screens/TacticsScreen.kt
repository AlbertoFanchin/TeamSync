package com.teamsync.app.ui.screens

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.RotateRight
import androidx.compose.material.icons.filled.Save
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.teamsync.app.ui.theme.*

private data class Player(val pos: Int, val rx: Float, val ry: Float, val role: String, val jersey: Int)

private enum class Side { US, OPP }

// ── Court coordinate system ──────────────────────────────────────────────────
// rx,ry ∈ [0,1]. The net is at ry = 0.5.
//   • Our team plays in the BOTTOM half (ry > 0.5):
//       back row  = ry ≈ 0.85   (P1 right, P6 center, P5 left)
//       front row = ry ≈ 0.62   (P2 right, P3 center, P4 left)
//   • Opponents are MIRRORED in the TOP half (ry < 0.5):
//       front row = ry ≈ 0.38   (their P2/P3/P4 — closest to net)
//       back row  = ry ≈ 0.15   (their P1/P6/P5)

private val INITIAL_US = listOf(
    Player(1, 0.78f, 0.85f, "S",   1),
    Player(2, 0.78f, 0.62f, "OH",  7),
    Player(3, 0.50f, 0.62f, "MB", 11),
    Player(4, 0.22f, 0.62f, "OPP", 4),
    Player(5, 0.22f, 0.85f, "L",   6),
    Player(6, 0.50f, 0.85f, "OH",  9),
)

private val INITIAL_OPP = listOf(
    Player(1, 0.22f, 0.15f, "S",   2),
    Player(2, 0.22f, 0.38f, "OH",  5),
    Player(3, 0.50f, 0.38f, "MB", 10),
    Player(4, 0.78f, 0.38f, "OPP", 3),
    Player(5, 0.78f, 0.15f, "L",   8),
    Player(6, 0.50f, 0.15f, "OH", 12),
)

private fun roleColor(role: String) = when (role) {
    "S"   -> DangerRed
    "OH"  -> SuccessGreen
    "MB"  -> WarnAmber
    "OPP" -> Color(0xFFD946EF)
    "L"   -> Color(0xFFF97316)
    else  -> TextMid
}

@Composable
fun TacticsScreen() {
    var us by remember { mutableStateOf(INITIAL_US) }
    var opp by remember { mutableStateOf(INITIAL_OPP) }
    val density = LocalDensity.current
    val playerRadiusPx = with(density) { 24.dp.toPx() }

    Column(
        Modifier
            .fillMaxSize()
            .background(Bg950)
            .padding(horizontal = 20.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        Spacer(Modifier.height(8.dp))
        Column {
            Text("Rotation Builder", color = TextHi, style = MaterialTheme.typography.titleLarge)
            Text(
                "Drag any player · 6 v 6 · independent rotations",
                color = TextLo,
                style = MaterialTheme.typography.bodyMedium,
            )
        }

        // Action row — Reset + two rotate buttons + Save
        Row(
            Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            OutlinedButton(
                onClick = { us = INITIAL_US; opp = INITIAL_OPP },
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.height(40.dp),
            ) { Text("Reset", style = MaterialTheme.typography.labelMedium) }

            FilledTonalButton(
                onClick = { us = rotateClockwise(us, INITIAL_US) },
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.height(40.dp).weight(1f),
                colors = ButtonDefaults.filledTonalButtonColors(
                    containerColor = Lime.copy(alpha = 0.18f), contentColor = Lime,
                ),
            ) {
                Icon(Icons.Default.RotateRight, null, modifier = Modifier.size(14.dp))
                Spacer(Modifier.width(4.dp))
                Text("Rotate Us", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.SemiBold)
            }

            FilledTonalButton(
                onClick = { opp = rotateClockwise(opp, INITIAL_OPP) },
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.height(40.dp).weight(1f),
                colors = ButtonDefaults.filledTonalButtonColors(
                    containerColor = DangerRed.copy(alpha = 0.18f), contentColor = DangerRed,
                ),
            ) {
                Icon(Icons.Default.RotateRight, null, modifier = Modifier.size(14.dp))
                Spacer(Modifier.width(4.dp))
                Text("Rotate Opp", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.SemiBold)
            }

            Button(
                onClick = { /* TODO: persist via /api/rotations */ },
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.height(40.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Lime, contentColor = Bg950),
            ) {
                Icon(Icons.Default.Save, null, modifier = Modifier.size(14.dp))
            }
        }

        // Side legend
        Row(
            Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(20.dp),
        ) {
            LegendDot("Our Team", Lime)
            LegendDot("Opponents", DangerRed)
        }

        // Court — Canvas + draggable players (both teams)
        BoxWithConstraints(
            Modifier
                .fillMaxWidth()
                .aspectRatio(2f / 3f)
                .clip(RoundedCornerShape(24.dp))
                .background(Brush.linearGradient(listOf(Color(0xFF3D2A12), Color(0xFF1B1206), Bg950))),
        ) {
            val courtW = this.maxWidth
            val courtH = this.maxHeight
            val widthPx  = with(density) { courtW.toPx() }
            val heightPx = with(density) { courtH.toPx() }

            Canvas(Modifier.fillMaxSize()) {
                val pad = 14f
                val courtTopLeft = Offset(pad, pad)
                val courtSize = Size(size.width - 2 * pad, size.height - 2 * pad)
                // Outer court line
                drawRect(
                    color = Color(0xFFFDE68A).copy(alpha = 0.45f),
                    topLeft = courtTopLeft, size = courtSize,
                    style = Stroke(width = 4f),
                )
                // 3 m attack lines (one each side of the net)
                val a1 = courtTopLeft.y + courtSize.height * (1f / 3f)
                val a2 = courtTopLeft.y + courtSize.height * (2f / 3f)
                drawLine(
                    Color(0xFFFDE68A).copy(alpha = 0.35f),
                    Offset(courtTopLeft.x, a1), Offset(courtTopLeft.x + courtSize.width, a1),
                    strokeWidth = 2f,
                )
                drawLine(
                    Color(0xFFFDE68A).copy(alpha = 0.35f),
                    Offset(courtTopLeft.x, a2), Offset(courtTopLeft.x + courtSize.width, a2),
                    strokeWidth = 2f,
                )
                // Net mid-court
                val net = courtTopLeft.y + courtSize.height / 2f
                drawLine(
                    Color.White.copy(alpha = 0.85f),
                    Offset(courtTopLeft.x, net), Offset(courtTopLeft.x + courtSize.width, net),
                    strokeWidth = 5f,
                )
            }

            // Opponents first → drawn under our players
            opp.forEachIndexed { idx, p ->
                PlayerChip(
                    player = p,
                    side = Side.OPP,
                    xPx = p.rx * widthPx,
                    yPx = p.ry * heightPx,
                    radiusPx = playerRadiusPx,
                    onDrag = { dxPx, dyPx ->
                        opp = opp.toMutableList().also {
                            val cur = it[idx]
                            it[idx] = cur.copy(
                                rx = (cur.rx + dxPx / widthPx).coerceIn(0.06f, 0.94f),
                                ry = (cur.ry + dyPx / heightPx).coerceIn(0.06f, 0.94f),
                            )
                        }
                    },
                )
            }
            us.forEachIndexed { idx, p ->
                PlayerChip(
                    player = p,
                    side = Side.US,
                    xPx = p.rx * widthPx,
                    yPx = p.ry * heightPx,
                    radiusPx = playerRadiusPx,
                    onDrag = { dxPx, dyPx ->
                        us = us.toMutableList().also {
                            val cur = it[idx]
                            it[idx] = cur.copy(
                                rx = (cur.rx + dxPx / widthPx).coerceIn(0.06f, 0.94f),
                                ry = (cur.ry + dyPx / heightPx).coerceIn(0.06f, 0.94f),
                            )
                        }
                    },
                )
            }
        }

        // Lineup grid — our team
        LineupGrid(title = "OUR LINEUP", players = us, accent = Lime)
    }
}

@Composable
private fun LegendDot(label: String, color: Color) {
    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
        Box(
            Modifier
                .size(10.dp)
                .clip(CircleShape)
                .background(color)
        )
        Text(label, color = TextMid, style = MaterialTheme.typography.labelMedium)
    }
}

@Composable
private fun LineupGrid(title: String, players: List<Player>, accent: Color) {
    Column(
        Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .background(Bg900)
            .padding(14.dp),
    ) {
        Text(
            title,
            color = accent,
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.SemiBold,
        )
        Spacer(Modifier.height(10.dp))
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            players.sortedBy { it.pos }.forEach { p ->
                Column(
                    Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(12.dp))
                        .background(Bg950)
                        .padding(vertical = 8.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    Text("P${p.pos}", color = TextLo, style = MaterialTheme.typography.labelSmall)
                    Text(
                        p.role,
                        color = roleColor(p.role),
                        fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.labelMedium,
                    )
                    Text("#${p.jersey}", color = TextMid, style = MaterialTheme.typography.labelSmall)
                }
            }
        }
    }
}

@Composable
private fun PlayerChip(
    player: Player,
    side: Side,
    xPx: Float,
    yPx: Float,
    radiusPx: Float,
    onDrag: (dxPx: Float, dyPx: Float) -> Unit,
) {
    val density = LocalDensity.current
    val sizeDp = with(density) { (radiusPx * 2).toDp() }
    val offsetXDp = with(density) { (xPx - radiusPx).toDp() }
    val offsetYDp = with(density) { (yPx - radiusPx).toDp() }
    // `pointerInput(player.pos)` keeps the same coroutine alive across drags,
    // which can capture a stale `onDrag` reference. rememberUpdatedState keeps
    // the lambda fresh every recomposition.
    val onDragUpdated by rememberUpdatedState(onDrag)

    // Our team: solid role-color fill. Opponents: dark fill with red outline.
    val fill = if (side == Side.US) roleColor(player.role) else Bg900
    val labelColor = if (side == Side.US) Bg950 else TextHi

    val baseModifier = Modifier
        .offset(x = offsetXDp, y = offsetYDp)
        .size(sizeDp)
        .clip(CircleShape)
        .background(fill)

    val outlined = if (side == Side.OPP) {
        baseModifier.border(2.dp, DangerRed, CircleShape)
    } else {
        baseModifier
    }

    Box(
        outlined.pointerInput(side to player.pos) {
            detectDragGestures { change, dragAmount ->
                change.consume()
                onDragUpdated(dragAmount.x, dragAmount.y)
            }
        },
        contentAlignment = Alignment.Center,
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                player.jersey.toString(),
                color = labelColor,
                fontWeight = FontWeight.ExtraBold,
                fontSize = 13.sp,
            )
            Text(
                player.role,
                color = labelColor.copy(alpha = 0.75f),
                fontWeight = FontWeight.SemiBold,
                fontSize = 8.sp,
            )
        }
    }
}

// Clockwise serve rotation: 1→6→5→4→3→2→1
// Each player advances to the next position and is snapped to that position's
// initial coordinates for the side they belong to (us vs opp).
private fun rotateClockwise(players: List<Player>, initial: List<Player>): List<Player> {
    val next = mapOf(1 to 6, 6 to 5, 5 to 4, 4 to 3, 3 to 2, 2 to 1)
    val initialByPos = initial.associateBy { it.pos }
    return players.map {
        val newPos = next[it.pos]!!
        val slot = initialByPos[newPos]!!
        it.copy(pos = newPos, rx = slot.rx, ry = slot.ry)
    }
}
