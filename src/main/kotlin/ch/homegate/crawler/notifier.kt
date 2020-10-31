package ch.homegate.crawler

import com.github.kotlintelegrambot.bot
import com.github.kotlintelegrambot.entities.ParseMode
import io.ktor.util.*
import org.apache.http.client.utils.URIBuilder
import java.net.URI

@KtorExperimentalAPI
class HomegateNotifier(
        private val homegate: HomegateClient,
        private val listingsRecorder: ListingsRecorder,
) {

    private val bot = bot {
        token = System.getenv("TELEGRAM_TOKEN")
    }

    private val chatId = System.getenv("CHAT_ID").toLong()

    suspend fun notify(request: ListingsRequest) {
        val response = homegate.search(request)
        for (result in response.results) {
            if (listingsRecorder.isNew(result.id)) {
                bot.sendMessage(chatId, buildMessage(result), parseMode = ParseMode.MARKDOWN)
                listingsRecorder.add(result.id)
            }
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
