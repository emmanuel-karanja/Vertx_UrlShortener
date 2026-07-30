package com.example.urlshortener.verticles;

import com.example.urlshortener.config.AppConfig;
import com.example.urlshortener.config.DatabaseConfig;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.Promise;
import io.vertx.pgclient.PgConnectOptions;
import io.vertx.sqlclient.Pool;
import io.vertx.sqlclient.PoolOptions;

import io.vertx.core.Future;
import io.vertx.core.eventbus.Message;
import io.vertx.core.json.JsonObject;

import io.vertx.sqlclient.Row;
import io.vertx.sqlclient.Tuple;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DatabaseVerticle extends AbstractVerticle{

    private static final Logger _logger=LoggerFactory.getLogger(DatabaseVerticle.class);

    private Pool _pool;

    @Override
    public void start(Promise<Void> startPromise){

      try{

        createPool();
        // Verify pool
        verifyDatabase()
            .compose(v -> registerHandlers())
            .onSuccess(v -> {
                _logger.info("PostgresVerticle started");
                startPromise.complete();
            })
            .onFailure(err -> {
                _logger.error("PostgresVerticle failed", err);
                startPromise.fail(err);
            });
        }catch(Exception e){
            startPromise.fail(e);
        }
    }

    private void createPool(){

        AppConfig appConfig=config().mapTo(AppConfig.class);

        DatabaseConfig dbConfig=appConfig.database();

        PgConnectOptions connectOptions = new PgConnectOptions()
                .setHost(dbConfig.host())
                .setPort(dbConfig.port())
                .setDatabase(dbConfig.database())
                .setUser(dbConfig.user())
                .setPassword(dbConfig.password());

        PoolOptions poolOptions = new PoolOptions()
                .setMaxSize(10);

        _pool = Pool.pool(
                vertx,
                connectOptions,
                poolOptions
        );
    }


    private Future<Void> registerHandlers(){
        // Register listeners for events

        vertx.eventBus().<JsonObject>consumer("url.save").handler(this::saveUrl);
        vertx.eventBus().<JsonObject>consumer("url.find").handler(this::getLongUrl);
        vertx.eventBus().<JsonObject>consumer("db.health.check").handler(this::healthCheck);

        return Future.succeededFuture();

    }

    private void healthCheck(Message<JsonObject> message){
        _logger.info("Ping health check");

         _pool.query("SELECT 1")
        .execute()
        .onSuccess(result -> {
            message.reply(
                new JsonObject()
                    .put("status", "Up")
            );
        })
        .onFailure(error -> {

            message.fail(
                500,
                error.getMessage()
            );
        });
    }

    private void saveUrl(Message<JsonObject> message){

        JsonObject body = message.body();
        //
        _logger.info("saving Url:{}",body.getString("longUrl"));

        String longUrl=body.getString("longUrl");
        String shortCode=body.getString("shortCode");

        _pool.preparedQuery("""
        INSERT INTO urls(short_code,long_url) VALUES ($1,$2) RETURNING id""")
        .execute(Tuple.of(shortCode,longUrl))
        .onSuccess(rows->{
            Row row=rows.iterator().next();

            JsonObject response=new JsonObject().put("id",row.getLong("id"))
                                                .put("longUrl",longUrl)
                    .put("shortCode",shortCode);

            // Respond
            message.reply(response);
        }).onFailure(err->{
            message.fail(500,err.getMessage());
        });
    }

    private void getLongUrl(Message<JsonObject> message){
        JsonObject body = message.body();
        _logger.info("Fetching url info for shortcode:{}",body.getString("shortCode"));

        String shortCode=body.getString("shortCode");

        _pool.preparedQuery("""
        SELECT * from urls WHERE short_code=$1""")
        .execute(Tuple.of(shortCode))
        .onSuccess(rows->{
            if(!rows.iterator().hasNext()){
                message.fail(404, "short_code sent doesn't represent a valid url in our system." +
                        "Try creating a short code via /shorten");
                return;
            }

            Row row=rows.iterator().next();
            String longUrl=row.getString("long_url");

            // We got it
            _logger.info("Row found for shortcode:{} and longUrl:{}",shortCode,longUrl);

            JsonObject response=new JsonObject().put("longUrl",longUrl)
                    .put("shortCode",shortCode);

            message.reply(response);

        }).onFailure(err->{
            _logger.error(err.toString());
            message.fail(500,err.getMessage());
        });

    }

    private Future<Void> verifyDatabase() {
        return _pool.query("SELECT 1")
                .execute()
                .mapEmpty();
    }

    @Override
    public void stop() {
        _pool.close();
    }
}