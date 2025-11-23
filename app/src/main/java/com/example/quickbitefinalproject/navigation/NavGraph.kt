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

@Composable
fun AppNavGraph(navController: NavHostController) {

    NavHost(
        navController = navController,
        startDestination = "splash"
    ) {

        // ---------- SPLASH ----------
        composable("splash") {
            SplashScreen(navController)
        }

        // ---------- CUSTOMER MAIN MENU ----------
        composable("main_menu") {
            MainMenuScreen(navController)
        }

        // ---------- KIOSK MENU BY CATEGORY ----------
        composable(
            route = "kiosk_menu/{categoryId}"
        ) { backStackEntry ->
            val categoryId = backStackEntry.arguments?.getString("categoryId") ?: ""
            MenuScreen(
                navController = navController,
                categoryId = categoryId
            )
        }

        // ---------- KIOSK CART ----------
        composable("kiosk_cart") {
            CartScreen(navController)
        }

        // ---------- ADMIN LOGIN ----------
        composable("admin_login") {
            LoginAdminScreen(navController)
        }

        // ---------- ADMIN PANEL ----------
        composable("admin_panel") {
            AdminPanelScreen(navController)
        }

        // ---------- ADMIN MENU MGMT ----------
        composable("admin_menu_management") {
            AdminMenuManagementScreen(navController)
        }

        // ---------- ADMIN EDIT ITEM ----------
        composable(
            route = "edit_item/{itemId}?tabIndex={tabIndex}"
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

        // ---------- ADMIN ADD ITEM ----------
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
