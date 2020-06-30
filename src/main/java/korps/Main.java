package korps;

import io.vertx.core.Vertx;

public final class Main {
    public static void main(String[] args) {
        System.out.println("hello, world!");
        Vertx.vertx().createHttpServer().requestHandler(req -> {
            req.response()
                    .putHeader("content-type", "text/plain")
                    .end("Hello from Java Vert.x!");
        }).listen(8080, listen -> {
            if (listen.succeeded()) {
                System.out.println("Server listening on http://localhost:8080/");
            } else {
                listen.cause().printStackTrace();
                System.exit(1);
            }
        });
    }
}