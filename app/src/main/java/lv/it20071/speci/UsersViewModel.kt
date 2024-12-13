package lv.it20071.speci

import androidx.lifecycle.ViewModel
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import lv.it20071.speci.pages.Order

class UsersViewModel : ViewModel() {
    private val database = FirebaseDatabase.getInstance("https://speci-7eb3e-default-rtdb.europe-west1.firebasedatabase.app")
    private val _users = MutableStateFlow<List<Order>>(emptyList())
    val users: StateFlow<List<Order>> = _users.asStateFlow()

    init {
        fetchOrders()
    }

    private fun fetchOrders() {
        val ordersRef = database.getReference("tasks")
        ordersRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val fetchedOrders = snapshot.children.mapNotNull { it.getValue(Order::class.java) }
                _users.value = fetchedOrders
            }

            override fun onCancelled(error: DatabaseError) {
                println("Error fetching data: ${error.message}")
            }
        })
    }
}
