package com.teamsync.app.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Save
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.teamsync.app.data.SkillLevels
import com.teamsync.app.data.SessionStore
import com.teamsync.app.data.ServiceLocator
import com.teamsync.app.ui.theme.*
import com.teamsync.app.viewmodel.ProfileViewModel
import kotlin.math.cos
import kotlin.math.roundToInt
import kotlin.math.sin

private data class Skill(val key: String, val label: String, val description: String)

private val SKILLS = listOf(
    Skill("spiking",  "Spiking",  "Attack power & shot selection"),
    Skill("setting",  "Setting",  "Ball control, location, tempo"),
    Skill("digging",  "Digging",  "Defensive reaction & platform"),
    Skill("serving",  "Serving",  "Accuracy, zone, jump-serve"),
    Skill("blocking", "Blocking", "Footwork, press, timing"),
    Skill("passing",  "Passing",  "Reception platform & angle"),
)

@Composable
fun ProfileScreen(vm: ProfileViewModel = viewModel()) {
    val ui by vm.ui.collectAsState()
    val session: SessionStore = ServiceLocator.session
    val sessionState by session.state.collectAsState()

    Column(
        Modifier
            .fillMaxSize()
            .background(Bg950)
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp)
            .padding(bottom = 100.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        Spacer(Modifier.height(4.dp))

        // Identity card
        Row(
            Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(20.dp))
                .background(Bg900)
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Box(
                Modifier
                    .size(56.dp)
                    .clip(CircleShape)
                    .background(Lime.copy(alpha = 0.20f)),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    (sessionState.name ?: "T").take(1).uppercase(),
                    color = Lime, fontWeight = FontWeight.ExtraBold,
                    style = MaterialTheme.typography.headlineMedium,
                )
            }
            Spacer(Modifier.width(14.dp))
            Column(Modifier.weight(1f)) {
                Text(
                    sessionState.name ?: "Player",
                    color = TextHi, style = MaterialTheme.typography.titleLarge,
                )
                Text(
                    sessionState.email ?: "",
                    color = TextLo, style = MaterialTheme.typography.bodyMedium,
                )
            }
            Text(
                "${overall(ui.levels).format1()} / 5",
                color = Lime, fontWeight = FontWeight.ExtraBold,
                style = MaterialTheme.typography.titleLarge,
            )
        }

        // Self-evaluation
        Column(
            Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(20.dp))
                .background(Bg900)
                .padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                Icon(Icons.Default.Bolt, null, tint = Lime, modifier = Modifier.size(18.dp))
                Text(
                    "SELF-EVALUATION",
                    color = TextMid,
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.SemiBold,
                )
            }
            Text(
                "Rate each skill 0–5 (half-step granularity). Your coach uses this to plan drills and rotations.",
                color = TextLo, style = MaterialTheme.typography.bodyMedium,
            )
            Spacer(Modifier.height(16.dp))

            // Radar Chart inserted here
            RadarChart(
                levels = ui.levels,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(240.dp)
                    .padding(vertical = 16.dp)
            )

            Spacer(Modifier.height(16.dp))

            if (ui.loading) {
                Box(Modifier.fillMaxWidth().height(120.dp), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = Lime)
                }
            } else {
                SKILLS.forEach { s ->
                    SkillSlider(
                        skill = s,
                        value = ui.levels.read(s.key),
                        onChange = { v -> vm.update { it.write(s.key, v) } },
                    )
                }
            }
        }

        // Save button + status
        Button(
            onClick = { vm.save() },
            enabled = !ui.saving && !ui.loading,
            modifier = Modifier
                .fillMaxWidth()
                .height(54.dp),
            shape = RoundedCornerShape(18.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Lime, contentColor = Bg950),
        ) {
            when {
                ui.saving -> CircularProgressIndicator(color = Bg950, strokeWidth = 2.dp, modifier = Modifier.size(20.dp))
                ui.saved  -> { Icon(Icons.Default.Check, null); Spacer(Modifier.width(6.dp)); Text("Saved", fontWeight = FontWeight.SemiBold) }
                else      -> { Icon(Icons.Default.Save,  null); Spacer(Modifier.width(6.dp)); Text("Save evaluation", fontWeight = FontWeight.SemiBold) }
            }
        }
        if (ui.error != null) {
            Text(ui.error!!, color = DangerRed, style = MaterialTheme.typography.bodyMedium)
        }
    }
}

