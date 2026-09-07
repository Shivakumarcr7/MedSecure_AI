package com.example.ui.navigation

import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AdminPanelSettings
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.HealthAndSafety
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.QrCodeScanner
import androidx.compose.material.icons.outlined.AdminPanelSettings
import androidx.compose.material.icons.outlined.AutoAwesome
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.QrCodeScanner
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.ui.MedSecureViewModel
import com.example.ui.screens.AdminDashboardScreen
import com.example.ui.screens.AdminMedicinesScreen
import com.example.ui.screens.AdminRegisterScreen
import com.example.ui.screens.AdminVerificationsScreen
import com.example.ui.screens.AiAssistantScreen
import com.example.ui.screens.LandingScreen
import com.example.ui.screens.LoginScreen
import com.example.ui.screens.MedicineDetailScreen
import com.example.ui.screens.ScanScreen
import com.example.ui.screens.VerifyScreen

sealed class Screen(val route: String, val title: String, val selectedIcon: ImageVector, val unselectedIcon: ImageVector) {
    data object Landing : Screen("landing", "Home", Icons.Filled.Home, Icons.Outlined.Home)
    data object Scan : Screen("scan", "Scan", Icons.Filled.QrCodeScanner, Icons.Outlined.QrCodeScanner)
    data object Verify : Screen("verify/{medicineId}", "Verify", Icons.Filled.HealthAndSafety, Icons.Filled.HealthAndSafety) {
        fun createRoute(id: String) = "verify/$id"
    }
    data object MedicineDetail : Screen("medicine/{medicineId}", "Medicine", Icons.Filled.HealthAndSafety, Icons.Filled.HealthAndSafety) {
        fun createRoute(id: String) = "medicine/$id"
    }
    data object Assistant : Screen("assistant", "AI Assistant", Icons.Filled.AutoAwesome, Icons.Outlined.AutoAwesome)
    data object Login : Screen("login", "Login", Icons.Filled.AdminPanelSettings, Icons.Outlined.AdminPanelSettings)
    data object AdminDashboard : Screen("admin", "Admin", Icons.Filled.AdminPanelSettings, Icons.Outlined.AdminPanelSettings)
    data object AdminRegister : Screen("admin/register", "Register", Icons.Filled.AdminPanelSettings, Icons.Outlined.AdminPanelSettings)
    data object AdminMedicines : Screen("admin/medicines", "Inventory", Icons.Filled.AdminPanelSettings, Icons.Outlined.AdminPanelSettings)
    data object AdminVerifications : Screen("admin/verifications", "Audit Logs", Icons.Filled.AdminPanelSettings, Icons.Outlined.AdminPanelSettings)
}

val bottomNavItems = listOf(
    Screen.Landing,
    Screen.Scan,
    Screen.Assistant,
    Screen.AdminDashboard
)

