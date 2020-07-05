package korps

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import io.vertx.core.http.HttpServerRequest
import io.vertx.core.http.ServerWebSocket
import io.vertx.kotlin.core.http.writeTextMessageAwait
import java.lang.Integer.toHexString
import kotlin.random.Random
import kotlin.random.Random.Default.nextInt


class RpsServer {
    val clients = mutableMapOf<String, ConnectedClient>()
    val objectMapper = jacksonObjectMapper()

    fun handle(req: HttpServerRequest) {
        println("Got http ${req.path()}")
        req.response().putHeader("content-type", "text/plain").end("Hello from Kotlin Vert.x!")
    }

    fun handle(ws: ServerWebSocket) {
        ws.accept()
        val id = randomId()
        println("connected: ${ws.path()} $id")
        clients[id] = ConnectedClient(ws, id)
        sendClientsList()
        ws.closeHandler {
            clients.remove(id)
            sendClientsList()
        }
        ws.textMessageHandler { msg ->
            sendMessage("$id: $msg")
        }
    }

    private fun sendClientsList() {
        val payload = objectMapper.writeValueAsString(clients.values.map { it.id })
        clients.values.forEach {
            it.ws.writeTextMessage(payload)
        }
    }

    private fun sendMessage(msg: String) {
        val payload = objectMapper.writeValueAsString(msg)
        clients.values.forEach {
            it.ws.writeTextMessage(payload)
        }
    }
}

data class ConnectedClient(
    val ws: ServerWebSocket,
    val id: String
)

fun randomId() = toHexString(nextInt())