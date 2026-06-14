package com.teamsync.app.ui.screens

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.teamsync.app.data.Drill
import com.teamsync.app.data.Match
import com.teamsync.app.ui.theme.*
import com.teamsync.app.viewmodel.DashboardViewModel
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Locale
import java.util.TimeZone
import kotlin.math.abs

private const val SWIPE_THRESHOLD_DP = 110

@Composable
fun DashboardScreen(
    onOpenMatch: (Int) -> Unit = {},
    vm: DashboardViewModel = viewModel(),
) {
    val ui by vm.ui.collectAsState()
    val next = ui.matches.firstOrNull()
    val rest = if (ui.matches.size > 1) ui.matches.drop(1) else emptyList()

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(Bg950)
            .padding(horizontal = 20.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp),
        contentPadding = PaddingValues(top = 12.dp, bottom = 96.dp),
    ) {
        item { SectionHeader("Next Match", trailing = "Swipe to RSVP") }
        item {
            when {
                ui.loading -> SkeletonCard(240.dp)
                next == null -> EmptyCard()
                else -> SwipeRsvpCard(
                    match = next,
                    onDecide = { vm.rsvp(next.id, it) },
                    onOpenDetails = { onOpenMatch(next.id) },
                )
            }
        }

        item { SectionHeader("Daily Drill", icon = true, trailing = "From your skill gaps") }
        item {
            when {
                ui.loading && !ui.drillLoaded -> SkeletonCard(78.dp)
                ui.drill != null              -> DrillCard(ui.drill!!)
                else                          -> EmptyDrillCard()
            }
        }

        if (rest.isNotEmpty()) {
            item { SectionHeader("Schedule") }
            items(rest, key = { it.id }) { m -> ScheduleRow(m, onClick = { onOpenMatch(m.id) }) }
        }
    }
}

@Composable
private fun SwipeRsvpCard(match: Match, onDecide: (String) -> Unit, onOpenDetails: () -> Unit) {
    val density = LocalDensity.current
    val thresholdPx = with(density) { SWIPE_THRESHOLD_DP.dp.toPx() }
    val offsetX = remember { Animatable(0f) }
    val scope = rememberCoroutineScope()

    // Source of truth for the persistent color of the card.
    val status = match.my_rsvp // null = unvoted (gray), "in" = green, "out" = red

    // Background palette by state — these gradients survive after the swipe.
    val (gradTop, gradBot, contentColor) = when (status) {
        "in"  -> Triple(SuccessGreen, Color(0xFF14532D), Bg950)
        "out" -> Triple(DangerRed,    Color(0xFF7F1D1D), TextHi)
        else  -> Triple(Bg800,        Bg900,             TextHi)
    }

    // Live overlay opacities while dragging (relative to current status — let user
    // re-vote by swiping the opposite direction even after a previous choice).
    val inOverlay  = (offsetX.value / thresholdPx).coerceIn(0f, 1f)
    val outOverlay = (-offsetX.value / thresholdPx).coerceIn(0f, 1f)

    Box(
        Modifier
            .fillMaxWidth()
            .height(260.dp),
    ) {
        Box(
            Modifier
                .fillMaxSize()
                .graphicsLayer {
                    translationX = offsetX.value
                    rotationZ = offsetX.value / 28f
                }
                .clip(RoundedCornerShape(28.dp))
                .background(
                    Brush.linearGradient(
                        listOf(gradTop, gradBot),
                        start = Offset(0f, 0f),
                        end = Offset(900f, 900f),
                    )
                )
                .pointerInput(status) {
                    detectHorizontalDragGestures(
                        onDragEnd = {
                            val dx = offsetX.value
                            scope.launch {
                                if (abs(dx) > thresholdPx) {
                                    val choice = if (dx > 0) "in" else "out"
                                    onDecide(choice) // VM updates match.my_rsvp → recomposes with locked color
                                }
                                offsetX.animateTo(0f, tween(280))
                            }
                        },
                        onDragCancel = {
                            scope.launch { offsetX.animateTo(0f, tween(220)) }
                        },
                        onHorizontalDrag = { change, delta ->
                            change.consume()
                            scope.launch { offsetX.snapTo(offsetX.value + delta) }
                        },
                    )
                }
                .padding(20.dp),
        ) {
            Column(Modifier.fillMaxSize(), verticalArrangement = Arrangement.SpaceBetween) {
                Row(
                    Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Top,
                ) {
                    Column(Modifier.weight(1f)) {
                        Text(
                            if (match.is_home == 1) "HOME" else "AWAY",
                            color = contentColor.copy(alpha = 0.65f),
                            fontWeight = FontWeight.SemiBold,
                            style = MaterialTheme.typography.labelSmall,
                        )
                        Spacer(Modifier.height(6.dp))
                        Text(
                            "vs ${match.opponent}",
                            color = contentColor,
                            style = MaterialTheme.typography.headlineMedium,
                            fontWeight = FontWeight.ExtraBold,
                        )
                    }
                    // Persistent status pill (gray / green / red)
                    StatusPill(status)
                }
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    InfoRow(Icons.Default.CalendarToday, formatDate(match.kickoff_at), contentColor)
                    InfoRow(Icons.Default.LocationOn, match.venue, contentColor)
                }
            }
            // Live drag-feedback overlays
            Box(Modifier.align(Alignment.TopEnd)) {
                Pill("I'M IN", SuccessGreen, alpha = inOverlay)
            }
            Box(Modifier.align(Alignment.TopStart)) {
                Pill("OUT", DangerRed, alpha = outOverlay)
            }
        }

        // Tap target for the detail screen — sits below the card so it doesn't fight the drag.
        Row(
            Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 6.dp)
                .clip(RoundedCornerShape(50))
                .clickable(onClick = onOpenDetails)
                .padding(horizontal = 14.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            Text(
                "View attendance & details",
                color = TextMid,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.SemiBold,
            )
            Icon(Icons.AutoMirrored.Filled.KeyboardArrowRight, null, tint = TextMid, modifier = Modifier.size(14.dp))
        }
    }
}

