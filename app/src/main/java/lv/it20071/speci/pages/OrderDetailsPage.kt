package lv.it20071.speci.pages

import android.content.Context
import android.location.Geocoder
import android.os.Bundle
import android.util.Log
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.navigation.NavHostController
import androidx.navigation.compose.currentBackStackEntryAsState
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.MapView
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import lv.it20071.speci.MyBottomNavigation
import java.util.Locale

@Composable
fun OrderDetailsPage(
    modifier: Modifier = Modifier,
    navController: NavHostController,
    personName: String,
    rating: Float,
    task: String,
    location: String,
    dueDate: String,
    currentPrice: Double
) {
    val currentRoute = navController.currentBackStackEntryAsState().value?.destination?.route
    val context = LocalContext.current
    var mapView by remember { mutableStateOf<MapView?>(null) }
    var locationLatLng by remember { mutableStateOf<LatLng?>(null) }

    DisposableEffect(Unit) {
        mapView = MapView(context).apply {
            onCreate(Bundle())
        }
        onDispose {
            mapView?.onDestroy()
        }
    }

    locationLatLng = getLocationFromAddress(context, location)

    Scaffold(
        topBar = {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp),
                horizontalArrangement = Arrangement.Start
            ) {
                IconButton(onClick = { navController.popBackStack() }) {
                    Icon(
                        imageVector = Icons.Filled.ArrowBack,
                        contentDescription = "Go Back"
                    )
                }
            }
        },
        bottomBar = {
            MyBottomNavigation(
                navController = navController,
                currentRoute = currentRoute
            )
        }
    ) { innerPadding ->
        Column(
            modifier = modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            Text(
                text = "Pasūtījuma detaļas",
                fontSize = 32.sp,
                modifier = Modifier.padding(16.dp)
            )

            Spacer(modifier = Modifier.height(16.dp))

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                Text(text = "Pasūtītājs: $personName", fontSize = 18.sp)
                Text(text = "Pasūtītāja vērtējums: $rating", fontSize = 16.sp)
                Text(text = "Uzdevums: $task", fontSize = 16.sp)
                Text(text = "Vieta: $location", fontSize = 16.sp)
                Text(text = "Termiņš: $dueDate", fontSize = 16.sp)
                Text(text = "Budžets: €$currentPrice", fontSize = 16.sp)
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Google Map Section
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(300.dp) // Fixed height for the map
                    .padding(horizontal = 16.dp)
            ) {
                mapView?.let { map ->
                    AndroidView(factory = { map }) { mapView ->
                        mapView.getMapAsync(OnMapReadyCallback { googleMap ->
                            locationLatLng?.let { latLng ->
                                val markerOptions = MarkerOptions().position(latLng).title(personName)
                                googleMap.addMarker(markerOptions)
                                googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, 12f))
                            }
                        })
                    }
                }
            }
        }
    }
}

fun getLocationFromAddress(context: Context, address: String): LatLng? {
    return try {
        val geocoder = Geocoder(context, Locale.getDefault())
        val addresses = geocoder.getFromLocationName(address, 1)

        if (!addresses.isNullOrEmpty()) {
            val location = addresses[0]
            LatLng(location.latitude, location.longitude)
        } else {
            Log.e("Geocoding", "Address not found for: $address")
            null
        }
    } catch (e: Exception) {
        Log.e("Geocoding", "Failed to get location from address", e)
        null
    }
}
