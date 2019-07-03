package com.boris;

import com.boris.users.UserDAOImpl;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.Vertx;
import io.vertx.core.VertxOptions;
import io.vertx.core.json.Json;
import io.vertx.core.json.JsonObject;
import io.vertx.core.logging.Logger;
import io.vertx.core.logging.LoggerFactory;
import io.vertx.ext.mongo.FindOptions;
import io.vertx.ext.mongo.MongoClient;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.RoutingContext;

import java.util.List;

public class Application extends AbstractVerticle {


    private static final Logger LOGGER = LoggerFactory.getLogger(Application.class);


    public static MongoClient mongoClient = null;


    private int port;


    private UserDAOImpl userDAO;


    public Application() {


        if (System.getenv("PORT") != null) {
            this.port = Integer.valueOf(System.getenv("PORT"));
        } else {
            this.port = 8088;
        }
    }

    public static void main(String[] args) {
//        Vertx vertx = Vertx.vertx();
//        vertx.deployVerticle(new UserService(UserDAOImpl.getInstance()));
//
//        vertx.deployVerticle(new Application());
//
//
//


        VertxOptions vertxOptions = new VertxOptions();

        vertxOptions.setClustered(true);

        Vertx.clusteredVertx(vertxOptions, results -> {

            if (results.succeeded()) {

                Vertx vertx = results.result();

                vertx.deployVerticle(new Application());
            }

        });


//    	Vertx vertx = Vertx.vertx();

//    	vertx.deployVerticle(new VertxMongoVerticle());
    }


//    @Override
//    public void start() throws Exception {
//        super.start();
//
//        HttpServer server = vertx.createHttpServer();
//        Router router = Router.router(vertx);
//        EventBus eventBus = vertx.eventBus();
//
//
//        router.get("/test").handler(context -> context.response().end("The test succeed "));
//
//
//        router.get("/users/:userId").handler(context -> {
//            String userId = context.pathParam("userId");
//            JsonObject msg = new JsonObject().put("user_id", userId);
//            eventBus.send("users.getUser", msg, result -> {
//                if (result.succeeded()) {
//                    //return to user
//                    context.response().setStatusCode(200).end(Json.encodePrettily(result.result().body()));
//                } else {
//                    //display 404 error
//                    context.response().setStatusCode(404).end();
//                }
//            });
//        });
//
//        router.delete("/users/:id").handler(context -> {
//            String userId = context.pathParam ("id");
//            JsonObject msg = new JsonObject().put("user_id", userId);
//            eventBus.send("users.remove", msg, result -> {
//                if (result.succeeded()) {
//                    //return to user
//                    context.response().setStatusCode(200).end(Json.encodePrettily(result.result().body()));
//                } else {
//                    //display 404 error
//                    context.response().setStatusCode(404).end();
//                }
//            });
//        });
//
//        router.get("/users").handler(ctx -> {
//            JsonObject payload = ctx.getBodyAsJson();
//            eventBus.send("users.list", payload);
//        });
//
//
//        server.requestHandler(router).listen(8088, httpServerAsyncResult -> {
//            if (httpServerAsyncResult.succeeded()) {
//                System.out.println("Serveer is created");
//            } else {
//                System.out.println(httpServerAsyncResult.cause().getLocalizedMessage());
//            }
//        });
//
//
//        router.route("/users").handler(BodyHandler.create());
//
//        router.post("/users").handler(ctx -> {
//            JsonObject payload = ctx.getBodyAsJson();
//            eventBus.send("users.add", payload);
//        });
//    }


