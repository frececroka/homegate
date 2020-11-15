package ch.homegate.client.http

data class AddressTemplate(
    val country: Boolean = true,
    val geoCoordinates: Boolean = true,
    val geoHierarchy: Boolean = true,
    val geoTags: Boolean = true,
    val locality: Boolean = true,
    val postalCode: Boolean = true,
    val postOfficeBoxNumber: Boolean = true,
    val region: Boolean = true,
    val street: Boolean = true,
    val streetAddition: Boolean = true,
    val streetNumber: Boolean = true,
)
