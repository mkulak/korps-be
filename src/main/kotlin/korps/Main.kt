package korps

import io.vertx.core.Vertx
import io.vertx.kotlin.core.http.listenAwait

suspend fun main() {
    println("hello, world!")
    Vertx.vertx().createHttpServer().requestHandler { req ->
        req.response().putHeader("content-type", "text/plain").end("Hello from Kotlin Vert.x!")
    }.listenAwait(8080)
    println("Server listening on http://localhost:8080/")
}                                  