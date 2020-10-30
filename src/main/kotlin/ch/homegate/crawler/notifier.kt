package ch.homegate.crawler

import com.github.kotlintelegrambot.bot
import io.ktor.util.*

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
                bot.sendMessage(chatId, buildMessage(result))
                listingsRecorder.add(result.id)
            }
        }
    }

    private fun buildMessage(result: ListingResponse): String {
        val listing = result.listing
        return """
            ${listing.address}: ${listing.characteristics} for CHF ${listing.prices.rent.gross}
            ${result.url}
        """.trimIndent()
    }

}
