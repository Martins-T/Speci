package lv.it20071.speci.pages

import android.util.Log
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import lv.it20071.speci.AuthViewModel
import lv.it20071.speci.MyBottomNavigation

@Composable
fun EditProfilePage(
    modifier: Modifier = Modifier,
    navController: NavHostController,
    authViewModel: AuthViewModel
) {
    val database = FirebaseDatabase.getInstance("https://speci-7eb3e-default-rtdb.europe-west1.firebasedatabase.app")
    val usersRef = database.getReference("users")
    val currentUser = authViewModel.currentUser

    var firstName by remember { mutableStateOf("") }
    var lastName by remember { mutableStateOf("") }
    var address by remember { mutableStateOf("") }
    var phoneNumber by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }

    val allCategories = listOf(
        "Santehniķis",
        "Elektriķis",
        "Apdares darbu speciālists",
        "Jumta meistars",
        "Skaistumkopšana",
        "Auto remonts",
        "Pasniedzējs/Instruktors",
        "Datoru remonts",
        "Mājas uzkopšana",
        "Mēbeļu montētājs"
    )
    var selectedSkills by remember { mutableStateOf<Set<String>>(emptySet()) }
    var expanded by remember { mutableStateOf(false) }

    LaunchedEffect(currentUser) {
        currentUser?.uid?.let { uid ->
            usersRef.child(uid).addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    snapshot.child("firstName").value?.let { firstName = it as String }
                    snapshot.child("lastName").value?.let { lastName = it as String }
                    snapshot.child("address").value?.let { address = it as String }
                    snapshot.child("phoneNumber").value?.let { phoneNumber = it as String }
                    snapshot.child("description").value?.let { description = it as String }
                    snapshot.child("skills").value?.let { skills ->
                        selectedSkills = (skills as? List<*>)?.mapNotNull { it as? String }?.toSet() ?: emptySet()
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    Log.e("EditProfilePage", "Error fetching user data: ${error.message}")
                }
            })
        }
    }

    Scaffold(
        bottomBar = {
            MyBottomNavigation(navController = navController, currentRoute = "edit_profile")
        }
    ) { innerPadding ->
        Column(
            modifier = modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(16.dp),
            verticalArrangement = Arrangement.Top,
            horizontalAlignment = Alignment.Start
        ) {
            Text(text = "Rediģēt Profilu", fontSize = 32.sp, modifier = Modifier.padding(16.dp))

            OutlinedTextField(
                value = firstName,
                onValueChange = { firstName = it },
                label = { Text("Vārds") },
                modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp)
            )

            OutlinedTextField(
                value = lastName,
                onValueChange = { lastName = it },
                label = { Text("Uzvārds") },
                modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp)
            )

            OutlinedTextField(
                value = address,
                onValueChange = { address = it },
                label = { Text("Adrese") },
                modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp)
            )

            OutlinedTextField(
                value = phoneNumber,
                onValueChange = { phoneNumber = it },
                label = { Text("Telefona numurs") },
                modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp)
            )

            OutlinedTextField(
                value = description,
                onValueChange = { description = it },
                label = { Text("Apraksts") },
                modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp)
            )

            Text(text = "Izvēlies savas prasmes:", fontSize = 18.sp, modifier = Modifier.padding(top = 8.dp))

            Box(modifier = Modifier.fillMaxWidth()) {
                Button(
                    onClick = { expanded = !expanded },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(text = "Izvēlēties prasmes (${selectedSkills.size} izvēlētas)")
                }
                DropdownMenu(
                    expanded = expanded,
                    onDismissRequest = { expanded = false },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    allCategories.forEach { category ->
                        DropdownMenuItem(
                            onClick = {
                                if (selectedSkills.contains(category)) {
                                    selectedSkills = selectedSkills - category
                                } else {
                                    selectedSkills = selectedSkills + category
                                }
                            },
                            text = {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Checkbox(
                                        checked = selectedSkills.contains(category),
                                        onCheckedChange = null // Handled in onClick
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(text = category)
                                }
                            }
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.weight(1f))

            Button(
                onClick = {
                    currentUser?.uid?.let { uid ->
                        val userProfile = mapOf(
                            "firstName" to firstName,
                            "lastName" to lastName,
                            "address" to address,
                            "phoneNumber" to phoneNumber,
                            "description" to description,
                            "skills" to selectedSkills.toList()
                        )
                        usersRef.child(uid).setValue(userProfile)
                        navController.popBackStack()
                    }
                },
                modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp)
            ) {
                Text(text = "Saglabāt izmaiņas")
            }
        }
    }
}