@Composable
private fun StatusPill(status: String?) {
    val (label, bg, fg) = when (status) {
        "in"  -> Triple("ACCEPTED", Bg950.copy(alpha = 0.65f), SuccessGreen)
        "out" -> Triple("DECLINED", Bg950.copy(alpha = 0.65f), DangerRed)
        else  -> Triple("UNVOTED",  Bg950.copy(alpha = 0.65f), TextMid)
    }
    Box(
        Modifier
            .clip(RoundedCornerShape(50))
            .background(bg)
            .padding(horizontal = 12.dp, vertical = 6.dp),
    ) {
        Text(label, color = fg, fontWeight = FontWeight.ExtraBold, style = MaterialTheme.typography.labelSmall)
    }
}

@Composable
private fun Pill(text: String, bg: Color, alpha: Float) {
    Box(
        Modifier
            .alpha(alpha)
            .clip(RoundedCornerShape(50))
            .background(bg)
            .padding(horizontal = 12.dp, vertical = 6.dp),
    ) {
        Text(text, color = Bg950, fontWeight = FontWeight.ExtraBold, style = MaterialTheme.typography.labelSmall)
    }
}

@Composable
private fun InfoRow(icon: androidx.compose.ui.graphics.vector.ImageVector, text: String, tint: Color) {
    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
        Icon(icon, null, tint = tint, modifier = Modifier.size(14.dp))
        Text(text, color = tint, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold)
    }
}

