package ch.homegate.crawler

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

data class Location(
        val geoTags: List<String>,
)

data class IntRange(
        val from: Int? = null,
        val to: Int? = null,
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

data class LocalizationTemplate(
        val de: SingleLocalizationTemplate = SingleLocalizationTemplate(),
        val primary: Boolean = true,
)

data class SingleLocalizationTemplate(
        val text: Boolean = true,
        val urls: Boolean = true,
        val attachments: AttachmentsTemplate = AttachmentsTemplate(),
)

data class AttachmentsTemplate(
        val type: Boolean = true,
        val url: Boolean = true,
)

data class ListingsResponse(
        val from: Int,
        val size: Int,
        val total: Int,
        val results: List<ListingResponse>,
)

data class ListingResponse(
        val id: String,
        val listing: Listing,
) {
    val url: String get() = "https://www.homegate.ch/rent/$id"
}

data class Listing(
        val address: Address,
        val characteristics: Characteristics,
        val localization: Localizations,
        val prices: Prices,
)

data class Address(
        val country: String? = null,
        val locality: String? = null,
        val postalCode: String? = null,
        val region: String? = null,
        val street: String? = null,
) {
    override fun toString(): String {
        return "$street, $postalCode $locality"
    }
}

data class Characteristics(
        val numberOfRooms: Double? = null,
        val totalFloorSpace: Int? = null,
        val floor: Int? = null,
) {
    override fun toString(): String {
        return listOfNotNull(
                numberOfRooms?.let { "$it rooms" },
                totalFloorSpace?.let { "$it sqm" },
                floor?.let { "${it + 1}-th floor" },
        ).joinToString(", ")
    }
}

data class Localizations(
        val de: Localization,
        val primary: String,
)

data class Localization(
        val attachments: List<Attachment>,
        val text: LocalizationText,
)

data class Attachment(
        val type: String,
        val url: String,
)

data class LocalizationText(
        val title: String,
        val description: String,
)

data class Prices(
        val rent: Price,
)

data class Price(
        val gross: Int,
)

data class PubSubMessage(
        val data: String? = null,
        val attributes: Map<String, String>? = null,
        val messageId: String? = null,
        val publishTime: String? = null,
)
