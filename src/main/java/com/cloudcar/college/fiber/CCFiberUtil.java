package com.cloudcar.college.fiber;

import co.paralleluniverse.fibers.Fiber;
import co.paralleluniverse.fibers.FiberScheduler;
import co.paralleluniverse.fibers.Suspendable;
import co.paralleluniverse.strands.SuspendableCallable;
import co.paralleluniverse.strands.SuspendableRunnable;
import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.ext.sync.Sync;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by Yong on 7/31/17.
 */
public class CCFiberUtil {

	static private final Logger logger = LoggerFactory.getLogger(CCFiberUtil.class);

	@Suspendable
	public static <T> Handler<T> ccFiberHandler(SuspendableHandler<T> handler) {
		FiberScheduler scheduler = Sync.getContextScheduler();
//        if (Fiber.isCurrentFiber()) {
//            // only inherit thread locals from fiber
//            return p -> new Fiber<Void>(scheduler, () -> handler.handle(p)).inheritThreadLocals().start();
//        }
//        else {
		// not to do anything if not fiber, try to cut down unnecessary, and reduce memory leaking
		return p -> new Fiber<Void>(scheduler, () -> handler.handle(p)).start();
//        }
	}

	/**
	 * create new Fiber, you should call {@linkplain Fiber#start()} to start Fiber.
	 * @param runnable
	 * @return
	 */
	public static Fiber<Void> newFiber(SuspendableRunnable runnable) {
		FiberScheduler scheduler = Sync.getContextScheduler();
		return new Fiber<>(scheduler, runnable);
	}

	/**
	 * execute an callable in new Fiber, <br>
	 * call Sync.await in the callable will not block parent Fiber. <br>
	 * parent Fiber could decide when await the result. <br>
	 * <pre>
	 *     //start running in parallel mode
	 *     Future f1 = fiberExecute(()->getMongoData("a")); //not blocked
	 *     Future f2 = fiberExecute(()->getMongoData("b")); //not blocked
	 *     //do something ...
	 *     Sync.awaitResult(f1::setHandler);    //blocked
	 *     //do something ...
	 *     Sync.awaitResult(f2::setHandler);    //blocked
	 * </pre>
	 *
	 * @param callable
	 * @param <T>      type
	 * @return Future
	 */
	public static <T> Future<T> fiberExecute(SuspendableCallable<T> callable) {
		Future<T> future = Future.future();
		newFiber(() -> {

			try {
				T value = callable.run();
				future.complete(value);
			} catch (Throwable th) {
				future.fail(th);
			}
		}).start();
		return future;
	}

	/**
	 * wrapper runnable with Fiber and start it at once.
	 * @param runnable
	 */
	public static void fiberExecute(SuspendableRunnable runnable) {
		newFiber(runnable).start();
	}
}
