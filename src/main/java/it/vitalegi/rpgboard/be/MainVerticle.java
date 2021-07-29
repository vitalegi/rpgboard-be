package it.vitalegi.rpgboard.be;

import io.reactivex.Completable;
import io.vertx.core.http.HttpMethod;
import io.vertx.reactivex.core.AbstractVerticle;
import io.vertx.reactivex.core.eventbus.EventBus;
import io.vertx.reactivex.ext.web.Router;
import io.vertx.reactivex.ext.web.handler.CorsHandler;
import it.vitalegi.rpgboard.be.handler.AccountAddHandler;
import it.vitalegi.rpgboard.be.handler.AccountFindAllHandler;
import it.vitalegi.rpgboard.be.handler.AccountFindByIdHandler;
import it.vitalegi.rpgboard.be.security.FirebaseJWTAuthenticationHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MainVerticle extends AbstractVerticle {
  Logger log = LoggerFactory.getLogger(MainVerticle.class);

  @Override
  public Completable rxStart() {
    log.info("start");

    vertx.deployVerticle(new AccountVerticle());
    vertx.deployVerticle(new GameVerticle());
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

    FirebaseJWTAuthenticationHandler.init();
    router.route("/api/*").blockingHandler(new FirebaseJWTAuthenticationHandler(), false);

    EventBus eventBus = vertx.eventBus();

    router.get("/api/account").handler(new AccountFindByIdHandler(eventBus));
    router.post("/api/account").handler(new AccountAddHandler(eventBus));
    router.get("/api/accounts").handler(new AccountFindAllHandler(eventBus));

    WebSocketConfig.init(vertx, router);

    return vertx
        .createHttpServer()
        .requestHandler(router)
        .rxListen(Integer.parseInt(System.getenv("PORT")), "0.0.0.0")
        .ignoreElement();
  }
}
