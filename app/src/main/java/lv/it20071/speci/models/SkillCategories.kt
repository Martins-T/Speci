package lv.it20071.speci.models

object SkillCategories {

    val categories: Map<String, List<String>> = mapOf(
        "Celtniecības darbi" to listOf(
            "Fasādes darbi",
            "Demontāžas darbi",
            "Jumta darbi",
            "Iekšdarbi",
            "Apmetums",
            "Grīdu ieklāšana"
        ),
        "Skolotājs" to listOf(
            "Matemātika",
            "Latviešu valoda",
            "Angļu valoda",
            "Fizika",
            "Ķīmija",
            "Bioloģija"
        ),
        "Skaistumkopšana" to listOf(
            "Frizieris",
            "Kosmetologs",
            "Manikīrs",
            "Masāžas"
        ),
        "Auto remonts" to listOf(
            "Ritošā daļa",
            "Dzinēja remonts",
            "Elektronika",
            "Krāsošana"
        ),
        "IT pakalpojumi" to listOf(
            "Datoru remonts",
            "Programmēšana",
            "Tīkla konfigurācija",
            "UI/UX dizains"
        )
    )

    val allCategories: List<String>
        get() = categories.keys.toList()

    val allSubcategories: List<String>
        get() = categories.values.flatten()
}
