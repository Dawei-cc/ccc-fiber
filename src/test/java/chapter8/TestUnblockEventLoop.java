package chapter8;

import java.util.concurrent.locks.LockSupport;

/**
 * test use
 * {@linkplain LockSupport#park()} and {@linkplain LockSupport#unpark(Thread)}
 * implement an EventLoop without Thread.sleep()
 */
public class TestUnblockEventLoop {
    public static void main(String[] args) throws InterruptedException {
        EventLoop eventLoop = new EventLoop();
        eventLoop.start();

        for(int i = 0; i < 10; i++) {
            Thread.sleep(Math.round(Math.random()*3000));
            eventLoop.submit("task" + i);
        }

        eventLoop.term();
        Thread.sleep(100);
    }

    private static class EventLoop extends Thread {
        private boolean FLAG = true;
        private String taskName;

        void submit(String taskName){
            System.out.println("---submit---");
            this.taskName = taskName;

            System.out.println("unpark");
            LockSupport.unpark(this);
        }

        @Override
        public void run() {
            while (FLAG) {
                System.out.println("event loop parked.");
                LockSupport.park();

                System.out.println("dispatch task: " + taskName);
            }
        }

        void term(){
            FLAG = false;
        }
    }
}
