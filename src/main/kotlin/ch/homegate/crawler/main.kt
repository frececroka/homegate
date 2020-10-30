package ch.homegate.crawler

import io.ktor.util.*
import kotlinx.coroutines.runBlocking

@KtorExperimentalAPI
fun main(): Unit = runBlocking {
    val notifier = HomegateNotifier(HomegateClient(), LocalListingsRecorder())
    notifier.notify(listingsRequest)
}

val listingsRequest = ListingsRequest(
        query = ListingsQuery(
                location = Location(listOf("geo-city-zurich")),
                monthlyRent = IntRange(to = 5000),
                numberOfRooms = IntRange(from = 4),
                offerType = "RENT",
        )
)
