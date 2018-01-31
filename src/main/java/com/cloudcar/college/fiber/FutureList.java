package com.cloudcar.college.fiber;

import co.paralleluniverse.fibers.Suspendable;
import co.paralleluniverse.strands.SuspendableCallable;
import io.vertx.core.CompositeFuture;
import io.vertx.core.Future;
import io.vertx.core.Handler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import static io.vertx.ext.sync.Sync.awaitEvent;

/**
 * a simple class for append future and await them.
 * feel free to add method if you need.
 * be careful: this class is not thread safe.
 *
 * @author Fulai Zhang
 * @since 2017/11/6.
 */
public class FutureList<T> {
	//default 5 seconds
	private static final long TIMEOUT = 5000;
	private static final Logger logger = LoggerFactory.getLogger(FutureList.class);

	private List<Future<T>> futureList = new ArrayList<>(10);


	public FutureList() {
	}

	/**
	 * append any futures
	 *
	 * @param future
	 */
	@Suspendable
	public void add(Future<T> future) {
		futureList.add(future);
	}

	/**
	 * append future, will running in new fiber
	 *
	 * @param callable
	 */
	@Suspendable
	public void add(SuspendableCallable<T> callable) {
		Future<T> future = CCFiberUtil.fiberExecute(callable);
		futureList.add(future);
	}


	/**
	 * await all futures succeeded.
	 * if any future is failed, then quit await.
	 *
	 * @return all results. include unresolved future.
	 */
	@Suspendable
	public List<T> awaitAll() {
		if (!futureList.isEmpty()) {
			awaitEvent(this::allHandler, TIMEOUT);
		}
		return getResults();
	}

	/**
	 * await any succeeded future.
	 * if any future is succeeded, then quit await.
	 * if all future is failed, then quit await.
	 *
	 * @return all results. include unresolved future.
	 */
	@Suspendable
	public List<T> awaitAny() {
		if (!futureList.isEmpty()) {
			awaitEvent(this::anyHandler, TIMEOUT);
		}
		return getResults();
	}

	/**
	 * await all future complete.
	 * no matter succeeded or failed.
	 * <p>
	 * if all future is failed, then quit await.
	 * if all future is succeeded, then quit await.
	 *
	 * @return all results. failed future will use null instead.
	 */
	@Suspendable
	public List<T> awaitComplete() {
		if (!futureList.isEmpty()) {
			awaitEvent(this::completeHandler, TIMEOUT);
		}
		return getResults();
	}

	/**
	 * get all future results.
	 * if future is unresolved, then use null instead.
	 *
	 * @return
	 */
	@Suspendable
	public List<T> getResults() {
		List<T> result = new ArrayList<T>(futureList.size());
		for (Future<T> f : futureList) {
			if (f.succeeded()) {
				result.add(f.result());
			} else {
				result.add(null);
			}
		}
		return result;
	}

	/**
	 * get all succeed future results.
	 * exclude unresolved future or future result is null.
	 *
	 * @return
	 */
	public List<T> getSucceedResults() {
		List<T> result = new LinkedList<>();
		for (Future<T> f : futureList) {
			if (f.succeeded() && f.result() != null) {
				result.add(f.result());
			}
		}
		return result;
	}

	@Suspendable
	private void allHandler(Handler<Void> handler) {
		long startTime = System.currentTimeMillis();

		CompositeFuture compositeFuture = CompositeFuture.all(new ArrayList<>(futureList));
		compositeFuture.setHandler(ar -> {
			logger.info("wait all {} futures success elapsed {} millis.", compositeFuture.size(), System.currentTimeMillis() - startTime);
			handler.handle(null);
		});
	}

	@Suspendable
	private void anyHandler(Handler<Void> handler) {
		long startTime = System.currentTimeMillis();

		CompositeFuture compositeFuture = CompositeFuture.any(new ArrayList<>(futureList));
		compositeFuture.setHandler(ar -> {
			logger.info("wait any one in {} futures success elapsed {} millis.", compositeFuture.size(), System.currentTimeMillis() - startTime);
			handler.handle(null);
		});
	}

	@Suspendable
	private void completeHandler(Handler<Void> handler) {
		int len = futureList.size();
		AtomicInteger count = new AtomicInteger(0);

		long startTime = System.currentTimeMillis();

		for (int i = 0; i < len; i++) {
			Future<T> future = futureList.get(i);
			future.setHandler(ar -> {
				int val = count.incrementAndGet();
				if (val == len) {
					logger.info("wait all {} futures complete elapsed {} millis.", len, System.currentTimeMillis() - startTime);
					handler.handle(null);
				}
			});
		}
	}
}
