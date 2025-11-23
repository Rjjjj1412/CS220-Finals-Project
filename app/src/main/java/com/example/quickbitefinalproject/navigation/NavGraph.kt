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
import com.example.quickbitefinalproject.ui.kiosk.MenuScreen
import com.example.quickbitefinalproject.ui.kiosk.CartScreen

@Composable
fun AppNavGraph(navController: NavHostController) {

    NavHost(
        navController = navController,
        startDestination = "splash"
    ) {

        // -----------------------
        // SPLASH SCREEN
        // -----------------------
        composable("splash") {
            SplashScreen(navController)
        }

        // -----------------------
        // ADMIN LOGIN
        // -----------------------
        composable("admin_login") {
            LoginAdminScreen(navController)
        }

        // -----------------------
        // ADMIN PANEL (HOME)
        // -----------------------
        composable("admin_panel") {
            AdminPanelScreen(navController)
        }

        // -----------------------
        // MENU MANAGEMENT (ADMIN)
        // -----------------------
        composable("admin_menu_management") {
            AdminMenuManagementScreen(navController)
        }

        // -----------------------
        // EDIT ITEM PAGE (ADMIN)
        // -----------------------
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

        // -----------------------
        // ADD ITEM PAGE (ADMIN)
        // -----------------------
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

        // -----------------------
        // KIOSK MENU (CUSTOMER)
        // example route: "kiosk_menu/burgers"
        // -----------------------
        composable(
            route = "kiosk_menu/{categoryId}"
        ) { backStackEntry ->
            val categoryId = backStackEntry.arguments?.getString("categoryId") ?: ""
            MenuScreen(
                navController = navController,
                categoryId = categoryId
            )
        }

        // -----------------------
        // KIOSK CART (CUSTOMER)
        // route: "kiosk_cart"
        // -----------------------
        composable("kiosk_cart") {
            CartScreen(navController = navController)
        }
    }
}
