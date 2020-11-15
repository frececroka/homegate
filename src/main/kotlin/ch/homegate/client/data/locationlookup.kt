package ch.homegate.client.data

data class LocationLookup(
    val geoLocation: GeoLocation
)

data class GeoLocation(
    val type: String,
    val id: String,
    val names: LocationNames,
)

data class LocationNames(
    val de: List<String>,
    val fr: List<String>,
    val it: List<String>,
    val en: List<String>,
)
