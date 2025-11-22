import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.example.quickbitefinalproject.ui.admin.AdminEditProfileScreen
import com.example.quickbitefinalproject.ui.admin.AdminMenuManagementScreen
import com.example.quickbitefinalproject.ui.admin.AdminPanelScreen
import com.example.quickbitefinalproject.ui.splash.SplashScreen
import com.example.quickbitefinalproject.ui.admin.LoginAdminScreen




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

        composable (route = "admin_menu_management" ) {
            AdminMenuManagementScreen(navController)
        }
    }
}
