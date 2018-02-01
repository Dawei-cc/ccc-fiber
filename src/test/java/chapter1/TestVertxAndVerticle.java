package chapter1;

import io.vertx.core.Vertx;

/**
 * @author Fulai Zhang
 * @since 2018/2/1.
 */
public class TestVertxAndVerticle {

    public static void main(String[] args) {
        Vertx vertx = Vertx.vertx();
        //single thread
        vertx.deployVerticle(new MyVerticle());
        vertx.setTimer(5000, h->vertx.close());
    }

}
