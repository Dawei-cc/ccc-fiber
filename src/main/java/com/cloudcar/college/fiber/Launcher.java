package com.cloudcar.college.fiber;

import io.vertx.core.DeploymentOptions;
import io.vertx.core.Vertx;
import io.vertx.core.VertxOptions;
import io.vertx.spi.cluster.hazelcast.HazelcastClusterManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.management.ManagementFactory;
import java.lang.management.RuntimeMXBean;
import java.util.List;

public class Launcher
{
	private final static Logger logger = LoggerFactory.getLogger(Launcher.class);

	public static void main(String[] args)
	{
		logger.info("Launcher starts");

		CloudCarClientUtil.setPort();

		HazelcastClusterManager manager = new HazelcastClusterManager();
		VertxOptions options = new VertxOptions().setClusterManager(manager);
		CloudCarClientUtil.setClusterManager(manager);
		logger.info("Main Thread: " + Thread.currentThread().getName());

		Vertx.clusteredVertx(options, h -> {
			if (h.failed())
			{
				logger.info("Failed to create clustered Vertx");
				System.exit(1);
			}
			logger.info("Vertx Handler Thread: " + Thread.currentThread().getName());
			logger.info("Cluster Vertx Node ID: " + manager.getNodeID());

			Vertx vertx = h.result();
			CloudCarClientUtil.setVertx(vertx);

			vertx.deployVerticle(new ServerInitVerticle(), res -> {
				logger.info("Deploy ServerInitVerticle Handler Thread: " + Thread.currentThread().getName());
				logger.info("ServerInitVerticle Deployment ID: " + res.result());

				vertx.deployVerticle(MainVerticle.class, new DeploymentOptions().setInstances(2), hh -> {
					logger.info("Deploy MainVerticle Handler Thread: " + Thread.currentThread().getName());
					logger.info("MainVerticle Deployment ID: " + hh.result());
				});
				vertx.deployVerticle(MainVerticle.class, new DeploymentOptions().setInstances(2), hh -> {
					logger.info("2nd Deploy MainVerticle Handler Thread: " + Thread.currentThread().getName());
					logger.info("2nd MainVerticle Deployment ID: " + hh.result());
				});
			});
		});

		logger.info("Launcher ends");
	}
}
