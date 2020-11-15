package ch.homegate.client.data

data class Listing(
    val address: Address,
    val characteristics: Characteristics,
    val localization: Localizations,
    val prices: Prices,
)
