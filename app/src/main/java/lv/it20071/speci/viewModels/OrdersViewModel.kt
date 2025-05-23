package lv.it20071.speci.viewModels

import android.util.Log
import androidx.lifecycle.ViewModel
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import lv.it20071.speci.models.Order

class OrdersViewModel : ViewModel() {

    private val db = Firebase.firestore

    private val _orders = MutableStateFlow<List<Order>>(emptyList())
    val orders: StateFlow<List<Order>> = _orders.asStateFlow()

    private val _isLoading = MutableStateFlow(true)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    init {
        fetchOrders()
    }

    private fun fetchOrders() {
        _isLoading.value = true

        db.collection("orders")
            .addSnapshotListener { snapshot, e ->
                if (e != null) {
                    Log.w("Firestore", "Listen failed.", e)
                    _isLoading.value = false
                    return@addSnapshotListener
                }

                val orders = snapshot?.documents?.mapNotNull { doc ->
                    val order = doc.toObject(Order::class.java)
                    order?.copy(orderId = doc.id)
                } ?: emptyList()

                _orders.value = orders
                _isLoading.value = false
            }
    }
}
