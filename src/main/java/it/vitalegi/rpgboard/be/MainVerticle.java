package it.vitalegi.rpgboard.be;

import io.reactivex.Completable;
import io.vertx.core.Handler;
import io.vertx.core.http.HttpMethod;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.bridge.PermittedOptions;
import io.vertx.ext.web.handler.sockjs.SockJSBridgeOptions;
import io.vertx.reactivex.core.AbstractVerticle;
import io.vertx.reactivex.core.eventbus.EventBus;
import io.vertx.reactivex.ext.web.Router;
import io.vertx.reactivex.ext.web.RoutingContext;
import io.vertx.reactivex.ext.web.handler.CorsHandler;
import io.vertx.reactivex.ext.web.handler.sockjs.SockJSHandler;
import it.vitalegi.rpgboard.be.handler.AccountAddHandler;
import it.vitalegi.rpgboard.be.handler.AccountFindAllHandler;
import it.vitalegi.rpgboard.be.handler.AccountFindByIdHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class MainVerticle extends AbstractVerticle {
  Logger log = LoggerFactory.getLogger(MainVerticle.class);

  @Override
  public Completable rxStart() {
    log.info("start");

    vertx.deployVerticle(new AccountVerticle());
    Router router = Router.router(vertx);

    router
        .route()
        .handler(
            CorsHandler.create("http://localhost:8080")
                .allowedMethod(HttpMethod.GET)
                .allowedMethod(HttpMethod.POST)
                .allowedMethod(HttpMethod.OPTIONS)
                .allowedHeader("Content-Type")
                .allowedHeader("Access-Control-Request-Method")
                .allowedHeader("Access-Control-Allow-Credentials")
                .allowedHeader("Access-Control-Allow-Origin")
                .allowedHeader("Access-Control-Allow-Headers")
                .allowedHeader("x-requested-with")
                .allowedHeader("origin")
                .allowedHeader("accept")
                .allowCredentials(true));

    EventBus eventBus = vertx.eventBus();
    router.get("/api/account").handler(new AccountFindByIdHandler(eventBus));
    router.post("/api/account").handler(new AccountAddHandler(eventBus));
    router.get("/api/accounts").handler(new AccountFindAllHandler(eventBus));

    SockJSHandler sockJSHandler = SockJSHandler.create(vertx);
    sockJSHandler.bridge(
        new SockJSBridgeOptions()
            .addInboundPermitted(new PermittedOptions().setAddressRegex("websocket.*"))
            .addOutboundPermitted(new PermittedOptions().setAddressRegex("websocket.*")));

    router.route("/eventbus/*").handler(sockJSHandler);
    List<String> names = new ArrayList<>();
    eventBus.consumer("websocket.add-name", event -> {
      log.info("Received a msg {}", event.toString());
      names.add("");

      JsonArray jsonArr = new JsonArray();
      names.forEach(jsonArr::add);
      eventBus.publish("websocket.names", jsonArr);
    });

    return vertx
        .createHttpServer()
        .requestHandler(router)
        .rxListen(Integer.parseInt(System.getenv("PORT")), "0.0.0.0")
        .ignoreElement();
  }
}
