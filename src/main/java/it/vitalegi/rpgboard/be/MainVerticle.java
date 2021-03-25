package it.vitalegi.rpgboard.be;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.AsyncResult;
import io.vertx.core.Handler;
import io.vertx.core.Promise;
import io.vertx.core.eventbus.EventBus;
import io.vertx.core.eventbus.Message;
import io.vertx.core.http.HttpServerResponse;
import io.vertx.core.http.ServerWebSocket;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.RoutingContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Random;

public class MainVerticle extends AbstractVerticle {
  Logger log = LoggerFactory.getLogger(MainVerticle.class);

  @Override
  public void start(Promise<Void> startPromise) throws Exception {
    log.info("start");

    //vertx.deployVerticle(new BoardVerticle());
    // vertx.deployVerticle(new AccountVerticle());
    vertx.deployVerticle(new AccountVerticle2());
    Router router = Router.router(vertx);
    configureRouter(router);

    vertx
        .createHttpServer()
        .requestHandler(router) //
        .webSocketHandler(configureWebSocket()) //
        .listen(Integer.parseInt(System.getenv("PORT")), "0.0.0.0") //
        .onSuccess(
            server -> {
              System.out.println("Start server on port " + server.actualPort());
              startPromise.complete();
            })
        .onFailure(
            cause -> {
              System.err.println(cause);
              startPromise.fail(cause);
            });
  }

  private Handler<ServerWebSocket> configureWebSocket() {
    return (context) -> {
      context.writeTextMessage("ping");
      context.textMessageHandler(
          (msg) -> {
            System.out.println("Server " + msg);

            if ((new Random()).nextInt(100) == 0) {
              context.close();
            } else {
              context.writeTextMessage("ping2");
            }
          });
    };
  }

  private void configureRouter(Router router) {

    EventBus eventBus = vertx.eventBus();

    router
        .get("/api/account")
        .handler(
            context -> {
              JsonObject message = new JsonObject();
              message.put("id", context.queryParam("id").get(0));
              eventBus.request("account.get", message, reply -> handleResponse(context, reply));
            });
    router
        .post("/api/account")
        .handler(
            context -> {
              JsonObject message = new JsonObject();
              message.put("id", context.queryParam("id").get(0));
              message.put("name", context.queryParam("name").get(0));
              eventBus.request("account.add", message, reply -> handleResponse(context, reply));
            });

    router
        .get("/api/accounts")
        .handler(
            context -> {
              eventBus.request("account.getAll", "", reply -> handleResponse(context, reply));
            });
  }

  private <T> void handleResponse(RoutingContext context, AsyncResult<Message<T>> reply) {
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
}
