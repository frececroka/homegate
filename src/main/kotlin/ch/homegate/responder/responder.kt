package ch.homegate.responder

import ch.homegate.ReplyOption
import ch.homegate.airtable.AirtableBackend
import ch.homegate.airtable.State
import ch.homegate.buildReplyKeyboard
import ch.homegate.ListingsRecorder
import ch.homegate.createBot
import com.github.kotlintelegrambot.entities.Message
import com.github.kotlintelegrambot.entities.Update
import com.google.gson.Gson
import io.ktor.util.*
import org.slf4j.LoggerFactory

@KtorExperimentalAPI
@Suppress("unused")
class QueryResponder(
    private val listingsRecorder: ListingsRecorder,
    private val airtableBackend: AirtableBackend,
) {

    private val log = LoggerFactory.getLogger(javaClass)

    private val bot = createBot { }
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
                val id = listingsRecorder.getId(message.messageId)
                if (id != null) {
                    when (selected) {
                        ReplyOption.Delete ->
                            airtableBackend.delete(id)
                        ReplyOption.Ignore ->
                            airtableBackend.setState(id, State.Rejected)
                        ReplyOption.Contacted ->
                            airtableBackend.setState(id, State.Contacted)
                        ReplyOption.Viewing ->
                            airtableBackend.setState(id, State.Viewing)
                        ReplyOption.Applied ->
                            airtableBackend.setState(id, State.Applied)
                    }
                } else {
                    log.warn("No listing identifier associated with message ${message.messageId}")
                }
            }
            bot.answerCallbackQuery(callbackQuery.id)
            log.debug("Acknowledged callback")
        }
    }

    private fun deleteMessage(message: Message) {
        log.info("Deleting message ${message.messageId}")
        bot.deleteMessage(message.chat.id, message.messageId)
        log.debug("Deleted message")
    }

    private fun updateReplyKeyboard(message: Message, selected: ReplyOption) {
        log.info("Selecting button $selected for message ${message.messageId}")
        val replyMarkup = buildReplyKeyboard(selected)
        bot.editMessageReplyMarkup(
            message.chat.id, message.messageId,
            replyMarkup = replyMarkup)
        log.debug("Updated message reply keyboard")
    }

}
