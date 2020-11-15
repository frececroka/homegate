package ch.homegate.client.http

import ch.homegate.client.data.*
import ch.homegate.client.data.IntRange

data class ListingsRequest(
    val from: Int = 0,
    val size: Int = 20,
    val query: ListingsQuery,
    val sortBy: String = "dateCreated",
    val sortDirection: String = "desc",
    val resultTemplate: ListingTemplate = ListingTemplate(),
)

data class ListingsQuery(
    val location: Location,
    val monthlyRent: IntRange,
    val numberOfRooms: IntRange,
    val offerType: String,
)

data class ListingTemplate(
    val id: Boolean = true,
    val listing: ListingDetailsTemplate = ListingDetailsTemplate(),
)

data class ListingDetailsTemplate(
    val address: AddressTemplate = AddressTemplate(),
    val categories: Boolean = true,
    val characteristics: Boolean = true,
    val localization: LocalizationTemplate = LocalizationTemplate(),
    val offerType: Boolean = true,
    val prices: Boolean = true,
)

data class ListingResponse(
    val id: String,
    val listing: Listing,
) {
    val url: String get() = "https://www.homegate.ch/rent/$id"
}
