package lv.it20071.speci.models

enum class OrderStatus(val displayName: String) {
    OPEN("Atvērts"),
    HAS_OFFERS("Saņemti piedāvājumi"),
    ACCEPTED("Speciālists izvēlēts"),
    IN_PROGRESS("Darbs procesā"),
    COMPLETED("Izpildīts"),
    CANCELED("Atcelts")
}
