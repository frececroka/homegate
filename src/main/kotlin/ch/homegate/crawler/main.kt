package ch.homegate.crawler

import ch.homegate.LocalListingsRecorder
import ch.homegate.airtable.AirtableBackend
import ch.homegate.setupJavaLogging
import io.ktor.util.*
import kotlinx.coroutines.runBlocking
import org.slf4j.LoggerFactory

private val log = LoggerFactory.getLogger("ch.homegate.crawler")

@KtorExperimentalAPI
fun main(): Unit = runBlocking {
    setupJavaLogging()
    log.info("Running bot locally")
    val homegate = HomegateClient()
    val listingsRecorder = LocalListingsRecorder()
    val airtableBackend = AirtableBackend(
        System.getenv("AIRTABLE_API_KEY"),
        System.getenv("AIRTABLE_APP_ID"))
    val notifier = HomegateNotifier(homegate, listingsRecorder, airtableBackend)
    notifier.notify(listingsRequest)
    log.info("Finished")
}
