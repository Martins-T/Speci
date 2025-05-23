package lv.it20071.speci.viewModels

import androidx.lifecycle.ViewModel
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import lv.it20071.speci.models.User

class UsersViewModel : ViewModel() {
    private val db = Firebase.firestore
    private val _users = MutableStateFlow<Map<String, User>>(emptyMap())
    val users: StateFlow<Map<String, User>> = _users.asStateFlow()

    fun fetchUser(userId: String) {
        if (_users.value.containsKey(userId)) return
        db.collection("users").document(userId).get()
            .addOnSuccessListener { doc ->
                doc.toObject(User::class.java)?.let { user ->
                    _users.value = _users.value + (userId to user)
                }
            }
    }
}
