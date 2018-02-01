package chapter3;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.http.HttpServer;
import io.vertx.ext.web.Router;

import java.io.PrintWriter;
import java.io.StringWriter;

/**
 * @author Fulai Zhang
 * @since 2018/2/1.
 */
public class HelloVerticle extends AbstractVerticle {

    @Override
    public void start() throws Exception {
        Router router = Router.router(vertx);

        //1.
        makeHelloRouter(router);

        //2.
        //makeFailureRouter(router);
        //TODO think about : could we have a global routing error handler ?
        //TODO think about : compare `throw Exception` and `write error response` in business logical code, which is better ?

        HttpServer httpServer = vertx.createHttpServer();
        httpServer.requestHandler(router::accept);
        httpServer.listen(8080);
        System.out.println("listen on:" + 8080);
    }

    private void makeHelloRouter(Router router) {
        router.route().handler(routingContext->{
            routingContext.response().setStatusCode(200)
                    .putHeader("Content-Type", "text/html")
                    .setChunked(true)
                    .write("<h1>It works!</h1>").end();
        });
    }

    private void makeFailureRouter(Router router) {
        router.route().handler(routingContext-> {
            int abc = 100 / 0;
            System.out.println(abc);
        });

        router.route().failureHandler(routingContext->{
            Throwable throwable = routingContext.failure();
            StringWriter sw = new StringWriter();
            PrintWriter pw = new PrintWriter(sw);
            throwable.printStackTrace(pw);

            routingContext.response().setStatusCode(500)
                    .putHeader("Content-Type", "text/html")
                    .setChunked(true)
                    .write(sw.toString().replaceAll("\n", "<br>")).end();
        });
    }
}
