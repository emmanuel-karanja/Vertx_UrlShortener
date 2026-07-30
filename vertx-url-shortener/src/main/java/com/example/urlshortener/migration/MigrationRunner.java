package com.example.urlshortener.migration;

import org.flywaydb.core.Flyway;

public class MigrationRunner {

    public static void main(String[] args) {

        Flyway.configure()
                .dataSource(
                    "jdbc:postgresql://localhost:5432/urlshortener",
                    "postgres",
                    "password"
                )
                .load()
                .migrate();
    }
}