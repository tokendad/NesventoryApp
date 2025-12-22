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
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.nesventorynew.ui.dashboard.DashboardScreen
import com.example.nesventorynew.ui.itemdetail.ItemDetailScreen
import com.example.nesventorynew.ui.items.ItemsScreen
import com.example.nesventorynew.ui.login.LoginScreen
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
                        startDestination = Routes.LOGIN
                    ) {
                        // 1. Login Screen (Now the start destination)
                        composable(Routes.LOGIN) {
                            LoginScreen(
                                onLoginSuccess = {
                                    // Navigate to Dashboard and clear backstack
                                    navController.navigate(Routes.DASHBOARD) {
                                        popUpTo(Routes.LOGIN) { inclusive = true }
                                    }
                                }
                            )
                        }

                        // 2. Dashboard Screen (Now with navigation to Items)
                        composable(Routes.DASHBOARD) {
                            DashboardScreen(
                                onNavigateToItems = {
                                    navController.navigate(Routes.ITEMS)
                                }
                            )
                        }

                        // 3. Items List Screen
                        composable(Routes.ITEMS) {
                            ItemsScreen(
                                onItemClick = { itemId ->
                                    navController.navigate(Routes.itemDetails(itemId.toString()))
                                }
                            )
                        }

                        // 4. Item Detail Screen
                        composable(
                            route = Routes.ITEM_DETAILS,
                            arguments = listOf(navArgument("itemId") { type = NavType.StringType })
                        ) {
                            ItemDetailScreen(
                                onBackClick = {
                                    navController.popBackStack()
                                }
                            )
                        }
                    }
                }

                // Initial redirect: If token exists, skip login
                LaunchedEffect(uiState.isLoggedIn) {
                    if (uiState.isLoggedIn) {
                        val currentRoute = navController.currentBackStackEntry?.destination?.route
                        if (currentRoute == Routes.LOGIN) {
                            navController.navigate(Routes.DASHBOARD) {
                                popUpTo(Routes.LOGIN) { inclusive = true }
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
    const val LOGIN = "login"
    const val DASHBOARD = "dashboard"
    const val ITEMS = "items"
    const val ITEM_DETAILS = "item_details/{itemId}"

    fun itemDetails(itemId: String) = "item_details/$itemId"
}