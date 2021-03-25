package it.vitalegi.rpgboard.be;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.Handler;
import io.vertx.core.Promise;
import io.vertx.core.eventbus.EventBus;
import io.vertx.core.http.ServerWebSocket;
import io.vertx.ext.web.Router;
import it.vitalegi.rpgboard.be.handler.AccountAddHandler;
import it.vitalegi.rpgboard.be.handler.AccountFindAllHandler;
import it.vitalegi.rpgboard.be.handler.AccountFindByIdHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Random;

public class MainVerticle extends AbstractVerticle {
  Logger log = LoggerFactory.getLogger(MainVerticle.class);

  @Override
  public void start(Promise<Void> startPromise) throws Exception {
    log.info("start");

    vertx.deployVerticle(new AccountVerticle());
    Router router = Router.router(vertx);

    EventBus eventBus = vertx.eventBus();

    router.get("/api/account").handler(new AccountFindByIdHandler(eventBus));
    router.post("/api/account").handler(new AccountAddHandler(eventBus));
    router.get("/api/accounts").handler(new AccountFindAllHandler(eventBus));

    vertx
        .createHttpServer()
        .requestHandler(router) //
        .webSocketHandler(configureWebSocket()) //
        .listen(Integer.parseInt(System.getenv("PORT")), "0.0.0.0") //
        .onSuccess(
            server -> {
              log.info("Start server on port {}", server.actualPort());
              startPromise.complete();
            })
        .onFailure(
            cause -> {
              log.error("Failed to start", cause);
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
}
