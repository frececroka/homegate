package ch.homegate.crawler

import ch.homegate.ListingsRecorder
import ch.homegate.client.HomegateClient
import ch.homegate.client.ListingsRequest
import com.google.common.eventbus.EventBus
import io.ktor.util.*
import org.slf4j.LoggerFactory

/**
 * Retrieves new listings from Homegate and puts them on the event bus.
 */
@KtorExperimentalAPI
@Suppress("UnstableApiUsage")
class HomegateCrawler(
    private val homegate: HomegateClient,
    private val listingsRecorder: ListingsRecorder,
    private val eventBus: EventBus,
) {

    private val log = LoggerFactory.getLogger(javaClass)

    suspend fun execute(request: ListingsRequest) {
        log.info("Searching for listings")
        log.debug("request = $request")
        val response = homegate.search(request)
        log.info("Received ${response.results.size} results")
        for (result in response.results) {
            val messageId = listingsRecorder.getMessageId(result.id)
            if (messageId == null) {
                log.info("Putting new listing ${result.id} on the event bus")
                eventBus.post(result)
            } else {
                log.debug("Listing ${result.id} has already been published as message $messageId")
            }
        }
        log.info("Finished")
    }

}
