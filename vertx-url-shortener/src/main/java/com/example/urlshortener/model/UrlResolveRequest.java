package com.example.urlshortener.model;
import jakarta.validation.constraints.NotBlank;


public record UrlResolveRequest(
        @NotBlank(message = "ShortCode is required")
        String shortCode){}