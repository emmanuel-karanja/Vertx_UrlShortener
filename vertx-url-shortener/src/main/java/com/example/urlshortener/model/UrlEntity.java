
package com.example.urlshortener.model;

public record UrlEntity(
        Long id,
        String shortCode,
        String longUrl
) {}