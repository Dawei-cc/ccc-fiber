package com.cloudcar.college.fiber;

import io.vertx.core.Vertx;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.core.spi.cluster.ClusterManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CloudCarClientUtil
{
	private final static Logger logger = LoggerFactory.getLogger(CloudCarClientUtil.class);

	private static Vertx vertx;
	private static ClusterManager clusterManager;
	private static int port;
	// deploymentID -> threadID
	private static Map<String, List<String>> deployment = new HashMap<>();

	private CloudCarClientUtil() {}

	public static void setVertx(Vertx vertx1)
	{
		vertx = vertx1;
	}

	public static Vertx getVertx()
	{
		return vertx;
	}

	public static void setClusterManager(ClusterManager clusterManager1)
	{
		clusterManager = clusterManager1;
	}

	public static ClusterManager getClusterManager()
	{
		return clusterManager;
	}

	public static void setPort()
	{
		try
		{
			String str = System.getProperty("port");
			int p = Integer.parseInt(str);
			port = p;
		}
		catch (Exception e)
		{
			port = 8080;
		}
		finally
		{
			logger.info("Port: " + port);
		}
	}

	public static int getPort()
	{
		return port;
	}

	public static void putDeployment(String deploymentID, String threadID)
	{
		synchronized (deployment)
		{
			if (!deployment.containsKey(deploymentID))
			{
				deployment.put(deploymentID, new ArrayList<>());
			}
			deployment.get(deploymentID).add(threadID);
		}
	}

	public static JsonObject getDeployment()
	{
		JsonObject result = new JsonObject();
		for (Map.Entry<String, List<String>> entry: deployment.entrySet())
		{
			JsonArray threads = new JsonArray(entry.getValue());
			result.put(entry.getKey(), threads);
		}
		return result;
	}
}
