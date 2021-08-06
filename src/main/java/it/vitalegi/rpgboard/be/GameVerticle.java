package it.vitalegi.rpgboard.be;

import io.micronaut.context.BeanContext;
import io.reactivex.Maybe;
import io.reactivex.Single;
import io.reactivex.functions.Consumer;
import io.reactivex.functions.Function;
import io.vertx.core.Promise;
import io.vertx.core.json.JsonObject;
import io.vertx.pgclient.SslMode;
import io.vertx.reactivex.core.AbstractVerticle;
import io.vertx.reactivex.core.eventbus.EventBus;
import io.vertx.reactivex.core.eventbus.Message;
import io.vertx.reactivex.pgclient.PgPool;
import io.vertx.reactivex.sqlclient.SqlConnection;
import it.vitalegi.rpgboard.be.data.Game;
import it.vitalegi.rpgboard.be.repository.GameRepository;
import it.vitalegi.rpgboard.be.security.FirebaseJWTAuthProvider;
import it.vitalegi.rpgboard.be.security.FirebaseJWTDeliveryContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.UUID;

public class GameVerticle extends AbstractVerticle {
  protected GameRepository gameRepository;
  protected PgPool client;
  Logger log = LoggerFactory.getLogger(GameVerticle.class);

  @Override
  public void start(Promise<Void> startPromise) throws Exception {
    log.info("Start");
    EventBus eventBus = vertx.eventBus();
    vertx.exceptionHandler(
        e -> log.error("Unhandled exception {}: {}", e.getClass().getName(), e.getMessage(), e));

    BeanContext beanContext = BeanContext.run();
    client = getClient();
    gameRepository = beanContext.getBean(GameRepository.class);

    initAuth(eventBus);

    eventBus.consumer("external.incoming.game.add", this::addGame);
    eventBus.consumer("external.incoming.game.getAll", this::getGames);
    eventBus.consumer("external.incoming.game.getById", this::getGame);

    eventBus.consumer("game.add", this::addGame);
    eventBus.consumer("game.get", this::getGame);
    eventBus.consumer("game.getAll", this::getGames);
    eventBus.consumer("game.update", this::updateGame);
    eventBus.consumer("game.delete", this::deleteGame);
    log.info("Start done");
    startPromise.complete();
  }

  protected void initAuth(EventBus eventBus) {
    FirebaseJWTAuthProvider authProvider = new FirebaseJWTAuthProvider();
    eventBus.addInboundInterceptor(new FirebaseJWTDeliveryContext(vertx, authProvider));
  }

  protected PgPool getClient() {
    SslMode sslMode = SslMode.valueOf(config().getJsonObject("database").getString("sslMode"));
    return VertxUtil.pool(vertx, sslMode);
  }

  protected void addGame(Message<JsonObject> msg) {
    log.info("add game");
    conn(conn ->
            Single.just(msg)
                .map(this::mapGameParams)
                .map(notNull(Game::getName, "name"))
                .map(notNull(Game::getOwnerId, "ownerId"))
                .map(notNull(Game::getOpen, "open"))
                .flatMap(game -> gameRepository.add(conn, game).singleOrError())
                .map(log("Step 1"))
                .toMaybe())
        .subscribe(replyJson(msg), handleError(msg));
  }

  protected <T> Maybe<T> tx(Function<SqlConnection, Maybe<T>> function) {
    return client.rxWithTransaction(function);
  }

  protected <T> Maybe<T> conn(Function<SqlConnection, Maybe<T>> function) {
    return client.rxWithConnection(function);
  }

  protected <E> Function<E, E> log(String msg) {
    return (E e) -> {
      log.info(">{}: {}", msg, e);
      return e;
    };
  }

  protected Game mapGameParams(Message<JsonObject> msg) {
    JsonObject body = msg.body();
    Game game = new Game();
    game.setId(getUUID(body.getString("gameId")));
    game.setName(body.getString("name"));
    game.setOwnerId(body.getString("ownerId"));
    game.setOpen(body.getBoolean("open"));
    log.info("map {}", game);
    return game;
  }

  protected void getGame(Message<JsonObject> msg) {
    UUID gameId = getUUID(msg.body().getString("gameId"));
    log.info("getGame {}", gameId);
    conn(conn ->
            Single.just(gameId)
                .flatMap(id -> gameRepository.getById(conn, gameId).singleOrError())
                .toMaybe())
        .subscribe(replyJson(msg), handleError(msg));
  }

  protected void updateGame(Message<JsonObject> msg) {
    log.info("updateGame");
    conn(conn ->
            Single.just(msg)
                .map(this::mapGameParams)
                .map(notNull(Game::getId, "gameId"))
                .map(notNull(Game::getName, "name"))
                .map(notNull(Game::getOwnerId, "ownerId"))
                .map(notNull(Game::getOpen, "open"))
                .flatMap(game -> gameRepository.update(conn, game).singleOrError())
                .toMaybe())
        .subscribe(replyJson(msg), handleError(msg));
  }

  protected void deleteGame(Message<JsonObject> msg) {
    log.info("deleteGame");
    UUID gameId = UUID.fromString(msg.body().getString("gameId"));

    conn(conn -> gameRepository.delete(conn, gameId).singleOrError().toMaybe())
        .subscribe(replyJson(msg), handleError(msg));
  }

  protected void getGames(Message<JsonObject> msg) {
    log.info("getGames");
    conn(conn -> gameRepository.getAll(conn).toMaybe())
        .subscribe(replyJsonArray(msg), handleError(msg));
  }

  protected <E> Function<E, E> notNull(Function<E, Object> extractor, String field) {
    return obj -> {
      if (extractor.apply(obj) == null) {
        throw new IllegalArgumentException("Argument " + field + " must be not null");
      }
      return obj;
    };
  }

  protected UUID getUUID(String str) {
    if (str == null) {
      return null;
    }
    return UUID.fromString(str);
  }

  protected <E> Consumer<E> replyJson(Message<JsonObject> msg) {
    return obj -> msg.reply(JsonObject.mapFrom(obj));
  }

  protected <E> Consumer<List<E>> replyJsonArray(Message<JsonObject> msg) {
    return list -> msg.reply(VertxUtil.jsonMap(list));
  }

  protected Consumer<? super Throwable> handleError(Message<JsonObject> msg) {
    return VertxUtil.handleError(msg);
  }
}
