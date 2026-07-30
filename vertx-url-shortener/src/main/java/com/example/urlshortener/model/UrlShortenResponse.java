package com.example.urlshortener.model;

public record UrlShortenResponse(
        String shortUrl,
        String longUrl
) {}