package ch.homegate.responder

import ch.homegate.FirestoreListingsRecorder
import ch.homegate.ListingsRecorder
import ch.homegate.LocalListingsRecorder
import ch.homegate.airtable.AirtableBackend
import ch.homegate.client.HomegateClient
import ch.homegate.crawler.consumers.AirtableRecorder
import ch.homegate.crawler.consumers.TelegramNotifier
import com.github.kotlintelegrambot.bot
import com.google.common.eventbus.EventBus
import io.ktor.util.*

@KtorExperimentalAPI
@Suppress("UnstableApiUsage")
abstract class Configuration(
    listingsRecorder: ListingsRecorder
) {

    val telegram = bot {
        token = System.getenv("TELEGRAM_TOKEN")
    }

    val airtableBackend = AirtableBackend(
        System.getenv("AIRTABLE_API_KEY"),
        System.getenv("AIRTABLE_APP_ID"))

    val responder = QueryResponder(telegram, listingsRecorder, airtableBackend)

}

@KtorExperimentalAPI
class LocalConfiguration : Configuration(LocalListingsRecorder())

@KtorExperimentalAPI
class GcfConfiguration : Configuration(
    FirestoreListingsRecorder(System.getenv("FIRESTORE_COLLECTION"))
)
