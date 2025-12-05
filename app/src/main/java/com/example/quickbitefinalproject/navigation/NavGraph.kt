package com.example.quickbitefinalproject.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.example.quickbitefinalproject.ui.splash.SplashScreen
import com.example.quickbitefinalproject.ui.admin.LoginAdminScreen
import com.example.quickbitefinalproject.ui.admin.AdminPanelScreen
import com.example.quickbitefinalproject.ui.admin.AdminMenuManagementScreen
import com.example.quickbitefinalproject.ui.admin.EditItemPage
import com.example.quickbitefinalproject.ui.admin.AddItemPage
import com.example.quickbitefinalproject.ui.admin.AdminEditProfileScreen
import com.example.quickbitefinalproject.ui.kiosk.MainMenuScreen
import com.example.quickbitefinalproject.ui.kiosk.CartScreen
import com.example.quickbitefinalproject.ui.kiosk.OrderDetailsScreen
import com.example.quickbitefinalproject.ui.kiosk.RegisterScreen
import com.example.quickbitefinalproject.ui.kiosk.UserAuthScreen
import com.example.quickbitefinalproject.ui.kiosk.UserProfileScreen

@Composable
fun AppNavGraph(navController: NavHostController) {

    NavHost(
        navController = navController,
        startDestination = "splash"
    ) {

        // SPLASH
        composable("splash") {
            SplashScreen(navController)
        }

        // USER AUTH (login page for customers)
        composable("user_auth") {
            UserAuthScreen(navController)
        }

        // --- REGISTRATION ROUTES ---
        composable("register_screen") {
            RegisterScreen(
                navController = navController,
                googleEmail = "",
                googlePassword = ""
            )
        }
        composable(
            route = "register_screen/{email}/{password}",
            arguments = listOf(
                navArgument("email") { type = NavType.StringType },
                navArgument("password") { type = NavType.StringType }
            )
        ) { backStackEntry ->
            val emailArg = backStackEntry.arguments?.getString("email") ?: ""
            val passwordArg = backStackEntry.arguments?.getString("password") ?: ""
            RegisterScreen(
                navController = navController,
                googleEmail = emailArg,
                googlePassword = passwordArg
            )
        }

        // MAIN MENU (customer landing page)
        composable("main_menu") {
            MainMenuScreen(navController)
        }

        // CART
        composable("kiosk_cart") {
            CartScreen(navController)
        }

        // ORDER DETAILS PAGE (for listing all orders)
        composable("order_details") {
            OrderDetailsScreen(navController, orderId = null)
        }

        // ORDER DETAILS PAGE (for tracking a specific order)
        composable(
            route = "order_details/{orderId}",
            arguments = listOf(
                navArgument("orderId") { type = NavType.StringType }
            )
        ) { backStackEntry ->
            val orderId = backStackEntry.arguments?.getString("orderId")
            OrderDetailsScreen(navController = navController, orderId = orderId)
        }

        // USER PROFILE
        composable("user_profile") {
            UserProfileScreen(navController)
        }

        // --- ADMIN ROUTES ---
        composable("admin_login") {
            LoginAdminScreen(navController)
        }
        composable("admin_panel") {
            AdminPanelScreen(navController)
        }
        composable("admin_menu_management") {
            AdminMenuManagementScreen(navController)
        }
        composable("admin_edit_profile") {
            AdminEditProfileScreen(navController)
        }
        composable(
            route = "edit_item/{itemId}?tabIndex={tabIndex}",
        ) { backStackEntry ->
            val itemId = backStackEntry.arguments?.getString("itemId")
            val tabIndex = backStackEntry.arguments?.getString("tabIndex")?.toIntOrNull() ?: 0
            EditItemPage(navController, itemId, tabIndex)
        }
        composable(
            route = "add_item_page?tabIndex={tabIndex}"
        ) { backStackEntry ->
            val tabIndex = backStackEntry.arguments?.getString("tabIndex")?.toIntOrNull() ?: 0
            AddItemPage(navController, tabIndex)
        }
    }
}
