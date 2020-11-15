package ch.homegate.initiator

import ch.homegate.QueryConstraints
import ch.homegate.crawler.HomegateCrawler
import com.google.cloud.pubsub.v1.Publisher
import com.google.common.eventbus.Subscribe
import com.google.gson.Gson
import com.google.protobuf.ByteString
import com.google.pubsub.v1.PubsubMessage
import com.google.pubsub.v1.TopicName
import io.ktor.util.*
import kotlinx.coroutines.runBlocking
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.context.annotation.Profile
import org.springframework.stereotype.Component

@Component
@Profile("local")
@Qualifier("crawl-request-sink")
@KtorExperimentalAPI
class ExecuteCrawlRequest(private val crawler: HomegateCrawler) {

    @Subscribe
    @Suppress("UnstableApiUsage", "unused")
    fun accept(message: Pair<Long, QueryConstraints>) = runBlocking {
        val (chatId, queryConstraints) = message
        crawler.execute(chatId, queryConstraints)
    }

}

@Component
@Profile("gcf")
@Qualifier("crawl-request-sink")
class SubmitCrawlRequestToPubSub(
    @Qualifier("crawl-request-topic") topicName: TopicName
) {

    private val log = LoggerFactory.getLogger(javaClass)

    private val gson = Gson()

    private val publisher = Publisher.newBuilder(topicName).build()

    @Subscribe
    @Suppress("UnstableApiUsage", "unused")
    fun accept(message: Pair<Long, QueryConstraints>) {
        log.info("Publishing message for $message to Pub/Sub")
        val serializedMessage = gson.toJson(message)
        val pubsubMessage = PubsubMessage.newBuilder()
            .setData(ByteString.copyFromUtf8(serializedMessage))
            .build()
        val messageId = publisher.publish(pubsubMessage).get()
        log.info("Published message $messageId")
    }

}
