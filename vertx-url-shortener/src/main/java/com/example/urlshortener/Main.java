package com.example.urlshortener;

import com.example.urlshortener.verticles.MainVerticle;

import io.vertx.core.Vertx;

public class Main {

    public static void main(String[] args) {

        Vertx vertx = Vertx.vertx();

        vertx.deployVerticle(new MainVerticle())
                .onSuccess(id -> System.out.println("Application started"))
                .onFailure(Throwable::printStackTrace);
    }
}