package com.example.urlshortener.config;

public record AppConfig(
        DatabaseConfig database,
        HttpServerConfig http
) {}