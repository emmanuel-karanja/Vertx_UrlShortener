package com.example.urlshortener.service;

import com.example.urlshortener.model.UrlResolveRequest;
import com.example.urlshortener.model.UrlShortenRequest;
import com.example.urlshortener.util.ShortCodeGenerator;
import io.vertx.core.Future;
import io.vertx.core.Vertx;
import io.vertx.core.eventbus.Message;
import io.vertx.core.json.JsonObject;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.ValidationException;
import jakarta.validation.Validator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Set;

public class UrlShortenerService implements IUrlShortenerService {

    private final Vertx vertx;

    private final Validator validator =
            Validation.buildDefaultValidatorFactory()
                    .getValidator();

    private static final Logger logger =
            LoggerFactory.getLogger(UrlShortenerService.class);

    private final ShortCodeGenerator shortCodeGenerator =
            new ShortCodeGenerator();

    public UrlShortenerService(Vertx vertx) {
        this.vertx = vertx;
    }

    @Override
    public Future<JsonObject> shorten(UrlShortenRequest request) {

        logger.info("Shorten request {}", request);

        return validate(request)
                .map(req -> new JsonObject()
                        .put("shortCode", generateShortCode(req.url()))
                        .put("longUrl", req.url())
                )
                .compose(this::save);
    }

    @Override
    public Future<JsonObject> resolve(UrlResolveRequest request) {

        return validate(request)
                .compose(req -> findLongUrl(req.shortCode()));
    }

    @Override
    public Future<JsonObject> health() {

        return vertx.eventBus()
                .<JsonObject>request("db.health.check", new JsonObject())
                .map(Message::body)
                .map(dbHealth ->
                        new JsonObject()
                                .put("status", "UP")
                                .put("database", dbHealth)
                );
    }

    private String generateShortCode(String url) {

        return shortCodeGenerator.generate(url);
    }

    private Future<JsonObject> save(JsonObject request) {

        return vertx.eventBus()
                .<JsonObject>request("url.save", request)
                .map(Message::body);
    }

    private Future<JsonObject> findLongUrl(String shortCode) {

        return vertx.eventBus()
                .<JsonObject>request(
                        "url.find",
                        new JsonObject().put("shortCode", shortCode)
                )
                .map(Message::body);
    }

    private Future<UrlShortenRequest> validate(UrlShortenRequest request) {

        Set<ConstraintViolation<UrlShortenRequest>> violations =
                validator.validate(request);

        if (!violations.isEmpty()) {
            return Future.failedFuture(
                    new ValidationException(
                            violations.iterator().next().getMessage()
                    )
            );
        }
        return Future.succeededFuture(request);
    }

    private Future<UrlResolveRequest> validate(UrlResolveRequest request) {

        Set<ConstraintViolation<UrlResolveRequest>> violations =
                validator.validate(request);

        if (!violations.isEmpty()) {
            return Future.failedFuture(
                    violations.iterator().next().getMessage()
            );
        }

        return Future.succeededFuture(request);
    }
}