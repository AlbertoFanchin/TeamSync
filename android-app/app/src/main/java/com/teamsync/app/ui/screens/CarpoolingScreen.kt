package com.teamsync.app.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material.icons.filled.EventSeat
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Navigation
import androidx.compose.material.icons.filled.People
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.teamsync.app.data.Carpool
import com.teamsync.app.ui.theme.*
import com.teamsync.app.viewmodel.CarpoolViewModel
import java.text.SimpleDateFormat
import java.util.Locale
import java.util.TimeZone

@Composable
fun CarpoolingScreen(vm: CarpoolViewModel = viewModel()) {
    val ui by vm.ui.collectAsState()
    val totalSeats = ui.cars.sumOf { it.seats }
    val taken = ui.cars.sumOf { it.passengers.size }
    var showOffer by remember { mutableStateOf(false) }

    if (showOffer) {
        OfferRideDialog(
            submitting = ui.offering,
            error = ui.offerError,
            onDismiss = { showOffer = false },
            onSubmit = { seats, departAt ->
                vm.offerRide(seats, departAt) { showOffer = false }
            },
        )
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(Bg950)
            .padding(horizontal = 20.dp),
        contentPadding = PaddingValues(top = 12.dp, bottom = 96.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        item {
            Row(
                Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                Stat("Drivers", ui.cars.size.toString(), Icons.Default.DirectionsCar, Modifier.weight(1f))
                Stat("Free seats", (totalSeats - taken).toString(), Icons.Default.People, Modifier.weight(1f))
                Stat("Confirmed", taken.toString(), Icons.Default.LocationOn, Modifier.weight(1f))
            }
        }
        item {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Button(
                    onClick = { vm.optimize() },
                    enabled = ui.cars.isNotEmpty() && !ui.optimizing,
                    modifier = Modifier
                        .weight(1f)
                        .height(52.dp),
                    shape = RoundedCornerShape(18.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Lime, contentColor = Bg950),
                ) {
                    Icon(Icons.Default.Bolt, null, modifier = Modifier.size(16.dp))
                    Spacer(Modifier.width(8.dp))
                    Text(
                        if (ui.optimizing) "Optimizing routes…" else "Optimize routes",
                        fontWeight = FontWeight.SemiBold,
                    )
                }
                OutlinedButton(
                    onClick = { showOffer = true },
                    modifier = Modifier.height(52.dp),
                    shape = RoundedCornerShape(18.dp),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = TextHi),
                ) {
                    Icon(Icons.Default.Add, null, modifier = Modifier.size(16.dp))
                    Spacer(Modifier.width(6.dp))
                    Text("Offer")
                }
            }
        }
        when {
            ui.loading -> item {
                Box(
                    Modifier
                        .fillMaxWidth()
                        .height(160.dp)
                        .clip(RoundedCornerShape(20.dp))
                        .background(Bg900)
                )
            }
            ui.cars.isEmpty() -> item {
                Box(
                    Modifier
                        .fillMaxWidth()
                        .height(160.dp)
                        .clip(RoundedCornerShape(20.dp))
                        .background(Bg900),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        "No rides offered yet. Tap \"Offer\" to volunteer your car.",
                        color = TextLo,
                        style = MaterialTheme.typography.bodyMedium,
                    )
                }
            }
            else -> items(ui.cars, key = { it.id }) { CarRow(it) }
        }
    }
}

@Composable
private fun Stat(label: String, value: String, icon: ImageVector, modifier: Modifier = Modifier) {
    Column(
        modifier
            .clip(RoundedCornerShape(16.dp))
            .background(Bg900)
            .padding(12.dp),
    ) {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
            Icon(icon, null, tint = TextMid, modifier = Modifier.size(14.dp))
            Text(label.uppercase(), color = TextMid, style = MaterialTheme.typography.labelSmall)
        }
        Spacer(Modifier.height(4.dp))
        Text(value, color = TextHi, fontWeight = FontWeight.ExtraBold, style = MaterialTheme.typography.headlineMedium)
    }
}

@Composable
private fun CarRow(c: Carpool) {
    Column(
        Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .background(Bg900)
            .padding(16.dp),
    ) {
        Row(
            Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                Box(
                    Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(Lime.copy(alpha = 0.15f)),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(c.driver_name.take(1), color = Lime, fontWeight = FontWeight.Bold)
                }
                Column {
                    Text(c.driver_name, color = TextHi, style = MaterialTheme.typography.titleMedium)
                    Text("Departs ${formatTime(c.depart_at)}", color = TextLo, style = MaterialTheme.typography.bodyMedium)
                }
            }
            Seats(taken = c.passengers.size, total = c.seats)
        }
        Spacer(Modifier.height(12.dp))
        RouteList(c)
    }
}

@Composable
private fun Seats(taken: Int, total: Int) {
    Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
        repeat(total) { i ->
            Box(
                Modifier
                    .size(10.dp)
                    .clip(CircleShape)
                    .background(if (i < taken) Lime else Bg800)
            )
        }
    }
}

