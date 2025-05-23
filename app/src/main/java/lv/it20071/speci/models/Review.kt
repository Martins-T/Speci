package lv.it20071.speci.models

import com.google.firebase.Timestamp

data class Review(
    val fromUserId: String = "",
    val toUserId: String = "",
    val rating: Float = 0f,
    val comment: String = "",
    val type: String = "", // "client" vai "specialist"
    val timestamp: Timestamp = Timestamp.now()
)
