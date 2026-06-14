package com.teamsync.app.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Mail
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.teamsync.app.ui.theme.Bg900
import com.teamsync.app.ui.theme.Bg950
import com.teamsync.app.ui.theme.Border
import com.teamsync.app.ui.theme.DangerRed
import com.teamsync.app.ui.theme.Lime
import com.teamsync.app.ui.theme.TextHi
import com.teamsync.app.ui.theme.TextLo
import com.teamsync.app.ui.theme.TextMid
import com.teamsync.app.viewmodel.AuthViewModel

@Composable
fun AuthScreen(
    isRegister: Boolean,
    onAuthed: () -> Unit,
    onBack: () -> Unit,
    vm: AuthViewModel = viewModel(),
) {
    val ui by vm.ui.collectAsState()
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var name by remember { mutableStateOf("") }
    var sport by remember { mutableStateOf("volleyball") }

    LaunchedEffect(ui.authed) { if (ui.authed) onAuthed() }

    Column(
        Modifier
            .fillMaxSize()
            .background(Bg950)
            .padding(horizontal = 24.dp, vertical = 32.dp),
    ) {
        IconButton(onClick = onBack, modifier = Modifier.size(40.dp)) {
            Icon(Icons.AutoMirrored.Filled.ArrowBack, null, tint = TextMid)
        }
        Spacer(Modifier.height(16.dp))
        Text(
            if (isRegister) "Join your team" else "Welcome back",
            color = TextHi,
            style = MaterialTheme.typography.headlineLarge,
        )
        Spacer(Modifier.height(6.dp))
        Text(
            if (isRegister) "Set up your profile in seconds." else "Sign in to sync up.",
            color = TextMid,
            style = MaterialTheme.typography.bodyMedium,
        )
        Spacer(Modifier.height(28.dp))

        if (isRegister) {
            TsField(value = name, onChange = { name = it }, placeholder = "Full name", leading = Icons.Default.Person)
            Spacer(Modifier.height(10.dp))
        }
        TsField(
            value = email, onChange = { email = it }, placeholder = "Email",
            leading = Icons.Default.Mail,
            keyboard = KeyboardOptions(keyboardType = KeyboardType.Email),
        )
        Spacer(Modifier.height(10.dp))
        TsField(
            value = password, onChange = { password = it }, placeholder = "Password",
            leading = Icons.Default.Lock, isPassword = true,
        )

        if (isRegister) {
            Spacer(Modifier.height(16.dp))
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                listOf("volleyball", "basketball", "soccer").forEach { s ->
                    val selected = sport == s
                    OutlinedButton(
                        onClick = { sport = s },
                        modifier = Modifier
                            .weight(1f)
                            .height(46.dp),
                        shape = RoundedCornerShape(14.dp),
                        colors = ButtonDefaults.outlinedButtonColors(
                            containerColor = if (selected) Lime.copy(alpha = 0.12f) else Bg950,
                            contentColor = if (selected) Lime else TextMid,
                        ),
                        border = androidx.compose.foundation.BorderStroke(
                            1.dp, if (selected) Lime else Border,
                        ),
                    ) {
                        Text(s.replaceFirstChar { it.uppercase() }, style = MaterialTheme.typography.labelMedium)
                    }
                }
            }
        }

        if (ui.error != null) {
            Spacer(Modifier.height(12.dp))
            Text(ui.error!!, color = DangerRed, style = MaterialTheme.typography.bodyMedium)
        }

        Spacer(Modifier.height(24.dp))
        Button(
            onClick = {
                if (isRegister) vm.register(email, password, name, sport)
                else vm.login(email, password)
            },
            enabled = !ui.loading,
            modifier = Modifier
                .fillMaxWidth()
                .height(54.dp),
            shape = RoundedCornerShape(18.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Lime, contentColor = Bg950),
        ) {
            if (ui.loading)
                CircularProgressIndicator(color = Bg950, strokeWidth = 2.dp, modifier = Modifier.size(20.dp))
            else
                Text(if (isRegister) "Create account" else "Sign in", fontWeight = FontWeight.SemiBold)
        }

        Spacer(Modifier.height(16.dp))
        Text(
            if (isRegister) "Already a member? Tap back to sign in."
            else "No account? Tap back to register.",
            color = TextLo,
            style = MaterialTheme.typography.bodyMedium,
            modifier = Modifier.align(Alignment.CenterHorizontally),
        )
    }
}

@Composable
private fun TsField(
    value: String,
    onChange: (String) -> Unit,
    placeholder: String,
    leading: androidx.compose.ui.graphics.vector.ImageVector,
    isPassword: Boolean = false,
    keyboard: KeyboardOptions = KeyboardOptions.Default,
) {
    OutlinedTextField(
        value = value,
        onValueChange = onChange,
        placeholder = { Text(placeholder, color = TextLo) },
        leadingIcon = { Icon(leading, null, tint = TextMid) },
        singleLine = true,
        modifier = Modifier
            .fillMaxWidth()
            .height(56.dp),
        shape = RoundedCornerShape(18.dp),
        visualTransformation = if (isPassword) PasswordVisualTransformation() else androidx.compose.ui.text.input.VisualTransformation.None,
        keyboardOptions = keyboard,
        colors = OutlinedTextFieldDefaults.colors(
            focusedContainerColor = Bg900,
            unfocusedContainerColor = Bg900,
            focusedBorderColor = Lime,
            unfocusedBorderColor = Border,
            focusedTextColor = TextHi,
            unfocusedTextColor = TextHi,
            cursorColor = Lime,
        ),
    )
}
