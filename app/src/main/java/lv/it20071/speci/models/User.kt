package lv.it20071.speci.models

data class User(
    val id: String = "",
    val email: String = "",
    val firstName: String = "",
    val lastName: String = "",
    val homeAddress: String = "",
    val workAddress: String = "",
    val phoneNumber: String = "",
    val description: String = "",
    val skills: List<String> = emptyList(),
    val isSpecialist: Boolean = false,
    val ratingAsClient: Double = 0.0,
    val ratingAsSpecialist: Double = 0.0
) {
    val fullName get() = "$firstName $lastName"
}
