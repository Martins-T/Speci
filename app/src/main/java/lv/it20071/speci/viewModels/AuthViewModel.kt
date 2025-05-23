package lv.it20071.speci.viewModels

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import lv.it20071.speci.models.User

sealed class AuthState {
    object Idle : AuthState()
    object Loading : AuthState()
    data class Authenticated(val userId: String, val email: String?) : AuthState()
    object Unauthenticated : AuthState()
    data class Error(val message: String) : AuthState()
}

class AuthViewModel : ViewModel() {
    private val auth = FirebaseAuth.getInstance()
    private val firestore = FirebaseFirestore.getInstance()
    private val usersCollection = firestore.collection("users")

    private val _authState = MutableLiveData<AuthState>(AuthState.Idle)
    val authState: LiveData<AuthState> = _authState

    val currentUser: FirebaseUser?
        get() = auth.currentUser

    private val authListener = FirebaseAuth.AuthStateListener { firebaseAuth ->
        val user = firebaseAuth.currentUser
        _authState.value = if (user != null) {
            AuthState.Authenticated(user.uid, user.email)
        } else {
            AuthState.Unauthenticated
        }
    }

    init {
        auth.addAuthStateListener(authListener)
        _authState.value = auth.currentUser?.let { user ->
            AuthState.Authenticated(user.uid, user.email)
        } ?: AuthState.Unauthenticated

        auth.currentUser?.getIdToken(true)?.addOnFailureListener {
            _authState.postValue(AuthState.Unauthenticated)
        }
    }

    fun login(email: String, password: String) {
        _authState.value = AuthState.Loading
        auth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val user = task.result?.user
                    _authState.value = if (user != null) {
                        AuthState.Authenticated(user.uid, user.email)
                    } else {
                        AuthState.Unauthenticated
                    }
                } else {
                    _authState.value = AuthState.Error(task.exception?.message ?: "Nezināma kļūda")
                }
            }
    }

    fun signup(email: String, password: String) {
        _authState.value = AuthState.Loading
        auth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val user = auth.currentUser
                    _authState.value = AuthState.Authenticated(user?.uid ?: "", user?.email)
                } else {
                    _authState.value = AuthState.Error(task.exception?.message ?: "Nezināma kļūda")
                }
            }
    }

    fun saveUserToFirestore(userId: String, email: String) {
        val user = User(email = email)
        usersCollection.document(userId).set(user, SetOptions.merge())
            .addOnSuccessListener {
                Log.d("AuthViewModel", "User data saved to Firestore for userId: $userId")
            }
            .addOnFailureListener { e ->
                Log.e("AuthViewModel", "Error saving user data to Firestore for userId: $userId", e)
            }
    }

    fun changePassword(
        newPassword: String,
        onSuccess: () -> Unit,
        onFailure: (Exception) -> Unit
    ) {
        val user = auth.currentUser
        if (user == null) {
            onFailure(Exception("Lietotājs nav autentificēts"))
            return
        }
        user.updatePassword(newPassword)
            .addOnSuccessListener { onSuccess() }
            .addOnFailureListener { e -> onFailure(e) }
    }

    fun signOut() {
        auth.signOut()
    }

    override fun onCleared() {
        super.onCleared()
        auth.removeAuthStateListener(authListener)
    }
}
