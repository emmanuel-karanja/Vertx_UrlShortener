package com.example.urlshortener.service;
import com.example.urlshortener.model.UserResponse;

import io.vertx.core.Future;

import java.util.List;
public interface IUserService {

    Future<List<UserResponse>> getUsers();
}
