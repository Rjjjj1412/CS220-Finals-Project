import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.example.quickbitefinalproject.ui.admin.AddItemPage
import com.example.quickbitefinalproject.ui.admin.AdminEditProfileScreen
import com.example.quickbitefinalproject.ui.admin.AdminMenuManagementScreen
import com.example.quickbitefinalproject.ui.admin.AdminPanelScreen
import com.example.quickbitefinalproject.ui.admin.EditItemPage
import com.example.quickbitefinalproject.ui.splash.SplashScreen
import com.example.quickbitefinalproject.ui.admin.LoginAdminScreen
import androidx.navigation.NavType
import androidx.navigation.navArgument


@Composable
fun AppNavGraph(navController: NavHostController) {

    NavHost(
        navController = navController,
        startDestination = "splash"
    ) {

        composable("splash") {
            SplashScreen(navController)
        }

        composable("admin_login") {
            LoginAdminScreen(navController)
        }

        composable("admin_panel") {
            AdminPanelScreen(navController)
        }

        composable ( "admin_edit_profile") {
            AdminEditProfileScreen(navController)
        }

        composable ("admin_menu_management" ) {
            AdminMenuManagementScreen(navController)
        }

        composable(
            "add_item_page?tabIndex={tabIndex}",
            arguments = listOf(navArgument("tabIndex") {
                type = NavType.IntType
                defaultValue = 0
            })
        ) { backStackEntry ->
            val tabIndex = backStackEntry.arguments?.getInt("tabIndex") ?: 0
            AddItemPage(navController, tabIndex)
        }

        composable(
            "edit_item/{itemId}?tabIndex={tabIndex}",
            arguments = listOf(
                navArgument("itemId") { type = NavType.StringType },
                navArgument("tabIndex") { type = NavType.IntType; defaultValue = 1; }
            )
        ) { backStackEntry ->
            val itemId = backStackEntry.arguments?.getString("itemId") ?: ""
            val tabIndex = backStackEntry.arguments?.getInt("tabIndex") ?: 0
            EditItemPage(navController, itemId, tabIndex)
        }
    }
}
