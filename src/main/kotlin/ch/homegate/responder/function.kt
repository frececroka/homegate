package ch.homegate.responder

import ch.homegate.airtable.AirtableBackend
import ch.homegate.FirestoreListingsRecorder
import ch.homegate.setupJavaLogging
import com.github.kotlintelegrambot.entities.Update
import com.google.cloud.functions.HttpFunction
import com.google.cloud.functions.HttpRequest
import com.google.cloud.functions.HttpResponse
import com.google.gson.Gson
import io.ktor.util.*
import kotlinx.coroutines.runBlocking

@KtorExperimentalAPI
@Suppress("unused")
class Function : HttpFunction {

    private val listingsRecorder = FirestoreListingsRecorder()
    private val airtableBackend = AirtableBackend(
        System.getenv("AIRTABLE_API_KEY"),
        System.getenv("AIRTABLE_APP_ID"))
    private val responder = QueryResponder(listingsRecorder, airtableBackend)

    private val gson = Gson()

    init {
        setupJavaLogging()
    }

    override fun service(request: HttpRequest, response: HttpResponse) {
        val update = gson.fromJson(request.reader, Update::class.java)
        runBlocking { responder.respond(update) }
    }

}
