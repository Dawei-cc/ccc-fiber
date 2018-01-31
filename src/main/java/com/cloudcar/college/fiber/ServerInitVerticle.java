package com.cloudcar.college.fiber;

import co.paralleluniverse.fibers.Suspendable;
import io.vertx.core.Vertx;
import io.vertx.core.eventbus.EventBus;
import io.vertx.core.json.JsonObject;
import io.vertx.core.spi.cluster.ClusterManager;
import io.vertx.ext.sync.SyncVerticle;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ServerInitVerticle extends SyncVerticle
{
	private final static Logger logger = LoggerFactory.getLogger(ServerInitVerticle.class);
	private String id;

	@Override
	@Suspendable
	public void start() throws Exception
	{
		logger.info("Deploying ServerInitVerticle starts");
		logger.info("Deploy ServerInitVerticle running in: " + Thread.currentThread().getName());

		Vertx vertx = CloudCarClientUtil.getVertx();
		EventBus eventBus = vertx.eventBus();
		ClusterManager clusterManager = CloudCarClientUtil.getClusterManager();
		eventBus.consumer("about:" + clusterManager.getNodeID(),  message -> {
			message.reply(getAboutData());
		});

		CloudCarClientUtil.putDeployment("ServerInitVerticle:" + deploymentID(), Thread.currentThread().getName());
		logger.info("Deploying ServerInitVerticle ends");

	}

	private JsonObject getAboutData()
	{
		JsonObject result = new JsonObject();
		result.put("port", CloudCarClientUtil.getPort());
		result.put("nodeID", CloudCarClientUtil.getClusterManager().getNodeID());
		result.put("deploymentIDs", CloudCarClientUtil.getDeployment());

		return result;
	}

	public void setID(String id)
	{
		this.id = id;
	}

	public String getID()
	{
		return id;
	}
}
