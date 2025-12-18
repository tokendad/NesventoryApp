package com.nesventory.android

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.NavType
import androidx.navigation.navArgument
import com.nesventory.android.ui.additem.AddItemScreen
import com.nesventory.android.ui.dashboard.DashboardScreen
import com.nesventory.android.ui.inventory.InventoryScreen
import com.nesventory.android.ui.itemdetails.ItemDetailsScreen
import com.nesventory.android.ui.locations.LocationsScreen
import com.nesventory.android.ui.login.LoginScreen
import com.nesventory.android.ui.maintenance.MaintenanceScreen
import com.nesventory.android.ui.serversettings.ServerSettingsScreen
import com.nesventory.android.ui.settings.SystemSettingsScreen
import com.nesventory.android.ui.settings.UserSettingsScreen
import com.nesventory.android.ui.theme.NesVentoryTheme
import dagger.hilt.android.AndroidEntryPoint

/**
 * Main activity for the NesVentory Android app.
 */
@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            NesVentoryTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    NesVentoryApp()
                }
            }
        }
    }
}

/**
 * Navigation routes for the app.
 */
sealed class Screen(val route: String) {
    data object ServerSettings : Screen("server_settings")
    data object Login : Screen("login")
    data object Dashboard : Screen("dashboard")
    data object Inventory : Screen("inventory")
    data object Locations : Screen("locations")
    data object AddItem : Screen("add_item")
    data object Maintenance : Screen("maintenance")
    data object UserSettings : Screen("user_settings")
    data object SystemSettings : Screen("system_settings")
    data object ItemDetails : Screen("item_details/{itemId}") {
        fun createRoute(itemId: String) = "item_details/$itemId"
    }
}

/**
 * Main app composable with navigation.
 */
@Composable
fun NesVentoryApp() {
    val navController = rememberNavController()
    val mainViewModel: MainViewModel = hiltViewModel()
    val isLoggedIn by mainViewModel.isLoggedIn.collectAsState(initial = false)
    val isServerConfigured by mainViewModel.isServerConfigured.collectAsState(initial = false)

    // Determine start destination based on configuration and login state
    // In demo version, skip server settings configuration
    val startDestination = when {
        isLoggedIn -> Screen.Dashboard.route
        else -> Screen.Login.route
    }

    NavHost(
        navController = navController,
        startDestination = startDestination
    ) {
        composable(Screen.ServerSettings.route) {
            ServerSettingsScreen(
                onSettingsSaved = {
                    navController.navigate(Screen.Login.route) {
                        popUpTo(Screen.ServerSettings.route) { inclusive = true }
                    }
                }
            )
        }

        composable(Screen.Login.route) {
            LoginScreen(
                onLoginSuccess = {
                    navController.navigate(Screen.Dashboard.route) {
                        popUpTo(Screen.Login.route) { inclusive = true }
                    }
                }
            )
        }

        composable(Screen.Dashboard.route) {
            DashboardScreen(
                onLogout = {
                    navController.navigate(Screen.Login.route) {
                        popUpTo(Screen.Dashboard.route) { inclusive = true }
                    }
                },
                onNavigateToInventory = {
                    navController.navigate(Screen.Inventory.route)
                },
                onNavigateToLocations = {
                    navController.navigate(Screen.Locations.route)
                },
                onNavigateToAddItem = {
                    navController.navigate(Screen.AddItem.route)
                },
                onNavigateToUserSettings = {
                    navController.navigate(Screen.UserSettings.route)
                },
                onNavigateToMaintenance = {
                    navController.navigate(Screen.Maintenance.route)
                },
                onNavigateToSystemSettings = {
                    navController.navigate(Screen.SystemSettings.route)
                }
            )
        }

        composable(Screen.Inventory.route) {
            InventoryScreen(
                onNavigateBack = { navController.popBackStack() },
                onNavigateToAddItem = {
                    navController.navigate(Screen.AddItem.route)
                },
                onNavigateToItemDetails = { itemId ->
                    navController.navigate(Screen.ItemDetails.createRoute(itemId))
                },
                onAIScan = {
                    // AI scan placeholder - will be implemented with camera integration
                }
            )
        }

        composable(Screen.Locations.route) {
            LocationsScreen(
                onNavigateBack = { navController.popBackStack() },
                onAddLocation = {
                    // Add location functionality - will be implemented with POST /api/locations/
                }
            )
        }

        composable(Screen.AddItem.route) {
            AddItemScreen(
                onNavigateBack = { navController.popBackStack() }
            )
        }

        composable(
            route = Screen.ItemDetails.route,
            arguments = listOf(navArgument("itemId") { type = NavType.StringType })
        ) { backStackEntry ->
            val itemId = backStackEntry.arguments?.getString("itemId") ?: ""
            ItemDetailsScreen(
                itemId = itemId,
                onNavigateBack = { navController.popBackStack() }
            )
        }

        composable(Screen.Maintenance.route) {
            MaintenanceScreen(
                onNavigateBack = { navController.popBackStack() },
                onAddTask = {
                    // Add task functionality - will be implemented with POST /api/maintenance/
                }
            )
        }

        composable(Screen.UserSettings.route) {
            UserSettingsScreen(
                onNavigateBack = { navController.popBackStack() }
            )
        }

        composable(Screen.SystemSettings.route) {
            SystemSettingsScreen(
                onNavigateBack = { navController.popBackStack() }
            )
        }
    }
}
