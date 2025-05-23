package lv.it20071.speci.pages

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.material3.MenuAnchorType
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import com.google.firebase.Timestamp
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import lv.it20071.speci.MyBottomNavigation
import lv.it20071.speci.models.OrderStatus
import lv.it20071.speci.models.SkillCategories
import lv.it20071.speci.viewModels.AuthViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateOrderPage(
    modifier: Modifier = Modifier,
    navController: NavHostController,
    authViewModel: AuthViewModel
) {
    val db = Firebase.firestore
    val userId = authViewModel.currentUser?.uid ?: ""

    var category by remember { mutableStateOf("") }
    var subcategory by remember { mutableStateOf("") }
    var task by remember { mutableStateOf("") }
    var location by remember { mutableStateOf("") }
    var dueDate by remember { mutableStateOf("") }
    var budget by remember { mutableStateOf("") }

    val categories = SkillCategories.categories
    var categoryExpanded by remember { mutableStateOf(false) }
    var subcategoryExpanded by remember { mutableStateOf(false) }

    var categoryError by remember { mutableStateOf(false) }
    val canSubmit = category.isNotBlank()

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "Izveidot pasūtījumu",
                        modifier = Modifier.padding(start = 48.dp),
                        maxLines = 1
                    )
                },
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
                currentRoute = "create_order"
            )
        }
    ) { innerPadding ->
        Column(
            modifier = modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.Top,
            horizontalAlignment = Alignment.Start
        ) {

            Text("Kategorija *", fontSize = 16.sp)
            ExposedDropdownMenuBox(
                expanded = categoryExpanded,
                onExpandedChange = { categoryExpanded = !categoryExpanded }
            ) {
                TextField(
                    value = category,
                    onValueChange = {},
                    readOnly = true,
                    isError = categoryError,
                    placeholder = { Text("Izvēlies kategoriju") },
                    trailingIcon = {
                        ExposedDropdownMenuDefaults.TrailingIcon(expanded = categoryExpanded)
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .menuAnchor(MenuAnchorType.PrimaryNotEditable)
                )
                ExposedDropdownMenu(
                    expanded = categoryExpanded,
                    onDismissRequest = { categoryExpanded = false }
                ) {
                    categories.keys.forEach { key ->
                        DropdownMenuItem(
                            text = { Text(key) },
                            onClick = {
                                category = key
                                subcategory = ""
                                categoryError = false
                                categoryExpanded = false
                            }
                        )
                    }
                }
            }
            if (categoryError) {
                Text(
                    text = "Obligāti jāizvēlas kategorija",
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodySmall,
                    modifier = Modifier.padding(start = 16.dp, top = 4.dp)
                )
            }

            Spacer(Modifier.height(12.dp))

            if (category.isNotBlank()) {
                Text("Apakškategorija (nav obligāta)", fontSize = 16.sp)
                ExposedDropdownMenuBox(
                    expanded = subcategoryExpanded,
                    onExpandedChange = { subcategoryExpanded = !subcategoryExpanded }
                ) {
                    TextField(
                        value = subcategory,
                        onValueChange = {},
                        readOnly = true,
                        placeholder = { Text("Izvēlies apakškategoriju") },
                        trailingIcon = {
                            ExposedDropdownMenuDefaults.TrailingIcon(expanded = subcategoryExpanded)
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .menuAnchor(MenuAnchorType.PrimaryNotEditable)
                    )
                    ExposedDropdownMenu(
                        expanded = subcategoryExpanded,
                        onDismissRequest = { subcategoryExpanded = false }
                    ) {
                        categories[category]?.forEach { sub ->
                            DropdownMenuItem(
                                text = { Text(sub) },
                                onClick = {
                                    subcategory = sub
                                    subcategoryExpanded = false
                                }
                            )
                        }
                    }
                }

                Spacer(Modifier.height(12.dp))
            }

            OutlinedTextField(
                value = task,
                onValueChange = { task = it },
                label = { Text("Uzdevuma apraksts") },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp)
            )

            OutlinedTextField(
                value = location,
                onValueChange = { location = it },
                label = { Text("Atrašanās vieta") },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp)
            )

            OutlinedTextField(
                value = dueDate,
                onValueChange = { dueDate = it },
                label = { Text("Termiņš (YYYY-MM-DD)") },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp)
            )

            OutlinedTextField(
                value = budget,
                onValueChange = { budget = it },
                label = { Text("Budžets (€)") },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp)
            )

            Spacer(Modifier.height(20.dp))

            Button(
                onClick = {
                    if (category.isBlank()) {
                        categoryError = true
                        return@Button
                    }
                    val order = hashMapOf(
                        "createdBy" to userId,
                        "task" to task,
                        "location" to location,
                        "dueDate" to dueDate,
                        "budget" to (budget.toDoubleOrNull() ?: 0.0),
                        "category" to category,
                        "subcategory" to subcategory,
                        "status" to OrderStatus.OPEN,
                        "timestamp" to Timestamp.now()
                    )
                    db.collection("orders")
                        .add(order)
                        .addOnSuccessListener {
                            navController.navigate("orders")
                        }
                },
                enabled = canSubmit,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Izveidot pasūtījumu")
            }
        }
    }
}
