package ch.homegate.crawler.consumers

import ch.homegate.airtable.AirtableBackend
import ch.homegate.airtable.AirtableUnauthorizedException
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
    private val airtableBackendFactory: (Long) -> AirtableBackend?,
) {

    private val log = LoggerFactory.getLogger(javaClass)

    @Subscribe
    @Suppress("UnstableApiUsage")
    fun saveToAirtable(message: Pair<Long, ListingResponse>) = runBlocking {
        val (chatId, result) = message
        log.info("Saving entry for result ${result.id} (belonging to chat $chatId) to Airtable")
        val airtableBackend = airtableBackendFactory(chatId)
        if (airtableBackend != null) {
            log.info("The user has provided to Airtable credentials")
            try {
                airtableBackend.add(result)
                log.info("Entry ${result.id} saved")
            } catch (e: AirtableUnauthorizedException) {
                log.info("The provided Airtable credentials are invalid")
            }
        } else {
            log.info("The user has not connected to Airtable")
        }
    }

}
