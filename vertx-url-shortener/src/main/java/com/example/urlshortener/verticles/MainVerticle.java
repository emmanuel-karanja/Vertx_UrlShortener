package com.example.urlshortener.verticles;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.Promise;

public class MainVerticle extends AbstractVerticle{
    @Override
    public void start(Promise<Void> startPromise) {

        vertx.deployVerticle(new DatabaseVerticle())
            .compose(id ->
                vertx.deployVerticle(new HttpVerticle()))
            .onSuccess(id -> {
                System.out.println("Application started successfuly");
                startPromise.complete();
            })
            .onFailure(startPromise::fail);
    }
}