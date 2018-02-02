package chapter8;

import io.vertx.core.Handler;

import java.util.concurrent.locks.LockSupport;
import java.util.function.Consumer;

/**
 * use {@linkplain LockSupport#park()} and {@linkplain LockSupport#unpark(Thread)}
 * turn callback-style to Sync-style, without Thread.sleep()
 */
public class TestThreadSyncAwait {

    public static void main(String[] args) throws InterruptedException {
        String result = await(TestThreadSyncAwait::networkRequest);
        System.out.println("result is :" + result);
    }

    //mock network request in callback-style
    private static void networkRequest(Handler<String> handler) {
        new Thread(()->{
            System.out.println("request something...");
            try {
                Thread.sleep(5000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            System.out.println("request finish...");
            handler.handle("abc");
        }).start();
    }

    //turn callback-style to Sync-style
    private static String await(Consumer<Handler<String>> consumer){
        return new AsyncAdapter() {
            @Override
            protected void requestAsync() {
                System.out.println("invoke consumer method");
                consumer.accept(this);
            }
        }.run();
    }

    private static abstract class AsyncAdapter implements Handler<String> {

        private String data;
        private Thread unparkThread;

        public AsyncAdapter() {
            this.unparkThread = Thread.currentThread();
        }

        @Override
        public void handle(String event) {
            System.out.println("call adapter handler()");
            this.data = event;
            System.out.println("unpark thread");
            LockSupport.unpark(unparkThread);
        }

        protected abstract void requestAsync();

        String run(){
            requestAsync();
            System.out.println("park current thread await value");
            LockSupport.park();
            return data;
        }
    }
}
