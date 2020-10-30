package ch.homegate.crawler

import com.google.cloud.functions.BackgroundFunction
import com.google.cloud.functions.Context
import io.ktor.util.*
import kotlinx.coroutines.runBlocking

@KtorExperimentalAPI
class Main : BackgroundFunction<PubSubMessage> {

    private val homegate = HomegateClient()
    private val listingsRecorder = FirestoreListingsRecorder()
    private val notifier = HomegateNotifier(homegate, listingsRecorder)

    override fun accept(payload: PubSubMessage, context: Context): Unit = runBlocking {
        notifier.notify(listingsRequest)
    }

}
