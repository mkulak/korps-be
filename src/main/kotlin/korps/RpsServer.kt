package korps

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import io.vertx.core.Vertx
import io.vertx.core.http.HttpServerRequest
import io.vertx.core.http.ServerWebSocket
import io.vertx.kotlin.core.http.writeTextMessageAwait
import io.vertx.kotlin.coroutines.dispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import java.lang.Integer.toHexString
import kotlin.coroutines.CoroutineContext
import kotlin.random.Random
import kotlin.random.Random.Default.nextInt


class RpsServer(val vertx: Vertx) {
    val clients = mutableMapOf<String, ConnectedClient>()
    val objectMapper = jacksonObjectMapper()
    val game = Game()

    fun handle(req: HttpServerRequest) {
        println("Got http ${req.path()}")
        req.response().putHeader("content-type", "text/plain").end("Hello from Kotlin Vert.x!")
    }

    fun handle(ws: ServerWebSocket) = CoroutineScope(vertx.dispatcher()).launch {
        ws.accept()
        val id = randomId()
        println("connected: ${ws.path()} $id")
        clients[id] = ConnectedClient(ws, id)
        ws.writeTextMessageAwait(id)
        ws.closeHandler {
            clients.remove(id)
        }

        if (game.id1 == null) {
            println("first player: $id")
            game.id1 = id
            ws.textMessageHandler { msg ->
                println("player 1 send $msg")
                if (game.choice1 == null) {
                    game.choice1 = objectMapper.readValue(msg)
                    CoroutineScope(vertx.dispatcher()).launch {
                        checkGameEnd()
                    }
                }
            }
        } else if (game.id2 == null) {
            println("second player: $id")
            game.id2 = id
            ws.textMessageHandler { msg ->
                println("player 2 send $msg")
                if (game.choice2 == null) {
                    game.choice2 = objectMapper.readValue(msg)
                    CoroutineScope(vertx.dispatcher()).launch {
                        checkGameEnd()
                    }
                }
            }
//            val result = RoundResult("1", "2", "1", 0, 0, Choice.Rock, Choice.Paper)
//            println("round end: $result")
//            val payload = objectMapper.writeValueAsString(result)
//            clients[game.id1!!]!!.ws.writeTextMessageAwait(payload)
//            clients[game.id2!!]!!.ws.writeTextMessageAwait(payload)
        }
    }

    private suspend fun checkGameEnd() {
        if (game.choice1 != null && game.choice2 != null) {
            game.score1++
            val result = RoundResult(game.id1!!, game.id2!!, game.id1, game.score1, game.score2, game.choice1!!, game.choice2!!)
            println("round end: $result")
            val payload = objectMapper.writeValueAsString(result)
            game.choice1 = null
            game.choice2 = null
            clients[game.id1!!]!!.ws.writeTextMessageAwait(payload)
            clients[game.id2!!]!!.ws.writeTextMessageAwait(payload)
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

data class Game(
    var id1: String? = null,
    var id2: String? = null,
    var score1: Int = 0,
    var score2: Int = 0,
    var choice1: Choice? = null,
    var choice2: Choice? = null
)