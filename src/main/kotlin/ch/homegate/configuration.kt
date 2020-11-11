package ch.homegate

import ch.homegate.airtable.AirtableBackend
import ch.homegate.client.HomegateClient
import ch.homegate.crawler.HomegateCrawler
import ch.homegate.crawler.consumers.AirtableRecorder
import ch.homegate.crawler.consumers.TelegramNotifier
import ch.homegate.responder.QueryResponder
import com.github.kotlintelegrambot.bot
import com.google.auth.oauth2.GoogleCredentials
import com.google.cloud.firestore.FirestoreOptions
import com.google.common.eventbus.EventBus
import io.ktor.util.*
import java.nio.file.Paths

@KtorExperimentalAPI
class Configuration(
    val crawler: HomegateCrawler,
    val responder: QueryResponder,
) {

    @KtorExperimentalAPI
    @Suppress("UnstableApiUsage")
    companion object {

        fun common(listingsRecorder: ListingsRecorder): Configuration {
            val eventBus = EventBus()

            val telegram = bot {
                token = System.getenv("TELEGRAM_TOKEN")
            }

            val chatId = System.getenv("CHAT_ID").toLong()

            val telegramNotifier = TelegramNotifier(telegram, chatId, listingsRecorder)
            eventBus.register(telegramNotifier)

            val airtableBackend = AirtableBackend(
                System.getenv("AIRTABLE_API_KEY"),
                System.getenv("AIRTABLE_APP_ID"))

            val airtableRecorder = AirtableRecorder(airtableBackend)
            eventBus.register(airtableRecorder)

            val homegate = HomegateClient()
            val crawler = HomegateCrawler(homegate, listingsRecorder, eventBus)

            val responder = QueryResponder(telegram, listingsRecorder, airtableBackend)

            return Configuration(crawler, responder)
        }

        fun local(): Configuration {
            val db = JsonDb(Paths.get("data"))
            val listingsRecorder = LocalListingsRecorder(db.child("listings"))
            return common(listingsRecorder)
        }

        fun gcf(): Configuration {
            val firestoreOptions = FirestoreOptions.getDefaultInstance().toBuilder()
                .setCredentials(GoogleCredentials.getApplicationDefault())
                .build()

            val db = firestoreOptions.service!!

            val collectionName = System.getenv("FIRESTORE_COLLECTION")!!
            val collection = db.collection(collectionName)

            val listingsRecorder = FirestoreListingsRecorder(collection)

            return common(listingsRecorder)
        }

    }

}