@Composable
private fun DrillCard(drill: Drill) {
    Row(
        Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .background(Bg900)
            .padding(14.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        Box(
            Modifier
                .size(56.dp)
                .clip(RoundedCornerShape(14.dp))
                .background(
                    Brush.linearGradient(listOf(WarnAmber, DangerRed))
                ),
            contentAlignment = Alignment.Center,
        ) {
            Icon(Icons.Default.PlayArrow, null, tint = Bg950, modifier = Modifier.size(28.dp))
        }
        Column(Modifier.weight(1f)) {
            Text(
                drill.skill_tag.uppercase(),
                color = WarnAmber,
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.SemiBold,
            )
            Text(drill.title, color = TextHi, style = MaterialTheme.typography.titleMedium, maxLines = 1)
            Text(
                "${(drill.duration_s / 60).coerceAtLeast(1)} min · video",
                color = TextLo,
                style = MaterialTheme.typography.bodyMedium,
            )
        }
        Icon(Icons.AutoMirrored.Filled.KeyboardArrowRight, null, tint = TextLo)
    }
}

@Composable
private fun ScheduleRow(m: Match, onClick: () -> Unit) {
    Row(
        Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(18.dp))
            .background(Bg900)
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(Modifier.weight(1f)) {
            Text("vs ${m.opponent}", color = TextHi, style = MaterialTheme.typography.titleMedium)
            Text("${formatDate(m.kickoff_at)} · ${m.venue}", color = TextLo, style = MaterialTheme.typography.bodyMedium)
        }
        RsvpChip(m.my_rsvp)
    }
}

@Composable
private fun RsvpChip(status: String?) {
    if (status == null) {
        Text("No reply", color = TextLo, style = MaterialTheme.typography.bodyMedium)
        return
    }
    val (icon, bg, fg) = when (status) {
        "in"    -> Triple(Icons.Default.Check, SuccessGreen.copy(alpha = 0.15f), SuccessGreen)
        "out"   -> Triple(Icons.Default.Close, DangerRed.copy(alpha = 0.15f), DangerRed)
        else    -> Triple(Icons.Default.CalendarToday, WarnAmber.copy(alpha = 0.15f), WarnAmber)
    }
    Row(
        Modifier
            .clip(RoundedCornerShape(50))
            .background(bg)
            .padding(horizontal = 10.dp, vertical = 5.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        Icon(icon, null, tint = fg, modifier = Modifier.size(12.dp))
        Text(status, color = fg, style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.SemiBold)
    }
}

@Composable
private fun SectionHeader(title: String, icon: Boolean = false, trailing: String? = null) {
    Row(
        Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            if (icon) Icon(Icons.Default.LocalFireDepartment, null, tint = WarnAmber, modifier = Modifier.size(14.dp))
            Text(
                title.uppercase(),
                color = TextMid,
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.SemiBold,
            )
        }
        if (trailing != null)
            Text(trailing, color = TextLo, style = MaterialTheme.typography.bodyMedium)
    }
}

@Composable
private fun SkeletonCard(height: androidx.compose.ui.unit.Dp) {
    Box(
        Modifier
            .fillMaxWidth()
            .height(height)
            .clip(RoundedCornerShape(22.dp))
            .background(Bg900)
    )
}

@Composable
private fun EmptyCard() {
    Box(
        Modifier
            .fillMaxWidth()
            .height(160.dp)
            .clip(RoundedCornerShape(22.dp))
            .background(Bg900),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            "No upcoming matches scheduled.",
            color = TextLo,
            style = MaterialTheme.typography.bodyMedium,
        )
    }
}

@Composable
private fun EmptyDrillCard() {
    Box(
        Modifier
            .fillMaxWidth()
            .height(72.dp)
            .clip(RoundedCornerShape(20.dp))
            .background(Bg900),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            "No drill suggested today — pick a skill gap in Profile.",
            color = TextLo,
            style = MaterialTheme.typography.bodyMedium,
        )
    }
}

@Suppress("SpellCheckingInspection")
private fun formatDate(iso: String): String {
    return runCatching {
        val parsers = listOf(
            "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'",
            "yyyy-MM-dd'T'HH:mm:ss'Z'",
            "yyyy-MM-dd HH:mm:ss",
        )
        for (p in parsers) {
            val f = SimpleDateFormat(p, Locale.US).apply { timeZone = TimeZone.getTimeZone("UTC") }
            runCatching { f.parse(iso) }.getOrNull()?.let {
                return SimpleDateFormat("EEE MMM d, HH:mm", Locale.getDefault()).format(it)
            }
        }
        iso
    }.getOrDefault(iso)
}
