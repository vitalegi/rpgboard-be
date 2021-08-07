package it.vitalegi.rpgboard.be;

import io.reactivex.Completable;
import io.vertx.config.ConfigRetrieverOptions;
import io.vertx.config.ConfigStoreOptions;
import io.vertx.core.AsyncResult;
import io.vertx.core.DeploymentOptions;
import io.vertx.core.eventbus.DeliveryOptions;
import io.vertx.core.http.HttpMethod;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.bridge.PermittedOptions;
import io.vertx.ext.web.handler.sockjs.SockJSBridgeOptions;
import io.vertx.reactivex.config.ConfigRetriever;
import io.vertx.reactivex.core.AbstractVerticle;
import io.vertx.reactivex.core.eventbus.EventBus;
import io.vertx.reactivex.core.eventbus.Message;
import io.vertx.reactivex.core.http.HttpServerResponse;
import io.vertx.reactivex.ext.web.Route;
import io.vertx.reactivex.ext.web.Router;
import io.vertx.reactivex.ext.web.RoutingContext;
import io.vertx.reactivex.ext.web.handler.BodyHandler;
import io.vertx.reactivex.ext.web.handler.CorsHandler;
import io.vertx.reactivex.ext.web.handler.SessionHandler;
import io.vertx.reactivex.ext.web.handler.sockjs.SockJSHandler;
import io.vertx.reactivex.ext.web.sstore.LocalSessionStore;
import it.vitalegi.rpgboard.be.security.DummyAuthProvider;
import it.vitalegi.rpgboard.be.security.FirebaseJWTAuthProvider;
import it.vitalegi.rpgboard.be.security.FirebaseJWTAuthenticationHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.stream.Collectors;

public class MainVerticle extends AbstractVerticle {
  public static final String UID = "uid";
  Logger log = LoggerFactory.getLogger(MainVerticle.class);

  @Override
  public Completable rxStart() {
    log.info("start");
    vertx.exceptionHandler(
        e -> {
          log.error("Generic exception {}:{}", e.getClass().getName(), e.getMessage(), e);
        });
    log.info("Setup properties");

    return ConfigRetriever.create(vertx, setupConfig())
        .rxGetConfig()
        .doOnError(
            error -> {
              log.error("Failed to load props", error);
              throw new RuntimeException(error);
            })
        .flatMapCompletable(this::rxStart)
        .doOnError(
            e -> {
              log.error("Error", e);
            });
  }

  public Completable rxStart(JsonObject config) {
    log.info("Setup properties done");
    vertx.deployVerticle(new GameVerticle(), new DeploymentOptions().setConfig(config));
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

    EventBus eventBus = vertx.eventBus();

    String authMethod = config.getJsonObject("security").getString("auth");
    if (authMethod.equals(DummyAuthProvider.METHOD_NAME)) {
      log.info("DUMMY auth method");
      router.route("/api/*").handler(new DummyAuthProvider());
    } else if (authMethod.equals("FIREBASE")) {
      log.info("FIREBASE auth method");
      router.route("/api/*").blockingHandler(authHandler);
    } else {
      throw new IllegalArgumentException("Invalid auth method " + authMethod);
    }

    router
        .post("/api/game")
        .handler(
            ctx -> {
              eventBus.request(
                  "game.add",
                  ctx.getBodyAsJson(),
                  deliveryOptions(ctx),
                  reply -> handleResponse(ctx, reply));
            });

    router
        .get("/api/game/:gameId")
        .handler(
            ctx -> {
              JsonObject message = new JsonObject().put("gameId", ctx.pathParam("gameId"));
              eventBus.request(
                  "game.get", message, deliveryOptions(ctx), reply -> handleResponse(ctx, reply));
            });

    router
        .delete("/api/game/:gameId")
        .handler(
            ctx -> {
              JsonObject message = new JsonObject().put("gameId", ctx.pathParam("gameId"));
              eventBus.request(
                  "game.delete",
                  message,
                  deliveryOptions(ctx),
                  reply -> handleResponse(ctx, reply));
            });

    router
        .get("/api/games")
        .handler(
            ctx -> {
              JsonObject message = new JsonObject();
              eventBus.request(
                  "game.getAll",
                  message,
                  deliveryOptions(ctx),
                  reply -> handleResponse(ctx, reply));
            });

    log.info("Deployed routes");
    for (Route route : router.getRoutes()) {
      String methods = "";
      if (route.methods() != null) {
        methods = route.methods().stream().map(HttpMethod::name).collect(Collectors.joining(", "));
      }
      log.info("{} {}", methods, route.getName());
    }

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

  private <T> void handleResponse(RoutingContext context, AsyncResult<Message<T>> reply) {
    if (reply.succeeded()) {
      HttpServerResponse response = context.response();
      response.putHeader("content-type", "application/json; charset=utf-8");
      response.end(reply.result().body().toString());
    } else {
      context.response().setStatusCode(500);
      JsonObject payload =
          new JsonObject()
              .put("error", reply.cause().getClass().getName())
              .put("description", reply.cause().getMessage());
      context.json(payload);
    }
  }

  private DeliveryOptions deliveryOptions(RoutingContext ctx) {
    return new DeliveryOptions().addHeader(UID, ctx.get(UID));
  }
}