@Composable
fun RadarChart(levels: SkillLevels, modifier: Modifier = Modifier) {
    val textMeasurer = rememberTextMeasurer()
    val labelStyle = MaterialTheme.typography.labelSmall.copy(color = TextMid, fontWeight = FontWeight.SemiBold)

    val values = listOf(levels.spiking, levels.setting, levels.digging, levels.serving, levels.blocking, levels.passing)
    val labels = listOf("Spike", "Set", "Dig", "Serve", "Block", "Pass")
    val maxLevel = 5f
    val sides = 6

    androidx.compose.foundation.Canvas(modifier = modifier) {
        val radius = (size.minDimension / 2) * 0.75f
        val center = Offset(size.width / 2, size.height / 2)

        // Draw web rings (1 to 5)
        for (step in 1..5) {
            val r = (step / maxLevel) * radius
            val webPath = Path()
            for (i in 0 until sides) {
                val angle = -Math.PI / 2 + i * (2 * Math.PI / sides)
                val x = center.x + (r * cos(angle)).toFloat()
                val y = center.y + (r * sin(angle)).toFloat()
                if (i == 0) webPath.moveTo(x, y) else webPath.lineTo(x, y)
            }
            webPath.close()
            drawPath(path = webPath, color = Bg800, style = Stroke(width = 1.dp.toPx()))
        }

        // Draw axis lines and labels
        for (i in 0 until sides) {
            val angle = -Math.PI / 2 + i * (2 * Math.PI / sides)
            val endX = center.x + (radius * cos(angle)).toFloat()
            val endY = center.y + (radius * sin(angle)).toFloat()
            drawLine(color = Bg800, start = center, end = Offset(endX, endY), strokeWidth = 1.dp.toPx())

            // Labels
            val textRadius = radius + 22.dp.toPx()
            val textX = center.x + (textRadius * cos(angle)).toFloat()
            val textY = center.y + (textRadius * sin(angle)).toFloat()
            val layoutResult = textMeasurer.measure(labels[i], labelStyle)
            drawText(
                textMeasurer = textMeasurer,
                text = labels[i],
                style = labelStyle,
                topLeft = Offset(textX - layoutResult.size.width / 2, textY - layoutResult.size.height / 2)
            )
        }

        // Draw data polygon
        if (values.any { it > 0 }) {
            val dataPath = Path()
            values.forEachIndexed { i, value ->
                val angle = -Math.PI / 2 + i * (2 * Math.PI / sides)
                val r = (value / maxLevel) * radius
                val x = center.x + (r * cos(angle)).toFloat()
                val y = center.y + (r * sin(angle)).toFloat()
                if (i == 0) dataPath.moveTo(x, y) else dataPath.lineTo(x, y)
            }
            dataPath.close()

            drawPath(path = dataPath, color = Lime.copy(alpha = 0.35f))
            drawPath(path = dataPath, color = Lime, style = Stroke(width = 3.dp.toPx()))
        }
    }
}

@Composable
private fun SkillSlider(skill: Skill, value: Float, onChange: (Float) -> Unit) {
    Column {
        Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            Column(Modifier.weight(1f)) {
                Text(skill.label, color = TextHi, style = MaterialTheme.typography.titleMedium)
                Text(skill.description, color = TextLo, style = MaterialTheme.typography.bodyMedium)
            }
            Text(
                value.format1(),
                color = Lime,
                fontWeight = FontWeight.ExtraBold,
                style = MaterialTheme.typography.titleMedium,
            )
        }
        Slider(
            value = value,
            onValueChange = onChange,
            valueRange = 0f..5f,
            steps = 9,
            colors = SliderDefaults.colors(
                thumbColor = Lime,
                activeTrackColor = Lime,
                inactiveTrackColor = Bg800,
                activeTickColor = Lime.copy(alpha = 0.7f),
                inactiveTickColor = TextLo.copy(alpha = 0.5f),
            ),
        )
    }
}

// ── helpers ───────────────────────────────────────────────────────────────────

private fun overall(s: SkillLevels): Float {
    val vals = listOf(s.spiking, s.setting, s.digging, s.serving, s.blocking, s.passing)
    val nonZero = vals.filter { it > 0f }
    return if (nonZero.isEmpty()) 0f else nonZero.average().toFloat()
}

private fun Float.format1(): String = "${(this * 10).roundToInt() / 10f}"

private fun SkillLevels.read(key: String): Float = when (key) {
    "spiking"  -> spiking
    "setting"  -> setting
    "digging"  -> digging
    "serving"  -> serving
    "blocking" -> blocking
    "passing"  -> passing
    else -> 0f
}

private fun SkillLevels.write(key: String, v: Float): SkillLevels = when (key) {
    "spiking"  -> copy(spiking = v)
    "setting"  -> copy(setting = v)
    "digging"  -> copy(digging = v)
    "serving"  -> copy(serving = v)
    "blocking" -> copy(blocking = v)
    "passing"  -> copy(passing = v)
    else -> this
}