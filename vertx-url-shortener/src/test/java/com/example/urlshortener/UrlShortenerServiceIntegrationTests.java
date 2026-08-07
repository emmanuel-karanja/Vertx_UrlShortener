package com.example.urlshortener;

import com.example.urlshortener.model.UrlResolveRequest;
import com.example.urlshortener.model.UrlShortenRequest;
import com.example.urlshortener.model.UrlShortenResponse;
import com.example.urlshortener.service.IUrlShortenerService;
import com.example.urlshortener.service.UrlShortenerService;
import com.example.urlshortener.verticles.DatabaseVerticle;
import com.example.urlshortener.verticles.MainVerticle;
import io.vertx.core.Future;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;
import io.vertx.junit5.VertxExtension;
import io.vertx.junit5.VertxTestContext;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(VertxExtension.class)
public class UrlShortenerServiceIntegrationTests {

    private static final Logger _logger= LoggerFactory.getLogger(UrlShortenerServiceIntegrationTests.class);
    private IUrlShortenerService service;

    @BeforeEach
    void setup(Vertx vertx, VertxTestContext ctx){

        vertx.deployVerticle(new MainVerticle())
                .onSuccess(id->ctx.completeNow())
                .onFailure(ctx::failNow);

        service=new UrlShortenerService(vertx);
    }

    @BeforeAll
    static void setupDatabase() {
        TestDatabase.migrate();
    }

    @AfterAll
    static void cleanupDatabase() {
        TestDatabase.teardown();
    }

    @Test
    void shouldShortenUrl(Vertx vertx, VertxTestContext ctx){
        UrlShortenRequest request=new UrlShortenRequest("https://www.example.com");

        service.shorten(request)
                .onSuccess(response->{
                    ctx.verify(()->{
                        assertNotNull(response);
                        assertNotNull(response.getString("shortCode"));
                        assertTrue(response.getString("shortCode").length()>0);
                    });
                    ctx.completeNow();
                }).onFailure(ctx::failNow);

    }

    @Test
    void shouldGenerate7DigitShortCodeInBase62Alphabet(Vertx vertx, VertxTestContext ctx){
        service.shorten(new UrlShortenRequest("https://www.good-example.com"))
                .onSuccess(response->{
                    ctx.verify(()->{
                        String shortCode=response.getString("shortCode");
                        assertEquals(7, shortCode.length());
                        assertTrue(shortCode.matches("[A-Za-z0-9]+"));
                    });

                    ctx.completeNow();
                }).onFailure(ctx::failNow);
    }

    @Test
    void shouldInvalidateEmptyShortUrl(Vertx vertx, VertxTestContext ctx){

       service.shorten(new UrlShortenRequest(""))
               .onFailure(err->{
                   ctx.verify(()->{
                       assertTrue(err.getMessage().contains("URL"));
                   });

                   ctx.completeNow();
               }).onSuccess(response->{
                   ctx.failNow( new AssertionError("Expected failure."));
               });
    }

    @Test
    void shouldFailOnInvalidUrl(Vertx vertx, VertxTestContext ctx){
        service.shorten(new UrlShortenRequest("bad_url.com"))
                .onFailure(err->{
                    ctx.verify(()->{
                        assertEquals("URL must start with http:// or https://", err.getMessage());
                    });

                    ctx.completeNow();
                }).onSuccess(response->{
                    ctx.failNow( new AssertionError("Expected failure."));
                });
    }

    @Test
    void shouldReturnHealthSuccessfully(Vertx vertx, VertxTestContext ctx){
        service.health()
                .onSuccess(response->{
                    ctx.verify(()->{

                            assertNotNull(response);
                            assertEquals("UP", response.getString("status"));
                    });

                    ctx.completeNow();
                }).onFailure(ctx::failNow);
    }

    @Test
    void shouldResolveUrlSuccessfully(Vertx vertx, VertxTestContext ctx){
        service.shorten(new UrlShortenRequest("http://good-url-com"))
                .compose(shortened->
                        service.resolve(new UrlResolveRequest(shortened.getString("shortCode"))))
                .onSuccess(resolved->{
                    ctx.verify(()->{
                       assertEquals("http://good-url-com", resolved.getString("longUrl"));
                    });

                    ctx.completeNow();
                }).onFailure(ctx::failNow);
    }

    @Test
    void shouldFailResolveWhenShortCodeIsEmpty(Vertx vertx, VertxTestContext ctx){
        service.resolve(new UrlResolveRequest(""))
                .onFailure(err->{
                    ctx.verify(()->{
                        assertEquals("ShortCode is required", err.getMessage());
                    });

                    ctx.completeNow();
                }).onSuccess(response-> ctx.failNow(new AssertionError("Expected to fail.")));
    }
}