    @Override
    public void start() {
        LOGGER.info("Verticle Application Started");

        Router router = Router.router(vertx);

        router.get("/users").handler(this::getAllUsers);
        router.get("/users/:user_id").handler(this::findUserById);


        JsonObject dbConfig = new JsonObject();

        dbConfig.put("connection_string", "mongodb://localhost:27017/user-mongo-db");
//		dbConfig.put("username", "admin");
//		dbConfig.put("password", "password");
//		dbConfig.put("authSource", "MongoTest");
        dbConfig.put("useObjectId", true);

        mongoClient = MongoClient.createShared(vertx, dbConfig);


        vertx.createHttpServer().requestHandler(router::accept).listen(8088);

        vertx.eventBus().consumer("com.boris.my-microservice-vertx-java_rx2_websocket_app", message -> {

            System.out.println("Recevied message: " + message.body());

            message.reply(new JsonObject().put("responseCode", "OK").put("message", "This is your response to your event"));

        });

//        vertx.setTimer(5000, handler ->{
//
//            sendTestEvent();
//
//        });


    }


    private void sendTestEvent() {

        JsonObject testInfo = new JsonObject();

        testInfo.put("info", "Hi");

        System.out.println("Sending message=" + testInfo.toString());

        vertx.eventBus().send("com.boris.my-microservice-vertx-java_rx2_websocket_app", testInfo.toString(), reply -> {

            if (reply.succeeded()) {
                JsonObject replyResults = (JsonObject) reply.result().body();

                System.out.println("Got Reply message=" + replyResults.toString());
            }

        });
    }

    @Override
    public void stop() {
        LOGGER.info("Verticle ApplicationVerticle Stopped");
    }

    private void findUserById(RoutingContext routingContext){


        FindOptions findOptions = new FindOptions();

        findOptions.setLimit(1);

        mongoClient.findWithOptions("users/:id", new JsonObject(), findOptions, results -> {

            try {
                List<JsonObject> objects = results.result();

                if (objects != null && objects.size() != 0) {

                    System.out.println("Got some data len=" + objects.size());

                    JsonObject jsonResponse = new JsonObject();

                    jsonResponse.put("users/:user_id", objects);

                    routingContext.response()
                            .putHeader("content-type", "application/json; charset=utf-8")
                            .setStatusCode(200)
                            .end(Json.encodePrettily(jsonResponse));


                } else {

                    JsonObject jsonResponse = new JsonObject();

                    jsonResponse.put("error", "No users found");

                    routingContext.response()
                            .putHeader("content-type", "application/json; charset=utf-8")
                            .setStatusCode(400)
                            .end(Json.encodePrettily(jsonResponse));

                }

            } catch (Exception e) {
                LOGGER.info("getUserById Failed exception e=", e.getLocalizedMessage());

                JsonObject jsonResponse = new JsonObject();

                jsonResponse.put("error", "Exception and No with that Id found");

                routingContext.response()
                        .putHeader("content-type", "application/json; charset=utf-8")
                        .setStatusCode(400)
                        .end(Json.encodePrettily(jsonResponse));

            }

        });


    }



    private void getAllUsers(RoutingContext routingContext) {


        FindOptions findOptions = new FindOptions();

        //findOptions.setLimit(1);

        mongoClient.findWithOptions("users", new JsonObject(), findOptions, results -> {

            try {
                List<JsonObject> objects = results.result();

                if (objects != null && objects.size() != 0) {

                    System.out.println("Got some data len=" + objects.size());

                    JsonObject jsonResponse = new JsonObject();

                    jsonResponse.put("users", objects);

                    routingContext.response()
                            .putHeader("content-type", "application/json; charset=utf-8")
                            .setStatusCode(200)
                            .end(Json.encodePrettily(jsonResponse));


                } else {

                    JsonObject jsonResponse = new JsonObject();

                    jsonResponse.put("error", "No users found");

                    routingContext.response()
                            .putHeader("content-type", "application/json; charset=utf-8")
                            .setStatusCode(400)
                            .end(Json.encodePrettily(jsonResponse));

                }

            } catch (Exception e) {
                LOGGER.info("getAllUsers Failed exception e=", e.getLocalizedMessage());

                JsonObject jsonResponse = new JsonObject();

                jsonResponse.put("error", "Exception and No users found");

                routingContext.response()
                        .putHeader("content-type", "application/json; charset=utf-8")
                        .setStatusCode(400)
                        .end(Json.encodePrettily(jsonResponse));

            }

        });

    }

}
