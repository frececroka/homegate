package ch.homegate.responder

import ch.homegate.createBot
import ch.homegate.setupJavaLogging
import com.github.kotlintelegrambot.dispatch
import com.github.kotlintelegrambot.dispatcher.handlers.Handler
import com.github.kotlintelegrambot.entities.Update
import org.slf4j.LoggerFactory

private val log = LoggerFactory.getLogger("ch.homegate.query")

fun main() {
    setupJavaLogging()
    log.info("Running query responder locally")
    log.info("If you don't receive any messages, make sure the webhook is not set for the bot")

    val responder = QueryResponder()

    val bot = createBot {
        dispatch {
            addHandler(object : Handler({ _, update ->
                responder.respond(update)
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
