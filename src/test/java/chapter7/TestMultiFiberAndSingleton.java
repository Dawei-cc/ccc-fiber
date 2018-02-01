package chapter7;

import co.paralleluniverse.fibers.Fiber;
import co.paralleluniverse.fibers.SuspendExecution;
import co.paralleluniverse.fibers.Suspendable;
import co.paralleluniverse.strands.concurrent.ReentrantLock;

import java.util.concurrent.ExecutionException;

//-javaagent:F:\config\quasar-core-0.7.5-jdk8.jar
public class TestMultiFiberAndSingleton {

    //TODO try execute multi times, until two native thread `try lock` at same time.
    public static void main(String[] args) throws ExecutionException, InterruptedException {
        Fiber<String>[] fibers = new Fiber[Runtime.getRuntime().availableProcessors()*2];

        for (int i = 0; i < fibers.length; i++) {
            fibers[i] = new Fiber<>(TestMultiFiberAndSingleton::run);
        }

        for (int i = 0; i < fibers.length; i++) {
            fibers[i].start();
        }

        for (int i = 0; i < fibers.length; i++) {
            fibers[i].join();
        }
    }

    private static void run() throws InterruptedException, SuspendExecution {
        Singleton.getInstance();
    }

    private static class Singleton {
        private static ReentrantLock lock = new ReentrantLock();
        private static Singleton instance;

        @Suspendable
        public static Singleton getInstance(){
            if (instance == null) {
                try {
                    System.out.println("try lock " + Thread.currentThread().getId());
                    lock.lock();
                    System.out.println("locked " + Thread.currentThread().getId());
                    if (instance == null) {
                        System.out.println("new instance " + Thread.currentThread().getId());
                        instance = new Singleton();
                    }
                }finally {
                    System.out.println("unlock " + Thread.currentThread().getId());
                    lock.unlock();
                }
            }
            return instance;
        }
    }
}
