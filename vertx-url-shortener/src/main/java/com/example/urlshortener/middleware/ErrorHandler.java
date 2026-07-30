package com.example.urlshortener.middleware;

import io.vertx.core.Handler;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.RoutingContext;
import jakarta.validation.ValidationException;
import com.example.urlshortener.exceptions.NotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;



public class ErrorHandler implements Handler<RoutingContext>{

    private static final Logger _logger=LoggerFactory.getLogger(ErrorHandler.class);
    @Override
    public void handle(RoutingContext ctx) {

        Throwable error = ctx.failure();

        _logger.error(error.getStackTrace().toString());

        _logger.error(error.getClass().getName());

        if (error instanceof ValidationException e) {
            ctx.response()
                    .setStatusCode(400)
                    .end(json(error));
        } else if (error instanceof NotFoundException e) {
            ctx.response()
                    .setStatusCode(404)
                    .end(json(error));
        } else {
            _logger.error(error.getMessage());
            ctx.response()
                    .setStatusCode(500)
                    .end(json(error));

        }
    }

    private String json(Throwable error){
        return new JsonObject()
                .put("error", error == null ? "Unknown error" : error.getMessage())
                .encode();
    }
}