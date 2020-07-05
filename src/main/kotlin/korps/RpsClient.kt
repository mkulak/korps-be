package korps

import io.vertx.core.http.HttpClient
import io.vertx.kotlin.core.http.webSocketAwait
import kotlin.random.Random


class RpsClient(val client: HttpClient) {
    suspend fun start() {
        println("Trying to connect")
        val ws = client.webSocketAwait(8080, "localhost", "/some-uri")
        println("Connected")
        ws.writeTextMessage("client msg ${Random.nextInt(100)}")
        ws.textMessageHandler {
            println("Received: $it")
        }
        ws.closeHandler {
            println("The end!")
            System.exit(0)
        }
        while (true) {
            val line = readLine()
            ws.writeTextMessage(line)
        }
    }
}