package ch.homegate.crawler

import ch.homegate.setupJavaLogging
import com.google.cloud.functions.BackgroundFunction
import com.google.cloud.functions.Context
import io.ktor.util.*
import kotlinx.coroutines.runBlocking
import org.slf4j.LoggerFactory

@KtorExperimentalAPI
class Function : BackgroundFunction<PubSubMessage> {

    private val conf = GcfConfiguration()
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
