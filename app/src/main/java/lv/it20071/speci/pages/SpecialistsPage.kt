package lv.it20071.speci.pages

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import com.google.firebase.database.*
import lv.it20071.speci.AuthViewModel
import lv.it20071.speci.AuthenticatedPageContent

data class Specialist(
    val firstName: String = "",
    val lastName: String = "",
    val rating: Float = 0f,
    val skills: List<String> = emptyList(),
    val address: String = ""
) {
    val name: String get() = "$firstName $lastName"
}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SpecialistsPage(
    modifier: Modifier = Modifier,
    navController: NavHostController,
    authViewModel: AuthViewModel
) {
    val database = FirebaseDatabase.getInstance("https://speci-7eb3e-default-rtdb.europe-west1.firebasedatabase.app")
    val specialistsRef = database.getReference("specialists")

    var specialists by remember { mutableStateOf<List<Specialist>>(emptyList()) }

    LaunchedEffect(Unit) {
        specialistsRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val specialistList = mutableListOf<Specialist>()
                for (child in snapshot.children) {
                    val specialist = child.getValue(Specialist::class.java)
                    if (specialist != null) {
                        specialistList.add(specialist)
                    }
                }
                specialists = specialistList
            }

            override fun onCancelled(error: DatabaseError) {
                // Handle error
            }
        })
    }

    AuthenticatedPageContent(navController, authViewModel, "specialists") { innerPadding ->
        Column(
            modifier = modifier
                .fillMaxSize()
                .padding(innerPadding),
            verticalArrangement = Arrangement.Top,
            horizontalAlignment = Alignment.Start
        ) {
            Text(
                text = "Speciālisti",
                fontSize = 32.sp,
                modifier = Modifier.padding(16.dp).align(Alignment.CenterHorizontally),
                color = MaterialTheme.colorScheme.primary
            )

            Spacer(modifier = Modifier.height(16.dp))

            LazyColumn(modifier = Modifier.fillMaxSize()) {
                items(specialists) { specialist ->
                    SpecialistItem(specialist)
                }
            }
        }
    }
}

@Composable
fun SpecialistItem(specialist: Specialist) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        elevation = CardDefaults.cardElevation(4.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(text = "Vārds: ${specialist.name}", style = MaterialTheme.typography.bodyLarge)
            Text(text = "Vērtējums: ${specialist.rating} zvaigznes", style = MaterialTheme.typography.bodyMedium)
            Text(
                text = "Prasmes: ${specialist.skills.joinToString(", ")}",
                style = MaterialTheme.typography.bodyMedium
            )
            Text(text = "Adrese: ${specialist.address}", style = MaterialTheme.typography.bodyMedium)
        }
    }
}

