package ch.homegate.crawler

import com.google.cloud.functions.HttpFunction
import com.google.cloud.functions.HttpRequest
import com.google.cloud.functions.HttpResponse
import org.slf4j.LoggerFactory

class QueryFunction : HttpFunction {

    private val log = LoggerFactory.getLogger(javaClass)

    override fun service(request: HttpRequest, response: HttpResponse) {
        log.info("Invoked HTTP function")
    }

}
