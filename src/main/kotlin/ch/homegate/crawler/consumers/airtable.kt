package ch.homegate.crawler.consumers

import ch.homegate.airtable.AirtableBackend
import ch.homegate.client.http.ListingResponse
import com.google.common.eventbus.Subscribe
import io.ktor.util.*
import kotlinx.coroutines.runBlocking
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component

/**
 * Receives listings from an event bus and saves each on to an Airtable.
 */
@Component
@KtorExperimentalAPI
@Suppress("unused")
class AirtableRecorder(
    private val airtableBackend: AirtableBackend,
) {

    private val log = LoggerFactory.getLogger(javaClass)

    @Subscribe
    @Suppress("UnstableApiUsage")
    fun saveToAirtable(message: Pair<Long, ListingResponse>) = runBlocking {
        val (chatId, result) = message
        log.info("Saving entry for result ${result.id} (belonging to chat $chatId) to Airtable")
        airtableBackend.add(result)
    }

}
