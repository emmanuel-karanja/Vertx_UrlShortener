package com.example.urlshortener.service;

import com.example.urlshortener.model.UserResponse;
import io.vertx.circuitbreaker.CircuitBreakerOptions;
import io.vertx.core.Future;

import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.util.List;
import java.util.stream.Collectors;

import io.vertx.core.Vertx;
import io.vertx.ext.web.client.WebClient;

import io.vertx.circuitbreaker.CircuitBreaker;

public class UserService implements IUserService{

    private static final Logger _logger=LoggerFactory.getLogger(UserService.class);

    private final WebClient _webClient;

    private final CircuitBreaker _breaker;

    public UserService(WebClient client,CircuitBreaker breaker){
        _webClient=client;
        _breaker=breaker;
    }

    @Override
    public Future<List<UserResponse>> getUsers() {
        return _breaker.execute(promise->{
            _webClient.get(443, "jsonplaceholder.typicode.com","/users")
                    .ssl(true)
                    .timeout(5000)
                    .send()
                    .onSuccess(response-> {

                        if (response.statusCode() != 200) {
                            _logger.error("Fetch users request failed wit code :{}",response.statusCode());
                            promise.fail("Request failed:"+response.statusCode()+ ": " +response.statusMessage());
                            return;
                        }

                        JsonArray users = response.bodyAsJsonArray();

                        _logger.info("Fetched:  {} users",users.size());

                        List<UserResponse> usersList=users.stream().map(item->{
                            var json=(JsonObject) item;

                            return new UserResponse(json.getInteger("id"),
                                    json.getString("name"),
                                    json.getString("username"),
                                    json.getString("email"));

                        }).collect(Collectors.toList());

                        promise.complete(usersList);

                    }).onFailure(err->{
                        _logger.error("Request failed :{}",err.getStackTrace());
                        promise.fail(err);
                    });
        });

    }
}
