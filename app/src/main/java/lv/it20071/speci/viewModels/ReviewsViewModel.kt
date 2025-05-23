package lv.it20071.speci.viewModels

import android.util.Log
import androidx.lifecycle.ViewModel
import com.google.firebase.Timestamp
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import lv.it20071.speci.models.Review

class ReviewsViewModel : ViewModel() {

    private val db = Firebase.firestore

    private val _reviews = MutableStateFlow<List<Review>>(emptyList())
    val reviews: StateFlow<List<Review>> = _reviews.asStateFlow()

    fun addReview(review: Review, onSuccess: () -> Unit = {}, onFailure: (Exception) -> Unit = {}) {
        val reviewWithTimestamp = review.copy(timestamp = Timestamp.now())
        db.collection("reviews")
            .add(reviewWithTimestamp)
            .addOnSuccessListener { onSuccess() }
            .addOnFailureListener { e ->
                Log.e("Firestore", "Error adding review", e)
                onFailure(e)
            }
    }

    fun getReviewsForUser(userId: String) {
        db.collection("reviews")
            .whereEqualTo("toUserId", userId)
            .addSnapshotListener { snapshot, e ->
                if (e != null) {
                    Log.e("Firestore", "Error fetching reviews", e)
                    return@addSnapshotListener
                }

                val fetchedReviews = snapshot?.documents?.mapNotNull {
                    it.toObject(Review::class.java)
                } ?: emptyList()

                _reviews.value = fetchedReviews
            }
    }
}
