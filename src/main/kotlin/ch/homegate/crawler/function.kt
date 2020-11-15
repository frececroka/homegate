package ch.homegate.crawler

import ch.homegate.QueryConstraints
import ch.homegate.context
import ch.homegate.setupJavaLogging
import com.google.cloud.functions.BackgroundFunction
import com.google.cloud.functions.Context
import com.google.common.reflect.TypeToken
import com.google.gson.Gson
import io.ktor.util.*
import kotlinx.coroutines.runBlocking
import org.slf4j.LoggerFactory
import java.nio.charset.StandardCharsets
import java.util.*

@KtorExperimentalAPI
class Function : BackgroundFunction<PubSubMessage> {

    private val log = LoggerFactory.getLogger(javaClass)

    private val gson = Gson()

    private val ctx by lazy { context() }
    private val crawler by lazy { ctx.getBean(HomegateCrawler::class.java) }

    @Suppress("UnstableApiUsage")
    private val type = (object : TypeToken<Pair<Long, QueryConstraints>>() {}).type

    override fun accept(payload: PubSubMessage, context: Context): Unit = runBlocking {
        ctx; setupJavaLogging()
        log.debug("Received message: ${payload.data}")
        val json = Base64.getDecoder().decode(payload.data).toString(StandardCharsets.UTF_8)
        val data = gson.fromJson<Pair<Long, QueryConstraints>>(json, type)
        log.info("Parsed message: $data")
        val (chatId, queryConstraints) = data
        crawler.execute(chatId, queryConstraints)
    }

}

data class PubSubMessage(
    val data: String? = null,
    val attributes: Map<String, String>? = null,
    val messageId: String? = null,
    val publishTime: String? = null,
)
