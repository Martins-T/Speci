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
import lv.it20071.speci.components.ChangePasswordDialog

@Composable
fun ProfilePage(
    modifier: Modifier = Modifier,
    navController: NavHostController,
    authViewModel: AuthViewModel
) {
    val database = FirebaseDatabase.getInstance("https://speci-7eb3e-default-rtdb.europe-west1.firebasedatabase.app")
    val usersRef = database.getReference("users")
    val currentUser = authViewModel.currentUser
    var userProfile by remember { mutableStateOf<Map<String, Any>>(emptyMap()) }
    var showChangePasswordDialog by remember { mutableStateOf(false) }
    var isSpecialist by remember { mutableStateOf(false) }
    var ratingAsClient by remember { mutableStateOf(0.0) }
    var ratingAsSpecialist by remember { mutableStateOf(0.0) }

    LaunchedEffect(currentUser) {
        currentUser?.uid?.let { uid ->
            usersRef.child(uid).addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    userProfile = snapshot.value as? Map<String, Any> ?: emptyMap()
                    isSpecialist = userProfile["isSpecialist"] as? Boolean ?: false
                    ratingAsClient = userProfile["ratingAsClient"] as? Double ?: 0.0
                    ratingAsSpecialist = userProfile["ratingAsSpecialist"] as? Double ?: 0.0
                }

                override fun onCancelled(error: DatabaseError) {
                    Log.e("ProfilePage", "Error fetching user data: ${error.message}")
                }
            })
        }
    }

    Scaffold(
        bottomBar = {
            MyBottomNavigation(navController = navController, currentRoute = "profile")
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
            Text(
                text = "Mans Profils",
                fontSize = 32.sp,
                modifier = Modifier.padding(bottom = 16.dp).align(Alignment.CenterHorizontally),
                color = MaterialTheme.colorScheme.primary
            )

            userProfile["firstName"]?.let { Text(text = "Vārds: $it", fontSize = 18.sp) }
            userProfile["lastName"]?.let { Text(text = "Uzvārds: $it", fontSize = 18.sp) }
            userProfile["address"]?.let { Text(text = "Adrese: $it", fontSize = 18.sp) }
            userProfile["phoneNumber"]?.let { Text(text = "Telefona numurs: $it", fontSize = 18.sp) }
            userProfile["skills"]?.let { skills ->
                val formattedSkills = if (skills is List<*>) skills.joinToString(", ") else skills.toString()
                Text(text = "Prasmes: $formattedSkills", fontSize = 18.sp)
            }
            userProfile["description"]?.let { Text(text = "Apraksts: $it", fontSize = 18.sp) }

            Spacer(modifier = Modifier.height(16.dp))

            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(text = "Kļūt par speciālistu:", fontSize = 18.sp)
                Spacer(modifier = Modifier.width(8.dp))
                Switch(
                    checked = isSpecialist,
                    onCheckedChange = {
                        isSpecialist = it
                        currentUser?.uid?.let { uid ->
                            usersRef.child(uid).child("isSpecialist").setValue(it)
                        }
                    }
                )
            }

            Text(text = "Vērtējums:", fontSize = 20.sp, color = MaterialTheme.colorScheme.primary)
            Text(text = "Kā pasūtītāju: ${"%.1f".format(ratingAsClient)} zvaigznes", fontSize = 16.sp)
            Text(text = "Kā speciālistu: ${"%.1f".format(ratingAsSpecialist)} zvaigznes", fontSize = 16.sp)

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "Atsauksmes",
                fontSize = 20.sp,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(vertical = 8.dp).align(Alignment.CenterHorizontally),
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Button(
                    onClick = {
                        // Action for Pasūtītājs button
                        navController.navigate("client_reviews")
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.secondary,
                        contentColor = MaterialTheme.colorScheme.onSecondary
                    ),
                    modifier = Modifier.weight(1f).padding(end = 8.dp)
                ) {
                    Text(text = "Pasūtītājs")
                }

                Button(
                    onClick = {
                        // Action for Speciālists button
                        navController.navigate("specialist_reviews")
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.secondary,
                        contentColor = MaterialTheme.colorScheme.onSecondary
                    ),
                    modifier = Modifier.weight(1f).padding(start = 8.dp)
                ) {
                    Text(text = "Speciālists")
                }
            }

            Spacer(modifier = Modifier.weight(1f))

            Button(
                onClick = { navController.navigate("edit_profile") },
                modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp)
            ) {
                Text(text = "Rediģēt profilu")
            }

            Button(
                onClick = { showChangePasswordDialog = true },
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.secondary,
                    contentColor = MaterialTheme.colorScheme.onSecondary
                ),
                modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp)
            ) {
                Text(text = "Mainīt paroli")
            }

            Button(
                onClick = {
                    authViewModel.signOut()
                    navController.navigate("login") {
                        popUpTo("profile") { inclusive = true }
                    }
                },
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant,
                    contentColor = MaterialTheme.colorScheme.onSurfaceVariant
                ),
                modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp)
            ) {
                Text(text = "Iziet")
            }

            if (showChangePasswordDialog) {
                ChangePasswordDialog(
                    onDismiss = { showChangePasswordDialog = false },
                    onChangePassword = { newPassword ->
                        currentUser?.updatePassword(newPassword)?.addOnCompleteListener { task ->
                            if (task.isSuccessful) {
                                Log.d("ProfilePage", "Password updated successfully")
                            } else {
                                Log.e("ProfilePage", "Password update failed", task.exception)
                            }
                        }
                    }
                )
            }
        }
    }
}
