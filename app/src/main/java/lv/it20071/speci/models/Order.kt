package lv.it20071.speci.models

import com.google.firebase.Timestamp
import java.util.Date

data class Order(
    val orderId: String = "",
    val createdBy: String = "",
    val task: String = "",
    val location: String = "",
    val dueDate: String = "",
    val budget: Double = 0.0,
    val category: String = "",
    val subcategory: String = "",
    val status: String = "open",
    val timestamp: Timestamp = Timestamp.now()
)

fun Order.toParcelable(): ParcelableOrder = ParcelableOrder(
    orderId = orderId,
    createdBy = createdBy,
    task = task,
    location = location,
    dueDate = dueDate,
    budget = budget,
    category = category,
    subcategory = subcategory,
    status = status,
    timestamp = timestamp.toDate().time
)

fun ParcelableOrder.toOrder(): Order = Order(
    orderId = orderId,
    createdBy = createdBy,
    task = task,
    location = location,
    dueDate = dueDate,
    budget = budget,
    category = category,
    subcategory = subcategory,
    status = status,
    timestamp = Timestamp(Date(timestamp))
)
