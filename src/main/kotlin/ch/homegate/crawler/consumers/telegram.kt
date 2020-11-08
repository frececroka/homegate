package ch.homegate.crawler.consumers

import ch.homegate.ListingsRecorder
import ch.homegate.buildReplyKeyboard
import ch.homegate.client.Address
import ch.homegate.client.ListingResponse
import com.github.kotlintelegrambot.Bot
import com.github.kotlintelegrambot.entities.ParseMode
import com.google.common.eventbus.Subscribe
import org.apache.http.client.utils.URIBuilder
import org.slf4j.LoggerFactory
import java.net.URI

/**
 * Receives listings from an event bus and sends a Telegram message for each.
 */
@Suppress("UnstableApiUsage", "unused")
class TelegramNotifier(
    private val telegram: Bot,
    private val chatId: Long,
    private val listingsRecorder: ListingsRecorder,
) {

    private val log = LoggerFactory.getLogger(javaClass)

    @Subscribe
    fun sendMessageForListing(result: ListingResponse) {
        log.info("Sending message for listing ${result.id}")
        val replyMarkup = buildReplyKeyboard()
        val (botResponse, exception) = telegram.sendMessage(chatId, buildMessage(result),
            parseMode = ParseMode.MARKDOWN,
            replyMarkup = replyMarkup)
        if (botResponse != null && botResponse.isSuccessful) {
            val createdMessageResponse = botResponse.body()
            if (createdMessageResponse != null) {
                val createdMessage = createdMessageResponse.result
                if (createdMessage != null) {
                    log.debug("Marking result ${result.id} as old")
                    listingsRecorder.add(result.id, createdMessage.messageId)
                } else {
                    log.error("Telegram API did not return information about created message")
                }
            } else {
                log.error("Telegram API did not return information about created message")
            }
        } else {
            log.error("Failed to send Telegram message", exception)
        }
    }

    private fun buildMessage(result: ListingResponse): String {
        val listing = result.listing
        val calendarLink = buildCalendarLink(result)
        val mapsLink = buildMapsLink(listing.address)
        return """
            ${listing.address}: ${listing.characteristics} for CHF ${listing.prices.rent.gross}
            \[[open listing](${result.url})] \[[open map]($mapsLink)] \[[add to calendar]($calendarLink)]
        """.trimIndent()
    }

    private fun buildCalendarLink(result: ListingResponse): URI {
        val listing = result.listing
        val address = listing.address
        val details = """
            ${listing.localization.de.text.description}

            <a href="${result.url}">${result.url}</a>
        """.trimIndent()
        return URIBuilder("https://www.google.com/calendar/render")
            .addParameter("action", "TEMPLATE")
            .addParameter("text", address.street)
            .addParameter("details", details)
            .addParameter("location", address.toString())
            .build()
    }

    private fun buildMapsLink(address: Address): URI {
        return URIBuilder("https://www.google.com/maps/search/")
            .addParameter("api", "1")
            .addParameter("query", address.toString())
            .build()
    }

}
