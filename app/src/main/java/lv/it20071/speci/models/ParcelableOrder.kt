package lv.it20071.speci.models

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class ParcelableOrder(
    val orderId: String = "",
    val createdBy: String = "",
    val task: String = "",
    val location: String = "",
    val dueDate: String = "",
    val budget: Double = 0.0,
    val category: String = "",
    val subcategory: String = "",
    val status: String = "open",
    val timestamp: Long = 0L
) : Parcelable

