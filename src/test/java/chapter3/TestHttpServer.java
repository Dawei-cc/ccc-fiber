package chapter3;

import io.vertx.core.Vertx;

/**
 * @author Fulai Zhang
 * @since 2018/2/1.
 */
public class TestHttpServer {
    public static void main(String[] args) {
        Vertx vertx = Vertx.vertx();
        vertx.deployVerticle(new HelloVerticle());
        vertx.setTimer(300000, h->vertx.close());
    }
}
