package lv.it20071.speci.pages

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.currentBackStackEntryAsState
import lv.it20071.speci.AuthViewModel
import lv.it20071.speci.AuthenticatedPageContent
import lv.it20071.speci.UsersViewModel


// Data Model
data class Order(
    val personName: String = "",
    val rating: Float = 0f,
    val task: String = "",
    val location: String = "",
    val dueDate: String = "",
    val currentPrice: Double = 0.0
)

// Composables
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OrderCard(order: Order, onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp)
            .clickable { onClick() }
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(text = order.personName, style = MaterialTheme.typography.titleMedium)
            Text(text = "Pasūtītāja vērtējums: ${order.rating}")
            Text(text = "Uzdevums: ${order.task}")
            Text(text = "Vieta: ${order.location}")
            Text(text = "Termiņš: ${order.dueDate}")
            Text(text = "Budžets: €${order.currentPrice}")
        }
    }
}



@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OrdersPage(
    modifier: Modifier = Modifier,
    navController: NavHostController,
    authViewModel: AuthViewModel
) {
    val usersViewModel: UsersViewModel = viewModel()
    val users by usersViewModel.users.collectAsState()
    val currentRoute = navController.currentBackStackEntryAsState().value?.destination?.route

    AuthenticatedPageContent(navController, authViewModel, currentRoute) { innerPadding ->
        Scaffold(
            floatingActionButton = {
                ExtendedFloatingActionButton(
                    onClick = { navController.navigate("create_order") },
                    text = { Text("Izveidot pasūtījumu") },
                    icon = { Icon(Icons.Default.Add, contentDescription = "Izveidot pasūtījumu") }
                )
            },
            modifier = modifier.padding(innerPadding)
        ) { scaffoldPadding ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(scaffoldPadding),
                verticalArrangement = Arrangement.Top,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "Pasūtījumi",
                    fontSize = 32.sp,
                    modifier = Modifier.padding(16.dp).align(Alignment.CenterHorizontally),
                    color = MaterialTheme.colorScheme.primary
                )

                if (users.isEmpty()) {
                    CircularProgressIndicator(modifier = Modifier.align(Alignment.CenterHorizontally))
                } else {
                    LazyColumn(modifier = Modifier.fillMaxSize()) {
                        items(users) { order ->
                            OrderCard(order = order, onClick = {
                                navController.navigate("order_details/${order.personName}/${order.rating}/${order.task}/${order.location}/${order.dueDate}/${order.currentPrice}")
                            })
                        }
                    }

                }
            }
        }
    }
}



