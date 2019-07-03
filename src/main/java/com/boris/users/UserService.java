package com.boris.users;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.eventbus.EventBus;
import io.vertx.core.json.Json;
import io.vertx.core.json.JsonObject;

import java.sql.SQLOutput;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class UserService extends AbstractVerticle {


    private IUserDAO userDAO;

    private List<User> userList = new ArrayList<>();

    public UserService(IUserDAO userDAO) {
        this.userDAO = userDAO;
    }


    @Override
    public void start() throws Exception{
        super.start();

        EventBus eventBus = vertx.eventBus();
        eventBus.consumer("users.getUser").handler(message->{
            JsonObject payload = JsonObject.mapFrom(message.body());
            String userId = payload.getString("user_id");

            vertx.executeBlocking(future->{
                User user = userDAO.findUserById(userId);
                if (user==null){
                    future.fail("Nothing found");
                } else {
                    future.complete(JsonObject.mapFrom(user));
                }
            }, result->{
                if (result.succeeded()){
                    message.reply(result.result());
                } else {
                    message.fail(404, "Nothing found");
                }
            });
        });

        eventBus.consumer("users.add").handler(objectMessage -> {
            JsonObject payload = JsonObject.mapFrom(objectMessage.body());
            User user = Json.decodeValue(payload.toString(),User.class);
            String userId = UUID.randomUUID().toString();
            user.setId(userId);
            vertx.executeBlocking(future->userDAO.add(user),
                    result->System.out.println("Bike added"));
        });

        eventBus.consumer("users.remove").handler(message -> {
            JsonObject payload = JsonObject.mapFrom(message.body());
            String userId = payload.getString("user_id");
            vertx.executeBlocking(future->userDAO.remove(userId),
                    result->System.out.println("User removed"));
        });

        eventBus.consumer("users.list").handler(objectMessage -> {
            JsonObject payload = JsonObject.mapFrom(objectMessage.body());
            vertx.executeBlocking(future->{
                 userList = userDAO.getUserList();
                if (userList==null){
                    future.fail("The list is empty");
                } else {
                    future.complete(JsonObject.mapFrom(userList));
                }
            }, result->{
                if (result.succeeded()){
                    objectMessage.reply(result.result());
                } else {
                    objectMessage.fail(404, "Nothing found");
                }
            });
        });
    }

}
