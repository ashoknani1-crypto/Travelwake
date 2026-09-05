package com.example

import android.Manifest
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Checklist
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.FloatingActionButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.runtime.collectAsState
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.travelwake.ui.components.VoiceCommandDialog
import com.example.travelwake.voice.VoiceCommand
import com.example.travelwake.ui.screens.ActiveJourneyScreen
import com.example.travelwake.ui.screens.BelongingsScreen
import com.example.travelwake.ui.screens.HistoryScreen
import com.example.travelwake.ui.screens.HomeScreen
import com.example.travelwake.ui.screens.JourneySetupScreen
import com.example.travelwake.ui.screens.MainAlarmScreen
import com.example.travelwake.ui.screens.SavedPlacesScreen
import com.example.travelwake.ui.screens.SearchDestinationScreen
import com.example.travelwake.ui.screens.SecurityLabScreen
import com.example.travelwake.ui.screens.SettingsScreen
import com.example.travelwake.ui.screens.WearOSPreviewScreen
import com.example.travelwake.viewmodel.TravelWakeViewModel
import com.example.ui.theme.LocalWeatherThemePalette
import com.example.ui.theme.MyApplicationTheme
import com.example.ui.theme.ProfessionalBackground
import com.example.ui.theme.ProfessionalBorder
import com.example.ui.theme.ProfessionalDivider
import com.example.ui.theme.ProfessionalOnPrimaryContainer
import com.example.ui.theme.ProfessionalPrimary
import com.example.ui.theme.ProfessionalPrimaryContainer
import com.example.ui.theme.ProfessionalTextSecondary

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            val travelViewModel: TravelWakeViewModel = viewModel()
            val weatherTheme by travelViewModel.weatherTheme.collectAsState()
            val themeMode by travelViewModel.themeMode.collectAsState()
            MyApplicationTheme(weatherPalette = weatherTheme, themeMode = themeMode) {
                TravelWakeApp(travelViewModel)
            }
        }
    }
}

object Routes {
    const val HOME = "home"
    const val SEARCH = "search"
    const val SETUP = "setup"
    const val ACTIVE = "active"
    const val ALARM = "alarm"
    const val BELONGINGS = "belongings"
    const val HISTORY = "history"
    const val SAVED_PLACES = "saved_places"
    const val SETTINGS = "settings"
    const val SECURITY_LAB = "security_lab"
    const val WEAR_OS = "wear_os"
}

