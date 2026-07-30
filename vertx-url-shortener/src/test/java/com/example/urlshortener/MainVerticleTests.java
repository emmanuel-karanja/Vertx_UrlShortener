package com.example.urlshortener;

import com.example.urlshortener.verticles.DatabaseVerticle;
import com.example.urlshortener.verticles.HttpVerticle;
import io.vertx.junit5.VertxExtension;
import io.vertx.junit5.VertxTestContext;
import org.junit.jupiter.api.Test;
import io.vertx.core.Vertx;
import org.junit.jupiter.api.extension.ExtendWith;

import static org.junit.jupiter.api.Assertions.assertNotNull;


@ExtendWith(VertxExtension.class)
public class MainVerticleTests {

    @Test
    void shouldDeploysHttpVerticleSuccessfully(Vertx vertx, VertxTestContext testContext){
        vertx.deployVerticle(new HttpVerticle()).onSuccess(id->{
            assertNotNull(id);

            testContext.completeNow();

        }).onFailure(testContext::failNow);
    }

    @Test
    void shouldDeployDatabaseVerticleSuccessfully(Vertx vertx, VertxTestContext ctx){
        vertx.deployVerticle(new DatabaseVerticle())
                .onSuccess(id->{
                    assertNotNull(id);

                    ctx.completeNow();
                }).onFailure(ctx::failNow);
    }
}
