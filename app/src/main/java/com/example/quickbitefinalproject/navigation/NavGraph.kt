package com.example.quickbitefinalproject.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.example.quickbitefinalproject.ui.splash.SplashScreen
import com.example.quickbitefinalproject.ui.admin.LoginAdminScreen
import com.example.quickbitefinalproject.ui.admin.AdminPanelScreen
import com.example.quickbitefinalproject.ui.admin.AdminMenuManagementScreen
import com.example.quickbitefinalproject.ui.admin.EditItemPage
import com.example.quickbitefinalproject.ui.admin.AddItemPage
import com.example.quickbitefinalproject.ui.kiosk.MainMenuScreen
import com.example.quickbitefinalproject.ui.kiosk.MenuScreen
import com.example.quickbitefinalproject.ui.kiosk.CartScreen
import com.example.quickbitefinalproject.ui.kiosk.CheckoutScreen
import com.example.quickbitefinalproject.ui.kiosk.OrderSuccessScreen
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

        // MAIN MENU (customer landing page)
        composable("main_menu") {
            MainMenuScreen(navController)
        }

        // KIOSK CATEGORY MENU (items under category)
        composable("kiosk_menu/{categoryId}") { backStackEntry ->
            val categoryId = backStackEntry.arguments?.getString("categoryId") ?: ""
            MenuScreen(navController = navController, categoryId = categoryId)
        }

        // CART
        composable("kiosk_cart") {
            CartScreen(navController)
        }

        // CHECKOUT
        composable("kiosk_checkout") {
            CheckoutScreen(navController)
        }

        // ORDER SUCCESS
        composable("order_success") {
            OrderSuccessScreen(navController)
        }

        // USER AUTH (login/signup page for customers)
        composable("user_auth") {
            UserAuthScreen(navController)
        }

        // USER PROFILE
        composable("user_profile") {
            UserProfileScreen(navController)
        }

        // ADMIN LOGIN
        composable("admin_login") {
            LoginAdminScreen(navController)
        }

        // ADMIN PANEL (HOME)
        composable("admin_panel") {
            AdminPanelScreen(navController)
        }

        // MENU MANAGEMENT
        composable("admin_menu_management") {
            AdminMenuManagementScreen(navController)
        }

        // EDIT ITEM PAGE
        composable(
            route = "edit_item/{itemId}?tabIndex={tabIndex}",
        ) { backStackEntry ->

            val itemId = backStackEntry.arguments?.getString("itemId")
            val tabIndex =
                backStackEntry.arguments?.getString("tabIndex")?.toIntOrNull() ?: 0

            EditItemPage(
                navController = navController,
                itemId = itemId,
                tabIndex = tabIndex
            )
        }

        // ADD ITEM PAGE
        composable(
            route = "add_item_page?tabIndex={tabIndex}"
        ) { backStackEntry ->

            val tabIndex =
                backStackEntry.arguments?.getString("tabIndex")?.toIntOrNull() ?: 0

            AddItemPage(
                navController = navController,
                tabIndex = tabIndex
            )
        }
    }
}
