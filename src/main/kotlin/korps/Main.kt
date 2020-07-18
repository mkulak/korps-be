package korps

import io.vertx.core.Vertx
import io.vertx.kotlin.core.http.listenAwait
import kotlinx.coroutines.runBlocking


fun main() = runBlocking {
    println("korps be ver 0.1")
    val port = (System.getenv("KORPS_PORT") ?: "8080").toInt()
    val vertx = Vertx.vertx()
    val server = vertx.createHttpServer()
    val rps = RpsServer(vertx, port)
    server.requestHandler(rps::handle)
        .webSocketHandler(rps::handle)
        .exceptionHandler { it.printStackTrace() }
        .listenAwait(port)
    println("Server listening on http://localhost:$port/")
}