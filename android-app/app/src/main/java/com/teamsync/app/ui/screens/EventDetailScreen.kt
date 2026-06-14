package com.teamsync.app.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.HelpOutline
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.QuestionMark
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.teamsync.app.data.AttendanceEntry
import com.teamsync.app.data.Match
import com.teamsync.app.ui.theme.*
import com.teamsync.app.viewmodel.EventDetailViewModel
import java.text.SimpleDateFormat
import java.util.Locale
import java.util.TimeZone

@Composable
fun EventDetailScreen(
    matchId: Int,
    onBack: () -> Unit,
    vm: EventDetailViewModel = viewModel(),
) {
    LaunchedEffect(matchId) { vm.load(matchId) }
    val ui by vm.ui.collectAsState()

    Column(
        Modifier
            .fillMaxSize()
            .background(Bg950),
    ) {
        Row(
            Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            IconButton(onClick = onBack) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, null, tint = TextHi)
            }
            Text(
                "Match Details",
                color = TextHi,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier.padding(start = 4.dp),
            )
        }

        when {
            ui.loading -> Loading()
            ui.detail == null -> ErrorBox(ui.error ?: "Could not load match.")
            else -> {
                val match = ui.detail!!.match
                val list = ui.detail!!.attendance
                val present = list.count { it.status == "in" }
                val absent  = list.count { it.status == "out" }
                val pending = list.count { it.status == "pending" || it.status == "maybe" }

                LazyColumn(
                    contentPadding = PaddingValues(horizontal = 20.dp, vertical = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp),
                ) {
                    item { MatchHeader(match) }
                    item {
                        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                            CountChip("Present", present, SuccessGreen, Modifier.weight(1f))
                            CountChip("Pending", pending, WarnAmber,    Modifier.weight(1f))
                            CountChip("Absent",  absent,  DangerRed,    Modifier.weight(1f))
                        }
                    }
                    item { RsvpActions(myRsvp = match.my_rsvp) { vm.rsvp(matchId, it) } }

                    item {
                        Text(
                            "ATTENDANCE",
                            color = TextMid,
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.SemiBold,
                            modifier = Modifier.padding(top = 6.dp),
                        )
                    }
                    items(list, key = { it.user_id }) { AttendanceRow(it) }
                }
            }
        }
    }
}

@Composable
private fun MatchHeader(m: Match) {
    Column(
        Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(22.dp))
            .background(Bg900)
            .padding(18.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Text(
            if (m.is_home == 1) "HOME GAME" else "AWAY GAME",
            color = Lime,
            fontWeight = FontWeight.ExtraBold,
            style = MaterialTheme.typography.labelSmall,
        )
        Text(
            "vs ${m.opponent}",
            color = TextHi,
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.ExtraBold,
        )
        Spacer(Modifier.height(4.dp))
        IconLine(Icons.Default.CalendarToday, formatLong(m.kickoff_at))
        IconLine(Icons.Default.LocationOn, m.venue)
        if (!m.address.isNullOrBlank()) {
            Text(
                m.address,
                color = TextMid,
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.padding(start = 22.dp),
            )
        }
    }
}

@Composable
private fun IconLine(icon: ImageVector, text: String) {
    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
        Icon(icon, null, tint = TextMid, modifier = Modifier.size(14.dp))
        Text(text, color = TextHi, style = MaterialTheme.typography.bodyLarge)
    }
}

@Composable
private fun CountChip(label: String, count: Int, color: Color, modifier: Modifier = Modifier) {
    Column(
        modifier
            .clip(RoundedCornerShape(18.dp))
            .background(color.copy(alpha = 0.10f))
            .padding(vertical = 12.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(count.toString(), color = color, fontWeight = FontWeight.ExtraBold, style = MaterialTheme.typography.headlineMedium)
        Text(label.uppercase(), color = color, style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.SemiBold)
    }
}

@Composable
private fun RsvpActions(myRsvp: String?, onPick: (String) -> Unit) {
    Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
        RsvpBtn("Accept",  Icons.Default.Check,       SuccessGreen, selected = myRsvp == "in",    Modifier.weight(1f)) { onPick("in") }
        RsvpBtn("Maybe",   Icons.Default.HelpOutline, WarnAmber,    selected = myRsvp == "maybe", Modifier.weight(1f)) { onPick("maybe") }
        RsvpBtn("Decline", Icons.Default.Close,       DangerRed,    selected = myRsvp == "out",   Modifier.weight(1f)) { onPick("out") }
    }
}

@Composable
private fun RsvpBtn(
    label: String, icon: ImageVector, color: Color, selected: Boolean, modifier: Modifier = Modifier, onClick: () -> Unit,
) {
    Button(
        onClick = onClick,
        modifier = modifier.height(48.dp),
        shape = RoundedCornerShape(16.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = if (selected) color else color.copy(alpha = 0.12f),
            contentColor   = if (selected) Bg950 else color,
        ),
    ) {
        Icon(icon, null, modifier = Modifier.size(16.dp))
        Spacer(Modifier.width(6.dp))
        Text(label, fontWeight = FontWeight.SemiBold, style = MaterialTheme.typography.labelMedium)
    }
}

@Composable
private fun AttendanceRow(e: AttendanceEntry) {
    val (color, icon, label) = when (e.status) {
        "in"      -> Triple(SuccessGreen, Icons.Default.Check,       "Present")
        "out"     -> Triple(DangerRed,    Icons.Default.Close,       "Absent")
        "maybe"   -> Triple(WarnAmber,    Icons.Default.HelpOutline, "Maybe")
        else      -> Triple(TextMid,      Icons.Default.QuestionMark,"Pending")
    }
    Row(
        Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(Bg900)
            .padding(horizontal = 14.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            Modifier
                .size(36.dp)
                .clip(CircleShape)
                .background(color.copy(alpha = 0.20f)),
            contentAlignment = Alignment.Center,
        ) {
            Text(e.name.take(1).uppercase(), color = color, fontWeight = FontWeight.Bold)
        }
        Spacer(Modifier.width(12.dp))
        Column(Modifier.weight(1f)) {
            Text(e.name, color = TextHi, style = MaterialTheme.typography.titleMedium)
            Text(e.position ?: "—", color = TextLo, style = MaterialTheme.typography.bodyMedium)
        }
        Row(
            Modifier
                .clip(RoundedCornerShape(50))
                .background(color.copy(alpha = 0.15f))
                .padding(horizontal = 10.dp, vertical = 5.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            Icon(icon, null, tint = color, modifier = Modifier.size(13.dp))
            Text(label, color = color, fontWeight = FontWeight.SemiBold, style = MaterialTheme.typography.labelSmall)
        }
    }
}

@Composable
private fun Loading() {
    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        CircularProgressIndicator(color = Lime)
    }
}

@Composable
private fun ErrorBox(text: String) {
    Box(Modifier.fillMaxSize().padding(24.dp), contentAlignment = Alignment.Center) {
        Text(text, color = DangerRed)
    }
}

@Suppress("SpellCheckingInspection")
private fun formatLong(iso: String): String {
    return runCatching {
        val parsers = listOf(
            "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'",
            "yyyy-MM-dd'T'HH:mm:ss'Z'",
            "yyyy-MM-dd HH:mm:ss",
        )
        for (p in parsers) {
            val f = SimpleDateFormat(p, Locale.US).apply { timeZone = TimeZone.getTimeZone("UTC") }
            runCatching { f.parse(iso) }.getOrNull()?.let {
                return SimpleDateFormat("EEEE, MMM d · HH:mm", Locale.getDefault()).format(it)
            }
        }
        iso
    }.getOrDefault(iso)
}