@Composable
fun TravelWakeApp(viewModel: TravelWakeViewModel = viewModel()) {
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route
    var showVoiceDialog by remember { mutableStateOf(false) }

    // Request necessary runtime permissions
    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        // Permissions handled
    }

    LaunchedEffect(Unit) {
        val perms = mutableListOf(
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION,
            Manifest.permission.RECORD_AUDIO
        )
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            perms.add(Manifest.permission.POST_NOTIFICATIONS)
        }
        permissionLauncher.launch(perms.toTypedArray())
    }

    // Top-level navigation items for the "Professional Polish" bottom navigation bar
    val showBottomBar = currentRoute in listOf(Routes.HOME, Routes.HISTORY, Routes.BELONGINGS, Routes.SETTINGS)

    val weatherTheme = LocalWeatherThemePalette.current

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        containerColor = weatherTheme.backgroundColor,
        floatingActionButton = {
            Column(
                horizontalAlignment = Alignment.End,
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // Hands-Free Voice Assistant FAB on Home, Active, Alarm, and Settings screens
                if (currentRoute in listOf(Routes.HOME, Routes.ACTIVE, Routes.ALARM, Routes.SETTINGS)) {
                    FloatingActionButton(
                        onClick = { showVoiceDialog = true },
                        containerColor = weatherTheme.surfaceVariantColor,
                        contentColor = weatherTheme.primaryColor,
                        shape = RoundedCornerShape(16.dp),
                        elevation = FloatingActionButtonDefaults.elevation(defaultElevation = 4.dp),
                        modifier = Modifier.testTag("fab_voice_command")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Mic,
                            contentDescription = "Hands-free Voice Commands",
                            modifier = Modifier.size(24.dp),
                            tint = weatherTheme.primaryColor
                        )
                    }
                }

                if (currentRoute == Routes.HOME) {
                    // FAB matching Design HTML (bg-[#D3E4FF] text-[#001D36] h-14 w-14 rounded-2xl shadow-xl)
                    FloatingActionButton(
                        onClick = { navController.navigate(Routes.SEARCH) },
                        containerColor = weatherTheme.primaryContainerColor,
                        contentColor = weatherTheme.primaryColor,
                        shape = RoundedCornerShape(18.dp),
                        elevation = FloatingActionButtonDefaults.elevation(defaultElevation = 6.dp),
                        modifier = Modifier.testTag("fab_add_destination")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = "Search Destination",
                            modifier = Modifier.size(30.dp),
                            tint = weatherTheme.primaryColor
                        )
                    }
                }
            }
        },
        bottomBar = {
            if (showBottomBar) {
                ProfessionalBottomNav(
                    currentRoute = currentRoute,
                    onNavigate = { route ->
                        if (currentRoute != route) {
                            navController.navigate(route) {
                                popUpTo(Routes.HOME) { saveState = true }
                                launchSingleTop = true
                                restoreState = true
                            }
                        }
                    }
                )
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = Routes.HOME,
            modifier = Modifier.padding(innerPadding)
        ) {
            composable(Routes.HOME) {
                HomeScreen(
                    viewModel = viewModel,
                    onNavigateToSearch = { navController.navigate(Routes.SEARCH) },
                    onNavigateToSetup = { navController.navigate(Routes.SETUP) },
                    onNavigateToActive = { navController.navigate(Routes.ACTIVE) },
                    onNavigateToSettings = { navController.navigate(Routes.SETTINGS) },
                    onNavigateToHistory = { navController.navigate(Routes.HISTORY) },
                    onNavigateToBelongings = { navController.navigate(Routes.BELONGINGS) }
                )
            }

            composable(Routes.SEARCH) {
                SearchDestinationScreen(
                    viewModel = viewModel,
                    onDestinationSelected = { navController.navigate(Routes.SETUP) },
                    onBack = { navController.popBackStack() }
                )
            }

            composable(Routes.SETUP) {
                JourneySetupScreen(
                    viewModel = viewModel,
                    onStartJourney = {
                        navController.navigate(Routes.ACTIVE) {
                            popUpTo(Routes.HOME)
                        }
                    },
                    onBack = { navController.popBackStack() }
                )
            }

            composable(Routes.ACTIVE) {
                ActiveJourneyScreen(
                    viewModel = viewModel,
                    onNavigateToAlarm = { navController.navigate(Routes.ALARM) },
                    onBack = { navController.navigate(Routes.HOME) },
                    onEndJourney = {
                        navController.navigate(Routes.HOME) {
                            popUpTo(Routes.HOME) { inclusive = true }
                        }
                    }
                )
            }

            composable(Routes.ALARM) {
                MainAlarmScreen(
                    viewModel = viewModel,
                    onAwakeConfirmed = {
                        navController.navigate(Routes.ACTIVE) {
                            popUpTo(Routes.ACTIVE) { inclusive = true }
                        }
                    }
                )
            }

            composable(Routes.BELONGINGS) {
                BelongingsScreen(
                    viewModel = viewModel,
                    onBack = { navController.popBackStack() }
                )
            }

            composable(Routes.HISTORY) {
                HistoryScreen(
                    viewModel = viewModel,
                    onBack = { navController.popBackStack() }
                )
            }

            composable(Routes.SAVED_PLACES) {
                SavedPlacesScreen(
                    viewModel = viewModel,
                    onPlaceSelected = { navController.navigate(Routes.SETUP) },
                    onBack = { navController.popBackStack() }
                )
            }

            composable(Routes.SETTINGS) {
                SettingsScreen(
                    viewModel = viewModel,
                    onNavigateToHistory = { navController.navigate(Routes.HISTORY) },
                    onNavigateToSavedPlaces = { navController.navigate(Routes.SAVED_PLACES) },
                    onNavigateToBelongings = { navController.navigate(Routes.BELONGINGS) },
                    onNavigateToSecurityLab = { navController.navigate(Routes.SECURITY_LAB) },
                    onNavigateToWearOSPreview = { navController.navigate(Routes.WEAR_OS) },
                    onBack = { navController.popBackStack() }
                )
            }

            composable(Routes.SECURITY_LAB) {
                SecurityLabScreen(
                    viewModel = viewModel,
                    onBack = { navController.popBackStack() }
                )
            }

            composable(Routes.WEAR_OS) {
                WearOSPreviewScreen(
                    viewModel = viewModel,
                    onBack = { navController.popBackStack() }
                )
            }
        }
    }

    if (showVoiceDialog) {
        VoiceCommandDialog(
            viewModel = viewModel,
            onDismiss = { showVoiceDialog = false },
            onActionExecuted = { action ->
                if (action is VoiceCommand.StartJourney) {
                    navController.navigate(Routes.ACTIVE) {
                        popUpTo(Routes.HOME)
                    }
                }
            }
        )
    }
}

