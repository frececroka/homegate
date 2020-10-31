package ch.homegate.crawler

import io.ktor.util.*
import kotlinx.coroutines.runBlocking

@KtorExperimentalAPI
fun main(): Unit = runBlocking {
    val notifier = HomegateNotifier(HomegateClient(), LocalListingsRecorder())
    notifier.notify(listingsRequest)
}
