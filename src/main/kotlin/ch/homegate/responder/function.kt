package ch.homegate.responder

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

    private val config = GcfConfiguration()
    private val responder = config.responder

    private val gson = Gson()

    init {
        setupJavaLogging()
    }

    override fun service(request: HttpRequest, response: HttpResponse) {
        val update = gson.fromJson(request.reader, Update::class.java)
        runBlocking { responder.respond(update) }
    }

}
