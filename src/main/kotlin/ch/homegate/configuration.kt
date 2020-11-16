package ch.homegate

import ch.homegate.airtable.AirtableBackend
import ch.homegate.crawler.consumers.AirtableRecorder
import ch.homegate.crawler.consumers.TelegramNotifier
import com.github.kotlintelegrambot.bot
import com.google.auth.oauth2.GoogleCredentials
import com.google.cloud.firestore.CollectionReference
import com.google.cloud.firestore.Firestore
import com.google.cloud.firestore.FirestoreOptions
import com.google.common.eventbus.EventBus
import com.google.pubsub.v1.TopicName
import io.ktor.util.*
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.context.annotation.*
import org.springframework.context.event.ContextRefreshedEvent
import org.springframework.context.event.EventListener
import org.springframework.core.env.Environment
import org.springframework.stereotype.Component
import java.nio.file.Paths

@Configuration
open class CommonConfiguration {

    @Bean(name = ["new-listing-events"])
    @Suppress("UnstableApiUsage")
    open fun newListingBus() = EventBus()

    @Bean(name = ["crawl-request-events"])
    @Suppress("UnstableApiUsage")
    open fun crawlRequestBus() = EventBus()


    @Bean
    open fun telegramBot() = bot {
        token = System.getenv("TELEGRAM_TOKEN")
    }

    @Bean
    @KtorExperimentalAPI
    open fun airtableBackend(env: Environment) = AirtableBackend(
        env.getRequiredProperty("AIRTABLE_API_KEY"),
        env.getRequiredProperty("AIRTABLE_APP_ID"))

}

@Configuration
@Profile("local")
open class LocalConfiguration {

    @Bean
    @Primary
    open fun jsonDb() = JsonDb(Paths.get("data"))

    @Bean(name = ["listings-db"])
    open fun listingsDb(db: JsonDb) = db.child("listings")

    @Bean(name = ["query-constraints-db"])
    open fun queryConstraintsDb(db: JsonDb) = db.child("constraints")

}

@Configuration
@Profile("gcf")
open class GcfConfiguration {

    @Bean
    open fun firestore(): Firestore {
        val firestoreOptions = FirestoreOptions.getDefaultInstance().toBuilder()
            .setCredentials(GoogleCredentials.getApplicationDefault())
            .build()
        return firestoreOptions.service!!
    }

    @Bean(name = ["listings-db"])
    open fun listingsDb(db: Firestore): CollectionReference {
        val listingsCollectionName = System.getenv("FIRESTORE_LISTINGS_COLLECTION")!!
        return db.collection(listingsCollectionName)
    }

    @Bean(name = ["query-constraints-db"])
    open fun queryConstraintsDb(db: Firestore): CollectionReference {
        val listingsCollectionName = System.getenv("FIRESTORE_QUERY_CONSTRAINTS_COLLECTION")!!
        return db.collection(listingsCollectionName)
    }

    @Bean(name = ["crawl-request-topic"])
    open fun crawlRequestTopic(): TopicName {
        return TopicName.parse(System.getenv("CRAWL_REQUEST_TOPIC"))
    }

}

@Component
@Profile("local", "gcf")
@KtorExperimentalAPI
@Suppress("UnstableApiUsage")
class ConfigureNewListingBusSubscribers(
    @Qualifier("new-listing-events") val newListingBus: EventBus,
    val telegramNotifier: TelegramNotifier,
    val airtableRecorder: AirtableRecorder,
) {

    @EventListener
    fun onContextRefreshed(event: ContextRefreshedEvent) {
        newListingBus.register(telegramNotifier)
        newListingBus.register(airtableRecorder)
    }

}

@Component
@Profile("local", "gcf")
@KtorExperimentalAPI
@Suppress("UnstableApiUsage")
class ConfigureCrawlRequestBusSubscribers(
    @Qualifier("crawl-request-events") val crawlRequestBus: EventBus,
    @Qualifier("crawl-request-sink") val crawlRequestSink: Any,
) {

    @EventListener
    fun onContextRefreshed(event: ContextRefreshedEvent) {
        crawlRequestBus.register(crawlRequestSink)
    }

}

fun context() = AnnotationConfigApplicationContext("ch.homegate")
