package com.cloudcar.college.fiber;

import co.paralleluniverse.fibers.Suspendable;
import io.vertx.core.eventbus.EventBus;
import io.vertx.core.http.HttpMethod;
import io.vertx.core.http.HttpServer;
import io.vertx.core.json.Json;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.sync.Sync;
import io.vertx.ext.sync.SyncVerticle;
import io.vertx.ext.web.Router;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MainVerticle extends SyncVerticle
{
	private final static Logger logger = LoggerFactory.getLogger(MainVerticle.class);

	@Override
	@Suspendable
	public void start()
	{
		logger.info("Deploying MainVerticle starts in: " + Thread.currentThread().getName());

		HttpServer server = vertx.createHttpServer();

		Router router = Router.router(vertx);
		setupRouter(router);

		server.requestHandler(router::accept).listen(CloudCarClientUtil.getPort());

		EventBus eventBus = vertx.eventBus();
		eventBus.consumer("testing:send", h -> {
			JsonObject result = new JsonObject();
			result.put("port", CloudCarClientUtil.getPort());
			result.put("nodeID", CloudCarClientUtil.getClusterManager().getNodeID());
			result.put("deploymentID", deploymentID());
			result.put("thread", Thread.currentThread().getName());
			h.reply(result);
		});

		eventBus.consumer("testing:publish", h -> {
			h.reply(new JsonObject().put("success", true));
		//	logger.info("[testing:publish] nodeID: {}, deploymentID: {}, thread: {}", CloudCarClientUtil.getClusterManager().getNodeID(), deploymentID(), Thread.currentThread().getName());
		});

		CloudCarClientUtil.putDeployment("MainVerticle:" + deploymentID(), Thread.currentThread().getName());
		logger.info("Deploying MainVerticle ends in: " + Thread.currentThread().getName());
	}

	private void setupRouter(Router router)
	{
		Dispatcher dispatcher = new Dispatcher(this);
		router.route(HttpMethod.GET, "/about").handler(Sync.fiberHandler(dispatcher::about));
		router.route(HttpMethod.GET, "/test1").handler(Sync.fiberHandler(dispatcher::test1));
		router.route(HttpMethod.GET, "/test1a").handler(dispatcher::test1);
		router.route(HttpMethod.GET, "/test2").handler(Sync.fiberHandler(dispatcher::test2));
		router.route(HttpMethod.GET, "/test2a").handler(Sync.fiberHandler(dispatcher::test2a));
		router.route(HttpMethod.GET, "/test3").handler(Sync.fiberHandler(dispatcher::test3));
		router.route(HttpMethod.GET, "/test3a").handler(Sync.fiberHandler(dispatcher::test3a));
	}
}
