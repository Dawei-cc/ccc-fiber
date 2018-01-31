package com.cloudcar.college.fiber;

import co.paralleluniverse.fibers.SuspendExecution;
import co.paralleluniverse.fibers.Suspendable;

/**
 * this interface is same as {@linkplain io.vertx.core.Handler}.
 * used for quasar instrument optimize.
 *
 * @author Fulai Zhang
 * @since 2017/12/13.
 */
@FunctionalInterface
public interface SuspendableHandler<T> {

	@Suspendable
	void handle(T event) throws SuspendExecution;
}