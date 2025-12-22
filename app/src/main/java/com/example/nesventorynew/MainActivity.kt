package com.example.nesventorynew

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.nesventorynew.ui.dashboard.DashboardScreen
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

                // Explicitly typing the ViewModel
                val viewModel: MainViewModel = hiltViewModel<MainViewModel>()

                // FIX: Manual assignment instead of 'by' delegate to resolve getValue error
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
                        composable(Routes.SERVER_SETTINGS) {
                            ServerSettingsScreen(
                                onSettingsSaved = {
                                    navController.navigate(Routes.LOGIN)
                                }
                            )
                        }

                        composable(Routes.LOGIN) {
                            LoginScreen(
                                onLoginSuccess = {
                                    navController.navigate(Routes.DASHBOARD) {
                                        popUpTo(Routes.SERVER_SETTINGS) { inclusive = true }
                                    }
                                }
                            )
                        }

                        composable(Routes.DASHBOARD) {
                            DashboardScreen()
                        }
                    }
                }

                LaunchedEffect(uiState.isLoggedIn) {
                    if (uiState.isLoggedIn) {
                        navController.navigate(Routes.DASHBOARD) {
                            popUpTo(Routes.SERVER_SETTINGS) { inclusive = true }
                        }
                    }
                }
            }
        }
    }
}

object Routes {
    const val SERVER_SETTINGS = "server_settings"
    const val LOGIN = "login"
    const val DASHBOARD = "dashboard"
}