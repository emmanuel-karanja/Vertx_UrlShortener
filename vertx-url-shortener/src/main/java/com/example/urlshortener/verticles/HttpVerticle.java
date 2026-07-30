package com.example.urlshortener.verticles;

import io.vertx.circuitbreaker.CircuitBreaker;
import io.vertx.circuitbreaker.CircuitBreakerOptions;
import io.vertx.ext.web.client.WebClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.example.urlshortener.middleware.ErrorHandler;
import com.example.urlshortener.middleware.JsonResponseHeaderHandler;
import com.example.urlshortener.middleware.LoggingHandler;
import com.example.urlshortener.middleware.RequestIdHandler;


import io.vertx.core.AbstractVerticle;
import io.vertx.core.Promise;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.RoutingContext;
import io.vertx.ext.web.handler.BodyHandler;
import com.example.urlshortener.service.*;
import com.example.urlshortener.model.*;

public class HttpVerticle extends AbstractVerticle{

    private static final Logger _logger=LoggerFactory.getLogger(HttpVerticle.class);

    private IUrlShortenerService _urlService;
    private IUserService _userService;

    @Override
    public void start(Promise<Void> startPromise){

       WebClient client= WebClient.create(vertx);
       CircuitBreaker breaker= CircuitBreaker.create("user-service",
                vertx,
                new CircuitBreakerOptions()
                        .setMaxFailures(5)
                        .setTimeout(5000)
                        .setFallbackOnFailure(true));

        // Init service
        _urlService= new UrlShortenerService(vertx);
        _userService=new UserService(client,breaker);

        // Init router
        Router router=Router.router(vertx);

        // Add middleware
        router.route().handler(BodyHandler.create());
        router.route().handler(new RequestIdHandler());
        router.route().handler(new LoggingHandler());
        router.route().handler(new JsonResponseHeaderHandler());




        // Define routes
        router.post("/api/shorten").handler(this::handleShortenUrl);
        router.get("/api/resolve/:short_code").handler(this::handleResolve);
        router.get("/api/health").handler(this::handleHealthCheck);

        router.get("/api/users").handler(this::handleUsers);

        // Define failure handler
        router.route().failureHandler(new ErrorHandler());


        // Create http Server
        vertx.createHttpServer()
                .requestHandler(router)
                .listen(8080)
                .onSuccess(id->{
                    System.out.println("Http Server started listening on 8080");
                    startPromise.complete();
                }).onFailure(err->{
                    System.out.println("Failed with {err}");
                    startPromise.fail(err);
                });
    }

    private void handleHealthCheck(RoutingContext ctx){
        _urlService.health()
                .onSuccess(ctx::json)
                .onFailure(ctx::fail);
    }

    private void handleShortenUrl(RoutingContext ctx){
        JsonObject body=ctx.body().asJsonObject();

        _urlService.shorten(new UrlShortenRequest(body.getString("url")))
                .onSuccess(ctx::json)
                .onFailure(ctx::fail);
    }

    private void handleResolve(RoutingContext ctx){
        String shortCode=ctx.pathParam("short_code");

        _urlService.resolve(new UrlResolveRequest(shortCode))
                .onSuccess(ctx::json)
                .onFailure(ctx::fail);
    }

    private void handleUsers(RoutingContext ctx){
      _userService.getUsers()
              .onSuccess(ctx::json)
              .onFailure(ctx::fail);
    }
}
