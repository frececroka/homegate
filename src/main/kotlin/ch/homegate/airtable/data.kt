package ch.homegate.airtable

data class Record<T>(val id: String, val fields: T)
data class NewRecord<T>(val fields: T)

enum class State {
    Initial,
    Contacted,
    Viewing,
    Applied,
    Rejected
}

data class AirtableListing(
    val address: String,
    val zip: String?,
    val gross_rent: Int,
    val floor_space: Int?,
    val state: String,
    val url: String,
)

data class AirtableListingPatch(
    val state: String? = null,
)
