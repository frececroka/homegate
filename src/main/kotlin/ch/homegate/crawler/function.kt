package ch.homegate.crawler

import ch.homegate.FirestoreListingsRecorder
import ch.homegate.airtable.AirtableBackend
import ch.homegate.setupJavaLogging
import com.google.cloud.functions.BackgroundFunction
import com.google.cloud.functions.Context
import io.ktor.util.*
import kotlinx.coroutines.runBlocking
import org.slf4j.LoggerFactory

@KtorExperimentalAPI
class Function : BackgroundFunction<PubSubMessage> {

    private val log = LoggerFactory.getLogger(javaClass)

    private val homegate = HomegateClient()
    private val listingsRecorder = FirestoreListingsRecorder()
    private val airtableBackend = AirtableBackend(
        System.getenv("AIRTABLE_API_KEY"),
        System.getenv("AIRTABLE_APP_ID"))
    private val notifier = HomegateNotifier(homegate, listingsRecorder, airtableBackend)

    init {
        setupJavaLogging()
    }

    override fun accept(payload: PubSubMessage, context: Context): Unit = runBlocking {
        log.info("Running bot as GCF")
        notifier.notify(listingsRequest)
        log.info("Finished")
    }

}
