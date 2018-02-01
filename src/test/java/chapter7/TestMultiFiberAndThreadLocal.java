package chapter7;

import co.paralleluniverse.fibers.Fiber;
import co.paralleluniverse.fibers.SuspendExecution;
import co.paralleluniverse.fibers.Suspendable;
import co.paralleluniverse.strands.Strand;
import co.paralleluniverse.strands.concurrent.ReentrantLock;
import com.sun.org.apache.xpath.internal.SourceTree;

import java.util.concurrent.ExecutionException;

//-javaagent:F:\config\quasar-core-0.7.5-jdk8.jar
public class TestMultiFiberAndThreadLocal {

    public static void main(String[] args) throws ExecutionException, InterruptedException {
        //Start 4 Fiber and set different thread locals.
        Fiber<String>[] fibers = new Fiber[4];

        for (int i = 0; i < fibers.length; i++) {
            fibers[i] = new Fiber<>("Fiber"+i, TestMultiFiberAndThreadLocal::spawnChild);
        }

        for (int i = 0; i < fibers.length; i++) {
            fibers[i].start();
        }

        for (int i = 0; i < fibers.length; i++) {
            fibers[i].join();
        }
    }

    private static void spawnChild() throws InterruptedException, SuspendExecution {
        String name = Strand.currentStrand().getName();

        //put thread locals
        TestThreadLocal.put(name);
        TestThreadLocal.putInheritable("inheritable." + name);

        //get parent ThreadLocals in child
        Fiber<Void> childFiber = new Fiber<>(TestMultiFiberAndThreadLocal::childRun);
        try {
            childFiber.start().join();
        } catch (ExecutionException e) {
            e.printStackTrace();
        }
    }

    private static void childRun(){
        System.out.println("ThreadLocal:" + TestThreadLocal.get());
        System.out.println("InheritableThreadLocal:" + TestThreadLocal.getInheritable());
    }

    private static class TestThreadLocal {
        private static InheritableThreadLocal<String> inheritableVariable = new InheritableThreadLocal<>();
        private static ThreadLocal<String> variables = new ThreadLocal<>();

        static void putInheritable(String value) {
            inheritableVariable.set(value);
        }

        static void put(String value) {
            variables.set(value);
        }

        static String get(){
            return variables.get();
        }

        static String getInheritable(){
            return inheritableVariable.get();
        }
    }
}
