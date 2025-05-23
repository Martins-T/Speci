package lv.it20071.speci.pages

import android.content.Context
import android.location.Geocoder
import android.os.Bundle
import android.util.Log
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.MapView
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import lv.it20071.speci.MyBottomNavigation
import lv.it20071.speci.models.Offer
import lv.it20071.speci.models.Order
import lv.it20071.speci.models.OrderStatus
import lv.it20071.speci.models.User
import lv.it20071.speci.viewModels.AuthState
import lv.it20071.speci.viewModels.AuthViewModel
import lv.it20071.speci.viewModels.OffersViewModel
import lv.it20071.speci.viewModels.UsersViewModel
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OrderDetailsPage(
    modifier: Modifier = Modifier,
    navController: NavHostController,
    orderId: String,
    authViewModel: AuthViewModel
) {
    val authState by authViewModel.authState.observeAsState()
    val currentUserId = (authState as? AuthState.Authenticated)?.userId ?: ""

    val context = LocalContext.current
    val db = Firebase.firestore
    var order by remember { mutableStateOf<Order?>(null) }
    var locationLatLng by remember { mutableStateOf<LatLng?>(null) }
    var mapView by remember { mutableStateOf<MapView?>(null) }

    val offersViewModel: OffersViewModel = viewModel()
    var showOfferDialog by remember { mutableStateOf(false) }
    var minPriceText by remember { mutableStateOf("") }
    var maxPriceText by remember { mutableStateOf("") }
    var offerMessageText by remember { mutableStateOf("") }

    val usersViewModel: UsersViewModel = viewModel()
    val usersMap by usersViewModel.users.collectAsState()

    LaunchedEffect(authState) {
        if (authState is AuthState.Unauthenticated) {
            navController.navigate("login") {
                popUpTo("orders") { inclusive = true }
            }
        }
    }

    LaunchedEffect(orderId) {
        db.collection("orders")
            .document(orderId)
            .get()
            .addOnSuccessListener { doc ->
                doc.toObject(Order::class.java)?.let { loaded ->
                    order = loaded.copy(orderId = doc.id)
                }
            }
    }

    LaunchedEffect(orderId) {
        offersViewModel.fetchOffersForOrder(orderId)
    }
    val orderOffers by offersViewModel.orderOffers.collectAsState()

    val existingOffer = orderOffers.find { it.specialistId == currentUserId }

    LaunchedEffect(orderOffers) {
        orderOffers.map { it.specialistId }
            .distinct()
            .forEach { usersViewModel.fetchUser(it) }
    }

    LaunchedEffect(orderOffers) {
        Log.d("OrderDetailsPage", "Order $orderId – fetched ${orderOffers.size} offers")
        orderOffers.forEach { Log.d("OrderDetailsPage", it.toString()) }
    }

    LaunchedEffect(existingOffer) {
        existingOffer?.let {
            minPriceText = it.minPrice?.toString().orEmpty()
            maxPriceText = it.maxPrice?.toString().orEmpty()
            offerMessageText = it.message
        }
    }

    DisposableEffect(Unit) {
        mapView = MapView(context).apply { onCreate(Bundle()) }
        onDispose { mapView?.onDestroy() }
    }

    LaunchedEffect(order?.location) {
        order?.location?.let {
            locationLatLng = getLocationFromAddress(context, it)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Pasūtījuma detaļas",
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
            MyBottomNavigation(navController, currentRoute = "orders")
        }
    ) { innerPadding ->
        if (order == null) {
            Box(
                Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        } else {

            val ord = order!!
            Log.d("OrderDetailsPage", "Order: $ord")


            Column(
                modifier = modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .verticalScroll(rememberScrollState())
            ) {


                Column(Modifier.padding(horizontal = 16.dp)) {
                    Text("Kategorija: ${order!!.category}", fontSize = 16.sp)
                    Text("Apakškategorija: ${order!!.subcategory}", fontSize = 16.sp)
                    Text("Uzdevums: ${order!!.task}", fontSize = 16.sp)
                    Text("Vieta: ${order!!.location}", fontSize = 16.sp)
                    Text("Termiņš: ${order!!.dueDate}", fontSize = 16.sp)
                    Text(
                        text = "Budžets: €${"%.2f".format(order!!.budget)}",
                        fontSize = 16.sp
                    )

                    val rawStatus = order!!.status
                    val statusEnum = OrderStatus.entries
                        .firstOrNull { it.name.equals(rawStatus, ignoreCase = true) }
                        ?: OrderStatus.OPEN
                    val statusDisplay = statusEnum.displayName

                    Text(
                        text = "Statuss: $statusDisplay",
                        fontSize = 16.sp
                    )
                }
                Spacer(Modifier.height(16.dp))
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(300.dp)
                        .padding(horizontal = 16.dp)
                ) {
                    mapView?.let { mv ->
                        AndroidView(factory = { mv }) { mapV ->
                            mapV.getMapAsync(OnMapReadyCallback { gMap ->
                                locationLatLng?.let { ll ->
                                    val opts = MarkerOptions().position(ll).title(order!!.task)
                                    gMap.addMarker(opts)
                                    gMap.moveCamera(CameraUpdateFactory.newLatLngZoom(ll, 12f))
                                }
                            })
                        }
                    }
                }
                Spacer(Modifier.height(24.dp))

                if (authState is AuthState.Authenticated && order?.createdBy != currentUserId) {
                    Button(
                        onClick = { showOfferDialog = true },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp)
                    ) {
                        Text(
                            text = if (existingOffer != null) "Atjaunot piedāvājumu" else "Izteikt piedāvājumu"
                        )
                    }
                }

                Spacer(Modifier.height(16.dp))

                if (orderOffers.isNotEmpty()) {
                    Text(
                        text = "Esošie piedāvājumi",
                        style = MaterialTheme.typography.titleSmall,
                        fontSize = 16.sp,
                        modifier = Modifier.padding(horizontal = 16.dp)
                    )

                    orderOffers.forEach { offer ->
                        val user = usersMap[offer.specialistId]
                        OfferCard(
                            offer = offer,
                            user = user,
                            onClick = {
                                navController.navigate("specialist_profile/${offer.specialistId}")
                            }
                        )
                    }
                }

                if (showOfferDialog) {
                    AlertDialog(
                        onDismissRequest = { showOfferDialog = false },
                        title = { Text(if (existingOffer != null) "Atjaunot piedāvājumu" else "Izteikt piedāvājumu") },
                        text = {
                            Column {
                                OutlinedTextField(
                                    value = minPriceText,
                                    onValueChange = { minPriceText = it },
                                    label = { Text("Minimālā cena (€)") },
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                    modifier = Modifier.fillMaxWidth()
                                )
                                Spacer(Modifier.height(8.dp))
                                OutlinedTextField(
                                    value = maxPriceText,
                                    onValueChange = { maxPriceText = it },
                                    label = { Text("Maksimālā cena (€)") },
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                    modifier = Modifier.fillMaxWidth()
                                )
                                Spacer(Modifier.height(8.dp))
                                OutlinedTextField(
                                    value = offerMessageText,
                                    onValueChange = { offerMessageText = it },
                                    label = { Text("Ziņa") },
                                    modifier = Modifier.fillMaxWidth()
                                )
                            }
                        },
                        confirmButton = {
                            TextButton(onClick = {
                                val minP = minPriceText.toDoubleOrNull()
                                val maxP = maxPriceText.toDoubleOrNull()
                                if (minP == null || maxP == null || minP > maxP) {
                                    return@TextButton
                                }
                                offersViewModel.sendOffer(
                                    orderId = orderId,
                                    specialistId = currentUserId,
                                    minPrice = minP,
                                    maxPrice = maxP,
                                    message = offerMessageText,
                                    onSuccess = { showOfferDialog = false },
                                    onFailure = { /* error */ }
                                )
                            }) {
                                Text(if (existingOffer != null) "Atjaunot" else "Sūtīt")
                            }
                        },
                        dismissButton = {
                            TextButton(onClick = { showOfferDialog = false }) {
                                Text("Atcelt")
                            }
                        }
                    )
                }
            }
        }
    }
}

