package it.vitalegi.rpgboard.be;

import io.reactivex.functions.Consumer;
import io.vertx.core.AsyncResult;
import io.vertx.core.http.HttpServerResponse;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.RoutingContext;
import io.vertx.pgclient.PgConnectOptions;
import io.vertx.reactivex.core.Vertx;
import io.vertx.reactivex.core.eventbus.Message;
import io.vertx.reactivex.pgclient.PgPool;
import io.vertx.sqlclient.PoolOptions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

public class VertxUtil {
  static Logger log = LoggerFactory.getLogger(VertxUtil.class);

  public static Consumer<? super Throwable> handleError(Message<?> msg) {
    return failure -> {
      log.error("", failure);
      msg.reply(
          new JsonObject()
              .put("error", failure.getClass().getName())
              .put("description", failure.getMessage()));
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

  public static PgPool pool(Vertx vertx) {
    PgConnectOptions connectOptions = PgConnectOptions.fromUri(System.getenv("JDBC_DATABASE_URL"));

    // Pool options
    PoolOptions poolOptions = new PoolOptions().setMaxSize(5);

    // Create the pooled client
    return PgPool.pool(vertx, connectOptions, poolOptions);
  }
}
