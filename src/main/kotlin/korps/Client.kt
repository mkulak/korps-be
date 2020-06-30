//package korps
//
//import io.vertx.core.Vertx
//import io.vertx.kotlin.core.http.webSocketAwait
//import kotlin.random.Random
//
//
//suspend fun main() {
//    val vertx = Vertx.vertx()
//    val client = vertx.createHttpClient()
//
//    println("Trying to connect")
//    val ws = client.webSocketAwait(8080, "localhost", "/some-uri")
//    println("Connected")
//    ws.writeTextMessage("client msg ${Random.nextInt(100)}")
//    ws.textMessageHandler {
//        println("Received: $it")
//    }
//    ws.close()
////    ws.writeTextMessage("client msg ${Random.nextInt(100)}")
//    ws.closeHandler {
//        println("The end!")
//        System.exit(0)
//    }
//}