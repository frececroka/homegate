package ch.homegate.crawler.consumers

import ch.homegate.airtable.AirtableBackend
import ch.homegate.client.ListingResponse
import com.google.common.eventbus.Subscribe
import io.ktor.util.*
import kotlinx.coroutines.runBlocking
import org.slf4j.LoggerFactory

/**
 * Receives listings from an event bus and saves each on to an Airtable.
 */
@KtorExperimentalAPI
@Suppress("unused")
class AirtableRecorder(
    private val airtableBackend: AirtableBackend,
) {

    private val log = LoggerFactory.getLogger(javaClass)

    @Subscribe
    @Suppress("UnstableApiUsage")
    fun saveToAirtable(result: ListingResponse) = runBlocking {
        log.info("Saving entry for result ${result.id} to Airtable")
        airtableBackend.add(result)
    }

}
