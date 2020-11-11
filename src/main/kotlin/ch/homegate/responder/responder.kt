package ch.homegate.responder

import ch.homegate.ListingsRecorder
import ch.homegate.ReplyOption
import ch.homegate.airtable.AirtableBackend
import ch.homegate.airtable.State
import ch.homegate.buildReplyKeyboard
import com.github.kotlintelegrambot.Bot
import com.github.kotlintelegrambot.entities.Message
import com.github.kotlintelegrambot.entities.Update
import com.google.gson.Gson
import io.ktor.util.*
import org.slf4j.LoggerFactory

@KtorExperimentalAPI
@Suppress("unused")
class QueryResponder(
    private val telegram: Bot,
    private val listingsRecorder: ListingsRecorder,
    private val airtableBackend: AirtableBackend,
) {

    private val log = LoggerFactory.getLogger(javaClass)

    private val gson = Gson()

    suspend fun respond(update: Update) {
        log.debug("update = $update")

        val callbackQuery = update.callbackQuery
        if (callbackQuery != null) {
            val selected = ReplyOption.fromString(callbackQuery.data)
            val message = callbackQuery.message
            if (message != null) {
                when (selected) {
                    ReplyOption.Delete -> deleteMessage(message)
                    else -> updateReplyKeyboard(message, selected)
                }
                val telegramId = Pair(message.chat.id, message.messageId)
                val homegateId = listingsRecorder.getHomegateId(telegramId)
                if (homegateId != null) {
                    when (selected) {
                        ReplyOption.Delete ->
                            airtableBackend.delete(homegateId)
                        ReplyOption.Ignore ->
                            airtableBackend.setState(homegateId, State.Rejected)
                        ReplyOption.Contacted ->
                            airtableBackend.setState(homegateId, State.Contacted)
                        ReplyOption.Viewing ->
                            airtableBackend.setState(homegateId, State.Viewing)
                        ReplyOption.Applied ->
                            airtableBackend.setState(homegateId, State.Applied)
                    }
                } else {
                    log.warn("No listing identifier associated with message ${message.messageId}")
                }
            }
            telegram.answerCallbackQuery(callbackQuery.id)
            log.debug("Acknowledged callback")
        }
    }

    private fun deleteMessage(message: Message) {
        log.info("Deleting message ${message.messageId}")
        telegram.deleteMessage(message.chat.id, message.messageId)
        log.debug("Deleted message")
    }

    private fun updateReplyKeyboard(message: Message, selected: ReplyOption) {
        log.info("Selecting button $selected for message ${message.messageId}")
        val replyMarkup = buildReplyKeyboard(selected)
        telegram.editMessageReplyMarkup(
            message.chat.id, message.messageId,
            replyMarkup = replyMarkup)
        log.debug("Updated message reply keyboard")
    }

}
