package korps

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import io.vertx.core.Vertx
import io.vertx.core.http.HttpServerRequest
import io.vertx.core.http.ServerWebSocket
import io.vertx.kotlin.core.http.writeTextMessageAwait
import io.vertx.kotlin.coroutines.dispatcher
import kotlinx.coroutines.*
import kotlinx.coroutines.channels.Channel
import java.lang.Integer.toHexString
import kotlin.coroutines.CoroutineContext
import kotlin.random.Random.Default.nextInt


class RpsServer(val vertx: Vertx, val port: Int) {
    val clients = mutableMapOf<String, ConnectedClient>()
    val objectMapper = jacksonObjectMapper()
    val game = Game()

    fun handle(ws: ServerWebSocket) {
        val id = randomId()
        println("connected: ${ws.path()} $id")
        clients[id] = ConnectedClient(ws, id)

        onJoin(id)
        ws.textMessageHandler { msg ->
            println("Got $msg from $id")
            onMessage(id, msg)
        }
        ws.closeHandler {
            println("Disconnected $id")
            onDisconnect(id)
            clients.remove(id)
        }
        ws.writeTextMessage(id)
    }

    private fun onJoin(id: String) {
        if (game.players.size < 2) {
            game.players += id
            println("$id joined as player ${game.players.size}")
        }
    }

    private fun onDisconnect(id: String) {
        game.players -= id
    }

    private fun onMessage(playerId: String, msg: String) {
        val choice = Choice.valueOf(msg)
        println("Received $choice from $playerId")
        if (playerId in game.players && playerId !in game.roundState) {
            println("Added to roundState")
            game.roundState[playerId] = choice
        }
        if (game.roundState.size == 2) {
            println("both players gave their choices")
            val (id1, id2) = game.players
            val choice1 = game.roundState[id1]!!.ordinal
            val choice2 = game.roundState[id2]!!.ordinal
            val winnerId = when(choice1 - choice2) {
                0 -> null
                1, -2 -> id1
                else -> id2
            }
            if (winnerId != null) {
                game.scores.merge(winnerId, 1) { a, b -> a + b }
            }
            val result = RoundResult(winnerId, game.scores)
            val payload = objectMapper.writeValueAsString(result)
            println("round end: $result")
            game.players.forEach { id ->
                clients[id]?.ws?.writeTextMessage(payload)
            }
            game.roundState.clear()
        } else {
            println("should wait for 1 more choice")
        }
    }

    fun handle(req: HttpServerRequest) {
        println("Got http ${req.path()}")
        val payload = """
            <script>
                var socket = new WebSocket("ws://localhost:$port")

                socket.onmessage = function(event) {
                    alert("Received data from websocket: " + event.data)
                }

                socket.onopen = function(event) {
                    alert("Web Socket opened")
                    socket.send("Rock")
                }

                socket.onclose = function(event) {
                    alert("Web Socket closed")
                }
            </script>
        """.trimIndent()
        req.response().putHeader("content-type", "text/html").end(payload)
    }

    private fun exceptionHandler(ctx: CoroutineContext, t: Throwable) {
        t.printStackTrace()
    }
}

data class ConnectedClient(
    val ws: ServerWebSocket,
    val id: String
)

fun randomId(): String = toHexString(nextInt())

class Game {
    val players: MutableList<String> = ArrayList()
    val scores: MutableMap<String, Int> = HashMap()
    val roundState: MutableMap<String, Choice> = HashMap()
}
