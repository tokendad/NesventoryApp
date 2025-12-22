package com.example.nesventorynew

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.nesventorynew.ui.dashboard.DashboardScreen
import com.example.nesventorynew.ui.items.ItemsScreen
import com.example.nesventorynew.ui.login.LoginScreen
import com.example.nesventorynew.ui.serversettings.ServerSettingsScreen
import com.example.nesventorynew.ui.theme.NesVentoryNewTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            NesVentoryNewTheme {
                val navController = rememberNavController()

                // Explicitly typing the ViewModel and State to prevent inference errors
                val viewModel: MainViewModel = hiltViewModel<MainViewModel>()
                val uiStateState = viewModel.uiState.collectAsState(initial = MainUiState())
                val uiState = uiStateState.value

                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    NavHost(
                        navController = navController,
                        startDestination = Routes.SERVER_SETTINGS
                    ) {
                        // 1. Server Settings Screen
                        composable(Routes.SERVER_SETTINGS) {
                            ServerSettingsScreen(
                                onSettingsSaved = {
                                    navController.navigate(Routes.LOGIN)
                                }
                            )
                        }

                        // 2. Login Screen
                        composable(Routes.LOGIN) {
                            LoginScreen(
                                onLoginSuccess = {
                                    // Navigate to Dashboard and clear backstack
                                    navController.navigate(Routes.DASHBOARD) {
                                        popUpTo(Routes.SERVER_SETTINGS) { inclusive = true }
                                    }
                                }
                            )
                        }

                        // 3. Dashboard Screen (Now with navigation to Items)
                        composable(Routes.DASHBOARD) {
                            DashboardScreen(
                                onNavigateToItems = {
                                    navController.navigate(Routes.ITEMS)
                                }
                            )
                        }

                        // 4. Items List Screen
                        composable(Routes.ITEMS) {
                            ItemsScreen()
                        }
                    }
                }

                // Initial redirect: If token exists, skip settings and login
                LaunchedEffect(uiState.isLoggedIn) {
                    if (uiState.isLoggedIn) {
                        // Only navigate if we are currently on a setup screen
                        val currentRoute = navController.currentBackStackEntry?.destination?.route
                        if (currentRoute == Routes.SERVER_SETTINGS || currentRoute == Routes.LOGIN) {
                            navController.navigate(Routes.DASHBOARD) {
                                popUpTo(Routes.SERVER_SETTINGS) { inclusive = true }
                            }
                        }
                    }
                }
            }
        }
    }
}

/**
 * Global Navigation Routes
 */
object Routes {
    const val SERVER_SETTINGS = "server_settings"
    const val LOGIN = "login"
    const val DASHBOARD = "dashboard"
    const val ITEMS = "items"
}