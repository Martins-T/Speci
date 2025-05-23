package lv.it20071.speci.pages

import android.util.Log
import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import lv.it20071.speci.MyBottomNavigation
import lv.it20071.speci.components.ChangePasswordDialog
import lv.it20071.speci.models.User
import lv.it20071.speci.viewModels.AuthViewModel

@Composable
fun ProfilePage(
    modifier: Modifier = Modifier,
    navController: NavHostController,
    authViewModel: AuthViewModel
) {
    val context = LocalContext.current
    val db = Firebase.firestore
    val currentUser = authViewModel.currentUser

    val userDocRef = remember(currentUser) {
        currentUser?.uid?.let { uid -> db.collection("users").document(uid) }
    }

    var userProfile by remember { mutableStateOf<User?>(null) }
    var isSpecialist by remember { mutableStateOf(false) }
    var showChangePasswordDialog by remember { mutableStateOf(false) }

    LaunchedEffect(userDocRef) {
        userDocRef?.addSnapshotListener { snapshot, error ->
            if (error != null) {
                Log.e("ProfilePage", "Error fetching profile", error)
                return@addSnapshotListener
            }
            val data = snapshot?.data ?: return@addSnapshotListener
            userProfile = snapshot?.toObject(User::class.java)
            isSpecialist = (data["isSpecialist"] as? Boolean) ?: false
        }
    }

    Scaffold(
        topBar = {
            Surface(
                color = MaterialTheme.colorScheme.primaryContainer,
                tonalElevation = 4.dp
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        "Mans Profils",
                        style = MaterialTheme.typography.headlineMedium,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                }
            }
        },
        bottomBar = {
            MyBottomNavigation(navController = navController, currentRoute = "profile")
        }
    ) { innerPadding ->
        Column(
            modifier = modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp)
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.fillMaxWidth()
            ) {
                Surface(
                    shape = CircleShape,
                    color = MaterialTheme.colorScheme.secondaryContainer,
                    modifier = Modifier.size(96.dp)
                ) {
                    Image(
                        painter = rememberVectorPainter(Icons.Default.Person),
                        contentDescription = "Avatar",
                        modifier = Modifier.padding(16.dp)
                    )
                }
                Spacer(Modifier.height(8.dp))
                Text(
                    text = "${userProfile?.firstName.orEmpty()} ${userProfile?.lastName.orEmpty()}",
                    style = MaterialTheme.typography.headlineSmall
                )
                val desc = userProfile?.description
                if (!desc.isNullOrBlank()) {
                    Text(
                        text = desc,
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier.padding(top = 4.dp)
                    )
                }
            }

            Spacer(Modifier.height(16.dp))

            ElevatedCard(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("Kontaktinformācija", style = MaterialTheme.typography.titleMedium)
                    Spacer(Modifier.height(8.dp))
                    ProfileRow("E-pasts", userProfile?.email.orEmpty())
                    ProfileRow("Telefons", userProfile?.phoneNumber.orEmpty())
                    ProfileRow("Mājas adrese", userProfile?.homeAddress.orEmpty())
                    ProfileRow("Darba adrese", userProfile?.workAddress.orEmpty())

                    Spacer(Modifier.height(8.dp))
                    Button(
                        onClick = { navController.navigate("edit_profile") },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Rediģēt kontaktinformāciju")
                    }
                }
            }

            Spacer(Modifier.height(16.dp))

            ElevatedCard(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("Speciālista statuss", style = MaterialTheme.typography.titleMedium)
                    Spacer(modifier = Modifier.height(8.dp))

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(text = "Kļūt par speciālistu:", fontSize = 18.sp)
                        Spacer(modifier = Modifier.width(8.dp))
                        Switch(
                            checked = isSpecialist,
                            onCheckedChange = { newValue ->
                                if (newValue && userProfile?.skills.isNullOrEmpty()) {
                                    Toast.makeText(
                                        context,
                                        "Lai kļūtu par speciālistu, jāizvēlas vismaz viena prasme",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                } else {
                                    isSpecialist = newValue
                                    currentUser?.uid?.let { uid ->
                                        Firebase.firestore.collection("users")
                                            .document(uid)
                                            .update("isSpecialist", newValue)
                                    }
                                }
                            }
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))
                    Button(
                        onClick = { navController.navigate("skills") },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Izvēlēties prasmes")
                    }
                }
            }

            Spacer(Modifier.height(16.dp))

            ElevatedCard(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("Vērtējums un Atsauksmes", style = MaterialTheme.typography.titleMedium)
                    Spacer(Modifier.height(8.dp))

                    val clientRating = userProfile?.ratingAsClient?.toFloat() ?: 0f
                    val specRating = userProfile?.ratingAsSpecialist?.toFloat() ?: 0f

                    RatingRow("Kā pasūtītāju", clientRating)
                    RatingRow("Kā speciālistu", specRating)

                    Spacer(Modifier.height(12.dp))

                    Row(
                        horizontalArrangement = Arrangement.SpaceEvenly,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Button(onClick = { navController.navigate("client_reviews") }) {
                            Text("Pasūtītājs")
                        }
                        Button(onClick = { navController.navigate("specialist_reviews") }) {
                            Text("Speciālists")
                        }
                    }
                }
            }

            Spacer(Modifier.height(24.dp))

            Button(
                onClick = { showChangePasswordDialog = true },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Mainīt paroli")
            }

            Spacer(Modifier.height(8.dp))

            OutlinedButton(
                onClick = {
                    authViewModel.signOut()
                    navController.navigate("login") {
                        popUpTo("profile") { inclusive = true }
                    }
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Iziet")
            }
        }
    }

    if (showChangePasswordDialog) {
        ChangePasswordDialog(
            onDismiss = { showChangePasswordDialog = false },
            onChangePassword = { newPwd ->
                authViewModel.changePassword(
                    newPwd,
                    onSuccess = {
                        Toast.makeText(context, "Parole veiksmīgi nomainīta", Toast.LENGTH_SHORT)
                            .show()
                        showChangePasswordDialog = false
                    },
                    onFailure = { e ->
                        Toast.makeText(
                            context,
                            "Neizdevās nomainīt paroli: ${e.message}",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                )
            }
        )
    }
}

@Composable
private fun ProfileRow(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(label, modifier = Modifier.weight(1f), style = MaterialTheme.typography.bodyMedium)
        Text(value, style = MaterialTheme.typography.bodyLarge)
    }
}

@Composable
private fun RatingRow(label: String, rating: Float) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(label, modifier = Modifier.weight(1f), style = MaterialTheme.typography.bodyMedium)
        Text("${"%.1f".format(rating)} ★", style = MaterialTheme.typography.bodyLarge)
    }
}
