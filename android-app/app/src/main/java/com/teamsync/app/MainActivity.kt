package com.teamsync.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material.icons.filled.GridView
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Logout
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.teamsync.app.data.ServiceLocator
import com.teamsync.app.nav.Routes
import com.teamsync.app.ui.screens.AuthScreen
import com.teamsync.app.ui.screens.CarpoolingScreen
import com.teamsync.app.ui.screens.DashboardScreen
import com.teamsync.app.ui.screens.EventDetailScreen
import com.teamsync.app.ui.screens.ProfileScreen
import com.teamsync.app.ui.screens.TacticsScreen
import com.teamsync.app.ui.screens.WelcomeScreen
import com.teamsync.app.ui.theme.Bg900
import com.teamsync.app.ui.theme.Bg950
import com.teamsync.app.ui.theme.Lime
import com.teamsync.app.ui.theme.TextLo
import com.teamsync.app.ui.theme.TextMid
import com.teamsync.app.viewmodel.AuthViewModel

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen()
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            com.teamsync.app.ui.theme.TeamSyncTheme {
                TeamSyncRoot()
            }
        }
    }
}

private data class NavItem(val route: String, val label: String, val icon: ImageVector)

private val NAV = listOf(
    NavItem(Routes.DASHBOARD, "Home",    Icons.Default.Home),
    NavItem(Routes.CARPOOL,   "Carpool", Icons.Default.DirectionsCar),
    NavItem(Routes.TACTICS,   "Tactics", Icons.Default.GridView),
    NavItem(Routes.PROFILE,   "Profile", Icons.Default.Person),
)

@Composable
private fun TeamSyncRoot() {
    val nav = rememberNavController()
    val authVm: AuthViewModel = viewModel()
    val authState by authVm.ui.collectAsState()
    val sessionState by ServiceLocator.session.state.collectAsState()
    val authed = sessionState.token != null || authState.authed

    val backStackEntry by nav.currentBackStackEntryAsState()
    val currentRoute = backStackEntry?.destination?.route

    Scaffold(
        containerColor = Bg950,
        contentColor = androidx.compose.material3.MaterialTheme.colorScheme.onBackground,
        bottomBar = {
            if (authed && currentRoute in NAV.map { it.route }) {
                BottomNav(currentRoute) { route ->
                    nav.navigate(route) {
                        launchSingleTop = true
                        popUpTo(Routes.DASHBOARD) { saveState = true }
                        restoreState = true
                    }
                }
            }
        },
    ) { inner ->
        NavHost(
            navController = nav,
            startDestination = if (authed) Routes.DASHBOARD else Routes.WELCOME,
            modifier = Modifier
                .fillMaxSize()
                .padding(inner)
                .background(Bg950),
        ) {
            composable(Routes.WELCOME) {
                WelcomeScreen(
                    onLogin    = { nav.navigate(Routes.LOGIN) },
                    onRegister = { nav.navigate(Routes.REGISTER) },
                )
            }
            composable(Routes.LOGIN) {
                AuthScreen(
                    isRegister = false,
                    onAuthed   = { nav.navigate(Routes.DASHBOARD) { popUpTo(Routes.WELCOME) { inclusive = true } } },
                    onBack     = { nav.popBackStack() },
                    vm         = authVm,
                )
            }
            composable(Routes.REGISTER) {
                AuthScreen(
                    isRegister = true,
                    onAuthed   = { nav.navigate(Routes.DASHBOARD) { popUpTo(Routes.WELCOME) { inclusive = true } } },
                    onBack     = { nav.popBackStack() },
                    vm         = authVm,
                )
            }
            composable(Routes.DASHBOARD) {
                AppShell(
                    name = sessionState.name ?: "Player",
                    routeLabel = "Home",
                    onLogout = {
                        authVm.logout()
                        nav.navigate(Routes.WELCOME) { popUpTo(0) }
                    },
                ) {
                    DashboardScreen(onOpenMatch = { id -> nav.navigate(Routes.eventDetail(id)) })
                }
            }
            composable(Routes.CARPOOL) {
                AppShell(
                    name = sessionState.name ?: "Player",
                    routeLabel = "Carpool",
                    onLogout = {
                        authVm.logout()
                        nav.navigate(Routes.WELCOME) { popUpTo(0) }
                    },
                ) { CarpoolingScreen() }
            }
            composable(Routes.TACTICS) {
                AppShell(
                    name = sessionState.name ?: "Player",
                    routeLabel = "Tactics",
                    onLogout = {
                        authVm.logout()
                        nav.navigate(Routes.WELCOME) { popUpTo(0) }
                    },
                ) { TacticsScreen() }
            }
            composable(Routes.PROFILE) {
                AppShell(
                    name = sessionState.name ?: "Player",
                    routeLabel = "Profile",
                    onLogout = {
                        authVm.logout()
                        nav.navigate(Routes.WELCOME) { popUpTo(0) }
                    },
                ) { ProfileScreen() }
            }
            composable(
                route = Routes.EVENT_DETAIL,
                arguments = listOf(navArgument("matchId") { type = NavType.IntType }),
            ) { entry ->
                val matchId = entry.arguments?.getInt("matchId") ?: 0
                EventDetailScreen(matchId = matchId, onBack = { nav.popBackStack() })
            }
        }
    }
}

@Composable
private fun AppShell(name: String, routeLabel: String, onLogout: () -> Unit, content: @Composable () -> Unit) {
    Column(Modifier.fillMaxSize().background(Bg950)) {
        Row(
            Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 14.dp),
            verticalAlignment = androidx.compose.ui.Alignment.CenterVertically,
        ) {
            Column(Modifier.weight(1f)) {
                Text("Hi, ${name.split(" ").first()}", color = TextLo, style = MaterialTheme.typography.bodyMedium)
                Text(routeLabel, color = MaterialTheme.colorScheme.onBackground, style = MaterialTheme.typography.titleLarge)
            }
            IconButton(onClick = onLogout) {
                Icon(Icons.Default.Logout, null, tint = TextMid)
            }
        }
        Box(Modifier.weight(1f)) { content() }
    }
}

@Composable
private fun BottomNav(current: String?, onClick: (String) -> Unit) {
    NavigationBar(
        containerColor = Bg900,
        tonalElevation = 0.dp,
        modifier = Modifier.fillMaxWidth(),
    ) {
        NAV.forEach { item ->
            val selected = current == item.route
            NavigationBarItem(
                selected = selected,
                onClick = { onClick(item.route) },
                icon = { Icon(item.icon, null) },
                label = { Text(item.label, style = MaterialTheme.typography.labelSmall) },
                colors = NavigationBarItemDefaults.colors(
                    selectedIconColor = Lime,
                    selectedTextColor = Lime,
                    unselectedIconColor = TextLo,
                    unselectedTextColor = TextLo,
                    indicatorColor = Lime.copy(alpha = 0.15f),
                ),
            )
        }
    }
}

