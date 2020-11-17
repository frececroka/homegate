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
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.*
import org.springframework.context.event.ContextRefreshedEvent
import org.springframework.context.event.EventListener
import org.springframework.context.support.PropertySourcesPlaceholderConfigurer
import org.springframework.stereotype.Component
import java.nio.file.Paths

@Configuration
@PropertySource("classpath:application.properties")
open class AppProperties {
    companion object {
        @Bean
        fun propertyResolver() = PropertySourcesPlaceholderConfigurer()
    }
}

@Configuration
@Profile("local", "gcf")
@KtorExperimentalAPI
open class CommonConfiguration {

    @Bean(name = ["new-listing-events"])
    @Suppress("UnstableApiUsage")
    open fun newListingBus() = EventBus()

    @Bean(name = ["crawl-request-events"])
    @Suppress("UnstableApiUsage")
    open fun crawlRequestBus() = EventBus()


    @Bean
    open fun telegramBot(
        @Value("\${telegram.token}") token: String
    ) = bot {
        this.token = token
    }

    @Bean
    open fun airtableBackendFactory(
        profileRepository: UserProfileRepository
    ) = { chatId: Long ->
        val profile = profileRepository.get(chatId)
        val credentials = profile.airtableCredentials
        if (credentials != null) {
            AirtableBackend(credentials.apiKey, credentials.appId)
        } else null
    }

}

@Configuration
@Profile("local")
open class LocalConfiguration {

    @Bean
    @Primary
    open fun jsonDb() = JsonDb(Paths.get("data"))

    @Bean(name = ["listings-db"])
    open fun listingsDb(db: JsonDb) = db.child("listings")

    @Bean(name = ["profiles-db"])
    open fun profilesDb(db: JsonDb) = db.child("profiles")

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
    open fun listingsDb(
        db: Firestore,
        @Value("\${firestore.listings}")
        listingsCollectionName: String,
    ): CollectionReference {
        return db.collection(listingsCollectionName)
    }

    @Bean(name = ["profiles-db"])
    open fun profileDb(
        db: Firestore,
        @Value("\${firestore.profiles}")
        profilesCollectionName: String,
    ): CollectionReference {
        return db.collection(profilesCollectionName)
    }

    @Bean(name = ["crawl-request-topic"])
    open fun crawlRequestTopic(
        @Value("\${pubsub.crawl}")
        crawlRequestTopic: String
    ): TopicName {
        return TopicName.parse(crawlRequestTopic)
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