@Composable
fun MedSecureApp(
    viewModel: MedSecureViewModel = viewModel()
) {
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    // ViewModel State
    val allMedicines by viewModel.allMedicines.collectAsState()
    val allLogs by viewModel.allLogs.collectAsState()
    val recentLogs by viewModel.recentLogs.collectAsState()
    val totalMedicines by viewModel.totalMedicinesCount.collectAsState()
    val totalVerifications by viewModel.totalVerificationsCount.collectAsState()
    val successfulVerifications by viewModel.successfulVerificationsCount.collectAsState()
    val failedVerifications by viewModel.failedVerificationsCount.collectAsState()
    val unknownVerifications by viewModel.unknownVerificationsCount.collectAsState()

    val isAdminLoggedIn by viewModel.isAdminLoggedIn.collectAsState()
    val adminEmail by viewModel.adminEmail.collectAsState()
    val verificationResult by viewModel.verificationResult.collectAsState()
    val isVerifying by viewModel.isVerifying.collectAsState()

    val selectedMedicineForAi by viewModel.selectedMedicine.collectAsState()
    val chatMessages by viewModel.chatMessages.collectAsState()
    val isAiThinking by viewModel.isAiThinking.collectAsState()
    val isRegistering by viewModel.isRegistering.collectAsState()

    Scaffold(
        bottomBar = {
            // Show bottom navigation bar on primary screens
            NavigationBar(
                containerColor = MaterialTheme.colorScheme.surface,
                modifier = Modifier.testTag("bottom_nav_bar")
            ) {
                bottomNavItems.forEach { item ->
                    val selected = when (item) {
                        is Screen.AdminDashboard -> currentRoute?.startsWith("admin") == true || currentRoute == Screen.Login.route
                        is Screen.Scan -> currentRoute == Screen.Scan.route
                        is Screen.Assistant -> currentRoute == Screen.Assistant.route
                        is Screen.Landing -> currentRoute == Screen.Landing.route
                        else -> currentRoute == item.route
                    }

                    NavigationBarItem(
                        selected = selected,
                        onClick = {
                            if (item is Screen.AdminDashboard && !isAdminLoggedIn) {
                                navController.navigate(Screen.Login.route) {
                                    popUpTo(navController.graph.findStartDestination().id) {
                                        saveState = true
                                    }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            } else {
                                navController.navigate(item.route) {
                                    popUpTo(navController.graph.findStartDestination().id) {
                                        saveState = true
                                    }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            }
                        },
                        icon = {
                            Icon(
                                imageVector = if (selected) item.selectedIcon else item.unselectedIcon,
                                contentDescription = item.title
                            )
                        },
                        label = { Text(item.title) },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = MaterialTheme.colorScheme.primary,
                            selectedTextColor = MaterialTheme.colorScheme.primary,
                            indicatorColor = MaterialTheme.colorScheme.primaryContainer
                        ),
                        modifier = Modifier.testTag("nav_item_${item.title.lowercase().replace(" ", "_")}")
                    )
                }
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = Screen.Landing.route,
            modifier = Modifier.padding(innerPadding)
        ) {
            composable(Screen.Landing.route) {
                LandingScreen(
                    onNavigateToScan = { navController.navigate(Screen.Scan.route) },
                    onNavigateToVerify = { id -> navController.navigate(Screen.Verify.createRoute(id)) },
                    onNavigateToLogin = { navController.navigate(Screen.Login.route) },
                    onNavigateToAdmin = { navController.navigate(Screen.AdminDashboard.route) },
                    isAdminLoggedIn = isAdminLoggedIn
                )
            }

            composable(Screen.Scan.route) {
                ScanScreen(
                    onNavigateBack = { navController.popBackStack() },
                    onNavigateToVerify = { id -> navController.navigate(Screen.Verify.createRoute(id)) }
                )
            }

            composable(
                route = Screen.Verify.route,
                arguments = listOf(navArgument("medicineId") { type = NavType.StringType })
            ) { backStackEntry ->
                val medId = backStackEntry.arguments?.getString("medicineId") ?: "MED-IND-2026-000001"
                VerifyScreen(
                    medicineId = medId,
                    verificationResult = verificationResult,
                    isVerifying = isVerifying,
                    onReVerify = { id -> viewModel.performVerification(id) },
                    onToggleTamper = { id, enabled -> viewModel.toggleTamperDemo(id, enabled) },
                    onNavigateBack = { navController.popBackStack() },
                    onNavigateToMedicineDetail = { id -> navController.navigate(Screen.MedicineDetail.createRoute(id)) },
                    onNavigateToAiAssistant = { med ->
                        viewModel.selectMedicineForAi(med)
                        navController.navigate(Screen.Assistant.route)
                    }
                )
            }

            composable(
                route = Screen.MedicineDetail.route,
                arguments = listOf(navArgument("medicineId") { type = NavType.StringType })
            ) { backStackEntry ->
                val medId = backStackEntry.arguments?.getString("medicineId") ?: ""
                val currentMedicine = allMedicines.find { it.medicineId.equals(medId, ignoreCase = true) }

                MedicineDetailScreen(
                    medicine = currentMedicine,
                    onNavigateBack = { navController.popBackStack() },
                    onNavigateToVerify = { id -> navController.navigate(Screen.Verify.createRoute(id)) },
                    onNavigateToAiAssistant = { med ->
                        viewModel.selectMedicineForAi(med)
                        navController.navigate(Screen.Assistant.route)
                    }
                )
            }

            composable(Screen.Assistant.route) {
                AiAssistantScreen(
                    selectedMedicine = selectedMedicineForAi,
                    allMedicines = allMedicines,
                    chatMessages = chatMessages,
                    isAiThinking = isAiThinking,
                    onSelectMedicine = { med -> viewModel.selectMedicineForAi(med) },
                    onSendMessage = { query -> viewModel.sendAiQuestion(query) },
                    onNavigateBack = { navController.popBackStack() }
                )
            }

            composable(Screen.Login.route) {
                LoginScreen(
                    onLoginSuccess = {
                        navController.navigate(Screen.AdminDashboard.route) {
                            popUpTo(Screen.Login.route) { inclusive = true }
                        }
                    },
                    onNavigateBack = { navController.popBackStack() },
                    onLoginAttempt = { email, pass -> viewModel.loginAdmin(email, pass) }
                )
            }

            composable(Screen.AdminDashboard.route) {
                AdminDashboardScreen(
                    adminEmail = adminEmail,
                    totalMedicines = totalMedicines,
                    totalVerifications = totalVerifications,
                    successfulVerifications = successfulVerifications,
                    failedVerifications = failedVerifications,
                    unknownVerifications = unknownVerifications,
                    recentLogs = recentLogs,
                    onNavigateToRegister = { navController.navigate(Screen.AdminRegister.route) },
                    onNavigateToMedicines = { navController.navigate(Screen.AdminMedicines.route) },
                    onNavigateToLogs = { navController.navigate(Screen.AdminVerifications.route) },
                    onNavigateToVerify = { id -> navController.navigate(Screen.Verify.createRoute(id)) },
                    onLogout = {
                        viewModel.logoutAdmin()
                        navController.navigate(Screen.Landing.route) {
                            popUpTo(Screen.Landing.route) { inclusive = true }
                        }
                    },
                    onNavigateHome = { navController.navigate(Screen.Landing.route) }
                )
            }

            composable(Screen.AdminRegister.route) {
                AdminRegisterScreen(
                    isRegistering = isRegistering,
                    onRegisterSubmit = { name, comp, dose, uses, sideEffects, mfg, batch, mDate, eDate, rx, onSuccess ->
                        viewModel.registerNewMedicine(
                            name, comp, dose, uses, sideEffects, mfg, batch, mDate, eDate, rx, onSuccess
                        )
                    },
                    onNavigateBack = { navController.popBackStack() },
                    onNavigateToMedicineDetail = { id -> navController.navigate(Screen.MedicineDetail.createRoute(id)) }
                )
            }

            composable(Screen.AdminMedicines.route) {
                AdminMedicinesScreen(
                    medicines = allMedicines,
                    onNavigateBack = { navController.popBackStack() },
                    onNavigateToRegister = { navController.navigate(Screen.AdminRegister.route) },
                    onNavigateToDetail = { id -> navController.navigate(Screen.MedicineDetail.createRoute(id)) },
                    onNavigateToVerify = { id -> navController.navigate(Screen.Verify.createRoute(id)) }
                )
            }

            composable(Screen.AdminVerifications.route) {
                AdminVerificationsScreen(
                    logs = allLogs,
                    onNavigateBack = { navController.popBackStack() },
                    onNavigateToVerify = { id -> navController.navigate(Screen.Verify.createRoute(id)) }
                )
            }
        }
    }
}
