package chapter8;

import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.locks.LockSupport;

/**
 * test
 * {@linkplain LockSupport#park()} and {@linkplain LockSupport#unpark(Thread)}
 * for understand Fiber park() and unpark()
 */
public class TestParkUnpark {
    private static boolean FLAG = true;
    private static List<Integer> datas = new LinkedList<>();

    public static void main(String[] args) throws InterruptedException {
        Thread child = new Thread(TestParkUnpark::run);
        child.start();

        for(int i = 0; i < 10; i++) {
            Thread.sleep(1000);
            datas.add(i);

            System.out.println("-----unpark------");
            LockSupport.unpark(child);  //give child a permit
        }

        Thread.sleep(1000);
        FLAG = false;
    }

    private static void run(){
        while (FLAG) {
            System.out.println("current data size:" + datas.size());

            System.out.println("park");
            LockSupport.park(); //await a permit
        }
    }
}
