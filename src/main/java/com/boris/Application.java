package com.boris;

import com.boris.users.UserDAOImpl;
import com.boris.users.UserService;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.Vertx;
import io.vertx.core.eventbus.EventBus;
import io.vertx.core.http.HttpServer;
import io.vertx.core.json.Json;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.handler.BodyHandler;

public class Application extends AbstractVerticle {


    private int port;



    public Application() {




        if (System.getenv("PORT")!=null){
            this.port = Integer.valueOf(System.getenv("PORT"));
        } else {
            this.port = 8088;
        }
    }

    @Override
    public void start() throws Exception{
        super.start();

        HttpServer server = vertx.createHttpServer();
        Router router = Router.router(vertx);
        EventBus eventBus = vertx.eventBus();


        router.get("/test").handler(context->context.response().end("The test succeed " ));


        router.get("/user/:id").handler(context->{
            String userId = context.pathParam("id");
            JsonObject msg = new JsonObject().put("user_id", userId);
            eventBus.send("users.getUser", msg, result->{
                if (result.succeeded()){
                    //return to user
                    context.response().setStatusCode(200).end(Json.encodePrettily(result.result().body()));
                } else {
                    //display 404 error
                    context.response().setStatusCode(404).end();
                }
            });
        });

        router.delete("/user/:id").handler(context->{
            String userId = context.pathParam("id");
            JsonObject msg = new JsonObject().put("user_id", userId);
            eventBus.send("users.remove", msg);
        });

        router.get("/users").handler(ctx->{
            JsonObject payload = ctx.getBodyAsJson();
            eventBus.send("users.list", payload);
        });


        server.requestHandler(router).listen(8088, httpServerAsyncResult -> {
            if (httpServerAsyncResult.succeeded()){
                System.out.println("Serveer is created");
            }
            else{
                System.out.println(httpServerAsyncResult.cause().getLocalizedMessage());
            }
        });


        router.route("/users").handler(BodyHandler.create());

        router.post("/users").handler(ctx->{
            JsonObject payload = ctx.getBodyAsJson();
            eventBus.send("users.add", payload);
        });
    }

    public static void main(String[] args) {
        Vertx vertx = Vertx.vertx();
        vertx.deployVerticle(new UserService(UserDAOImpl.getInstance()));

        vertx.deployVerticle(new Application());
    }


}
