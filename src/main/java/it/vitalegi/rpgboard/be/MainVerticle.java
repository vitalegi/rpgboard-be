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
import io.vertx.reactivex.ext.web.RoutingContext;
import io.vertx.reactivex.ext.web.handler.BodyHandler;
import io.vertx.reactivex.ext.web.handler.CorsHandler;
import io.vertx.reactivex.ext.web.handler.SessionHandler;
import io.vertx.reactivex.ext.web.handler.sockjs.SockJSHandler;
import io.vertx.reactivex.ext.web.sstore.LocalSessionStore;
import it.vitalegi.rpgboard.be.security.FirebaseJWTAuthenticationHandler;
import it.vitalegi.rpgboard.be.security.FirebaseJWTAuthProvider;
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
          log.error("Generic exception {}:{}", e.getClass().getName(), e.getMessage(), e);
        });
    log.info("Setup properties");

    Single<JsonObject> rxConfig = ConfigRetriever.create(vertx, setupConfig()).rxGetConfig();
    rxConfig.doOnError(
        error -> {
          log.error("Failed to load props", error);
          System.exit(0);
        });
    return rxConfig.flatMapCompletable(this::rxStart);
  }

  public Completable rxStart(JsonObject config) {
    log.info("Setup properties done");
    vertx.deployVerticle(new AccountVerticle());
    vertx.deployVerticle(new GameVerticle());
    Router router = Router.router(vertx);

    router
        .route()
        .handler(corsHandler(config))
        .handler(BodyHandler.create())
        .handler(SessionHandler.create(LocalSessionStore.create(vertx)));

    FirebaseJWTAuthProvider authProvider = new FirebaseJWTAuthProvider();
    FirebaseJWTAuthProvider.init();

    FirebaseJWTAuthenticationHandler authHandler =
        new FirebaseJWTAuthenticationHandler() {

          protected String getToken(RoutingContext ctx) {
            return ctx.request().getParam("jwt");
          }
        };
    authHandler.setProvider(authProvider);

    SockJSHandler sockJSHandler = SockJSHandler.create(vertx);
    Router sockJsRouter = sockJSHandler.bridge(getBridgeOptions());
    router.route("/eventbus/*").blockingHandler(authHandler);
    router.mountSubRouter("/eventbus", sockJsRouter);
    Completable out =
        vertx
            .createHttpServer()
            .requestHandler(router)
            .rxListen(Integer.parseInt(System.getenv("PORT")), "0.0.0.0")
            .ignoreElement();

    vertx.setPeriodic(
        10000,
        t -> {
          vertx.eventBus().publish("external.outgoing.games", "hello!");
        });
    return out;
  }

  protected ConfigRetrieverOptions setupConfig() {
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

    return new ConfigRetrieverOptions().addStore(commonFileStore).addStore(fileStore);
  }

  protected CorsHandler corsHandler(JsonObject config) {
    JsonObject sec = config.getJsonObject("security");
    log.info("Apply CORS configuration: {}", sec);

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
        .addOutboundPermitted(
            new PermittedOptions()
                .setAddressRegex("external\\.outgoing.*")
                .setRequiredAuthority("REGISTERED_USER"))
        .addInboundPermitted(
            new PermittedOptions()
                .setAddressRegex("external\\.incoming.*")
                .setRequiredAuthority("REGISTERED_USER"));
  }
}
