package com.cloudcar.college.fiber;

import co.paralleluniverse.fibers.Fiber;
import co.paralleluniverse.fibers.Suspendable;
import io.vertx.core.CompositeFuture;
import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.core.Vertx;
import io.vertx.core.eventbus.EventBus;
import io.vertx.core.eventbus.Message;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.sync.Sync;
import io.vertx.ext.web.RoutingContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

public class Dispatcher
{
	private final static Logger logger = LoggerFactory.getLogger(Dispatcher.class);
	private MainVerticle verticle;

	public Dispatcher(MainVerticle mainVerticle)
	{
		this.verticle = mainVerticle;
	}

	@Suspendable
	public void about(RoutingContext routingContext)
	{
		JsonObject result = new JsonObject();
		List<String> nodes = CloudCarClientUtil.getClusterManager().getNodes();
		EventBus eventBus = CloudCarClientUtil.getVertx().eventBus();
		List<Future> futures = new ArrayList<>();
		for (String node : nodes)
		{
			Future<JsonObject> future = Future.future();
			futures.add(future);
			eventBus.send("about:"+node, "", h -> {
				JsonObject message = (JsonObject)h.result().body();
				future.complete(message);
			});
		}
		CompositeFuture.join(futures).setHandler(h -> {
			if (h.succeeded())
			{
				JsonArray cluster = new JsonArray();
				for (int i=0;i<h.result().size();i++)
				{
					JsonObject r = h.result().resultAt(i);
					cluster.add(r);
				}
				result.put("cluster", cluster);
				routingContext
						.response()
						.putHeader("content-type", "application/json; charset=utf-8")
						.end(result.encodePrettily());
			}
		});
	}

	//3 @Suspendable //3
	public void test1(RoutingContext routingContext)
	{
		logger.info("hit /test1");
		//2 Long tid = Sync.awaitEvent(h -> CloudCarClientUtil.getVertx().setTimer(100, h)); //2
		JsonObject result = new JsonObject();
		result.put("port", CloudCarClientUtil.getPort());
		result.put("nodeID", CloudCarClientUtil.getClusterManager().getNodeID());
		result.put("deploymentID", verticle.deploymentID());
		result.put("thread", Thread.currentThread().getName());
		result.put("runningOnFiber", Fiber.isCurrentFiber());
		if (Fiber.isCurrentFiber())
		{
			result.put("fiber", Fiber.currentFiber().getName());
		}
		routingContext
				.response()
				.putHeader("content-type", "application/json; charset=utf-8")
				.end(result.encodePrettily());
	}

	public void test2(RoutingContext routingContext)
	{
		logger.info("hit /test2");
		JsonObject test2 = new JsonObject();
		test2.put("port", CloudCarClientUtil.getPort());
		test2.put("nodeID", CloudCarClientUtil.getClusterManager().getNodeID());
		test2.put("deploymentID", verticle.deploymentID());
		test2.put("thread", Thread.currentThread().getName());
		test2.put("runningOnFiber", Fiber.isCurrentFiber());
		if (Fiber.isCurrentFiber())
		{
			test2.put("fiber", Fiber.currentFiber().getName());
		}
		EventBus eventBus = CloudCarClientUtil.getVertx().eventBus();
		eventBus.send("testing:send", "", Sync.fiberHandler(h -> {
			logger.info("EventBus send handler runs in: " + Thread.currentThread().getName());
			JsonObject ret = new JsonObject();
			JsonObject runningOn = new JsonObject();
			runningOn.put("port", CloudCarClientUtil.getPort());
			runningOn.put("nodeID", CloudCarClientUtil.getClusterManager().getNodeID());
			runningOn.put("deploymentID", verticle.deploymentID());
			runningOn.put("thread", Thread.currentThread().getName());
			runningOn.put("runningOnFiber", Fiber.isCurrentFiber());
			if (Fiber.isCurrentFiber())
			{
				runningOn.put("fiber", Fiber.currentFiber().getName());
			}
			ret.put("test2RunningOn", test2);
			ret.put("handlerRunningOn", runningOn);
			JsonObject result = (JsonObject)h.result().body();
			ret.put("consumerRunningOn", result);
			routingContext
					.response()
					.putHeader("content-type", "application/json; charset=utf-8")
					.end(ret.encodePrettily());
		}));
	}

	@Suspendable
	public void test2a(RoutingContext routingContext)
	{
		logger.info("hit /test2a");
		JsonObject test2a = new JsonObject();
		test2a.put("port", CloudCarClientUtil.getPort());
		test2a.put("nodeID", CloudCarClientUtil.getClusterManager().getNodeID());
		test2a.put("deploymentID", verticle.deploymentID());
		test2a.put("thread", Thread.currentThread().getName());
		test2a.put("runningOnFiber", Fiber.isCurrentFiber());
		if (Fiber.isCurrentFiber())
		{
			test2a.put("fiber", Fiber.currentFiber().getName());
		}
		EventBus eventBus = CloudCarClientUtil.getVertx().eventBus();
		Message<JsonObject> eventBusResult = Sync.awaitResult(h -> eventBus.send("testing:send", "", h));
		JsonObject ret = new JsonObject();
		JsonObject runningOn = new JsonObject();
		runningOn.put("port", CloudCarClientUtil.getPort());
		runningOn.put("nodeID", CloudCarClientUtil.getClusterManager().getNodeID());
		runningOn.put("deploymentID", verticle.deploymentID());
		runningOn.put("thread", Thread.currentThread().getName());
		runningOn.put("runningOnFiber", Fiber.isCurrentFiber());
		if (Fiber.isCurrentFiber())
		{
			runningOn.put("fiber", Fiber.currentFiber().getName());
		}
		ret.put("test2aRunningOn", test2a);
		ret.put("handlerRunningOn", runningOn);
		ret.put("consumerRunningOn", eventBusResult.body());
		routingContext
				.response()
				.putHeader("content-type", "application/json; charset=utf-8")
				.end(ret.encodePrettily());
	}

	@Suspendable
	public void test3(RoutingContext routingContext)
	{
		logger.info("hit /test3");
		EventBus eventBus = CloudCarClientUtil.getVertx().eventBus();
		eventBus.publish("testing:publish", "");
		JsonObject runningOn = new JsonObject();
		runningOn.put("port", CloudCarClientUtil.getPort());
		runningOn.put("nodeID", CloudCarClientUtil.getClusterManager().getNodeID());
		runningOn.put("deploymentID", verticle.deploymentID());
		runningOn.put("thread", Thread.currentThread().getName());
		routingContext
				.response()
				.putHeader("content-type", "application/json; charset=utf-8")
				.end(runningOn.encodePrettily());
	}

	@Suspendable
	private Long handlerJob(Handler<Long> handler)
	{
		logger.info("Running in Fiber? " + Fiber.isCurrentFiber());
		if (Fiber.isCurrentFiber())
		{
			logger.info("Running in Fiber: " + Fiber.currentFiber().getName());
		}
		handler.handle(null);
		return 0L;
	}

	@Suspendable
	private JsonObject job()
	{
		logger.info("Running in Fiber? " + Fiber.isCurrentFiber());
		if (Fiber.isCurrentFiber())
		{
			logger.info("Running in Fiber: " + Fiber.currentFiber().getName());
		}
		Vertx vertx = CloudCarClientUtil.getVertx();
		Long tid = Sync.awaitEvent(h -> vertx.setTimer(100, h));
		return new JsonObject();
	}
}
