package com.example.urlshortener.middleware;
import java.util.UUID;

import io.vertx.core.Handler;
import io.vertx.ext.web.RoutingContext;

public class RequestIdHandler implements Handler<RoutingContext>{

    @Override
    public void handle(RoutingContext ctx){
        String requestId=UUID.randomUUID().toString();

        ctx.put("requestId",requestId);
        ctx.response().putHeader("x-request-id",requestId);

        ctx.next();
    }
}