@Composable
fun OfferCard(
    offer: Offer,
    user: User?,
    onClick: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 4.dp)
            .clickable { onClick() },
        shape = RoundedCornerShape(8.dp),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.onSurface),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(4.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
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
                Text(
                    text = buildString {
                        append("Speciālists: ")
                        if (user != null) {
                            append(user.firstName).append(" ").append(user.lastName)
                            append(" (").append(String.format("%.1f", user.ratingAsSpecialist))
                                .append("★)")
                        } else {
                            append("…")
                        }
                    },
                    style = MaterialTheme.typography.bodyMedium
                )

                Spacer(Modifier.height(4.dp))

                Text(
                    text = "Cena: €${"%.2f".format(offer.minPrice)} – €${"%.2f".format(offer.maxPrice)}",
                    style = MaterialTheme.typography.bodyMedium
                )

                Spacer(Modifier.height(2.dp))

                Text(
                    text = offer.message,
                    style = MaterialTheme.typography.bodySmall
                )
            }
        }
    }
}

private fun getLocationFromAddress(context: Context, address: String): LatLng? {
    return try {
        val geocoder = Geocoder(context, Locale.getDefault())
        val list = geocoder.getFromLocationName(address, 1) ?: return null
        if (list.isNotEmpty()) LatLng(list[0].latitude, list[0].longitude) else null
    } catch (e: Exception) {
        Log.e("OrderDetailsPage", "Geocoding failed", e)
        null
    }
}
