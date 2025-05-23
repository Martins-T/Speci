package lv.it20071.speci.pages

import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.selection.toggleable
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.twotone.ChatBubbleOutline
import androidx.compose.material3.*
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.MenuAnchorType
import androidx.compose.runtime.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.currentBackStackEntryAsState
import lv.it20071.speci.AuthenticatedPageContent
import lv.it20071.speci.models.Order
import lv.it20071.speci.models.OrderStatus
import lv.it20071.speci.models.SkillCategories
import lv.it20071.speci.models.toParcelable
import lv.it20071.speci.viewModels.AuthState
import lv.it20071.speci.viewModels.AuthViewModel
import lv.it20071.speci.viewModels.OffersViewModel
import lv.it20071.speci.viewModels.OrdersViewModel


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OrdersPage(
    modifier: Modifier = Modifier,
    navController: NavHostController,
    authViewModel: AuthViewModel
) {
    val ordersViewModel: OrdersViewModel = viewModel()
    val offersViewModel: OffersViewModel = viewModel()
    val orders by ordersViewModel.orders.collectAsState()
    val userOffers by offersViewModel.userOffers.collectAsState()
    val isLoading by ordersViewModel.isLoading.collectAsState()
    val authState by authViewModel.authState.observeAsState()
    val currentRoute = navController.currentBackStackEntryAsState().value?.destination?.route
    val currentUserId = (authState as? AuthState.Authenticated)?.userId ?: ""

    val allCategories = listOf("Visas") + SkillCategories.categories.keys
    var selectedCategory by remember { mutableStateOf("Visas") }
    var categoryExpanded by remember { mutableStateOf(false) }

    val subcatsForCategory = remember(selectedCategory) {
        SkillCategories.categories[selectedCategory]?.let { listOf("Visas") + it } ?: emptyList()
    }
    var selectedSubcategory by remember { mutableStateOf("Visas") }
    var subcategoryExpanded by remember { mutableStateOf(false) }
    var showMineOrOffered by remember { mutableStateOf(false) }

    val offeredOrderIds = remember(userOffers) {
        userOffers.map { it.orderId }.toSet()
    }

    val displayedOrders = orders.filter { order ->
        val catMatch = selectedCategory == "Visas" || order.category == selectedCategory
        val subMatch = selectedSubcategory == "Visas" ||
                (order.subcategory.isNotBlank() && order.subcategory == selectedSubcategory)

        val mineOrOfferedMatch = if (showMineOrOffered) {
            order.createdBy == currentUserId || offeredOrderIds.contains(order.orderId)
        } else true

        catMatch && subMatch && mineOrOfferedMatch
    }

    LaunchedEffect(authState) {
        if (authState is AuthState.Unauthenticated) {
            navController.navigate("login") {
                popUpTo("orders") { inclusive = true }
            }
        }
    }

    LaunchedEffect(currentUserId) {
        if (currentUserId.isNotBlank()) {
            offersViewModel.fetchOffersByUser(currentUserId)
        }
    }

    LaunchedEffect(orders) {
        orders.forEach {
            Log.d(
                "ORDERS_DEBUG",
                "orderId=${it.orderId}, createdBy='${it.createdBy}', currentUserId='$currentUserId'"
            )
        }
    }

    AuthenticatedPageContent(navController, authViewModel, currentRoute) { innerPadding ->
        Scaffold(
            floatingActionButton = {
                Column(
                    horizontalAlignment = Alignment.End
                ) {
                    FloatingActionButton(
                        onClick = { /* TODO: atvērt AI čatu */ },
                        modifier = Modifier.size(48.dp)
                    ) {
                        Icon(
                            imageVector = Icons.TwoTone.ChatBubbleOutline,
                            contentDescription = "AI čats"
                        )
                    }
                    Spacer(Modifier.height(8.dp))
                    ExtendedFloatingActionButton(
                        onClick = { navController.navigate("create_order") },
                        icon = { Icon(Icons.Default.Add, contentDescription = null) },
                        text = { Text("Izveidot pasūtījumu") }
                    )
                }
            },
            modifier = modifier.padding(innerPadding)
        ) { scaffoldPadding ->
            Column(
                Modifier
                    .fillMaxSize()
                    .padding(scaffoldPadding)
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                verticalArrangement = Arrangement.Top,
                horizontalAlignment = Alignment.Start
            ) {
                Text(
                    text = "Pasūtījumi",
                    fontSize = 32.sp,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp),
                    textAlign = TextAlign.Center
                )

                // Filtri
                Row(
                    Modifier
                        .fillMaxWidth()
                        .padding(bottom = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    ExposedDropdownMenuBox(
                        expanded = categoryExpanded,
                        onExpandedChange = { categoryExpanded = !categoryExpanded }
                    ) {
                        TextField(
                            value = selectedCategory,
                            onValueChange = {},
                            readOnly = true,
                            label = { Text("Filtrēt kategoriju") },
                            trailingIcon = {
                                ExposedDropdownMenuDefaults.TrailingIcon(expanded = categoryExpanded)
                            },
                            modifier = Modifier
                                .width(250.dp)
                                .menuAnchor(MenuAnchorType.PrimaryNotEditable)
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

                    Spacer(Modifier.width(8.dp))

                    MyOrdersToggleButton(
                        showMineOrOffered = showMineOrOffered,
                        onValueChange = { showMineOrOffered = it }
                    )

                }

                if (selectedCategory != "Visas") {
                    ExposedDropdownMenuBox(
                        expanded = subcategoryExpanded,
                        onExpandedChange = { subcategoryExpanded = !subcategoryExpanded }
                    ) {
                        TextField(
                            value = selectedSubcategory,
                            onValueChange = {},
                            readOnly = true,
                            label = { Text("Filtrēt pēc apakškategorijas") },
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

                when {
                    isLoading -> {
                        CircularProgressIndicator(Modifier.align(Alignment.CenterHorizontally))
                    }

                    displayedOrders.isEmpty() -> {
                        Text(
                            "Nav neviena pasūtījuma.",
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 16.dp),
                            textAlign = TextAlign.Center
                        )
                    }

                    else -> {
                        LazyColumn(
                            modifier = Modifier.fillMaxSize(),
                            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp),
                            verticalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            items(displayedOrders) { order ->
                                OrderCard(
                                    order = order,
                                    onClick = {
                                        navController.currentBackStackEntry
                                            ?.savedStateHandle
                                            ?.set("selectedOrder", order.toParcelable())
                                        navController.navigate("order_details/${order.orderId}")
                                    },
                                    modifier = Modifier.fillMaxWidth()
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun OrderCard(
    order: Order,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp, vertical = 4.dp)
            .clickable { onClick() },
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(IntrinsicSize.Min)
        ) {
            Box(
                modifier = Modifier
                    .size(100.dp)
                    .background(MaterialTheme.colorScheme.onSurface.copy(alpha = 0.05f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Image,
                    contentDescription = "Placeholder",
                    modifier = Modifier.size(48.dp),
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight()
                    .padding(horizontal = 12.dp, vertical = 12.dp),
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = order.subcategory.takeIf { it.isNotBlank() }
                        ?: order.category.takeIf { it.isNotBlank() }
                        ?: "Bez kategorijas",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.primary
                )

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = order.task,
                    style = MaterialTheme.typography.bodyMedium
                )

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = "Adrese: ${order.location}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Termiņš",
                            style = MaterialTheme.typography.labelSmall
                        )
                        Text(
                            text = order.dueDate,
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .padding(end = 8.dp),
                        horizontalAlignment = Alignment.End
                    ) {
                        Text(
                            text = "Budžets",
                            style = MaterialTheme.typography.labelSmall
                        )
                        Text(
                            text = "€${"%.2f".format(order.budget)}",
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                }

                Spacer(modifier = Modifier.height(4.dp))

                val statusDisplay = OrderStatus.valueOf(order.status).displayName

                Text(
                    text = "Statuss: $statusDisplay",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.primary
                )
            }
        }
    }
}

@Composable
fun MyOrdersToggleButton(
    showMineOrOffered: Boolean,
    onValueChange: (Boolean) -> Unit
) {
    val indication = ripple()
    val interactionSource = remember { MutableInteractionSource() }

    Box(
        modifier = Modifier
            .toggleable(
                value = showMineOrOffered,
                onValueChange = onValueChange,
                role = Role.Button,
                interactionSource = interactionSource,
                indication = indication
            )
            .clip(RoundedCornerShape(12.dp))
            .background(
                if (showMineOrOffered)
                    MaterialTheme.colorScheme.primary.copy(alpha = 0.12f)
                else
                    MaterialTheme.colorScheme.surfaceVariant
            )
            .border(
                width = 1.dp,
                color = MaterialTheme.colorScheme.primary,
                shape = RoundedCornerShape(12.dp)
            )
            .padding(horizontal = 12.dp, vertical = 8.dp)
    ) {
        Text(
            text = "Mani pasūtījumi un piedāvājumi",
            style = MaterialTheme.typography.labelSmall,
            fontWeight = if (showMineOrOffered) FontWeight.Bold else FontWeight.Normal,
            color = if (showMineOrOffered)
                MaterialTheme.colorScheme.primary
            else
                MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

