package com.example.urlshortener;

import com.example.urlshortener.model.UrlResolveRequest;
import com.example.urlshortener.model.UrlShortenRequest;
import com.example.urlshortener.service.UrlShortenerService;
import io.vertx.core.Future;
import io.vertx.core.Vertx;
import io.vertx.core.eventbus.EventBus;
import io.vertx.core.eventbus.Message;
import io.vertx.core.json.JsonObject;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class UrlShortenerServiceUnitTests {


    @Mock
    private Vertx vertx;
    @Mock
    private EventBus eventBus;

    @InjectMocks
    private UrlShortenerService service;

    @BeforeEach
    void setup(){
        // Wire together the mocks
        // We use lenient since not all tests need this.
        lenient()
                .when(vertx.eventBus())
                .thenReturn(eventBus);
    }

    @Test
    void shouldShortenUrl(){

        Message<JsonObject> message=mock(Message.class);

        JsonObject response=new JsonObject().put("id",1)
                .put("shortCode","abc123")
                .put("longUrl","https://www.example.com");

        when(message.body()).thenReturn(response);

        when(eventBus.<JsonObject>
                request(eq("url.save"),any(JsonObject.class)))
                .thenReturn(Future.succeededFuture(message));

        JsonObject result=service
                .shorten(new UrlShortenRequest("https://www.example.com"))
                .result();


        assertEquals(1, result.getInteger("id"));
        assertEquals("abc123",result.getString("shortCode"));
        assertEquals("https://www.example.com",result.getString("longUrl"));

        verify(eventBus).request(eq("url.save"),any(JsonObject.class));

    }

    @Test
    void shouldFailValidationForBlankUrl(){
        UrlShortenRequest req=new UrlShortenRequest("");

        Future<JsonObject> future=service.shorten(req);

        // assert validation
        assertTrue(future.failed());
        assertTrue(future.cause().getMessage().contains("URL"));

        // Verify that the event bus is never called
        verify(eventBus,never()).request(anyString(), any(JsonObject.class));
    }

    @Test
    void shouldFailValidationForInvalidUrl(){
        UrlShortenRequest req=new UrlShortenRequest("bad_url");

        Future<JsonObject> future=service.shorten(req);

        assertTrue(future.failed());

        assertEquals("URL must start with http:// or https://", future.cause().getMessage());

        verify(eventBus,never()).request(anyString(),any(JsonObject.class));
    }

    @Test
    void shouldResolveShortCode(){
         JsonObject response=new JsonObject()
                 .put("id",1)
                 .put("shortCode","abc123")
                 .put("longUrl","https://www.example.com");

         Message<JsonObject> message=mock(Message.class);

         when(message.body()).thenReturn(response);

         when(eventBus
                 .<JsonObject>request(eq("url.find"),any(JsonObject.class)))
                 .thenReturn(Future.succeededFuture(message));


         JsonObject result=service
                 .resolve(new UrlResolveRequest("abc123"))
                 .result();

         assertEquals("https://www.example.com", result.getString("longUrl"));

         // Verify the right methods are called.
         verify(eventBus).request(eq("url.find"),any(JsonObject.class));

    }

    @Test
    void shouldFailResolveOnEmptyShortCode(){
        UrlResolveRequest request=new UrlResolveRequest("");

        Future<JsonObject> future=service.resolve(request);

        assertTrue(future.failed());

        verify(eventBus,never()).request(anyString(),any(JsonObject.class));

    }

    @Test
    void shouldReturnHealthStatus(){
        JsonObject response=new JsonObject().put("status","Up");

        Message<JsonObject> message=mock(Message.class);

        when(message.body()).thenReturn(response);

        when(eventBus
                .<JsonObject>request(eq("db.health.check"),any(JsonObject.class)))
                .thenReturn(Future.succeededFuture(message));

        JsonObject result=service.health().result();

        assertEquals("UP",result.getString("status"));


        verify(eventBus).request(eq("db.health.check"),any(JsonObject.class));
    }

    @Test
    void shouldPropagateDatabaseFailure(){
        when(eventBus.<JsonObject>
                request(
                        eq("db.health.check"),
                any(JsonObject.class))).thenReturn(
                Future.failedFuture("Database unavailable")
        );

        Future<JsonObject> future=service.health();

        assertTrue(future.failed());

        assertEquals("Database unavailable",future.cause().getMessage());
    }

    @Test
    void shouldPropagateSaveFailure(){

        when(eventBus
                .<JsonObject>
                        request(
                                eq("url.save"),
                        any(JsonObject.class)))
                .thenReturn(Future.failedFuture("Insert failed"));

        Future<JsonObject> future=service.shorten(new UrlShortenRequest("https://www.example.com"));

        assertTrue(future.failed());

        assertEquals("Insert failed",future.cause().getMessage());
    }

    @Test
    void shouldPropagateFindFailure(){
        when(eventBus.<JsonObject>
                request(eq("url.find"),any(JsonObject.class)))
                .thenReturn(Future.failedFuture("Find failed"));

        Future<JsonObject> future = service.resolve(new UrlResolveRequest("abc123"));

        assertTrue(future.failed());

        assertEquals("Find failed",future.cause().getMessage());
    }
}
