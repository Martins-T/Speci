package lv.it20071.speci.dialogs

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import lv.it20071.speci.models.Review
import lv.it20071.speci.viewModels.ReviewsViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReviewDialog(
    open: Boolean,
    fromUserId: String,
    toUserId: String,
    reviewsViewModel: ReviewsViewModel,
    onDismiss: () -> Unit,
    onReviewSubmitted: () -> Unit
) {
    if (open) {
        // Fullscreen Dialog
        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text("Pievienot atsauksmi") },
                    navigationIcon = {
                        IconButton(onClick = onDismiss) {
                            Icon(Icons.Default.Close, contentDescription = "Aizvērt")
                        }
                    }
                )
            }
        ) { innerPadding ->

            var rating by remember { mutableStateOf(0f) }
            var comment by remember { mutableStateOf("") }
            var reviewType by remember { mutableStateOf("specialist") }

            Column(
                modifier = Modifier
                    .padding(innerPadding)
                    .fillMaxSize()
                    .padding(16.dp),
                verticalArrangement = Arrangement.Top
            ) {
                Text("Vērtējums (1–5):")
                Slider(
                    value = rating,
                    onValueChange = { rating = it },
                    valueRange = 1f..5f,
                    steps = 3
                )
                Text("Komentārs:")
                OutlinedTextField(
                    value = comment,
                    onValueChange = { comment = it },
                    modifier = Modifier.fillMaxWidth(),
                    placeholder = { Text("Ieraksti komentāru...") }
                )
                Spacer(modifier = Modifier.height(8.dp))

                Text("Atsauksmes veids:")
                Row {
                    RadioButton(
                        selected = reviewType == "specialist",
                        onClick = { reviewType = "specialist" }
                    )
                    Text("Speciālists")
                    Spacer(modifier = Modifier.width(16.dp))
                    RadioButton(
                        selected = reviewType == "client",
                        onClick = { reviewType = "client" }
                    )
                    Text("Klients")
                }

                Spacer(modifier = Modifier.height(16.dp))

                Button(
                    onClick = {
                        val review = Review(
                            fromUserId = fromUserId,
                            toUserId = toUserId,
                            rating = rating,
                            comment = comment,
                            type = reviewType
                        )
                        reviewsViewModel.addReview(review) {
                            onReviewSubmitted()
                            onDismiss()
                        }
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Nosūtīt atsauksmi")
                }
            }
        }
    }
}
