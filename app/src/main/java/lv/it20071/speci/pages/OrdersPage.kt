package lv.it20071.speci.pages

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import androidx.navigation.compose.currentBackStackEntryAsState
import com.google.firebase.Firebase
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.database
import lv.it20071.speci.AuthViewModel
import lv.it20071.speci.AuthenticatedPageContent


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OrdersPage(
    modifier: Modifier = Modifier,
    navController: NavHostController,
    authViewModel: AuthViewModel
) {
    val currentRoute = navController.currentBackStackEntryAsState().value?.destination?.route // Get current route
    val orders = listOf(
        Order("Vova", 4.5f, "Santehnikas pakalpojumi", "Rīga", "2025-01-17", 90.0),
        Order("Sasha", 4.5f, "Špaktelēšana", "Cēsis", "2025-02-18", 700.0),
        Order("Vlad", 4.5f, "Celtniecība", "Valmiera", "2025-03-01", 24000.0),
        Order("Zhenya", 4.5f, "Plumbing", "Riga", "2025-01-15", 150.0),
        Order("Leo", 4.5f, "Plumbing", "Riga", "2025-01-15", 150.0),
        Order("Dmitry", 4.5f, "Plumbing", "Riga", "2025-01-15", 150.0),
        Order("Alexandr", 4.5f, "Plumbing", "Riga", "2025-01-15", 150.0)
    )

    AuthenticatedPageContent(navController, authViewModel, currentRoute) { innerPadding -> // Pass currentRoute
        Column(
            modifier = modifier
                .fillMaxSize()
                .padding(innerPadding),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(text = "Pasūtījumi", fontSize = 32.sp)

            LazyColumn(modifier = Modifier.fillMaxSize()) {
                items(orders) { order ->
                    OrderCard(order)
                }
            }

        }
    }
}




//@OptIn(ExperimentalMaterial3Api::class)
//@Composable
//fun OrdersPage(
//    modifier: Modifier = Modifier,
//    navController: NavHostController,
//    authViewModel: AuthViewModel
//) {
//    val currentRoute = navController.currentBackStackEntryAsState().value?.destination?.route
////    val orders = listOf(
////        Order("Vova", 4.5f, "Santehnikas pakalpojumi", "Rīga", "2025-01-17", 90.0),
////        Order("Sasha", 4.5f, "Špaktelēšana", "Cēsis", "2025-02-18", 700.0),
////        Order("Vlad", 4.5f, "Celtniecība", "Valmiera", "2025-03-01", 24000.0),
////        Order("Zhenya", 4.5f, "Plumbing", "Riga", "2025-01-15", 150.0),
////        Order("Leo", 4.5f, "Plumbing", "Riga", "2025-01-15", 150.0),
////        Order("Dmitry", 4.5f, "Plumbing", "Riga", "2025-01-15", 150.0),
////        Order("Alexandr", 4.5f, "Plumbing", "Riga", "2025-01-15", 150.0)
////    )
//
//    var orders by remember { mutableStateOf<List<Order>>(emptyList()) }
//    var isLoading by remember { mutableStateOf(true) } // Add loading state
//
//    LaunchedEffect(Unit) {
//        val database = Firebase.database
//        val ordersRef = database.getReference("tasks")
//
//        ordersRef.addValueEventListener(object : ValueEventListener {
//            override fun onDataChange(snapshot: DataSnapshot) {
//                orders = snapshot.children.mapNotNull { it.getValue(Order::class.java) }
//                isLoading = false // Data fetched, set loading to false
//            }
//
//            override fun onCancelled(error: DatabaseError) {
//                // Handle error
//                isLoading = false // Error occurred, set loading to false
//            }
//        })
//    }
//
//    AuthenticatedPageContent(navController, authViewModel, currentRoute) { innerPadding ->
//        Scaffold(
//            modifier = modifier.padding(innerPadding) // Apply inner padding to Scaffold
//        ) { scaffoldPadding -> // Use scaffoldPadding for content
//            Column(
//                modifier = Modifier
//                    .fillMaxSize()
//                    .padding(scaffoldPadding), // Apply scaffoldPadding to Column
////                verticalArrangement = Arrangement.SpaceBetween, // Distribute space
//                horizontalAlignment = Alignment.CenterHorizontally
//            ) {
//                Text(text = "Pasūtījumu saraksts", fontSize = 32.sp)
//
//                if (isLoading) {
//                    CircularProgressIndicator() // Display loading indicator
//                } else {
//                    LazyColumn(
//                        modifier = Modifier
//                            .weight(1f) // Allow LazyColumn to take up available space
//                            .padding(scaffoldPadding)
//                    ) {
//                        items(orders) { order ->
//                            OrderCard(order)
//                        }
//                    }
//
//                    TextButton(onClick = { authViewModel.signout() }) { Text(text = "Iziet") }
//                }
//
//            }
//
//        }
//
//    }
//}

data class Order(
    val personName: String,
    val rating: Float,
    val task: String,
    val location: String,
    val dueDate: String,
    val currentPrice: Double
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OrderCard(order: Order) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(text = order.personName, fontWeight = FontWeight.Bold)
            Text(text = "Vērtējums: ${order.rating}")
            Text(text = "Uzdevums: ${order.task}")
            Text(text = "Vieta: ${order.location}")
            Text(text = "Termiņš: ${order.dueDate}")
            Text(text = "Cena: €${order.currentPrice}")
        }
    }
}