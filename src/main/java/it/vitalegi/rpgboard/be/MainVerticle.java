package it.vitalegi.rpgboard.be;

import io.reactivex.Completable;
import io.reactivex.Single;
import io.vertx.config.ConfigRetrieverOptions;
import io.vertx.config.ConfigStoreOptions;
import io.vertx.core.http.HttpMethod;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.bridge.PermittedOptions;
import io.vertx.ext.web.handler.sockjs.SockJSBridgeOptions;
import io.vertx.reactivex.config.ConfigRetriever;
import io.vertx.reactivex.core.AbstractVerticle;
import io.vertx.reactivex.ext.web.Router;
import io.vertx.reactivex.ext.web.handler.BodyHandler;
import io.vertx.reactivex.ext.web.handler.CorsHandler;
import io.vertx.reactivex.ext.web.handler.sockjs.SockJSHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.stream.Collectors;

public class MainVerticle extends AbstractVerticle {
  Logger log = LoggerFactory.getLogger(MainVerticle.class);

  @Override
  public Completable rxStart() {
    log.info("start");
    vertx.exceptionHandler(
        e -> {
          log.error("Generic exception123 {}:{}", e.getClass().getName(), e.getMessage(), e);
        });
    log.info("Setup properties");
    ConfigStoreOptions commonFileStore =
        new ConfigStoreOptions()
            .setType("file")
            .setFormat("json")
            .setOptional(false)
            .setConfig(new JsonObject().put("path", "config.json"));

    ConfigStoreOptions fileStore =
        new ConfigStoreOptions()
            .setType("file")
            .setFormat("json")
            .setOptional(false)
            .setConfig(new JsonObject().put("path", "config-" + System.getenv("ENV") + ".json"));

    ConfigRetrieverOptions options =
        new ConfigRetrieverOptions().addStore(commonFileStore).addStore(fileStore);

    Single<JsonObject> rxConfig = ConfigRetriever.create(vertx, options).rxGetConfig();
    rxConfig.doOnError(
        error -> {
          log.error("Failed to load props", error);
          System.exit(0);
        });
    return rxConfig.flatMapCompletable(
        config -> {
          log.info("Setup properties done");
          vertx.deployVerticle(new AccountVerticle());
          vertx.deployVerticle(new GameVerticle());
          Router router = Router.router(vertx);

          setHeaders(router);

          SockJSHandler sockJSHandler = SockJSHandler.create(vertx);
          Router sockJsRouter = sockJSHandler.bridge(getBridgeOptions());
          router.mountSubRouter("/eventbus", sockJsRouter);

          setHeaders(router);

          Completable out =
              vertx
                  .createHttpServer()
                  .requestHandler(router)
                  .rxListen(Integer.parseInt(System.getenv("PORT")), "0.0.0.0")
                  .ignoreElement();

          vertx.setPeriodic(
              1000, t -> vertx.eventBus().publish("external.outgoing.games", "hello!"));
          return out;
        });
  }

  protected CorsHandler corsHandler(JsonObject config) {
    JsonObject sec = config.getJsonObject("security");
    log.info("Apply CORS configuration: {}", sec.toString());
    CorsHandler corsHandler = CorsHandler.create();

    corsHandler.addOrigins(
        sec.getJsonArray("cors").stream().map(Object::toString).collect(Collectors.toList()));

    corsHandler.allowedMethods(
        sec.getJsonArray("allowedMethods").stream()
            .map(Object::toString)
            .map(HttpMethod::valueOf)
            .collect(Collectors.toSet()));

    corsHandler.allowedHeaders(
        sec.getJsonArray("allowedHeaders").stream()
            .map(Object::toString)
            .collect(Collectors.toSet()));

    corsHandler.allowCredentials(sec.getBoolean("allowCredentials"));
    return corsHandler;
  }

  private SockJSBridgeOptions getBridgeOptions() {
    return new SockJSBridgeOptions()
        .addOutboundPermitted(new PermittedOptions().setAddressRegex(".*"))
        .addInboundPermitted(new PermittedOptions().setAddressRegex(".*"));
  }

  private void setHeaders(Router router) {
    router
        .route()
        .handler(
            CorsHandler.create(".*.")
                .allowedMethod(HttpMethod.GET)
                .allowedMethod(HttpMethod.POST)
                .allowedMethod(HttpMethod.PATCH)
                .allowedMethod(HttpMethod.PUT)
                .allowedMethod(HttpMethod.OPTIONS)
                .allowCredentials(true)
                .allowedHeader("Access-Control-Request-Method")
                .allowedHeader("Access-Control-Allow-Method")
                .allowedHeader("Access-Control-Allow-Credentials")
                .allowedHeader("Access-Control-Allow-Origin")
                .allowedHeader("Access-Control-Allow-Headers")
                .allowedHeader("Content-Type"));
    router.route().handler(BodyHandler.create());
  }
}
