package chapter1;

import io.vertx.core.DeploymentOptions;
import io.vertx.core.Vertx;

/**
 * @author Fulai Zhang
 * @since 2018/2/1.
 */
public class TestMultiThreadVerticle {

    public static void main(String[] args) {
        Vertx vertx = Vertx.vertx();
        //multi thread
        vertx.deployVerticle("chapter1.MyVerticle", new DeploymentOptions().setInstances(2));

        vertx.setTimer(5000, h->vertx.close());
    }

}