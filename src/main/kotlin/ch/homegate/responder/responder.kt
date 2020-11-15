package ch.homegate.responder

import ch.homegate.*
import ch.homegate.airtable.AirtableBackend
import ch.homegate.airtable.State
import ch.homegate.client.HomegateClient
import com.github.kotlintelegrambot.Bot
import com.github.kotlintelegrambot.dispatcher.handlers.CallbackQueryHandler
import com.github.kotlintelegrambot.dispatcher.handlers.CommandHandler
import com.github.kotlintelegrambot.entities.*
import com.google.gson.Gson
import io.ktor.util.*
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.runBlocking
import org.slf4j.LoggerFactory
import org.springframework.context.annotation.Profile
import org.springframework.stereotype.Component

@Component
@Profile("local", "gcf")
@FlowPreview
@KtorExperimentalAPI
@Suppress("unused")
class QueryResponder(
    private val homegate: HomegateClient,
    private val telegram: Bot,
    private val listingsRecorder: ListingsRecorder,
    private val constraintsRepository: QueryConstraintsRepository,
    private val airtableBackend: AirtableBackend,
) {

    private val log = LoggerFactory.getLogger(javaClass)

    private val gson = Gson()

    fun respond(update: Update) {
        log.debug("update = $update")
        handlers
            .filter { it.checkUpdate(update) }
            .forEach { it.handlerCallback(telegram, update) }
    }

    private val handlers = listOf(
        handleCommand("start") { message, _ ->
            handleStart(message)
        },

        handleCommand("add_area") { message, args ->
            runBlocking { searchArea(message, args) }
        },

        handleCommand("remove_area") { message, _ ->
            runBlocking { initiateRemoveArea(message.chat.id) }
        },

        updateProperty("min_price") { copy(minPrice = it.toInt()) },
        updateProperty("max_price") { copy(maxPrice = it.toInt()) },
        updateProperty("min_rooms") { copy(minRooms = it.toInt()) },
        updateProperty("max_rooms") { copy(maxRooms = it.toInt()) },
        updateProperty("min_space") { copy(minSpace = it.toInt()) },
        updateProperty("max_space") { copy(maxSpace = it.toInt()) },

        handleCallback(ReplyOption.Delete.toString()) { message, homegateId, _ ->
            if (message != null) deleteMessage(message)
            if (homegateId != null) airtableBackend.delete(homegateId)
        },

        handleListingReply(ReplyOption.Ignore, State.Rejected),
        handleListingReply(ReplyOption.Contacted, State.Contacted),
        handleListingReply(ReplyOption.Viewing, State.Viewing),
        handleListingReply(ReplyOption.Applied, State.Applied),

        handleCallback("add_area") { message, _, callbackQuery ->
            addArea(message, callbackQuery)
        },

        handleCallback("remove_area") { message, _, callbackQuery ->
            removeArea(message, callbackQuery)
        },
    )

    private fun deleteMessage(message: Message) {
        log.info("Deleting message ${message.messageId}")
        telegram.deleteMessage(message.chat.id, message.messageId)
        log.debug("Deleted message")
    }

    private fun handleListingReply(option: ReplyOption, state: State): CallbackQueryHandler =
        handleCallback(option.toString()) { _, homegateId, callbackQuery ->
            val message = callbackQuery.message
            if (message != null) updateReplyKeyboard(message, option)
            airtableBackend.setState(homegateId!!, state)
        }

    private fun updateReplyKeyboard(message: Message, selected: ReplyOption) {
        log.info("Selecting button $selected for message ${message.messageId}")
        val replyMarkup = buildReplyKeyboard(selected)
        telegram.editMessageReplyMarkup(
            message.chat.id, message.messageId,
            replyMarkup = replyMarkup)
        log.debug("Updated message reply keyboard")
    }

    private fun handleStart(message: Message) {
        val chatId = message.chat.id
        constraintsRepository.update(chatId) {
            QueryConstraints(
                minRooms = 3,
                minSpace = 80,
                maxPrice = 3800,
            )
        }
        val greeting = javaClass.getResourceAsStream("/greeting.txt").bufferedReader().readText()
        telegram.sendMessage(chatId, greeting)
        telegram.sendMessage(chatId, "Your current search parameters are as follows:")
        reportConfig(chatId)
    }

    private suspend fun searchArea(message: Message, args: List<String>) {
        val chatId = message.chat.id

        if (args.isEmpty()) {
            telegram.sendMessage(chatId, "Please provide area to search for.")
            return
        }

        val query = args.joinToString(" ")
        if (query.length < 2) {
            telegram.sendMessage(chatId, "The search term must be at least 2 characters long.")
            return
        }

        val result = homegate.lookupLocation(query)

        val replyOptions = result.results
            .map {
                val label = it.geoLocation.names.en.joinToString(" ")
                InlineKeyboardButton(label, callbackData = "add_area ${it.geoLocation.id}")
            }
            .take(10)
            .map { listOf(it) }

        if (replyOptions.isEmpty()) {
            telegram.sendMessage(chatId, "The search returned no results.")
            return
        }

        val replyMarkup = InlineKeyboardMarkup(replyOptions)

        telegram.sendMessage(chatId, "Search results:",
            replyMarkup = replyMarkup)
    }

    private fun addArea(message: Message?, callbackQuery: CallbackQuery) {
        if (message == null) {
            log.error("Received callback query without associated message")
            return
        }

        val chatId = message.chat.id

        val locationId = callbackQuery.data.split(" ")[1]
        constraintsRepository.update(chatId) {
            it.copy(areas = (it.areas.toSet() + setOf(locationId)).toList())
        }

        reportConfig(chatId)
    }

    private fun initiateRemoveArea(chatId: Long) {
        val constraints = constraintsRepository.get(chatId)
        val replyOptions = constraints.areas
            .map { InlineKeyboardButton(it, callbackData = "remove_area $it") }
            .map { listOf(it) }
        val replyMarkup = InlineKeyboardMarkup(replyOptions)
        telegram.sendMessage(chatId, "Choose which area to delete:", replyMarkup = replyMarkup)
    }

    private fun removeArea(message: Message?, callbackQuery: CallbackQuery) {
        if (message == null) {
            log.error("Received callback query without associated message")
            return
        }

        val chatId = message.chat.id

        val locationId = callbackQuery.data.split(" ")[1]
        constraintsRepository.update(chatId) {
            it.copy(areas = it.areas - setOf(locationId))
        }

        reportConfig(chatId)
    }

    private fun updateProperty(
        propertyName: String,
        updater: QueryConstraints.(String) -> QueryConstraints
    ): CommandHandler {
        return handleCommand(propertyName) { message, args ->
            val chatId = message.chat.id
            if (args.size == 1) {
                constraintsRepository.update(chatId) {
                    updater(it, args[0])
                }
                reportConfig(chatId)
            } else {
                telegram.sendMessage(chatId, "Please provide exactly one argument.")
            }
        }
    }

    private fun reportConfig(chatId: Long) {
        val config = constraintsRepository.get(chatId)
        val areasLines =
            if (config.areas.isNotEmpty())
                listOf("areas" to config.areas.joinToString(", "))
            else listOf()
        val properties = listOf(
            "min price" to config.minPrice,
            "max price" to config.maxPrice,
            "min rooms" to config.minRooms,
            "max rooms" to config.maxRooms,
            "min space" to config.minSpace,
            "max space" to config.maxSpace)
        val propertyLines = properties
            .filter { (_, v) -> v != null }
            .map { (k, v) -> k to v.toString() }
        val text = (areasLines + propertyLines).joinToString("\n") { (k, v) -> "$k = $v" }
        telegram.sendMessage(chatId, text)
    }

    private fun handleCommand(command: String, handler: (Message, List<String>) -> Unit): CommandHandler {
        return CommandHandler(command) { _, update, args ->
            val message = update.editedMessage ?: update.message
            handler(message!!, args)
        }
    }

    private fun handleCallback(
        callbackData: String,
        handler: suspend (Message?, String?, CallbackQuery) -> Unit
    ): CallbackQueryHandler = CallbackQueryHandler(callbackData) { _, update ->
        val callbackQuery = update.callbackQuery!!
        val message = callbackQuery.message
        val homegateId = if (message != null) {
            val telegramId = Pair(message.chat.id, message.messageId)
            listingsRecorder.getHomegateId(telegramId)
        } else null
        runBlocking { handler(message, homegateId, callbackQuery) }
    }

}
