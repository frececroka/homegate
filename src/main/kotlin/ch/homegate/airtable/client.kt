package ch.homegate.airtable

import io.ktor.client.*
import io.ktor.client.engine.cio.*
import io.ktor.client.features.*
import io.ktor.client.features.json.*
import io.ktor.client.request.*
import io.ktor.http.*
import io.ktor.util.*
import org.apache.http.client.utils.URIBuilder

@KtorExperimentalAPI
class AirtableClient(
    val apiKey: String,
    val appId: String,
) {

    val http by lazy { HttpClient(CIO) {
        install(JsonFeature)
        defaultRequest {
            header(HttpHeaders.Authorization, "Bearer $apiKey")
        }
    } }

    suspend inline fun <reified T> api(path: String, params: Map<String, String>, builder: HttpRequestBuilder.() -> Unit): T {
        val urlBuilder = URIBuilder("https://api.airtable.com/v0/$appId/$path")
        params.forEach { (k, v) -> urlBuilder.addParameter(k, v) }
        try {
            return http.request(urlBuilder.toString(), builder)
        } catch (e: ClientRequestException) {
            if (e.response.status == HttpStatusCode.Unauthorized) {
                throw AirtableUnauthorizedException()
            } else throw e
        }
    }

}

@KtorExperimentalAPI
class AirtableListingsTable(
    private val client: AirtableClient,
) {

    private val table = "Listings"

    data class Response<T>(val records: List<Record<T>>)
    data class Request<T>(val records: List<T>)

    suspend fun find(filter: Expression? = null): List<Record<AirtableListing>> {
        val params = mutableMapOf<String, String>()
        if (filter != null) {
            params["filterByFormula"] = filter.toString()
        }
        val response = client.api<Response<AirtableListing>>(table, params) {}
        return response.records
    }

    suspend fun findOne(filter: Expression? = null): Record<AirtableListing>? {
        val results = find(filter)
        return when {
            results.size > 1 -> throw IllegalStateException()
            results.size == 1 -> results[0]
            else -> null
        }
    }

    suspend fun create(records: List<NewRecord<AirtableListing>>): List<Record<AirtableListing>> {
        val response = client.api<Response<AirtableListing>>(table, emptyMap()) {
            method = HttpMethod.Post
            contentType(ContentType.Application.Json)
            body = Request(records)
        }
        return response.records
    }

    suspend fun update(request: List<Record<AirtableListingPatch>>): List<Record<AirtableListing>> {
        val response = client.api<Response<AirtableListing>>(table, emptyMap()) {
            method = HttpMethod.Patch
            contentType(ContentType.Application.Json)
            body = Request(request)
        }
        return response.records
    }

    suspend fun delete(recordId: String) {
        val path = "$table/$recordId"
        client.api<Response<AirtableListing>>(path, emptyMap()) {
            method = HttpMethod.Delete
        }
    }

}


class AirtableUnauthorizedException : Exception()
