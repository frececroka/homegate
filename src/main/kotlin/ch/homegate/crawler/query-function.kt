package ch.homegate.crawler

import com.github.kotlintelegrambot.bot
import com.github.kotlintelegrambot.entities.Update
import com.google.cloud.functions.HttpFunction
import com.google.cloud.functions.HttpRequest
import com.google.cloud.functions.HttpResponse
import com.google.gson.Gson
import org.slf4j.LoggerFactory

class QueryFunction : HttpFunction {

    private val log = LoggerFactory.getLogger(javaClass)

    private val gson = Gson()

    private val bot = bot {
        token = System.getenv("TELEGRAM_TOKEN")
    }

    init {
        setupJavaLogging()
    }

    override fun service(request: HttpRequest, response: HttpResponse) {
        val update = gson.fromJson(request.reader, Update::class.java)
        log.debug("update = $update")

        val callbackQuery = update.callbackQuery
        if (callbackQuery != null) {
            val selected = ReplyOption.fromString(callbackQuery.data)
            val message = callbackQuery.message
            if (message != null) {
                log.info("Selecting button $selected for message ${message.messageId}")
                val replyMarkup = buildReplyKeyboard(selected)
                bot.editMessageReplyMarkup(
                    message.chat.id, message.messageId,
                    replyMarkup = replyMarkup)
                log.debug("Updated message reply keyboard")
            }
            bot.answerCallbackQuery(callbackQuery.id)
            log.debug("Acknowledged callback")
        }
    }

}
