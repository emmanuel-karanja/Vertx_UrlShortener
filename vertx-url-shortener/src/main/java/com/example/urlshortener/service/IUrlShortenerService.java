package com.example.urlshortener.service;

import io.vertx.core.json.JsonObject;
import io.vertx.core.Future;
import com.example.urlshortener.model.*;

public interface IUrlShortenerService{

    public Future<JsonObject> shorten(UrlShortenRequest request);
    public Future<JsonObject> resolve(UrlResolveRequest request);
    public Future<JsonObject> health();
}