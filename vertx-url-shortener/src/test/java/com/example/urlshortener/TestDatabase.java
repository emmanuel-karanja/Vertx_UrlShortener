package com.example.urlshortener;

import org.flywaydb.core.Flyway;

public final class TestDatabase {

    public static void migrate() {


        Flyway flyway = Flyway.configure()
                .dataSource(
                        "jdbc:postgresql://localhost:5432/url_shortener_test",
                        "postgres",
                        "password")
                .cleanDisabled(false)
                .load();

        // Start from scratch
        flyway.clean();
        flyway.migrate();
    }

    public static void teardown() {
        Flyway flyway = Flyway.configure()
                .dataSource(
                        "jdbc:postgresql://localhost:5432/url_shortener_test",
                        "postgres",
                        "password")
                .cleanDisabled(false)
                .load();

        flyway.clean();
    }
}