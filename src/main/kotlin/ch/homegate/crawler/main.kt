package ch.homegate.crawler

import ch.homegate.Configuration
import ch.homegate.setupJavaLogging
import io.ktor.util.*
import kotlinx.coroutines.runBlocking
import org.slf4j.LoggerFactory

private val log = LoggerFactory.getLogger("ch.homegate.crawler")

@KtorExperimentalAPI
private val conf = Configuration.local()

@KtorExperimentalAPI
private val crawler = conf.crawler

@KtorExperimentalAPI
fun main(): Unit = runBlocking {
    setupJavaLogging()
    log.info("Running bot locally")
    crawler.execute(listingsRequest)
    log.info("Finished")
}
