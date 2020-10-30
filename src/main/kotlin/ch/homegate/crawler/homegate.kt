package ch.homegate.crawler

import com.beust.klaxon.Klaxon
import io.ktor.client.*
import io.ktor.client.engine.cio.*
import io.ktor.client.request.*
import io.ktor.http.*
import io.ktor.util.*

@KtorExperimentalAPI
class HomegateClient {

    private val httpClient: HttpClient = HttpClient(CIO)
    private val json = Klaxon()

    suspend fun search(request: ListingsRequest): ListingsResponse {
        val response = httpClient.request<String> {
            url("https://api.homegate.ch/search/listings")
            method = HttpMethod.Post
            body = json.toJsonString(request)
        }
        return json.parse<ListingsResponse>(response)!!
    }

}
