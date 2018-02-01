package chapter1;

import io.vertx.core.AbstractVerticle;

public class MyVerticle extends AbstractVerticle {

    @Override
    public void start() throws Exception {
        System.out.println("start verticle");
        //TODO check thread id, are they different ?
        System.out.println("thread is :" + Thread.currentThread().getId());

        //What's the thread type
        System.out.println(Thread.currentThread().getClass());
        //TODO find Hierarchy SubTypes of VertxThread
    }

    @Override
    public void stop() throws Exception {
        System.out.println("stop verticle");
        System.out.println("thread is :" + Thread.currentThread().getId());
    }
}