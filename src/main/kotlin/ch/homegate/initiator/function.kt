package ch.homegate.initiator

import ch.homegate.context
import ch.homegate.setupJavaLogging
import com.google.cloud.functions.BackgroundFunction
import com.google.cloud.functions.Context
import io.ktor.util.*
import kotlinx.coroutines.runBlocking

@KtorExperimentalAPI
class Function : BackgroundFunction<PubSubMessage> {

    private val ctx by lazy { context() }
    private val crawlInitiator by lazy { ctx.getBean(CrawlInitiator::class.java) }

    override fun accept(payload: PubSubMessage, context: Context): Unit = runBlocking {
        ctx; setupJavaLogging()
        crawlInitiator.initiate()
    }

}

class PubSubMessage
