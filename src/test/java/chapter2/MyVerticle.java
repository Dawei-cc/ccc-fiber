package chapter2;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.Context;

/**
 * @author Fulai Zhang
 * @since 2018/2/1.
 */
public class MyVerticle extends AbstractVerticle {

    @Override
    public void start() throws Exception {
        doseContextIsIndependentInstance();
        doseRunOnContextIsAsync();
        doseContextCallbackOnSameThread();
        //TODO read source code, find out Context is bind to VertxThread
        //TODO find Hierarchy SubTypes of Context
    }

    private void doseContextIsIndependentInstance() {
        Context context = vertx.getOrCreateContext();
        //TODO check hashcode is different ?
        System.out.println(context.hashCode());
    }

    private void doseRunOnContextIsAsync() {
        //TODO check `in context` is print after `end` ?
        Context context = vertx.getOrCreateContext();
        System.out.println("start");
        context.runOnContext(v->{
            System.out.println("in context");
        });
        System.out.println("end");
    }

    private void doseContextCallbackOnSameThread() {
        Thread thread = Thread.currentThread();
        System.out.println(thread.getId());

        Context context = vertx.getOrCreateContext();

        //TODO dose thread changed ?
        for (int i = 0; i < 10; i++) {
            context.runOnContext(v->{
                Thread th = Thread.currentThread();
                if (th != thread) {
                    throw new RuntimeException("not running in same thread");
                } else {
                    System.out.println("same thread:" + th.getId());
                }
            });
        }
    }

}
