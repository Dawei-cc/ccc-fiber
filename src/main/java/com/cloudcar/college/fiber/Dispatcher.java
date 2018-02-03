package com.cloudcar.college.fiber;

import co.paralleluniverse.fibers.Fiber;
import co.paralleluniverse.fibers.Suspendable;
import io.vertx.core.*;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.eventbus.EventBus;
import io.vertx.core.eventbus.Message;
import io.vertx.core.http.HttpMethod;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.sync.Sync;
import io.vertx.ext.web.RoutingContext;
import io.vertx.ext.web.client.HttpRequest;
import io.vertx.ext.web.client.HttpResponse;
import io.vertx.ext.web.client.WebClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

public class Dispatcher
{
	private final static Logger logger = LoggerFactory.getLogger(Dispatcher.class);
	private MainVerticle verticle;

	private static ThreadLocal<String> local = new InheritableThreadLocal<>();

	public Dispatcher(MainVerticle mainVerticle)
	{
		this.verticle = mainVerticle;
	}

	@Suspendable
	public void about(RoutingContext routingContext)
	{
		JsonObject result = new JsonObject();
		List<String> nodes = CloudCarClientUtil.getClusterManager().getNodes();

		FutureList<JsonObject> futureList = new FutureList<>();
		for (String node : nodes)
		{
			futureList.add(() -> getAbout(node));
		}
		List<JsonObject> results = futureList.awaitComplete();

		JsonArray cluster = new JsonArray();
		for (JsonObject jo : results)
		{
			cluster.add(jo);
		}
		result.put("cluster", cluster);

		routingContext
				.response()
				.putHeader("content-type", "application/json; charset=utf-8")
				.end(result.encodePrettily());
	}

	@Suspendable
	private JsonObject getAbout(String node)
	{
		String address = "about:" + node;
		Message<JsonObject> result = Sync.awaitResult(h -> CloudCarClientUtil.getVertx().eventBus().send(address, "", h));
		return result.body();
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

		//4 local.set("Testing ThreadLocal: test3"); //4

		List<Future<JsonObject>> futures = new ArrayList<>();

		for(int i =0; i < 10; i++) {
			Future<JsonObject> future = Future.future();
			Vertx.currentContext().owner().eventBus().send("testing:publish", "", CCFiberUtil.ccFiberHandler(ar -> {
						if (ar.succeeded()) {
							JsonObject res = request();
							future.complete(res);
						} else {
							future.fail(ar.cause());
						}
					}));
			futures.add(future);
		}

		JsonArray requests = new JsonArray();
		for (Future<JsonObject> future : futures) {
			Sync.awaitResult(future::setHandler, 5000);
			if (future.succeeded())
			{
				requests.add(future.result());
			}
		}

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

		JsonObject ret = new JsonObject();
		ret.put("test3RunningOn", runningOn);
		ret.put("requestsRunningOn", requests);
		routingContext
				.response()
				.putHeader("content-type", "application/json; charset=utf-8")
				.end(ret.encodePrettily());
	}

	@Suspendable
	public void test3a(RoutingContext routingContext)
	{
		logger.info("hit /test3a");

		//4 local.set("Testing ThreadLocal: test3a"); //4

		FutureList<JsonObject> futureList = new FutureList<>();
		for (int i = 0; i < 10; i++)
		{
			futureList.add(() -> request());
		}
		futureList.awaitComplete();

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
		ret.put("test3aRunningOn", runningOn);
		JsonArray requests = new JsonArray(futureList.getResults());
		ret.put("requestsRunningOn", requests);
		routingContext
				.response()
				.putHeader("content-type", "application/json; charset=utf-8")
				.end(ret.encodePrettily());
	}

	@Suspendable
	private JsonObject request()
	{
		String url = "https://www.google.com";
		WebClient webClient = WebClient.create(CloudCarClientUtil.getVertx());
		HttpRequest<Buffer> request = webClient.requestAbs(HttpMethod.GET, url);
		AsyncResult<HttpResponse<Buffer>> ar = Sync.awaitEvent(request::send, 5000);
		JsonObject res = new JsonObject();
		res.put("port", CloudCarClientUtil.getPort());
		res.put("nodeID", CloudCarClientUtil.getClusterManager().getNodeID());
		res.put("deploymentID", verticle.deploymentID());
		res.put("thread", Thread.currentThread().getName());
		//4 res.put("threadLocal", local.get()); //4
		res.put("runningOnFiber", Fiber.isCurrentFiber());
		if (Fiber.isCurrentFiber())
		{
			res.put("fiber", Fiber.currentFiber().getName());
		}
		return res;
	}
}
