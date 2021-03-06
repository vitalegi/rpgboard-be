package it.vitalegi.rpgboard.be;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import io.micronaut.context.BeanContext;
import io.micronaut.context.annotation.Factory;
import io.reactivex.Completable;
import io.vertx.config.ConfigRetrieverOptions;
import io.vertx.config.ConfigStoreOptions;
import io.vertx.core.AsyncResult;
import io.vertx.core.DeploymentOptions;
import io.vertx.core.Handler;
import io.vertx.core.eventbus.DeliveryOptions;
import io.vertx.core.http.HttpMethod;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.bridge.PermittedOptions;
import io.vertx.ext.web.handler.sockjs.SockJSBridgeOptions;
import io.vertx.reactivex.config.ConfigRetriever;
import io.vertx.reactivex.core.AbstractVerticle;
import io.vertx.reactivex.core.buffer.Buffer;
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
import it.vitalegi.rpgboard.be.security.AuthProvider;
import it.vitalegi.rpgboard.be.security.FirebaseAuthProvider;
import it.vitalegi.rpgboard.be.security.WebSocketBridgeListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;
import java.util.function.BiConsumer;
import java.util.stream.Collectors;

@Factory
public class MainVerticle extends AbstractVerticle {
  public static final String UID = "uid";
  public static final String EXTERNAL_UID = "externalUid";
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
    ObjectMapper mapper = io.vertx.core.json.jackson.DatabindCodec.mapper();
    mapper.registerModule(new JavaTimeModule());
    log.info("JavaTimeModule is registered, dates' handling is available.");

    vertx.deployVerticle(new GameVerticle(), new DeploymentOptions().setConfig(config));
    vertx.deployVerticle(new UserVerticle(), new DeploymentOptions().setConfig(config));
    BeanContext beanContext = BeanContext.run();
    beanContext.registerSingleton(config);
    beanContext.registerSingleton(vertx);
    beanContext.registerSingleton(vertx.eventBus());

    Router router = Router.router(vertx);
    router
        .route()
        .handler(corsHandler(config))
        .handler(BodyHandler.create())
        .handler(SessionHandler.create(LocalSessionStore.create(vertx)));

    FirebaseAuthProvider.init(config);

    AuthProvider authProvider = beanContext.getBean(AuthProvider.class);
    WebSocketBridgeListener wsBridgeListener = beanContext.getBean(WebSocketBridgeListener.class);

    SockJSHandler sockJSHandler = SockJSHandler.create(vertx);
    Router sockJsRouter = sockJSHandler.bridge(getBridgeOptions(), wsBridgeListener);
    router.route("/eventbus/*");
    router.mountSubRouter("/eventbus", sockJsRouter);

    EventBus eventBus = vertx.eventBus();

    router.route("/api/*").handler(authProvider);

    // users
    router.post("/api/user/registration").handler(toEventBus("user.registration"));
    router.get("/api/user").handler(toEventBus("user.findByExternalUserId"));

    // games
    router.post("/api/game").handler(toEventBus("game.add"));
    router.get("/api/game/:gameId").handler(toEventBus("game.get"));
    router.post("/api/game/:gameId/join").handler(toEventBus("game.join"));
    router.delete("/api/game/:gameId").handler(toEventBus("game.delete"));
    router.get("/api/games").handler(toEventBus("game.getAvailableGames"));
    // boards
    router.post("/api/game/:gameId/board").handler(toEventBus("game.board.add"));
    router.get("/api/game/:gameId/boards").handler(toEventBus("game.board.getAll"));
    router.get("/api/game/:gameId/activeBoard").handler(toEventBus("game.board.getActive"));
    // board's elements
    router.post("/api/board/:boardId/element").handler(toEventBus("game.boardelement.add"));
    router.patch("/api/board/:boardId/element").handler(toEventBus("game.boardelement.update"));
    router.get("/api/board/:boardId/elements").handler(toEventBus("game.boardelement.getAll"));
    router
        .delete("/api/board/:boardId/element/:entryId")
        .handler(toEventBus("game.boardelement.delete"));
    // assets
    router.post("/api/game/:gameId/asset").handler(toEventBus("game.asset.add"));
    router.get("/api/game/:gameId/assets").handler(toEventBus("game.asset.getAll"));
    router.get("/api/game/:gameId/asset/:assetId").handler(toEventBus("game.asset.get"));
    router.delete("/api/game/:gameId/assets/:assetId").handler(toEventBus("game.asset.delete"));

