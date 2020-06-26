package korps

import io.vertx.core.Vertx
import io.vertx.kotlin.core.http.listenAwait
import io.vertx.kotlin.coroutines.dispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

suspend fun main() {
    println("hello, world!")
    val vertx = Vertx.vertx()
    val server = vertx.createHttpServer()
    server.requestHandler { req ->
        req.response().putHeader("content-type", "text/plain").end("Hello from Kotlin Vert.x!")
    }
    server.webSocketHandler { socket ->
        CoroutineScope(vertx.dispatcher()).launch {
            repeat(10) {
                socket.writeTextMessage("message #$it")
                delay(300)
            }
            socket.close()
        }
    }
    server.listenAwait(8080)
    println("Server listening on http://localhost:8080/")
}                                  