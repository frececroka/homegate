package ch.homegate.responder

import ch.homegate.context
import ch.homegate.setupJavaLogging
import com.github.kotlintelegrambot.entities.Update
import com.google.cloud.functions.HttpFunction
import com.google.cloud.functions.HttpRequest
import com.google.cloud.functions.HttpResponse
import com.google.gson.Gson
import io.ktor.util.*
import kotlinx.coroutines.FlowPreview

@FlowPreview
@KtorExperimentalAPI
@Suppress("unused")
class Function : HttpFunction {

    private val ctx by lazy { context() }
    private val responder by lazy { ctx.getBean(QueryResponder::class.java) }

    private val gson = Gson()

    override fun service(request: HttpRequest, response: HttpResponse) {
        ctx; setupJavaLogging()
        val update = gson.fromJson(request.reader, Update::class.java)
        responder.respond(update)
    }

}
