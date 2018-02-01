package chapter2;

import io.vertx.core.DeploymentOptions;
import io.vertx.core.Vertx;

/**
 * @author Fulai Zhang
 * @since 2018/2/1.
 */
public class TestContext {
    public static void main(String[] args) {
        Vertx vertx = Vertx.vertx();
        vertx.deployVerticle("chapter2.MyVerticle", new DeploymentOptions().setInstances(2));
        vertx.setTimer(5000, h->vertx.close());
    }
}
