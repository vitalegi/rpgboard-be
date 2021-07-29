package it.vitalegi.rpgboard.be;

import io.reactivex.Completable;
import io.reactivex.Single;
import io.vertx.config.ConfigRetrieverOptions;
import io.vertx.config.ConfigStoreOptions;
import io.vertx.core.http.HttpMethod;
import io.vertx.core.json.JsonObject;
import io.vertx.reactivex.config.ConfigRetriever;
import io.vertx.reactivex.core.AbstractVerticle;
import io.vertx.reactivex.core.eventbus.EventBus;
import io.vertx.reactivex.ext.web.Router;
import io.vertx.reactivex.ext.web.handler.BodyHandler;
import io.vertx.reactivex.ext.web.handler.CorsHandler;
import it.vitalegi.rpgboard.be.handler.AccountAddHandler;
import it.vitalegi.rpgboard.be.handler.AccountFindAllHandler;
import it.vitalegi.rpgboard.be.handler.AccountFindByIdHandler;
import it.vitalegi.rpgboard.be.security.FirebaseJWTAuthenticationHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.stream.Collectors;

public class MainVerticle extends AbstractVerticle {
  Logger log = LoggerFactory.getLogger(MainVerticle.class);
  private ConfigRetrieverOptions options;

  @Override
  public Completable rxStart() {
    log.info("start");

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
          log.info("values: {}", config.encodePrettily());
          Router router = Router.router(vertx);

          router.route().handler(BodyHandler.create());
          router.route().handler(ctx -> {
              long start = System.currentTimeMillis();
              String uri = ctx.request().uri();
              if (uri.indexOf('?')!= -1) {
                  uri = uri.split("\\?")[0];
              }
              log.info("Incoming request {}", uri);
              ctx.next();
          });
          router.route().handler(corsHandler(config));

          FirebaseJWTAuthenticationHandler.init();
          router.route("/api/*").blockingHandler(new FirebaseJWTAuthenticationHandler(), false);

          EventBus eventBus = vertx.eventBus();

          router.get("/api/account").handler(new AccountFindByIdHandler(eventBus));
          router.post("/api/account").handler(new AccountAddHandler(eventBus));
          router.get("/api/accounts").handler(new AccountFindAllHandler(eventBus));

          WebSocketConfig.init(vertx, router);

          vertx.deployVerticle(new AccountVerticle());
          vertx.deployVerticle(new GameVerticle());

          Completable out =
              vertx
                  .createHttpServer()
                  .requestHandler(router)
                  .rxListen(Integer.parseInt(System.getenv("PORT")), "0.0.0.0")
                  .ignoreElement();
          return out;
        });
  }

  protected CorsHandler corsHandler(JsonObject config) {
    JsonObject sec = config.getJsonObject("security");
    log.info("Apply CORS configuration: {}", sec.encodePrettily());
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
}
