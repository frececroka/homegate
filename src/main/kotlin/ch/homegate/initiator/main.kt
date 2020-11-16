package ch.homegate.initiator

import ch.homegate.context
import ch.homegate.setupJavaLogging
import io.ktor.util.*
import kotlinx.coroutines.runBlocking

@KtorExperimentalAPI
fun main(): Unit = runBlocking {
    val ctx = context(); setupJavaLogging()
    val crawlInitiator = ctx.getBean(CrawlInitiator::class.java)
    crawlInitiator.initiate()
}
