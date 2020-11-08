package ch.homegate.crawler

import ch.homegate.client.IntRange
import ch.homegate.client.ListingsQuery
import ch.homegate.client.ListingsRequest
import ch.homegate.client.Location

val listingsRequest = ListingsRequest(
        query = ListingsQuery(
                location = Location(listOf(
                    "geo-city-kilchberg-zh",
                    "geo-city-kusnacht-zh",
                    "geo-city-ruschlikon",
                    "geo-city-zollikon",
                    "geo-city-zurich"
                )),
                monthlyRent = IntRange(from = 2500, to = 5000),
                numberOfRooms = IntRange(from = 4),
                offerType = "RENT",
        )
)
