package chapter7;

import co.paralleluniverse.fibers.Fiber;
import co.paralleluniverse.fibers.SuspendExecution;
import co.paralleluniverse.strands.Strand;

import java.util.UUID;
import java.util.concurrent.ExecutionException;

//-javaagent:F:\config\quasar-core-0.7.5-jdk8.jar
public class TestFiber {

    public static void main(String[] args) throws ExecutionException, InterruptedException {
        Fiber<String> fiber = new Fiber<>("myfiber", TestFiber::run);
        fiber.start();
        fiber.join();
        // start(), sleep(), join() worked same as Thread
    }

    private static String run() throws InterruptedException, SuspendExecution {
        System.out.println("isCurrentFiber" + Fiber.isCurrentFiber());
        Strand.sleep(1000);
        return UUID.randomUUID().toString();
    }
}
