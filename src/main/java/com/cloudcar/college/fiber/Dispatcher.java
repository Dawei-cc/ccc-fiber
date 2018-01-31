package com.cloudcar.college.fiber;

import co.paralleluniverse.fibers.Fiber;
import co.paralleluniverse.fibers.Suspendable;
import io.vertx.core.CompositeFuture;
import io.vertx.core.Future;
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

	@Suspendable
	public void test1(RoutingContext routingContext)
	{
		logger.info("hit /test1");
		Long tid = Sync.awaitEvent(h -> CloudCarClientUtil.getVertx().setTimer(100, h));
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
		EventBus eventBus = CloudCarClientUtil.getVertx().eventBus();
		logger.info("Verticle runs in: " + Thread.currentThread().getName());
		logger.info("This request runs in Fiber? " + Fiber.isCurrentFiber());
		if (Fiber.isCurrentFiber())
		{
			logger.info("This request runs in Fiber: " + Fiber.currentFiber().getName());
		}
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
//			Sync.awaitEvent(this::job);
			ret.put("runningOn", runningOn);
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
		logger.info("hit /test2");
		EventBus eventBus = CloudCarClientUtil.getVertx().eventBus();
		logger.info("Verticle runs in: " + Thread.currentThread().getName());
		logger.info("This request runs in Fiber? " + Fiber.isCurrentFiber());
		if (Fiber.isCurrentFiber())
		{
			logger.info("This request runs in Fiber: " + Fiber.currentFiber().getName());
		}
		Message<JsonObject> eventbusresult = Sync.awaitResult(h -> eventBus.send("testing:send", "", h)); //Sync.fiberHandler(h -> {
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
//			Sync.awaitEvent(this::job);
		FutureList<JsonObject> futureList = new FutureList<>();
			futureList.add(() -> job());
			futureList.awaitComplete();
			ret.put("runningOn", runningOn);
//			JsonObject result = (JsonObject)h.result().body();
			ret.put("consumerRunningOn", eventbusresult.body());
			routingContext
					.response()
					.putHeader("content-type", "application/json; charset=utf-8")
					.end(ret.encodePrettily());
//		}));
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
