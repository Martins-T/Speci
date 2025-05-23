package lv.it20071.speci.viewModels

import androidx.lifecycle.ViewModel
import com.google.firebase.Timestamp
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import lv.it20071.speci.models.Offer
import lv.it20071.speci.models.OrderStatus

class OffersViewModel : ViewModel() {
    private val db = Firebase.firestore

    private val _offers = MutableStateFlow<List<Offer>>(emptyList())
    val offers: StateFlow<List<Offer>> = _offers.asStateFlow()
    private var listener: ListenerRegistration? = null

    private val _userOffers = MutableStateFlow<List<Offer>>(emptyList())
    val userOffers: StateFlow<List<Offer>> = _userOffers.asStateFlow()
    private var userListener: ListenerRegistration? = null

    private val _orderOffers = MutableStateFlow<List<Offer>>(emptyList())
    val orderOffers: StateFlow<List<Offer>> = _orderOffers

    fun fetchOffersForOrder(orderId: String) {
        db.collection("offers")
            .whereEqualTo("orderId", orderId)
            .addSnapshotListener { snap, err ->
                if (err != null) {
                    return@addSnapshotListener
                }
                val list = snap?.documents
                    ?.mapNotNull { it.toObject(Offer::class.java)?.copy(offerId = it.id) }
                    ?: emptyList()
                _orderOffers.value = list
            }
    }

    fun fetchOffersByUser(specialistId: String) {
        userListener?.remove()
        userListener = db.collection("offers")
            .whereEqualTo("specialistId", specialistId)
            .addSnapshotListener { snap, e ->
                if (e != null) return@addSnapshotListener
                val list = snap
                    ?.documents
                    ?.mapNotNull { it.toObject(Offer::class.java)?.copy(offerId = it.id) }
                    ?: emptyList()
                _userOffers.value = list
            }
    }

    fun sendOffer(
        orderId: String,
        specialistId: String,
        minPrice: Double,
        maxPrice: Double,
        message: String,
        onSuccess: () -> Unit,
        onFailure: (Throwable) -> Unit
    ) {
        val existing = _orderOffers.value.find { it.specialistId == specialistId }
        if (existing != null) {
            updateOffer(
                offerId = existing.offerId,
                minPrice = minPrice,
                maxPrice = maxPrice,
                message = message,
                onSuccess = onSuccess,
                onFailure = onFailure
            )
        } else {
            val docRef = db.collection("offers").document()
            val newOffer = Offer(
                offerId = docRef.id,
                orderId = orderId,
                specialistId = specialistId,
                minPrice = minPrice,
                maxPrice = maxPrice,
                message = message,
                timestamp = Timestamp.now()
            )
            docRef
                .set(newOffer)
                .addOnSuccessListener {
                    onSuccess()
                    db.collection("orders")
                        .document(orderId)
                        .update("status", OrderStatus.HAS_OFFERS.name)
                }
                .addOnFailureListener { onFailure(it) }
        }
    }

    fun updateOffer(
        offerId: String,
        minPrice: Double,
        maxPrice: Double,
        message: String,
        onSuccess: () -> Unit,
        onFailure: (Throwable) -> Unit
    ) {
        db.collection("offers").document(offerId)
            .update(
                mapOf(
                    "minPrice" to minPrice,
                    "maxPrice" to maxPrice,
                    "message" to message,
                    "timestamp" to Timestamp.now()
                )
            )
            .addOnSuccessListener { onSuccess() }
            .addOnFailureListener { onFailure(it) }
    }

    override fun onCleared() {
        super.onCleared()
        listener?.remove()
        userListener?.remove()
    }
}
