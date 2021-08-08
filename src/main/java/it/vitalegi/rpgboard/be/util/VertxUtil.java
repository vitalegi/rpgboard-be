package it.vitalegi.rpgboard.be.util;

import io.reactivex.functions.Consumer;
import io.reactivex.functions.Function;
import io.vertx.core.AsyncResult;
import io.vertx.core.http.HttpServerResponse;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.RoutingContext;
import io.vertx.pgclient.PgConnectOptions;
import io.vertx.pgclient.SslMode;
import io.vertx.reactivex.core.Vertx;
import io.vertx.reactivex.core.eventbus.Message;
import io.vertx.reactivex.pgclient.PgPool;
import io.vertx.sqlclient.PoolOptions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class VertxUtil {
  static Logger log = LoggerFactory.getLogger(VertxUtil.class);

  public static Consumer<? super Throwable> handleError(Message<?> msg) {
    return failure -> {
      log.error(failure.getMessage(), failure);
      msg.fail(
          500,
          new JsonObject()
              .put("error", failure.getClass().getName())
              .put("description", failure.getMessage())
              .toString());
    };
  }

  public static <E> JsonArray jsonMap(List<E> list) {
    JsonArray jsonArr = new JsonArray();
    list.stream().map(JsonObject::mapFrom).forEach(jsonArr::add);
    return jsonArr;
  }

  public static <T> void handleResponse(
      RoutingContext context, AsyncResult<io.vertx.core.eventbus.Message<T>> reply) {
    if (reply.succeeded()) {
      HttpServerResponse response = context.response();
      response.putHeader("content-type", "application/json; charset=utf-8");
      response.end(reply.result().body().toString());
    } else {
      context.response().setStatusCode(500);
      context.json(
          new JsonObject()
              .put(
                  "error", //
                  reply.cause().getClass().getName()) //
              .put("description", reply.cause().getMessage()));
    }
  }

  public static PgPool pool(Vertx vertx, JsonObject config) {
    SslMode sslMode = SslMode.valueOf(config.getJsonObject("database").getString("sslMode"));
    PgConnectOptions connectOptions = PgConnectOptions.fromUri(System.getenv("DATABASE_URL"));
    connectOptions.setSslMode(sslMode);
    connectOptions.setTrustAll(true);

    // Pool options
    PoolOptions poolOptions = new PoolOptions().setMaxSize(5);

    // Create the pooled client
    return PgPool.pool(vertx, connectOptions, poolOptions);
  }

  public static <E> Function<E, E> logEntry(
      String msg, java.util.function.Function<E, Object>... reducers) {
    return entry -> {
      String value =
          Arrays.stream(reducers)
              .map(r -> r.apply(entry))
              .map(Object::toString)
              .collect(Collectors.joining(", "));
      log.info("{}: {}", msg, value);
      return entry;
    };
  }

  public static <E> Function<E, E> debug(
      String msg, java.util.function.Function<E, Object>... reducers) {
    return entry -> {
      String value =
          Arrays.stream(reducers)
              .map(r -> r.apply(entry))
              .map(Object::toString)
              .collect(Collectors.joining(", "));
      log.debug("{}: {}", msg, value);
      return entry;
    };
  }
}
