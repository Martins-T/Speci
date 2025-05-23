package lv.it20071.speci.pages

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Public
import androidx.compose.material.icons.filled.School
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import lv.it20071.speci.AuthenticatedPageContent
import lv.it20071.speci.models.SkillCategories
import lv.it20071.speci.models.User
import lv.it20071.speci.viewModels.AuthViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SpecialistsPage(
    modifier: Modifier = Modifier,
    navController: NavHostController,
    authViewModel: AuthViewModel
) {
    val db = Firebase.firestore
    var specialists by remember { mutableStateOf<List<User>>(emptyList()) }

    LaunchedEffect(Unit) {
        db.collection("users")
            .whereEqualTo("isSpecialist", true)
            .addSnapshotListener { snapshot, e ->
                if (e != null) return@addSnapshotListener

                specialists = snapshot
                    ?.documents
                    ?.mapNotNull { doc ->
                        doc.toObject(User::class.java)
                            ?.copy(id = doc.id)
                    }
                    .orEmpty()
            }
    }

    val allCategories = listOf("Visas") + SkillCategories.categories.keys
    var selectedCategory by remember { mutableStateOf("Visas") }
    var categoryExpanded by remember { mutableStateOf(false) }

    val subcatsForCategory = remember(selectedCategory) {
        SkillCategories.categories[selectedCategory]?.let { listOf("Visas") + it } ?: emptyList()
    }
    var selectedSubcategory by remember { mutableStateOf("Visas") }
    var subcategoryExpanded by remember { mutableStateOf(false) }

    AuthenticatedPageContent(navController, authViewModel, "specialists") { innerPadding ->
        Column(
            modifier = modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp, vertical = 8.dp)
        ) {
            Text(
                "Speciālisti",
                fontSize = 32.sp,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp),
                textAlign = TextAlign.Center
            )

            ExposedDropdownMenuBox(
                expanded = categoryExpanded,
                onExpandedChange = { categoryExpanded = !categoryExpanded },
            ) {
                TextField(
                    value = selectedCategory,
                    onValueChange = {},
                    readOnly = true,
                    singleLine = true,
                    label = { Text("Filtrēt kategoriju") },
                    trailingIcon = {
                        ExposedDropdownMenuDefaults.TrailingIcon(expanded = categoryExpanded)
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .menuAnchor()
                )
                ExposedDropdownMenu(
                    expanded = categoryExpanded,
                    onDismissRequest = { categoryExpanded = false }
                ) {
                    allCategories.forEach { cat ->
                        DropdownMenuItem(
                            text = { Text(cat) },
                            onClick = {
                                selectedCategory = cat
                                selectedSubcategory = "Visas"
                                categoryExpanded = false
                            }
                        )
                    }
                }
            }

            Spacer(Modifier.height(8.dp))

            if (selectedCategory != "Visas") {
                ExposedDropdownMenuBox(
                    expanded = subcategoryExpanded,
                    onExpandedChange = { subcategoryExpanded = !subcategoryExpanded }
                ) {
                    TextField(
                        value = selectedSubcategory,
                        onValueChange = {},
                        readOnly = true,
                        singleLine = true,
                        label = { Text("Filtrēt apakškategoriju") },
                        trailingIcon = {
                            ExposedDropdownMenuDefaults.TrailingIcon(expanded = subcategoryExpanded)
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .menuAnchor()
                    )
                    ExposedDropdownMenu(
                        expanded = subcategoryExpanded,
                        onDismissRequest = { subcategoryExpanded = false }
                    ) {
                        subcatsForCategory.forEach { sub ->
                            DropdownMenuItem(
                                text = { Text(sub) },
                                onClick = {
                                    selectedSubcategory = sub
                                    subcategoryExpanded = false
                                }
                            )
                        }
                    }
                }

                Spacer(Modifier.height(8.dp))
            }

            val displayed = specialists.filter { user ->
                when {
                    selectedCategory == "Visas" ->
                        true

                    selectedSubcategory == "Visas" -> {
                        val allowed = SkillCategories.categories[selectedCategory] ?: emptyList()
                        user.skills.any { it in allowed }
                    }

                    else ->
                        user.skills.contains(selectedSubcategory)
                }
            }

            if (displayed.isEmpty()) {
                Text(
                    "Nav neviena speciālista.",
                    modifier = Modifier.fillMaxWidth(),
                    textAlign = TextAlign.Center
                )
            } else {
                LazyColumn {
                    items(displayed) { user ->
                        SpecialistItem(
                            user = user,
                            onClick = {
                                navController.navigate("specialist_profile/${user.id}")
                            }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun SpecialistItem(
    user: User,
    modifier: Modifier = Modifier,
    onClick: () -> Unit = {}
) {
    val reviewCount = 0
    val hasCertification = true
    val languages = listOf("Latviešu", "English")

    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 4.dp)
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(8.dp),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.onSurface),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.Top
        ) {
            Box(
                modifier = Modifier
                    .size(50.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Person,
                    contentDescription = "Profila bilde",
                    modifier = Modifier.size(36.dp),
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Spacer(Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = user.fullName,
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                        )
                        if (hasCertification) {
                            Spacer(Modifier.width(4.dp))
                            Icon(
                                imageVector = Icons.Default.School,
                                contentDescription = "Sertifikāts",
                                modifier = Modifier.size(18.dp),
                                tint = MaterialTheme.colorScheme.primary
                            )
                        }
                    }

                    Text(
                        text = "${"%.1f".format(user.ratingAsSpecialist)}★",
                        style = MaterialTheme.typography.titleSmall.copy(
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold
                        )
                    )
                }

                Spacer(Modifier.height(4.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {

                        Icon(
                            imageVector = Icons.Default.Public,
                            contentDescription = "Valodas",
                            modifier = Modifier.size(16.dp)
                        )

                        Spacer(Modifier.width(2.dp))
                        Text(
                            text = languages.joinToString(", "),
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                    Text(
                        text = "$reviewCount atsauksmes",
                        style = MaterialTheme.typography.bodySmall
                    )
                }

                Spacer(Modifier.height(8.dp))

                Text(
                    text = "Prasmes",
                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.SemiBold)
                )
                Spacer(Modifier.height(2.dp))
                Text(
                    text = user.skills.joinToString(", "),
                    style = MaterialTheme.typography.bodySmall
                )

                Spacer(Modifier.height(8.dp))

                Text(
                    text = "Adrese",
                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.SemiBold)
                )
                Spacer(Modifier.height(2.dp))
                Text(
                    text = user.workAddress,
                    style = MaterialTheme.typography.bodySmall
                )
            }
        }
    }
}
