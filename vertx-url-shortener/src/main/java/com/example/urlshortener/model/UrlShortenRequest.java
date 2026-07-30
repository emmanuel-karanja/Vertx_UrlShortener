package com.example.urlshortener.model;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

public record UrlShortenRequest(

        @NotBlank(message = "URL is required")

        @Pattern(
                regexp = "^https?://.+",
                message = "URL must start with http:// or https://"
        )
        String url

) {}