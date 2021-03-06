package korps

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import io.vertx.core.Vertx
import io.vertx.core.http.HttpClient
import io.vertx.core.http.WebSocket
import io.vertx.kotlin.core.http.writeTextMessageAwait
import io.vertx.kotlin.coroutines.dispatcher
import kotlinx.coroutines.*
import kotlinx.coroutines.channels.Channel


class RpsClient(val vertx: Vertx, val client: HttpClient) {
    val objectMapper = jacksonObjectMapper()

    suspend fun start() {
        println("Trying to connect")
        client.webSocket(8080, "localhost", "/some-uri") {
            it.cause()?.printStackTrace()
            println("Connected")
            handle(it.result())
        }
    }

    private fun handle(ws: WebSocket) {
        val ch = Channel<String>(10)
        ws.textMessageHandler { msg ->
            println("send $msg")
            CoroutineScope(vertx.dispatcher()).launch {
                ch.send(msg)
            }
        }
        ws.closeHandler {
            println("The end!")
            System.exit(0)
        }
        CoroutineScope(vertx.dispatcher()).launch {
            val myId = ch.receive()
            println("My id: $myId")
            loop@ while (true) {
                println("Choose Rock[1], Paper[2], Scissors[3]:")
                val line = withContext(Dispatchers.IO) { readLine() }
                val choice = when (line) {
                    "1" -> Choice.Rock
                    "2" -> Choice.Paper
                    "3" -> Choice.Scissors
                    else -> {
                        println("Not a valid choice")
                        continue@loop
                    }
                }
                ws.writeTextMessageAwait(choice.name)
                val state = objectMapper.readValue<RoundResult>(ch.receive())
                println(if (state.winnerId == myId) "you won!" else if (state.winnerId == null) "draw" else "you lose!")
                println("Scores: ${state.scores}")
            }
        }
    }
}