package lv.it20071.speci.pages

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import lv.it20071.speci.AuthenticatedPageContent
import lv.it20071.speci.models.User
import lv.it20071.speci.viewModels.AuthViewModel
import lv.it20071.speci.viewModels.UsersViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SpecialistProfilePage(
    navController: NavHostController,
    specialistId: String,
    authViewModel: AuthViewModel
) {
    val usersViewModel: UsersViewModel = viewModel()
    val usersMap by usersViewModel.users.collectAsState()

    LaunchedEffect(specialistId) {
        usersViewModel.fetchUser(specialistId)
    }

    val user: User? = usersMap[specialistId]

    AuthenticatedPageContent(navController, authViewModel, "specialist_profile") { innerPadding ->
        Scaffold(
            topBar = {
                TopAppBar(
                    title = {
                        Text(
                            text = "Speciālista profils",
                            modifier = Modifier.padding(start = 48.dp),
                            maxLines = 1
                        )
                    },
                    navigationIcon = {
                        IconButton(onClick = { navController.popBackStack() }) {
                            Icon(
                                Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = "Atpakaļ"
                            )
                        }
                    }
                )
            }
        ) { inner ->
            if (user == null) {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            } else {
                Column(
                    Modifier
                        .fillMaxSize()
                        .padding(inner)
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Box(
                        Modifier
                            .size(80.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.onSurface.copy(.1f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Default.Person, contentDescription = null, Modifier.size(48.dp))
                    }

                    Row(
                        Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            user.fullName,
                            style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold)
                        )
                        Text(
                            "${"%.1f".format(user.ratingAsSpecialist)}★",
                            style = MaterialTheme.typography.headlineSmall
                        )
                    }

                    if (user.description.isNotBlank()) {
                        Text("Apraksts", style = MaterialTheme.typography.labelSmall)
                        Text(user.description, style = MaterialTheme.typography.bodyMedium)
                    }

                    if (user.skills.isNotEmpty()) {
                        Text("Prasmes", style = MaterialTheme.typography.labelSmall)
                        Text(
                            user.skills.joinToString(", "),
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }

                    Text("Darba adrese", style = MaterialTheme.typography.labelSmall)
                    Text(user.workAddress, style = MaterialTheme.typography.bodyMedium)
                }
            }
        }
    }
}