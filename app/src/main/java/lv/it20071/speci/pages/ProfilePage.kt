package lv.it20071.speci.pages

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import androidx.navigation.compose.currentBackStackEntryAsState
import lv.it20071.speci.AuthViewModel
import lv.it20071.speci.AuthenticatedPageContent


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfilePage(
    modifier: Modifier = Modifier,
    navController: NavHostController,
    authViewModel: AuthViewModel
) {
    val currentRoute = navController.currentBackStackEntryAsState().value?.destination?.route // Get current route

    AuthenticatedPageContent(navController, authViewModel, currentRoute) { innerPadding -> // Pass currentRoute
        Column(
            modifier = modifier
                .fillMaxSize()
                .padding(innerPadding),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(text = "Mans profils lapa", fontSize = 32.sp)

            TextButton(onClick = { authViewModel.signout() }) { Text(text = "Iziet") }
        }
    }
}