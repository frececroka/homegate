package ch.homegate.client.http

data class LocalizationTemplate(
    val de: SingleLocalizationTemplate = SingleLocalizationTemplate(),
    val primary: Boolean = true,
)

data class SingleLocalizationTemplate(
    val text: Boolean = true,
    val urls: Boolean = true,
    val attachments: AttachmentTemplate = AttachmentTemplate(),
)
