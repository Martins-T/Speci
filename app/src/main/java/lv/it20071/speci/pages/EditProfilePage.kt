package lv.it20071.speci.pages

import android.util.Log
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import lv.it20071.speci.MyBottomNavigation
import lv.it20071.speci.models.User
import lv.it20071.speci.viewModels.AuthViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditProfilePage(
    modifier: Modifier = Modifier,
    navController: NavHostController,
    authViewModel: AuthViewModel
) {
    val db = Firebase.firestore
    val currentUser = authViewModel.currentUser
    val userDocRef = remember(currentUser) {
        currentUser?.uid?.let { uid ->
            db.collection("users").document(uid)
        }
    }

    var firstName by remember { mutableStateOf("") }
    var lastName by remember { mutableStateOf("") }
    var homeAddress by remember { mutableStateOf("") }
    var workAddress by remember { mutableStateOf("") }
    var phoneNumber by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }

    LaunchedEffect(userDocRef) {
        userDocRef?.get()?.addOnSuccessListener { snapshot ->
            val user = snapshot.toObject(User::class.java)
            user?.let {
                firstName = it.firstName
                lastName = it.lastName
                homeAddress = it.homeAddress
                workAddress = it.workAddress
                phoneNumber = it.phoneNumber
                description = it.description
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Rediģēt profilu") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Atpakaļ"
                        )
                    }
                }
            )
        },
        bottomBar = {
            MyBottomNavigation(
                navController = navController,
                currentRoute = "edit_profile"
            )
        }
    ) { innerPadding ->
        Column(
            modifier = modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.Top,
            horizontalAlignment = Alignment.Start
        ) {
            OutlinedTextField(
                value = firstName,
                onValueChange = { firstName = it },
                label = { Text("Vārds") },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp)
            )

            OutlinedTextField(
                value = lastName,
                onValueChange = { lastName = it },
                label = { Text("Uzvārds") },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp)
            )

            OutlinedTextField(
                value = homeAddress,
                onValueChange = { homeAddress = it },
                label = { Text("Mājas adrese") },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp)
            )

            OutlinedTextField(
                value = workAddress,
                onValueChange = { workAddress = it },
                label = { Text("Darba adrese") },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp)
            )

            OutlinedTextField(
                value = phoneNumber,
                onValueChange = { phoneNumber = it },
                label = { Text("Telefona numurs") },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp)
            )

            OutlinedTextField(
                value = description,
                onValueChange = { description = it },
                label = { Text("Apraksts") },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp)
            )

            Spacer(modifier = Modifier.height(24.dp))

            Button(
                onClick = {
                    val uid = currentUser?.uid
                    val email = currentUser?.email
                    val docRef = userDocRef

                    if (uid != null && email != null && docRef != null) {
                        val updates = mapOf(
                            "firstName" to firstName,
                            "lastName" to lastName,
                            "homeAddress" to homeAddress,
                            "workAddress" to workAddress,
                            "phoneNumber" to phoneNumber,
                            "description" to description
                        )

                        userDocRef
                            ?.update(updates)
                            ?.addOnSuccessListener {
                                Log.d("EditProfilePage", "Profils veiksmīgi saglabāts")
                                navController.popBackStack()
                            }
                            ?.addOnFailureListener { e ->
                                Log.e("EditProfilePage", "Neizdevās saglabāt profilu", e)
                            }
                    } else {
                        Log.e("EditProfilePage", "Lietotājs vai dokumenta references nav pieejamas")
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp)
            ) {
                Text("Saglabāt profilu")
            }
        }
    }
}
