package ch.homegate.crawler

import ch.homegate.ListingsRecorder
import ch.homegate.QueryConstraints
import ch.homegate.client.HomegateClient
import ch.homegate.client.data.IntRange
import ch.homegate.client.data.Location
import ch.homegate.client.http.ListingsQuery
import ch.homegate.client.http.ListingsRequest
import com.google.common.eventbus.EventBus
import io.ktor.util.*
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.context.annotation.Profile
import org.springframework.stereotype.Component

/**
 * Retrieves new listings from Homegate and puts them on the event bus.
 */
@Component
@Profile("local", "gcf")
@KtorExperimentalAPI
@Suppress("UnstableApiUsage")
class HomegateCrawler(
    private val homegate: HomegateClient,
    private val listingsRecorder: ListingsRecorder,
    @Qualifier("new-listing-events") private val eventBus: EventBus,
) {

    private val log = LoggerFactory.getLogger(javaClass)

    suspend fun execute(chatId: Long, queryConstraints: QueryConstraints) {
        log.info("Searching for listings for chat $chatId")
        log.debug("queryConstraints = $queryConstraints")

        if (queryConstraints.areas.isEmpty()) {
            log.info("Not searching for listings because no areas have been set")
            return
        }

        if (queryConstraints.maxPrice == null) {
            log.info("Not searching for listings because no max rent has been set")
            return
        }

        val request = ListingsRequest(
            query = ListingsQuery(
                location = Location(queryConstraints.areas.toList()),
                monthlyRent = IntRange(
                    from = queryConstraints.minPrice,
                    to = queryConstraints.maxPrice),
                numberOfRooms = IntRange(
                    from = queryConstraints.minRooms,
                    to = queryConstraints.maxRooms),
                offerType = "RENT"))
        log.debug("request = $request")

        val response = homegate.search(request)
        log.info("Received ${response.results.size} results")

        for (result in response.results) {
            val telegramId = listingsRecorder.getMessageId(result.id, chatId)
            if (telegramId == null) {
                log.info("Putting new listing ${result.id} on the event bus")
                eventBus.post(Pair(chatId, result))
            } else {
                log.debug("Listing ${result.id} has already been published as message $telegramId")
            }
        }

        log.info("Finished")
    }

}