/**
 * Bottom Navigation Bar matching the Design HTML:
 * (h-20 bg-[#F3F4F9] border-t border-[#E1E2EC] flex justify-around items-center px-4 pb-2)
 * Active: bg-[#D3E4FF] px-5 py-1 rounded-full text-[#001D36]
 * Inactive: text-[#44474E]
 */
@Composable
fun ProfessionalBottomNav(
    currentRoute: String?,
    onNavigate: (String) -> Unit
) {
    val weatherTheme = LocalWeatherThemePalette.current

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .navigationBarsPadding(),
        color = weatherTheme.surfaceColor,
        border = BorderStroke(1.dp, weatherTheme.borderColor)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(72.dp)
                .padding(horizontal = 8.dp, vertical = 6.dp),
            horizontalArrangement = Arrangement.SpaceAround,
            verticalAlignment = Alignment.CenterVertically
        ) {
            BottomNavItem(
                label = "Home",
                icon = Icons.Default.Home,
                isSelected = currentRoute == Routes.HOME,
                onClick = { onNavigate(Routes.HOME) }
            )
            BottomNavItem(
                label = "Trips",
                icon = Icons.Default.History,
                isSelected = currentRoute == Routes.HISTORY,
                onClick = { onNavigate(Routes.HISTORY) }
            )
            BottomNavItem(
                label = "Belongings",
                icon = Icons.Default.Checklist,
                isSelected = currentRoute == Routes.BELONGINGS,
                onClick = { onNavigate(Routes.BELONGINGS) }
            )
            BottomNavItem(
                label = "Settings",
                icon = Icons.Default.Settings,
                isSelected = currentRoute == Routes.SETTINGS,
                onClick = { onNavigate(Routes.SETTINGS) }
            )
        }
    }
}

@Composable
private fun BottomNavItem(
    label: String,
    icon: ImageVector,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    val weatherTheme = LocalWeatherThemePalette.current

    Column(
        modifier = Modifier
            .clip(RoundedCornerShape(16.dp))
            .clickable { onClick() }
            .padding(horizontal = 8.dp, vertical = 4.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        if (isSelected) {
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(20.dp))
                    .background(weatherTheme.primaryContainerColor)
                    .padding(horizontal = 18.dp, vertical = 4.dp),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = label,
                    tint = weatherTheme.primaryColor,
                    modifier = Modifier.size(22.dp)
                )
            }
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                color = weatherTheme.primaryColor,
                fontWeight = FontWeight.Bold
            )
        } else {
            Box(
                modifier = Modifier.padding(horizontal = 18.dp, vertical = 4.dp),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = label,
                    tint = ProfessionalTextSecondary,
                    modifier = Modifier.size(22.dp)
                )
            }
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                color = ProfessionalTextSecondary,
                fontWeight = FontWeight.Normal
            )
        }
    }
}
