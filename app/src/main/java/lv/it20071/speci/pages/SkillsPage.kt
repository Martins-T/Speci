package lv.it20071.speci.pages

import android.util.Log
import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import lv.it20071.speci.MyBottomNavigation
import lv.it20071.speci.models.SkillCategories
import lv.it20071.speci.viewModels.AuthViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SkillsPage(
    modifier: Modifier = Modifier,
    navController: NavHostController,
    authViewModel: AuthViewModel
) {
    val db = Firebase.firestore
    val currentUser = authViewModel.currentUser
    val context = LocalContext.current

    var selectedSkills by remember { mutableStateOf<Set<String>>(emptySet()) }
    val expandedCategories = remember { mutableStateMapOf<String, Boolean>() }

    LaunchedEffect(currentUser) {
        currentUser?.uid?.let { uid ->
            db.collection("users").document(uid).get()
                .addOnSuccessListener { snapshot ->
                    @Suppress("UNCHECKED_CAST")
                    selectedSkills =
                        (snapshot.get("skills") as? List<String>)?.toSet() ?: emptySet()
                }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Manas prasmes") },
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
            Text(
                "Izvēlies darba prasmes",
                fontSize = 20.sp,
                modifier = Modifier.padding(bottom = 8.dp)
            )

            SkillCategories.categories.forEach { (category, subcategories) ->
                val isExpanded = expandedCategories[category] == true

                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp)
                ) {
                    Button(
                        onClick = { expandedCategories[category] = !isExpanded },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(if (isExpanded) "▲ $category" else "▼ $category")
                    }

                    if (isExpanded) {
                        subcategories.forEach { sub ->
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(start = 16.dp, top = 4.dp, bottom = 4.dp)
                            ) {
                                Checkbox(
                                    checked = selectedSkills.contains(sub),
                                    onCheckedChange = {
                                        selectedSkills = if (selectedSkills.contains(sub))
                                            selectedSkills - sub else selectedSkills + sub
                                    }
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(sub)
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Button(
                onClick = {
                    currentUser?.uid?.let { uid ->
                        val update = mutableMapOf<String, Any>(
                            "skills" to selectedSkills.toList()
                        )

                        if (selectedSkills.isEmpty()) {
                            update["isSpecialist"] = false
                        }

                        db.collection("users").document(uid)
                            .update(update)
                            .addOnSuccessListener {
                                Toast.makeText(context, "Prasmes saglabātas", Toast.LENGTH_SHORT)
                                    .show()
                                navController.popBackStack()
                            }
                            .addOnFailureListener {
                                Log.e("SkillsPage", "Neizdevās saglabāt prasmes", it)
                                Toast.makeText(context, "Kļūda saglabājot", Toast.LENGTH_SHORT)
                                    .show()
                            }
                    }
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Saglabāt prasmes")
            }
        }
    }
}
