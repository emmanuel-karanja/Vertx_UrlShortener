package com.example.urlshortener;

import com.example.urlshortener.verticles.DatabaseVerticle;
import com.example.urlshortener.verticles.HttpVerticle;
import com.example.urlshortener.verticles.MainVerticle;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.client.WebClient;
import io.vertx.junit5.VertxExtension;
import io.vertx.junit5.VertxTestContext;
import org.junit.jupiter.api.*;
import io.vertx.core.Vertx;
import org.junit.jupiter.api.extension.ExtendWith;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@ExtendWith(VertxExtension.class)
public class HttpVerticleIntegrationTests {

    private WebClient client;

    @BeforeAll
    static void setupDatabase() {
        TestDatabase.migrate();
    }

    @AfterAll
    static void cleanupDatabase() {
        TestDatabase.teardown();
    }

    @BeforeEach
    void setup(Vertx vertx, VertxTestContext ctx) {
        client = WebClient.create(vertx);

        // Deploy the verticle
        vertx.deployVerticle(new MainVerticle())
                .onSuccess(id -> ctx.completeNow())
                .onFailure(ctx::failNow);
    }

    @Test
    void shouldReturnHealth(Vertx vertx, VertxTestContext ctx) {

        client.get(8080, "localhost", "/api/health")
                .send()
                .onSuccess(response -> {
                    ctx.verify(() -> {
                        assertEquals(200, response.statusCode());

                        JsonObject body = response.bodyAsJsonObject();
                        assertEquals("UP", body.getString("status"));
                    });
                    ctx.completeNow();
                }).onFailure(ctx::failNow);
    }

    @Test
    void shouldCreateUrlShortCode(Vertx vertx, VertxTestContext ctx) {

        JsonObject request = new JsonObject().put("url", "https://www.example.com");

        client.post(8080, "localhost", "/api/shorten")
                .sendJsonObject(request)
                .onSuccess(response -> {

                    System.out.println(response.statusCode());
                    System.out.println(response.bodyAsString());

                    ctx.verify(() -> {
                        assertEquals(200, response.statusCode());

                        JsonObject body = response.bodyAsJsonObject();

                        assertNotNull(body.getString("shortCode"));
                    });
                    ctx.completeNow();
                }).onFailure(ctx::failNow);

    }


    @Test
    void shouldRejectInvalidRequest(Vertx vertx, VertxTestContext ctx){
        JsonObject request=new JsonObject();

        client.post(8080,"localhost","/api/shorten")
                .sendJsonObject(request)
                .onSuccess(response->{
                    System.out.println(response.statusCode());
                    System.out.println(response.bodyAsString());

                    ctx.verify(()->{

                        assertEquals(400, response.statusCode());
                    });
                    ctx.completeNow();
                }).onFailure(ctx::failNow);
    }

    @Test
    void shouldReturn404ForMissingShortCode(Vertx vertx, VertxTestContext ctx){
        client.get(8080,"localhost","/api/resolve/")
                .send()
                .onSuccess(response->{
                    ctx.verify(()->{
                        assertEquals(404, response.statusCode());
                    });

                    ctx.completeNow();
                }).onFailure(ctx::failNow);
    }

    @Test
    void shouldAddRequestIdToResponseHeader(Vertx vetx, VertxTestContext ctx){
        client.get(8080,"localhost","/api/health")
                .send()
                .onSuccess(response->{
                    ctx.verify(()->{
                        assertNotNull(response.getHeader("x-request-id"));
                    });

                    ctx.completeNow();
                }).onFailure(ctx::failNow);
    }
}


