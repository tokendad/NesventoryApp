package com.tokendad.nesventorynew

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.tokendad.nesventorynew.ui.additem.AddItemScreen
import com.tokendad.nesventorynew.ui.addlocation.AddLocationScreen
import com.tokendad.nesventorynew.ui.edititem.EditItemScreen
import com.tokendad.nesventorynew.ui.editlocation.EditLocationScreen
import com.tokendad.nesventorynew.ui.itemdetail.ItemDetailScreen
import com.tokendad.nesventorynew.ui.locationdetail.LocationDetailScreen
import com.tokendad.nesventorynew.ui.main.MainScreen
import com.tokendad.nesventorynew.ui.login.LoginScreen
import com.tokendad.nesventorynew.ui.theme.NesVentoryNewTheme
import com.tokendad.nesventorynew.ui.dashboard.DashboardViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            val dashboardViewModel: DashboardViewModel = hiltViewModel()
            val themeSetting = dashboardViewModel.theme
            
            val useDarkTheme = when (themeSetting) {
                "light" -> false
                "dark" -> true
                else -> isSystemInDarkTheme()
            }

            NesVentoryNewTheme(darkTheme = useDarkTheme) {
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
                        // 1. Login Screen
                        composable(Routes.LOGIN) {
                            LoginScreen(
                                onLoginSuccess = {
                                    navController.navigate(Routes.DASHBOARD) {
                                        popUpTo(Routes.LOGIN) { inclusive = true }
                                    }
                                }
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
                                onExit = {
                                    finish()
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
    const val ADD_ITEM = "add_item"
    const val EDIT_ITEM = "edit_item/{itemId}"
    const val ADD_LOCATION = "add_location"
    const val EDIT_LOCATION = "edit_location/{locationId}"
    const val ITEM_DETAILS = "item_details/{itemId}"
    const val LOCATION_DETAILS = "location_details/{locationId}"

    fun itemDetails(itemId: String) = "item_details/$itemId"
    fun editItem(itemId: String) = "edit_item/$itemId"
    fun locationDetails(locationId: String) = "location_details/$locationId"
    fun editLocation(locationId: String) = "edit_location/$locationId"
}
