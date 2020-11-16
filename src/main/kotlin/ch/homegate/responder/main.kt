package ch.homegate.responder

import ch.homegate.context
import ch.homegate.setupJavaLogging
import com.github.kotlintelegrambot.bot
import com.github.kotlintelegrambot.dispatch
import com.github.kotlintelegrambot.dispatcher.handlers.Handler
import com.github.kotlintelegrambot.entities.Update
import io.ktor.util.*
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.runBlocking
import org.slf4j.LoggerFactory

@FlowPreview
@KtorExperimentalAPI
open class LocalResponder(
    private val responder: QueryResponder
) {

    private val log = LoggerFactory.getLogger(javaClass)

    fun run() {
        setupJavaLogging()
        log.info("Running query responder locally")
        log.info("If you don't receive any messages, make sure the webhook is not set for the bot")

        val bot = bot {
            token = System.getenv("TELEGRAM_TOKEN")
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

}

@FlowPreview
@KtorExperimentalAPI
fun main() {
    val ctx = context()
    val localResponder = ctx.getBean(LocalResponder::class.java)
    localResponder.run()
}
