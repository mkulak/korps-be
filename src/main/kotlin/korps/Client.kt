package korps

import io.vertx.core.Vertx
import io.vertx.kotlin.core.http.webSocketAwait
import kotlinx.coroutines.runBlocking
import kotlin.random.Random


fun main() = runBlocking {
    Vertx.vertx().use { vertx ->
        val client = vertx.createHttpClient()
        val rpsClient = RpsClient(vertx, client)
        rpsClient.start()
        Thread.sleep(Long.MAX_VALUE)
    }
}

inline fun Vertx.use(f: (Vertx) -> Unit) {
    try {
        f(this)
    } finally {
        close()
    }
}