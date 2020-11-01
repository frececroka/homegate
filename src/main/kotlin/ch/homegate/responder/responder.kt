package ch.homegate.responder

import ch.homegate.ReplyOption
import ch.homegate.buildReplyKeyboard
import ch.homegate.createBot
import com.github.kotlintelegrambot.entities.Update
import com.google.gson.Gson
import org.slf4j.LoggerFactory

@Suppress("unused")
class QueryResponder {

    private val log = LoggerFactory.getLogger(javaClass)

    private val bot = createBot { }
    private val gson = Gson()

    fun respond(update: Update) {
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
