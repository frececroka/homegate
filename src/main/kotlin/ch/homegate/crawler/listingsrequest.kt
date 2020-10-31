package ch.homegate.crawler

val listingsRequest = ListingsRequest(
        query = ListingsQuery(
                location = Location(listOf("geo-city-zurich")),
                monthlyRent = IntRange(to = 5000),
                numberOfRooms = IntRange(from = 4),
                offerType = "RENT",
        )
)
