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
import lv.it20071.speci.pages.SpecialistsPage

@Composable
fun MyAppNavigation(modifier: Modifier = Modifier,authViewModel: AuthViewModel) {
    val navController = rememberNavController()

    NavHost(navController = navController, startDestination = "login", builder = {
        composable("login"){ LoginPage(modifier,navController,authViewModel) }
        composable("signup"){
            SignupPage(modifier,navController,authViewModel)
        }
        composable("orders"){
            OrdersPage(modifier,navController,authViewModel)
        }
        composable("specialists"){
            SpecialistsPage(modifier,navController,authViewModel)
        }
        composable("profile"){
            ProfilePage(modifier,navController,authViewModel)
        }
        composable("edit_profile") {
            EditProfilePage(
                navController = navController,
                authViewModel = authViewModel
            )
        }
        composable("create_order") {
            CreateOrderPage(
                navController = navController,
                authViewModel = authViewModel
            )
        }
        composable(
            route = "order_details/{personName}/{rating}/{task}/{location}/{dueDate}/{currentPrice}",
            arguments = listOf(
                navArgument("personName") { type = NavType.StringType },
                navArgument("rating") { type = NavType.FloatType },
                navArgument("task") { type = NavType.StringType },
                navArgument("location") { type = NavType.StringType },
                navArgument("dueDate") { type = NavType.StringType },
                navArgument("currentPrice") { type = NavType.FloatType }
            )
        ) { backStackEntry ->
            val personName = backStackEntry.arguments?.getString("personName") ?: ""
            val rating = backStackEntry.arguments?.getFloat("rating") ?: 0f
            val task = backStackEntry.arguments?.getString("task") ?: ""
            val location = backStackEntry.arguments?.getString("location") ?: ""
            val dueDate = backStackEntry.arguments?.getString("dueDate") ?: ""
            val currentPrice = backStackEntry.arguments?.getFloat("currentPrice")?.toDouble() ?: 0.0

            OrderDetailsPage(
                navController = navController,
                personName = personName,
                rating = rating,
                task = task,
                location = location,
                dueDate = dueDate,
                currentPrice = currentPrice
            )
        }
    })
}


@Composable
fun MyBottomNavigation(navController: NavHostController, currentRoute: String?) {
    var selectedItemIndex by remember { mutableStateOf(0) }
    val items = listOf("orders", "specialists", "profile") // Update with route names

    NavigationBar {
        items.forEachIndexed { index, route ->
            NavigationBarItem(
                icon = {
                    when (route) {
                        "orders" -> Icon(Icons.Filled.Menu, contentDescription = "Pasūtījumi")
                        "specialists" -> Icon(Icons.Filled.Build, contentDescription = "Speciālisti")
                        "profile" -> Icon(Icons.Filled.Person, contentDescription = "Mans profils")
                        else -> {} // Handle unexpected route if needed
                    }
                },
                label = { Text(
                    when (route) {
                        "orders" -> "Pasūtījumi"
                        "specialists" -> "Speciālisti"
                        "profile" -> "Mans profils"
                        else -> ""
                    }
                ) },
                selected = currentRoute == route,
                onClick = {
                    selectedItemIndex = index
                    navController.navigate(route) {
                        // Pop up to the root destination of the graph to
                        // avoid building up a large stack of destinations
                        // on the back stack as users select items
                        popUpTo(navController.graph.findStartDestination().id) {
                            saveState = true
                        }
                        // Avoid multiple copies of the same destination when
                        // reselecting the same item
                        launchSingleTop = true
                        // Restore state when reselecting a previously selected item
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
    currentRoute: String?, // Add currentRoute parameter
    content: @Composable (PaddingValues) -> Unit
) {
    val authState = authViewModel.authState.observeAsState()

    LaunchedEffect(authState.value) {
        when (authState.value) {
            is AuthState.Unauthenticated -> navController.navigate("login")
            else -> Unit
        }
    }

    Scaffold(
        bottomBar = { MyBottomNavigation(navController = navController, currentRoute = currentRoute) }
    ) { innerPadding ->
        content(innerPadding) // Call the content lambda with innerPadding
    }
}