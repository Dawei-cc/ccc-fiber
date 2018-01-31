# Vert.x for CloudCar College

## How to run
Basically it can be run with the following command:
```
./gradlew run
```
You can assign the listening port with the following command. The default port is 8080.
```
./gradlew run -Pport=8081
```
## How to use
### 0. Fresh start.
At the beginning, most of codes in Launcher.java are commmented out. The program contains an almost empty main() function. Running the program will exit immediately with 2 lines of logs.
### 1. Create a clusterd Vertx
Uncomment all the lines starting with `//1` in Launcher.java. Now the program will create a clustered vertx. Run it and you can see this time the JVM won't exit. And the following logs indicate the current cluster members.
```
Members [1] {
        Member [10.1.10.113]:5701 - 427409af-60ff-4ae1-a44d-691bcb82edff this
}
```
Run the second instance with command `./gradlew run -Pport=8081`. You can see the cluster memebers become something like this:
```
Members [2] {
        Member [10.1.10.113]:5701 - 427409af-60ff-4ae1-a44d-691bcb82edff this
        Member [10.1.10.113]:5702 - 505e049f-1325-42bd-aaec-baab795b3963
}
```
### 2. Check the handler running thread
Uncomment all the lines starting with `//2` in Launcher.java. Start an instance. You can see some logs like the following:
```
2018-01-29 15:22:25.021 INFO  Launcher:17 - Launcher starts
2018-01-29 15:22:25.085 INFO  Launcher:24 - Main Thread: main
2018-01-29 15:22:25.247 INFO  Launcher:63 - Launcher ends
...
2018-01-29 15:22:29.280 INFO  Launcher:34 - Vertx Handler Thread: vert.x-eventloop-thread-0
2018-01-29 15:22:29.280 INFO  Launcher:35 - Cluster Vertx Node ID: 98886ae2-32f2-4c75-b7e7-93b5fbf8d1c3
```
Notice that the handler may run in different thread with the calling thread. And we can have the NodeID from ClusterManager.
### 3. Deploy ServerInitVerticle
So far we didn't deploy any verticle yet. Uncomment all the lines starting with `//3` in Launcher.java. Now we deploy ServerInitVerticle. In the deployment handler, we can get the deploymentID from the result. In logs, you can see the following:
```
2018-01-29 15:32:52.218 INFO  ServerInitVerticle:24 - Deploy ServerInitVerticle running in: vert.x-eventloop-thread-1
```
This indicates that the ServerInitVerticle will run in a new thread.
### 4. Deploy 2 MainVerticles
Uncomment all the lines starting with `//4` in Launcher.java. Run the instance. And you can see some logs like this:
```
2018-01-29 15:38:24.816 INFO  MainVerticle:19 - Deploying MainVerticle starts in: vert.x-eventloop-thread-2
2018-01-29 15:38:24.816 INFO  MainVerticle:19 - Deploying MainVerticle starts in: vert.x-eventloop-thread-3
2018-01-29 15:38:24.911 INFO  MainVerticle:43 - Deploying MainVerticle ends in: vert.x-eventloop-thread-2
2018-01-29 15:38:24.911 INFO  MainVerticle:43 - Deploying MainVerticle ends in: vert.x-eventloop-thread-3
2018-01-29 15:38:24.911 INFO  Launcher:48 - Deploy MainVerticle Handler Thread: vert.x-eventloop-thread-0
2018-01-29 15:38:24.912 INFO  Launcher:49 - MainVerticle Deployment ID: 5ae04874-fe24-4064-a70a-e0b4fa76900b
```
We deployed 2 instances of MainVerticle now. So they run in 2 differnt threads. But they have only one deploymentID. 
### 5. Deploy 2 more MainVerticles
Uncomment all the lines starting with `//5` in Launcher.java. This time we deploy 2 more MainVerticles. You can see logs like this:
```
2018-01-29 15:43:57.885 INFO  MainVerticle:19 - Deploying MainVerticle starts in: vert.x-eventloop-thread-3
2018-01-29 15:43:57.885 INFO  MainVerticle:19 - Deploying MainVerticle starts in: vert.x-eventloop-thread-2
2018-01-29 15:43:57.886 INFO  MainVerticle:19 - Deploying MainVerticle starts in: vert.x-eventloop-thread-4
2018-01-29 15:43:57.886 INFO  MainVerticle:19 - Deploying MainVerticle starts in: vert.x-eventloop-thread-5
2018-01-29 15:43:57.965 INFO  MainVerticle:43 - Deploying MainVerticle ends in: vert.x-eventloop-thread-5
2018-01-29 15:43:57.965 INFO  MainVerticle:43 - Deploying MainVerticle ends in: vert.x-eventloop-thread-4
2018-01-29 15:43:57.965 INFO  MainVerticle:43 - Deploying MainVerticle ends in: vert.x-eventloop-thread-2
2018-01-29 15:43:57.967 INFO  Launcher:54 - 2nd Deploy MainVerticle Handler Thread: vert.x-eventloop-thread-0
2018-01-29 15:43:57.965 INFO  MainVerticle:43 - Deploying MainVerticle ends in: vert.x-eventloop-thread-3
2018-01-29 15:43:57.969 INFO  Launcher:55 - 2nd MainVerticle Deployment ID: 5fe191bb-4a32-4bd7-8549-3b5edbd6e283
2018-01-29 15:43:57.969 INFO  Launcher:48 - Deploy MainVerticle Handler Thread: vert.x-eventloop-thread-0
2018-01-29 15:43:57.970 INFO  Launcher:49 - MainVerticle Deployment ID: f3065ae5-3bf1-4dff-b06a-8f618da9f778
```
The 2nd MainVerticle deployment will have different deploymentID. And they will run in new threads.
At this point, you can visit http://localhost:8080/about. The Json output will look like this:
```
{
  "cluster" : [ {
    "port" : 8080,
    "nodeID" : "f8303bf4-10ee-4dd2-898a-6378cd762374",
    "deploymentIDs" : {
      "MainVerticle:95a6c607-6bd4-4d5a-8a2a-d568d0a3664a" : [ "vert.x-eventloop-thread-2", "vert.x-eventloop-thread-3" ],
      "MainVerticle:86e8a52f-681a-4287-b194-87d81af549af" : [ "vert.x-eventloop-thread-4", "vert.x-eventloop-thread-5" ],
      "ServerInitVerticle:e87ba259-9162-49cb-a4d1-d0ff66deda43" : [ "vert.x-eventloop-thread-1" ]
    }
  } ]
}
```
### 6. Running sequence
Uncomment all the lines starting with `//6` in Launcher.java. We put the sequence logs 1 to 10 from top to bottom. But the logs will show like this:
```
2018-01-29 15:50:09.792 INFO  Launcher:26 - sequence: 1
2018-01-29 15:50:09.953 INFO  Launcher:61 - sequence: 10
2018-01-29 15:50:13.683 INFO  Launcher:28 - sequence: 2
2018-01-29 15:50:13.684 INFO  Launcher:40 - sequence: 3
2018-01-29 15:50:13.689 INFO  Launcher:59 - sequence: 9
2018-01-29 15:50:13.695 INFO  Launcher:42 - sequence: 4
2018-01-29 15:50:13.763 INFO  Launcher:51 - sequence: 6
2018-01-29 15:50:13.766 INFO  Launcher:57 - sequence: 8
2018-01-29 15:50:13.786 INFO  Launcher:53 - sequence: 7
2018-01-29 15:50:13.789 INFO  Launcher:47 - sequence: 5
```
That means the actually running sequence is not from top to bottom. This will give us a lot of trouble to write and maintain our codes. That's why we will use Sync.
### 7. Check which verticle will perform the request
Open a browser and visit `localhost:8080/test1`. The result will indicate which MainVerticle Object handled the request. Refresh the browser and observe the results. Open an incognito window to do the same thing and observe the results.
### 8. Test sending message to EventBus address
Run multiple instances in localhost. Make sure they use different ports and they are clustered together. Visit `localhost:8080/test2`. /test2 will send a message to an address on EventBus and wait for the reply. The reply contains where the consumer was running on. The response will show where the consumer was running on and where the request handler was running on. Refresh the browser and observe the results.
Also you can see logs like this:
```
2018-01-29 17:33:14.732 INFO  Dispatcher:80 - Verticle runs in: vert.x-eventloop-thread-5
2018-01-29 17:33:14.741 INFO  Dispatcher:82 - EventBus send handler runs in: vert.x-eventloop-thread-5
```
Here the request handler and EventBus handler are running on the same thread.
### 9. Test publishing on EventBus address
Run multiple instances in localhost. Make sure they use different ports and they are clustered together. Visit `localhost:8080/test3`. /test3 will publish on an EventBus address. We can see logs like this:
```
2018-01-29 17:28:22.764 INFO  MainVerticle:39 - [testing:publish] nodeID: f8303bf4-10ee-4dd2-898a-6378cd762374, deploymentID: 86e8a52f-681a-4287-b194-87d81af549af, thread: vert.x-eventloop-thread-5
2018-01-29 17:28:22.764 INFO  MainVerticle:39 - [testing:publish] nodeID: f8303bf4-10ee-4dd2-898a-6378cd762374, deploymentID: 95a6c607-6bd4-4d5a-8a2a-d568d0a3664a, thread: vert.x-eventloop-thread-2
2018-01-29 17:28:22.764 INFO  MainVerticle:39 - [testing:publish] nodeID: f8303bf4-10ee-4dd2-898a-6378cd762374, deploymentID: 86e8a52f-681a-4287-b194-87d81af549af, thread: vert.x-eventloop-thread-4
2018-01-29 17:28:22.764 INFO  MainVerticle:39 - [testing:publish] nodeID: f8303bf4-10ee-4dd2-898a-6378cd762374, deploymentID: 95a6c607-6bd4-4d5a-8a2a-d568d0a3664a, thread: vert.x-eventloop-thread-3
```

