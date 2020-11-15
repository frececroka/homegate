package ch.homegate.client.data

data class Localizations(
    val de: Localization,
    val primary: String,
)

data class Localization(
    val attachments: List<Attachment>,
    val text: LocalizationText,
)

data class LocalizationText(
    val title: String,
    val description: String,
)
