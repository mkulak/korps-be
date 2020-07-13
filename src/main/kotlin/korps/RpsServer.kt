package korps

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import io.vertx.core.Vertx
import io.vertx.core.http.HttpServerRequest
import io.vertx.core.http.ServerWebSocket
import io.vertx.kotlin.core.http.writeTextMessageAwait
import io.vertx.kotlin.coroutines.dispatcher
import kotlinx.coroutines.*
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.selects.select
import java.lang.Integer.toHexString
import java.time.Duration
import kotlin.coroutines.CoroutineContext
import kotlin.random.Random.Default.nextInt


class RpsServer(val vertx: Vertx) {
    val clients = mutableMapOf<String, ConnectedClient>()
    val objectMapper = jacksonObjectMapper()
    val game = Game()

    fun handle(ws: ServerWebSocket) {
        ws.accept()
        val id = randomId()
        println("connected: ${ws.path()} $id")
        val channel = Channel<String>()
        clients[id] = ConnectedClient(ws, id, channel)
        if (game.players.size < 2) {
            game.players += id
            println("$id will be player number ${game.players.size}")
        }
        val scope = createScope()
        ws.textMessageHandler { msg ->
            println("Got $msg from $id")
            scope.launch {
                onMessage(id, msg)
            }
        }
        ws.closeHandler {
            clients.remove(id)
            scope.cancel()
            channel.close()
        }
        scope.launch {
            ws.writeTextMessageAwait(id)
        }
    }

    private suspend fun onMessage(playerId: String, msg: String) {
        val choice = Choice.valueOf(msg)
        println("Received $choice from $playerId")
        if (playerId in game.players && playerId !in game.roundState) {
            println("Added to roundState")
            game.roundState[playerId] = choice
        }
        if (game.roundState.size == 2) {
            println("both players gave their choices")
            val (id1, id2) = game.players
            val winnerId = id1
            game.scores[winnerId] = game.scores.getOrDefault(winnerId, 0) + 1
            val result = RoundResult(winnerId, game.scores)
            val payload = objectMapper.writeValueAsString(result)
            println("round end: $result")
            game.players.forEach { id ->
                clients[id]?.ws?.writeTextMessageAwait(payload)
            }
            game.roundState.clear()
        } else {
            println("should wait for 1 more choice")
        }
    }

    fun handle(req: HttpServerRequest) {
        println("Got http ${req.path()}")
        req.response().putHeader("content-type", "text/plain").end("Hello from Kotlin Vert.x!")
    }

    private fun createScope() = CoroutineScope(vertx.dispatcher() + CoroutineExceptionHandler(::exceptionHandler))

    private fun exceptionHandler(ctx: CoroutineContext, t: Throwable) {
        t.printStackTrace()
    }
}

data class ConnectedClient(
    val ws: ServerWebSocket,
    val id: String,
    val channel: Channel<String>
)

fun randomId() = toHexString(nextInt())

class Game {
    val players: MutableList<String> = ArrayList()
    val spectators: MutableList<String> = ArrayList()
    val scores: MutableMap<String, Int> = HashMap()
    val roundState: MutableMap<String, Choice> = HashMap()
}
