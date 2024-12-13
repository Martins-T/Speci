package lv.it20071.speci

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.ViewModel
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import lv.it20071.speci.ui.theme.SpeciTheme
import androidx.compose.foundation.lazy.items
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase


class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Firebase.database.setPersistenceEnabled(true)
        enableEdgeToEdge()
        val authViewModel : AuthViewModel by viewModels()
        setContent {
            SpeciTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    MyAppNavigation(modifier =  Modifier.padding(innerPadding),authViewModel = authViewModel)
                }
            }
        }
    }
}



////// DATABASE ////
//class MainActivity : ComponentActivity() {
//    override fun onCreate(savedInstanceState: Bundle?) {
//        super.onCreate(savedInstanceState)
//        enableEdgeToEdge()
//        setContent {
//            SpeciTheme {
//                val viewModel by viewModels<UsersViewModel>()
//                Scaffold { innerPadding ->
//                    LazyColumn(
//                        contentPadding = innerPadding
//                    ) {
//                        items(viewModel.users) { specialists ->
//                            Text(
//                                text = "ID: ${specialists.id}  Username: ${specialists.username}  Rating: ${specialists.rating}",
//                                fontSize = 20.sp,  // Adjust font size as needed
//                                fontWeight = FontWeight.Bold,
//                                modifier = Modifier.padding(8.dp)
//                            )
//                        }
//                    }
//                }
//            }
//        }
//    }
//}
//
//data class User(
//    val id: Int = 0,
//    val username: String = "",
//    val rating: Int = 0
//)
//
//class UsersViewModel: ViewModel() {
//
//    private val database = FirebaseDatabase.getInstance("https://speci-7eb3e-default-rtdb.europe-west1.firebasedatabase.app")
//
//    val users = mutableStateListOf<User>()
//
//    init {
//        getUsers()
//    }
//
//    fun getUsers() {
//        // Fetch users from Firebase Realtime Database
//        database.getReference("tasks")
//            .addValueEventListener(object : ValueEventListener {
//                override fun onDataChange(snapshot: DataSnapshot) {
//                    // Clear current list and add updated users
//                    users.clear()
//                    snapshot.children.forEach { childSnapshot ->
//                        val user = childSnapshot.getValue(User::class.java)
//                        if (user != null) {
//                            users.add(user)
//                        }
//                    }
//                }
//
//                override fun onCancelled(error: DatabaseError) {
//                    // Handle possible errors
//                    Log.e("UsersViewModel", "Error fetching data", error.toException())
//                }
//            })
//    }
//}

