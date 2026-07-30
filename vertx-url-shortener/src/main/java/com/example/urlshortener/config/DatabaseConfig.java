package com.example.urlshortener.config;

public record DatabaseConfig(
        String host,
        int port,
        String database,
        String user,
        String password
) {}