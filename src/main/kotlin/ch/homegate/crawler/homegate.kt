package ch.homegate.crawler

import io.ktor.client.*
import io.ktor.client.engine.cio.*
import io.ktor.client.features.*
import io.ktor.client.features.json.*
import io.ktor.client.request.*
import io.ktor.http.*
import io.ktor.util.*

@KtorExperimentalAPI
class HomegateClient {

    // Installing the JsonFeature tries to load an implementation of the JsonSerializer interface
    // using the ServiceLoader. The ServiceLoader, in turn, uses the context class loader of the
    // current thread (`Thread.currentThread().contextClassLoader`) to find such an implementation.
    // During the startup of the function, the context class loader seems to be the AppClassLoader,
    // or, more specifically, the `jdk.internal.loader.ClassLoaders$AppClassLoader`. This class
    // loader fails to find an implementation of JsonSerializer. But when the function is called,
    // the context class loader changes to be a URLClassLoader, which finds an implementation of
    // JsonSerializer. Specifically, the GsonSerializer class provided by `ktor-client-gson`. Using
    // the *by lazy* initialization here makes sure the implementation of JsonSerializer is located
    // when the function is called.
    private val httpClient by lazy { HttpClient(CIO) {
        install(JsonFeature)
        defaultRequest {
            if (method == HttpMethod.Post) {
                contentType(ContentType.Application.Json)
            }
        }
    } }

    suspend fun search(request: ListingsRequest): ListingsResponse =
        httpClient.post("https://api.homegate.ch/search/listings") {
            body = request
        }

}
