# Fiber for CloudCar College

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
This code is based on the ccc-vertx. But this time we added Vertx-Sync extension. So the ServerInitVerticle and MainVerticle are entended from SyncVerticle instead.
### 1. Run on Fiber
Run the program and open a browser to visit `localhost:8080/test1`. You will see Dispatcher::test1 is running on a fiber and the fiber name. Fresh the browser multiple times, the fiber name will change. That means each time the request comes in, a new fiber will be created.
Visit `localhost:8080/test1a`. You'll see this time Dispatcher::test1 is NOT running on any fiber. Check MainVerticle.java the following lines:
```
router.route(HttpMethod.GET, "/test1").handler(Sync.fiberHandler(dispatcher::test1));
router.route(HttpMethod.GET, "/test1a").handler(dispatcher::test1);
```
This is the only different between these 2 endpoints. Here `Sync.fiberHandler()` will create a new fiber and run the handler on that fiber. 
### 2. @Suspendable
Notice that Dispatcher::test1 has no @Suspendable at this point. Uncomment `//2` in Dispatcher::test1. Visit `localhost:8080/test1` again. This time you will see Uninstrument warning in log. Because this time we try to suspend fiber in Dispatcher::test1, but Dispatcher::test1 doesn't have @Suspendable. 
Uncomment `//3` and try again. This time the warning should be gone.
Visit `localhost:8080/test1a` again. This time you will see:
```
Jan 31, 2018 2:35:49 PM io.vertx.ext.web.impl.RoutingContextImplBase
SEVERE: Unexpected exception in route
io.vertx.core.VertxException: java.lang.IllegalThreadStateException: Method called not from within a fiber
        at io.vertx.ext.sync.Sync.awaitEvent(Sync.java:104)
        at com.cloudcar.college.fiber.Dispatcher.test1(Dispatcher.java:68)
        at io.vertx.ext.web.impl.RouteImpl.handleContext(RouteImpl.java:223)
```
So Sync codes need to be run on a fiber. That's required.
### 3. Sync and Async
Visit `localhost:8080/test2`. You can see test2RunningOn and handlerRunningOn are on different fibers. 
Visit `localhost:8080/test2a`. You can see test2aRunningOn and handlerRunningOn are on the same fiber.
Actually, test2 is still using Async coding style which we shall avoid. Test2a has the Sync coding style.
Notice that test2 doesn't have @Suspendable. 
Dispatcher.about() has been refactored into Sync style. Compare it with the Async style code in ccc-vertx project. This function will run from top to bottom. We should write all code in Sync style. There is only one exception that is EventBus consumer. But the handler of EventBus consumer still need to be wrapped by Sync.fiberHandler(). After wrapping, the handler will run on a fiber, but that will start a new fiber chain. This chain could not hook with EventBus.send() or EventBus.publish() fiber.
### 4. Running concurrent jobs & FutureList/FutureMap
Currently we are using the pattern in Dispatcher::test3 to run concurrent jobs which is Dispatcher.request() in this example. Visit `localhost:8080/test3`, you can see Dispatcher.request() is running on a fiber and each request() runs on different fibers. 
FutureList and FutureMap are the new tools to run concurrent jobs. We have an example in Dispatcher.test3a(). Visit `localhost:8080/test3a` and you can see the output is similiar with the one from test3. The code with FutureList/FutureMap is much less than the old style. The most important thing why we need to deprecate the old style is the old style could not keep the ThreadLocal chain. This issue is covered in next section.
### 5. ThreadLocal
Uncommment 3 lines starting with `//4` in test3(), test3a() and request(). Visit `localhost:8080/test3a` and `localhost:8080/test3` again. The threadLocal value could not be retrieved with the old style. This is because eventBus.send() belongs to Vert.x not Sync. When eventBus.send() is running, it puts the handler into Vert.x eventLoop which is running in native thread. After the handler is triggered, CCFiberUtil.ccFiberHandler() kicks off. Even CCFiberUtil.ccFiberHandler() creates a new fiber, but this fiber has no parent. So the code in this new fiber has no ThreadLocal values. FutureList/FutureMap do not have this issue, because FutureList/FutureMap creates new fibers in the parent fiber. So the new fibers can have ThreadLocal values in parent fiber.
### 6. Singleton
A class's constructor could not be suspendable. If you need to initialize some values when construct the singoleton object and fetching the values need to be suspended, this will cause trouble. Also we could not use synchronized on fiber environment. So we provide a good practice for writing singleton. The example is on Singleton.java.
