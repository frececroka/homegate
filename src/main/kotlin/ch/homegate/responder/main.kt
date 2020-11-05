package ch.homegate.responder

import ch.homegate.airtable.AirtableBackend
import ch.homegate.LocalListingsRecorder
import ch.homegate.createBot
import ch.homegate.setupJavaLogging
import com.github.kotlintelegrambot.dispatch
import com.github.kotlintelegrambot.dispatcher.handlers.Handler
import com.github.kotlintelegrambot.entities.Update
import io.ktor.util.*
import kotlinx.coroutines.runBlocking
import org.slf4j.LoggerFactory

private val log = LoggerFactory.getLogger("ch.homegate.query")

@KtorExperimentalAPI
fun main() {
    setupJavaLogging()
    log.info("Running query responder locally")
    log.info("If you don't receive any messages, make sure the webhook is not set for the bot")

    val listingsRecorder = LocalListingsRecorder()
    val airtableBackend = AirtableBackend(
        System.getenv("AIRTABLE_API_KEY"),
        System.getenv("AIRTABLE_APP_ID"))
    val responder = QueryResponder(listingsRecorder, airtableBackend)

    val bot = createBot {
        dispatch {
            addHandler(object : Handler({ _, update ->
                runBlocking { responder.respond(update) }
            }) {
                override val groupIdentifier: String
                    get() = "generic"
                override fun checkUpdate(update: Update) = true
            })
        }
    }

    bot.startPolling()
    Thread.sleep(Long.MAX_VALUE)
}
