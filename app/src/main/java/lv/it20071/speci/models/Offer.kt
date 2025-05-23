package lv.it20071.speci.models

import android.os.Parcelable
import com.google.firebase.Timestamp
import kotlinx.parcelize.Parcelize

@Parcelize
data class Offer(
    val offerId: String = "",
    val orderId: String = "",
    val specialistId: String = "",
    val minPrice: Double? = null,
    val maxPrice: Double? = null,
    val message: String = "",
    val timestamp: Timestamp = Timestamp.now()
) : Parcelable