    router
        .get("/content/game/:gameId/asset/:assetId")
        .handler(toEventBus("game.asset.getContent", this::handleAssetContentResponse));

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
            .rxListen(Integer.parseInt(config.getString("PORT")), "0.0.0.0")
            .ignoreElement();

    vertx.setPeriodic(
        10000,
        t -> {
          vertx.eventBus().publish("external.outgoing.games.123", "hello!");
          vertx.eventBus().publish("external.outgoing.test", "hello!");
        });
    return out;
  }

  protected ConfigRetrieverOptions setupConfig() {
    String env = config().getString("ENV", System.getenv("ENV"));

    ConfigStoreOptions verticleOptions =
        new ConfigStoreOptions().setConfig(config()).setType("json");

    ConfigStoreOptions environmentVariables =
        new ConfigStoreOptions()
            .setType("env")
            .setConfig(
                new JsonObject()
                    .put("raw-data", true)
                    .put(
                        "keys",
                        new JsonArray()
                            .add("ENV")
                            .add("PORT")
                            .add("DATABASE_URL")
                            .add("FIREBASE_PRIVATE_KEY")));

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
            .setConfig(new JsonObject().put("path", "config-" + env + ".json"));

    return new ConfigRetrieverOptions()
        .addStore(verticleOptions)
        .addStore(environmentVariables)
        .addStore(commonFileStore)
        .addStore(fileStore);
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

  protected Handler<RoutingContext> toEventBus(String address) {
    return toEventBus(address, this::handleResponse);
  }

  protected Handler<RoutingContext> toEventBus(
      String address, BiConsumer<RoutingContext, AsyncResult<Message<Object>>> responseHandler) {
    return ctx -> {
      try {
        JsonObject payload = ctx.getBodyAsJson();
        if (payload == null) {
          payload = new JsonObject();
        }
        for (Map.Entry<String, String> entry : ctx.pathParams().entrySet()) {
          payload.put(entry.getKey(), entry.getValue());
        }
        log.debug("Send to {}: {}", address, payload);
        vertx
            .eventBus()
            .request(
                address,
                payload,
                deliveryOptions(ctx),
                reply -> responseHandler.accept(ctx, reply));
      } catch (Throwable e) {
        handleFailureResponse(ctx, e);
      }
    };
  }

  private SockJSBridgeOptions getBridgeOptions() {
    return new SockJSBridgeOptions()
        .addOutboundPermitted(new PermittedOptions().setAddressRegex("external\\.outgoing.*"))
        .addInboundPermitted(new PermittedOptions().setAddressRegex("external\\.incoming.*"));
  }

  private <T> void handleResponse(RoutingContext context, AsyncResult<Message<T>> reply) {
    logRequestOutcome(context, reply);
    if (reply.succeeded()) {
      HttpServerResponse response = context.response();
      response.putHeader("content-type", "application/json; charset=utf-8");
      response.end(reply.result().body().toString());
    } else {
      handleFailureResponse(context, reply.cause());
    }
  }

  private void handleAssetContentResponse(
      RoutingContext context, AsyncResult<Message<Object>> reply) {
    logRequestOutcome(context, reply);
    if (reply.succeeded()) {
      HttpServerResponse response = context.response();
      JsonObject body = (JsonObject) reply.result().body();
      byte[] content = body.getBinary("content");
      Buffer payload = Buffer.buffer(content);
      response.putHeader("content-type", body.getString("contentType"));
      response.putHeader("cache-control", "max-age=" + (60 * 60 * 24 * 7) + ", public, immutable");
      response.end(payload);
    } else {
      handleFailureResponse(context, reply.cause());
    }
  }

  private <T> void logRequestOutcome(RoutingContext context, AsyncResult<Message<T>> reply) {
    log.info(
        "Request done method={}, uri={}, succeeded={}",
        context.request().method(),
        context.request().uri(),
        reply.succeeded());
  }

  private void handleFailureResponse(RoutingContext ctx, Throwable e) {
    ctx.response().setStatusCode(500);
    JsonObject payload =
        new JsonObject().put("error", e.getClass().getName()).put("description", e.getMessage());
    ctx.json(payload);
    log.error("Failed handling request, {}", e.getMessage(), e);
  }

  private DeliveryOptions deliveryOptions(RoutingContext ctx) {
    DeliveryOptions options = new DeliveryOptions();

    if (ctx.get(UID) != null) {
      options.addHeader(UID, ctx.get(UID));
    }
    if (ctx.get(EXTERNAL_UID) != null) {
      options.addHeader(EXTERNAL_UID, ctx.get(EXTERNAL_UID));
    }
    return options;
  }
}
