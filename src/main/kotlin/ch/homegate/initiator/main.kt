package ch.homegate.initiator

import ch.homegate.context
import ch.homegate.setupJavaLogging
import io.ktor.util.*
import kotlinx.coroutines.runBlocking

private val ctx = context()
private val crawlInitiator = ctx.getBean(CrawlInitiator::class.java)

@KtorExperimentalAPI
fun main(): Unit = runBlocking {
    setupJavaLogging()
    crawlInitiator.initiate()
}
