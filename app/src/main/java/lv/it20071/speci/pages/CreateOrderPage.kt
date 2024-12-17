package lv.it20071.speci.pages

import androidx.compose.foundation.layout.*
import androidx.compose.material3.Button
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.google.firebase.database.FirebaseDatabase
import lv.it20071.speci.AuthViewModel

@Composable
fun CreateOrderPage(
    modifier: Modifier = Modifier,
    navController: NavController,
    authViewModel: AuthViewModel
) {
    var personName by remember { mutableStateOf("") }
    var task by remember { mutableStateOf("") }
    var location by remember { mutableStateOf("") }
    var dueDate by remember { mutableStateOf("") }
    var currentPrice by remember { mutableStateOf("") }

    val database = FirebaseDatabase.getInstance("https://speci-7eb3e-default-rtdb.europe-west1.firebasedatabase.app")
    val ordersRef = database.getReference("tasks")

    Scaffold { innerPadding ->
        Column(
            modifier = modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(16.dp),
            verticalArrangement = Arrangement.Top,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(text = "Izveidot pasūtījumu", fontSize = 32.sp, modifier = Modifier.padding(16.dp))

            OutlinedTextField(
                value = personName,
                onValueChange = { personName = it },
                label = { Text("Jūsu vārds") },
                modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp)
            )

            OutlinedTextField(
                value = task,
                onValueChange = { task = it },
                label = { Text("Uzdevuma apraksts") },
                modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp)
            )

            OutlinedTextField(
                value = location,
                onValueChange = { location = it },
                label = { Text("Atrašanās vieta") },
                modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp)
            )

            OutlinedTextField(
                value = dueDate,
                onValueChange = { dueDate = it },
                label = { Text("Termiņš (YYYY-MM-DD)") },
                modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp)
            )

            OutlinedTextField(
                value = currentPrice,
                onValueChange = { currentPrice = it },
                label = { Text("Budžets (€)") },
                modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp)
            )

            Spacer(modifier = Modifier.height(16.dp))

            Button(
                onClick = {
                    val newOrder = Order(
                        personName = personName,
                        task = task,
                        location = location,
                        dueDate = dueDate,
                        currentPrice = currentPrice.toDoubleOrNull() ?: 0.0
                    )
                    ordersRef.push().setValue(newOrder) // Save the order to Firebase
                    navController.navigate("orders")
                },
                modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp)
            ) {
                Text(text = "Izveidot pasūtījumu")
            }

            TextButton(onClick = { navController.navigate("orders") }) {
                Text("Atpakaļ uz pasūtījumiem")
            }
        }
    }
}
