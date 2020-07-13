package korps

import io.vertx.core.Vertx
import io.vertx.kotlin.core.http.listenAwait
import kotlinx.coroutines.runBlocking


fun main() = runBlocking {
    println("hello, world!2")
    val vertx = Vertx.vertx()
    val server = vertx.createHttpServer()
    val rps = RpsServer(vertx)
    server.requestHandler(rps::handle)
        .webSocketHandler(rps::handle)
        .exceptionHandler { it.printStackTrace() }
        .listenAwait(8080)
    println("Server listening on http://localhost:8080/")
}