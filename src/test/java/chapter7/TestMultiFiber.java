package chapter7;

import co.paralleluniverse.fibers.Fiber;
import co.paralleluniverse.fibers.SuspendExecution;
import co.paralleluniverse.strands.Strand;

import java.util.UUID;
import java.util.concurrent.ExecutionException;

//-javaagent:F:\config\quasar-core-0.7.5-jdk8.jar
public class TestMultiFiber {

    public static void main(String[] args) throws ExecutionException, InterruptedException {
        Fiber<String>[] fibers = new Fiber[10];
        for (int i = 0; i < fibers.length; i++) {
            fibers[i] = new Fiber<>(TestMultiFiber::run);
        }

        for (int i = 0; i < fibers.length; i++) {
            fibers[i].start();
        }

        for (int i = 0; i < fibers.length; i++) {
            fibers[i].join();
        }

        for (int i = 0; i < fibers.length; i++) {
            String result = fibers[i].get();
            System.out.println("fiber " + i +" returned " + result);
        }
    }

    private static String run() throws InterruptedException, SuspendExecution {
        //TODO dose fiber running in same Thread ?
        System.out.println("ThreadId " + Thread.currentThread().getId());
        long millis = Math.round(Math.random() * 1000);
        Strand.sleep(millis);
        return UUID.randomUUID().toString();
    }
}