@Composable
private fun RouteList(c: Carpool) {
    val stops: List<Pair<String, Boolean>> = c.route_json
        ?.mapIndexed { i, p -> (p.name ?: "Pickup ${i + 1}") to (i == c.route_json.lastIndex) }
        ?: (c.passengers.map { it.name to false } + ("Venue" to true))

    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        stops.forEach { (label, final) ->
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                Box(
                    Modifier
                        .size(10.dp)
                        .clip(CircleShape)
                        .background(if (final) SuccessGreen else Lime)
                )
                if (final) Icon(Icons.Default.Navigation, null, tint = SuccessGreen, modifier = Modifier.size(12.dp))
                Text(
                    label,
                    color = if (final) SuccessGreen else TextHi,
                    fontWeight = if (final) FontWeight.SemiBold else FontWeight.Normal,
                    style = MaterialTheme.typography.bodyLarge,
                )
            }
        }
    }
}

// ── Offer Ride dialog ────────────────────────────────────────────────────────

@Composable
private fun OfferRideDialog(
    submitting: Boolean,
    error: String?,
    onDismiss: () -> Unit,
    onSubmit: (seats: Int, departAt: String) -> Unit,
) {
    var seats by remember { mutableStateOf("3") }
    var date by remember { mutableStateOf(defaultMatchDate()) }
    var time by remember { mutableStateOf("18:45") }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = Bg900,
        titleContentColor = TextHi,
        textContentColor = TextHi,
        shape = RoundedCornerShape(24.dp),
        title = {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Icon(Icons.Default.DirectionsCar, null, tint = Lime)
                Text("Offer a Ride", fontWeight = FontWeight.ExtraBold)
            }
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text(
                    "Volunteer your car for the next match. Teammates who need a ride will be auto-assigned.",
                    color = TextLo,
                    style = MaterialTheme.typography.bodyMedium,
                )
                OfferField(
                    value = seats,
                    onChange = { v -> seats = v.filter { c -> c.isDigit() }.take(1) },
                    leading = Icons.Default.EventSeat,
                    placeholder = "Available seats (1–8)",
                    keyboard = KeyboardOptions(keyboardType = KeyboardType.Number),
                )
                OfferField(
                    value = date, onChange = { date = it },
                    leading = Icons.Default.Schedule,
                    placeholder = "Date (YYYY-MM-DD)",
                )
                OfferField(
                    value = time, onChange = { time = it },
                    leading = Icons.Default.Schedule,
                    placeholder = "Departure time (HH:MM)",
                )
                if (error != null) {
                    Text(error, color = DangerRed, style = MaterialTheme.typography.bodyMedium)
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val n = seats.toIntOrNull() ?: 0
                    if (n in 1..8) onSubmit(n, "$date $time:00")
                },
                enabled = !submitting,
                colors = ButtonDefaults.buttonColors(containerColor = Lime, contentColor = Bg950),
                shape = RoundedCornerShape(14.dp),
            ) {
                if (submitting)
                    CircularProgressIndicator(color = Bg950, strokeWidth = 2.dp, modifier = Modifier.size(18.dp))
                else
                    Text("Confirm", fontWeight = FontWeight.SemiBold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss, colors = ButtonDefaults.textButtonColors(contentColor = TextMid)) {
                Icon(Icons.Default.Close, null, modifier = Modifier.size(16.dp))
                Spacer(Modifier.width(4.dp))
                Text("Cancel")
            }
        },
    )
}

@Composable
private fun OfferField(
    value: String,
    onChange: (String) -> Unit,
    leading: ImageVector,
    placeholder: String,
    keyboard: KeyboardOptions = KeyboardOptions.Default,
) {
    OutlinedTextField(
        value = value,
        onValueChange = onChange,
        placeholder = { Text(placeholder, color = TextLo) },
        leadingIcon = { Icon(leading, null, tint = TextMid) },
        singleLine = true,
        keyboardOptions = keyboard,
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        colors = OutlinedTextFieldDefaults.colors(
            focusedContainerColor = Bg950,
            unfocusedContainerColor = Bg950,
            focusedBorderColor = Lime,
            unfocusedBorderColor = Bg800,
            focusedTextColor = TextHi,
            unfocusedTextColor = TextHi,
            cursorColor = Lime,
        ),
    )
}

private fun defaultMatchDate(): String {
    val cal = java.util.Calendar.getInstance()
    cal.add(java.util.Calendar.DAY_OF_MONTH, 2)
    val y = cal.get(java.util.Calendar.YEAR)
    val m = (cal.get(java.util.Calendar.MONTH) + 1).toString().padStart(2, '0')
    val d = cal.get(java.util.Calendar.DAY_OF_MONTH).toString().padStart(2, '0')
    return "$y-$m-$d"
}

@Suppress("SpellCheckingInspection")
private fun formatTime(iso: String): String {
    return runCatching {
        val parsers = listOf(
            "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'",
            "yyyy-MM-dd'T'HH:mm:ss'Z'",
            "yyyy-MM-dd HH:mm:ss",
        )
        for (p in parsers) {
            val f = SimpleDateFormat(p, Locale.US).apply { timeZone = TimeZone.getTimeZone("UTC") }
            runCatching { f.parse(iso) }.getOrNull()?.let {
                return SimpleDateFormat("HH:mm", Locale.getDefault()).format(it)
            }
        }
        iso
    }.getOrDefault(iso)
}
