package lv.it20071.speci

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import lv.it20071.speci.pages.CreateOrderPage
import lv.it20071.speci.pages.EditProfilePage
import lv.it20071.speci.pages.LoginPage
import lv.it20071.speci.pages.OrderDetailsPage
import lv.it20071.speci.pages.OrdersPage
import lv.it20071.speci.pages.ProfilePage
import lv.it20071.speci.pages.SignupPage
import lv.it20071.speci.pages.SkillsPage
import lv.it20071.speci.pages.SpecialistProfilePage
import lv.it20071.speci.pages.SpecialistsPage
import lv.it20071.speci.viewModels.AuthState
import lv.it20071.speci.viewModels.AuthViewModel

@Composable
fun MyAppNavigation(
    modifier: Modifier = Modifier,
    authViewModel: AuthViewModel
) {
    val navController = rememberNavController()

    NavHost(navController = navController, startDestination = "login") {
        composable("login") {
            LoginPage(modifier, navController, authViewModel)
        }
        composable("signup") {
            SignupPage(modifier, navController, authViewModel)
        }
        composable("orders") {
            OrdersPage(modifier, navController, authViewModel)
        }
        composable("specialists") {
            SpecialistsPage(modifier, navController, authViewModel)
        }
        composable("profile") {
            ProfilePage(modifier, navController, authViewModel)
        }
        composable("edit_profile") {
            EditProfilePage(navController = navController, authViewModel = authViewModel)
        }
        composable("create_order") {
            CreateOrderPage(navController = navController, authViewModel = authViewModel)
        }
        composable("order_details/{orderId}") { backStackEntry ->
            val orderId = backStackEntry.arguments?.getString("orderId")
            if (orderId != null) {
                OrderDetailsPage(
                    navController = navController,
                    orderId = orderId,
                    authViewModel = authViewModel
                )
            } else {
                LaunchedEffect(Unit) {
                    navController.popBackStack()
                }
            }
        }
        composable("skills") {
            SkillsPage(navController = navController, authViewModel = authViewModel)
        }
        composable(
            "specialist_profile/{specialistId}",
            arguments = listOf(navArgument("specialistId") {
                type = NavType.StringType
            })
        ) { backStackEntry ->
            val id = backStackEntry.arguments?.getString("specialistId")!!
            SpecialistProfilePage(
                navController = navController,
                specialistId = id,
                authViewModel = authViewModel
            )
        }
    }
}

@Composable
fun MyBottomNavigation(
    navController: NavHostController,
    currentRoute: String?
) {
    var selectedItemIndex by remember { mutableStateOf(0) }
    val items = listOf("orders", "specialists", "profile")

    NavigationBar {
        items.forEachIndexed { index, route ->
            NavigationBarItem(
                icon = {
                    Icon(
                        imageVector = when (route) {
                            "orders" -> Icons.Filled.Menu
                            "specialists" -> Icons.Filled.Build
                            "profile" -> Icons.Filled.Person
                            else -> Icons.Filled.Menu
                        },
                        contentDescription = route
                    )
                },
                label = {
                    Text(
                        when (route) {
                            "orders" -> "Pasūtījumi"
                            "specialists" -> "Speciālisti"
                            "profile" -> "Mans profils"
                            else -> ""
                        }
                    )
                },
                selected = currentRoute == route,
                onClick = {
                    selectedItemIndex = index
                    navController.navigate(route) {
                        popUpTo(navController.graph.findStartDestination().id) {
                            saveState = true
                        }
                        launchSingleTop = true
                        restoreState = true
                    }
                }
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AuthenticatedPageContent(
    navController: NavHostController,
    authViewModel: AuthViewModel,
    currentRoute: String?,
    content: @Composable (PaddingValues) -> Unit
) {
    val authState by authViewModel.authState.observeAsState()

    LaunchedEffect(authState) {
        if (authState is AuthState.Unauthenticated) {
            navController.navigate("login") {
                popUpTo(0)
            }
        }
    }

    Scaffold(
        bottomBar = { MyBottomNavigation(navController, currentRoute) }
    ) { innerPadding ->
        content(innerPadding)
    }
}
