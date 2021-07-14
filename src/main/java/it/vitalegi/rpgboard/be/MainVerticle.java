package it.vitalegi.rpgboard.be;

import io.reactivex.Completable;
import io.vertx.core.Handler;
import io.vertx.core.http.HttpMethod;
import io.vertx.reactivex.core.AbstractVerticle;
import io.vertx.reactivex.core.eventbus.EventBus;
import io.vertx.reactivex.core.http.ServerWebSocket;
import io.vertx.reactivex.ext.web.Router;
import io.vertx.reactivex.ext.web.handler.BodyHandler;
import io.vertx.reactivex.ext.web.handler.CorsHandler;
import io.vertx.reactivex.ext.web.handler.graphql.GraphQLHandler;
import it.vitalegi.rpgboard.be.graphql.GraphQLHandlerBuilder;
import it.vitalegi.rpgboard.be.handler.AccountAddHandler;
import it.vitalegi.rpgboard.be.handler.AccountFindAllHandler;
import it.vitalegi.rpgboard.be.handler.AccountFindByIdHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Random;

public class MainVerticle extends AbstractVerticle {
  Logger log = LoggerFactory.getLogger(MainVerticle.class);

  @Override
  public Completable rxStart() {
    log.info("start");

    vertx.deployVerticle(new AccountVerticle());
    Router router = Router.router(vertx);

    router
        .route()
        .handler(BodyHandler.create())
        .handler(
            CorsHandler.create("http://localhost:8080")
                .allowedMethod(HttpMethod.GET)
                .allowedMethod(HttpMethod.POST)
                .allowedMethod(HttpMethod.OPTIONS)
                .allowedHeader("Content-Type"));
    router
        .route("/graphql")
        .handler(GraphQLHandler.create(new GraphQLHandlerBuilder().createGraphQL(vertx)));

    EventBus eventBus = vertx.eventBus();
    router.get("/api/account").handler(new AccountFindByIdHandler(eventBus));
    router.post("/api/account").handler(new AccountAddHandler(eventBus));
    router.get("/api/accounts").handler(new AccountFindAllHandler(eventBus));

    return vertx
        .createHttpServer()
        .requestHandler(router)
        .webSocketHandler(configureWebSocket())
        .rxListen(Integer.parseInt(System.getenv("PORT")), "0.0.0.0")
        .ignoreElement();
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
