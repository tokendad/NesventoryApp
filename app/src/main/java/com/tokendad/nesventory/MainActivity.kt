package com.tokendad.nesventory

import android.os.Bundle
import android.view.WindowManager
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.tokendad.nesventory.ui.additem.AddItemScreen
import com.tokendad.nesventory.ui.addlocation.AddLocationScreen
import com.tokendad.nesventory.ui.edititem.EditItemScreen
import com.tokendad.nesventory.ui.editlocation.EditLocationScreen
import com.tokendad.nesventory.ui.itemdetail.ItemDetailScreen
import com.tokendad.nesventory.ui.locationdetail.LocationDetailScreen
import com.tokendad.nesventory.ui.main.MainScreen
import com.tokendad.nesventory.ui.login.LoginScreen
import com.tokendad.nesventory.ui.profile.ProfileScreen
import com.tokendad.nesventory.ui.server.GDriveBackupScreen
import com.tokendad.nesventory.ui.theme.NesventoryTheme
import com.tokendad.nesventory.ui.dashboard.DashboardViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        window.setFlags(
            WindowManager.LayoutParams.FLAG_SECURE,
            WindowManager.LayoutParams.FLAG_SECURE
        )
        enableEdgeToEdge()
        setContent {
            val dashboardViewModel: DashboardViewModel = hiltViewModel()
            val themeSetting = dashboardViewModel.theme
            
            val useDarkTheme = when (themeSetting) {
                "light" -> false
                "dark" -> true
                else -> isSystemInDarkTheme()
            }

            NesventoryTheme(darkTheme = useDarkTheme) {
                val navController = rememberNavController()

                // Explicitly typing the ViewModel and State to prevent inference errors
                val viewModel: MainViewModel = hiltViewModel<MainViewModel>()
                val uiStateState = viewModel.uiState.collectAsState(initial = MainUiState())
                val uiState = uiStateState.value
                val snackbarHostState = remember { SnackbarHostState() }

                // Handle Deep Link
                LaunchedEffect(Unit) {
                    val data = intent?.data
                    if (data != null) {
                        if (data.scheme == "nesventory" && data.host == "auth") {
                            val returnedState = data.getQueryParameter("state")
                            val token = data.getQueryParameter("token")
                            if (viewModel.validateOidcState(returnedState) && !token.isNullOrBlank()) {
                                viewModel.handleOidcToken(token)
                            }
                        } else if (data.scheme == "https" && data.pathSegments.size >= 3) {
                            // Generic handler for any configured server domain
                            val pathSegments = data.pathSegments // [api, items, UUID]
                            if (pathSegments[0] == "api") {
                                val type = pathSegments[1]
                                val id = pathSegments[2]
                                if (type == "items") {
                                    viewModel.setPendingRoute(Routes.itemDetails(id))
                                } else if (type == "locations") {
                                    viewModel.setPendingRoute(Routes.locationDetails(id))
                                }
                            }
                        }
                    }
                }

                // Auth State Observer
                val pendingRouteState = viewModel.pendingRoute.collectAsState()
                val pendingRoute = pendingRouteState.value

                LaunchedEffect(uiState.isLoggedIn, pendingRoute) {
                    if (uiState.isLoggedIn) {
                        // If we are just logging in, navigate to dashboard first
                        if (navController.currentDestination?.route == Routes.LOGIN) {
                            navController.navigate(Routes.DASHBOARD) {
                                popUpTo(Routes.LOGIN) { inclusive = true }
                            }
                        }
                        
                        // Handle pending deep link navigation
                        if (pendingRoute != null) {
                            navController.navigate(pendingRoute)
                            viewModel.clearPendingRoute()
                        }
                    }
                }

                LaunchedEffect(viewModel) {
                    viewModel.forbiddenEvents.collect {
                        snackbarHostState.showSnackbar("You don't have permission to perform this action.")
                    }
                }

                Scaffold(
                    modifier = Modifier.fillMaxSize(),
                    snackbarHost = { SnackbarHost(hostState = snackbarHostState) }
                ) { innerPadding ->
                    Surface(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(innerPadding),
                        color = MaterialTheme.colorScheme.background
                    ) {
                        NavHost(
                            navController = navController,
                            startDestination = Routes.LOGIN
                        ) {
                        // 1. Login Screen
                        composable(Routes.LOGIN) {
                            LoginScreen(
                                onLoginSuccess = {
                                    navController.navigate(Routes.DASHBOARD) {
                                        popUpTo(Routes.LOGIN) { inclusive = true }
                                    }
                                },
                                onServerSettingsClick = {
                                    navController.navigate(Routes.SERVER_SETTINGS)
                                }
                            )
                        }

                        // Server Settings (Accessible from Login)
                        composable(Routes.SERVER_SETTINGS) {
                            com.tokendad.nesventory.ui.server.ServerScreen(
                                remoteUrl = dashboardViewModel.remoteUrl,
                                onRemoteUrlChange = { dashboardViewModel.onRemoteUrlChange(it) },
                                localUrl = dashboardViewModel.localUrl,
                                onLocalUrlChange = { dashboardViewModel.onLocalUrlChange(it) },
                                localSsid = dashboardViewModel.localSsid,
                                onLocalSsidChange = { dashboardViewModel.onLocalSsidChange(it) },
                                availableSsids = dashboardViewModel.availableSsids,
                                prioritizeLocal = dashboardViewModel.prioritizeLocal,
                                onPrioritizeLocalChange = { dashboardViewModel.onPrioritizeLocalChange(it) },
                                remoteStatus = dashboardViewModel.remoteStatus,
                                localStatus = dashboardViewModel.localStatus,
                                aiStatus = dashboardViewModel.aiStatus,
                                aiStatusMessage = dashboardViewModel.aiStatusMessage,
                                theme = dashboardViewModel.theme,
                                onThemeChange = { dashboardViewModel.onThemeChange(it) },
                                onTestConnection = { dashboardViewModel.testAndSaveConnection() },
                                onTestAIConnection = { dashboardViewModel.testAIConnection() },
                                showPermissionRationale = dashboardViewModel.showPermissionRationale,
                                onDismissPermissionRationale = { dashboardViewModel.dismissPermissionRationale() },
                                onRequestSsidScan = { dashboardViewModel.requestSsidScan() },
                                onPrinterSettingsClick = { navController.navigate(Routes.PRINTER_SETTINGS) },
                                onGDriveBackupClick = { navController.navigate(Routes.GDRIVE_BACKUP) },
                                isLoggedIn = uiState.isLoggedIn,
                                onExit = { navController.popBackStack() }
                            )
                        }

                        // Printer Settings
                        composable(Routes.PRINTER_SETTINGS) {
                            com.tokendad.nesventory.ui.printer.PrinterSettingsScreen(
                                onBackClick = { navController.popBackStack() }
                            )
                        }

                        // 2. Main Screen (Dashboard with Bottom Nav)
                        composable(Routes.DASHBOARD) {
                            MainScreen(
                                onItemClick = { itemId ->
                                    navController.navigate(Routes.itemDetails(itemId.toString()))
                                },
                                onLocationClick = { locationId ->
                                    navController.navigate(Routes.locationDetails(locationId.toString()))
                                },
                                onAddItemClick = {
                                    navController.navigate(Routes.ADD_ITEM)
                                },
                                onEditItemClick = { itemId ->
                                    navController.navigate(Routes.editItem(itemId.toString()))
                                },
                                onAddLocationClick = {
                                    navController.navigate(Routes.ADD_LOCATION)
                                },
                                onEditLocationClick = { locationId ->
                                    navController.navigate(Routes.editLocation(locationId.toString()))
                                },
                                onProfileClick = {
                                    navController.navigate(Routes.PROFILE)
                                },
                                onServerSettingsClick = {
                                    navController.navigate(Routes.SERVER_SETTINGS)
                                },
                                onPrinterSettingsClick = {
                                    navController.navigate(Routes.PRINTER_SETTINGS)
                                },
                                onExit = {
                                    viewModel.logout()
                                    navController.navigate(Routes.LOGIN) {
                                        popUpTo(0) { inclusive = true }
                                    }
                                }
                            )
                        }

                        // 3. Item Detail Screen
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

                        // 4. Location Detail Screen
                        composable(
                            route = Routes.LOCATION_DETAILS,
                            arguments = listOf(navArgument("locationId") { type = NavType.StringType })
                        ) {
                            LocationDetailScreen(
                                onBackClick = {
                                    navController.popBackStack()
                                }
                            )
                        }

                        // 5. Add Item Screen
                        composable(Routes.ADD_ITEM) {
                            AddItemScreen(
                                onBackClick = { navController.popBackStack() },
                                onItemCreated = {
                                    navController.popBackStack()
                                }
                            )
                        }

                        // 8. Edit Item Screen
                        composable(
                            route = Routes.EDIT_ITEM,
                            arguments = listOf(navArgument("itemId") { type = NavType.StringType })
                        ) {
                            EditItemScreen(
                                onBackClick = { navController.popBackStack() },
                                onItemUpdated = {
                                    navController.popBackStack()
                                }
                            )
                        }

                        // 6. Add Location Screen
                        composable(Routes.ADD_LOCATION) {
                            AddLocationScreen(
                                onBackClick = { navController.popBackStack() },
                                onLocationCreated = {
                                    navController.popBackStack()
                                }
                            )
                        }

                        // 7. Edit Location Screen
                        composable(
                            route = Routes.EDIT_LOCATION,
                            arguments = listOf(navArgument("locationId") { type = NavType.StringType })
                        ) {
                            EditLocationScreen(
                                onBackClick = { navController.popBackStack() },
                                onLocationUpdated = {
                                    navController.popBackStack()
                                }
                            )
                            }

                        composable(Routes.PROFILE) {
                            ProfileScreen(
                                onBackClick = { navController.popBackStack() }
                            )
                        }

                        composable(Routes.GDRIVE_BACKUP) {
                            GDriveBackupScreen(
                                onBackClick = { navController.popBackStack() }
                            )
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
    const val SERVER_SETTINGS = "server_settings"
    const val PRINTER_SETTINGS = "printer_settings"
    const val GDRIVE_BACKUP = "gdrive_backup"
    const val DASHBOARD = "dashboard"
    const val ADD_ITEM = "add_item"
    const val EDIT_ITEM = "edit_item/{itemId}"
    const val ADD_LOCATION = "add_location"
    const val EDIT_LOCATION = "edit_location/{locationId}"
    const val PROFILE = "profile"
    const val ITEM_DETAILS = "item_details/{itemId}"
    const val LOCATION_DETAILS = "location_details/{locationId}"

    fun itemDetails(itemId: String) = "item_details/$itemId"
    fun editItem(itemId: String) = "edit_item/$itemId"
    fun locationDetails(locationId: String) = "location_details/$locationId"
    fun editLocation(locationId: String) = "edit_location/$locationId"
}
