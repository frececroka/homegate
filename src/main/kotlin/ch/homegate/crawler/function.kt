package ch.homegate.crawler

import ch.homegate.Configuration
import ch.homegate.setupJavaLogging
import com.google.cloud.functions.BackgroundFunction
import com.google.cloud.functions.Context
import io.ktor.util.*
import kotlinx.coroutines.runBlocking

@KtorExperimentalAPI
class Function : BackgroundFunction<PubSubMessage> {

    private val conf = Configuration.gcf()
    private val crawler = conf.crawler

    init {
        setupJavaLogging()
    }

    override fun accept(payload: PubSubMessage, context: Context): Unit = runBlocking {
        crawler.execute(listingsRequest)
    }

}

data class PubSubMessage(
    val data: String? = null,
    val attributes: Map<String, String>? = null,
    val messageId: String? = null,
    val publishTime: String? = null,
)
