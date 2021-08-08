package it.vitalegi.rpgboard.be;

import io.micronaut.context.BeanContext;
import io.reactivex.Maybe;
import io.reactivex.Single;
import io.reactivex.functions.Function;
import io.vertx.core.Promise;
import io.vertx.core.json.JsonObject;
import io.vertx.reactivex.core.AbstractVerticle;
import io.vertx.reactivex.core.Vertx;
import io.vertx.reactivex.core.eventbus.EventBus;
import io.vertx.reactivex.core.eventbus.Message;
import io.vertx.reactivex.pgclient.PgPool;
import io.vertx.reactivex.sqlclient.SqlConnection;
import it.vitalegi.rpgboard.be.data.Game;
import it.vitalegi.rpgboard.be.service.GameService;
import it.vitalegi.rpgboard.be.util.JsonObserver;
import it.vitalegi.rpgboard.be.util.VertxUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Singleton;
import java.util.UUID;

public class GameVerticle extends AbstractVerticle {
  static Logger log = LoggerFactory.getLogger(GameVerticle.class);
  protected GameService gameService;
  protected PgPool client;

  @Override
  public void start(Promise<Void> startPromise) throws Exception {
    log.info("Start");
    EventBus eventBus = vertx.eventBus();
    vertx.exceptionHandler(
        e -> log.error("Unhandled exception {}: {}", e.getClass().getName(), e.getMessage(), e));

    BeanContext beanContext = BeanContext.run();
    beanContext.registerSingleton(config());
    beanContext.registerSingleton(vertx);

    gameService = beanContext.getBean(GameService.class);
    client = beanContext.getBean(PgPool.class);

    eventBus.consumer("external.incoming.game.add", this::addGame);
    eventBus.consumer("external.incoming.game.getAll", this::getGames);
    eventBus.consumer("external.incoming.game.getById", this::getGame);
    eventBus.consumer("game.add", this::addGame);
    eventBus.consumer("game.get", this::getGame);
    eventBus.consumer("game.join", this::joinGame);
    eventBus.consumer("game.getAll", this::getGames);
    eventBus.consumer("game.update", this::updateGame);
    eventBus.consumer("game.delete", this::deleteGame);
    log.info("Start done");
    startPromise.complete();
  }

  protected void addGame(Message<JsonObject> msg) {
    JsonObserver observer = JsonObserver.init(msg, "addGame");
    JsonObject body = msg.body();
    tx(conn -> {
          String name = body.getString("name");
          Boolean open = body.getBoolean("open");
          return Single.just(msg)
              .flatMap(m -> gameService.addGame(conn, getUserId(msg), name, open))
              .toMaybe();
        })
        .subscribe(observer);
  }

  protected void getGame(Message<JsonObject> msg) {
    JsonObserver observer = JsonObserver.init(msg, "getGame");
    UUID gameId = getUUID(msg.body().getString("gameId"));
    cx(conn -> gameService.getGame(conn, getUserId(msg), gameId).toMaybe()).subscribe(observer);
  }

  protected void joinGame(Message<JsonObject> msg) {
    JsonObserver observer = JsonObserver.init(msg, "joinGame");
    UUID gameId = getUUID(msg.body().getString("gameId"));
    cx(conn -> gameService.joinGame(conn, getUserId(msg), gameId).toMaybe()).subscribe(observer);
  }

  protected void updateGame(Message<JsonObject> msg) {
    JsonObserver observer = JsonObserver.init(msg, "updateGame");
    cx(conn ->
            Single.just(msg)
                .map(this::mapGameParams)
                .map(notNull(Game::getId, "id"))
                .map(notNull(Game::getName, "name"))
                .map(notNull(Game::getOwnerId, "ownerId"))
                .map(notNull(Game::getOpen, "open"))
                .flatMap(game -> gameService.updateGame(conn, getUserId(msg), game))
                .toMaybe())
        .subscribe(observer);
  }

  protected void deleteGame(Message<JsonObject> msg) {
    JsonObserver observer = JsonObserver.init(msg, "deleteGame");
    UUID gameId = getUUID(msg.body().getString("gameId"));

    cx(conn -> gameService.deleteGame(conn, getUserId(msg), gameId).toMaybe()).subscribe(observer);
  }

  protected void getGames(Message<JsonObject> msg) {
    JsonObserver observer = JsonObserver.init(msg, "getGames");
    cx(conn -> gameService.getGames(conn, getUserId(msg)).toMaybe()).subscribe(observer);
  }

  protected <E> Function<E, E> notNull(Function<E, Object> extractor, String field) {
    return obj -> {
      if (extractor.apply(obj) == null) {
        throw new IllegalArgumentException("Argument " + field + " must be not null");
      }
      return obj;
    };
  }

  protected <T> Maybe<T> tx(Function<SqlConnection, Maybe<T>> function) {
    return client.rxWithTransaction(function);
  }

  protected <T> Maybe<T> cx(Function<SqlConnection, Maybe<T>> function) {
    return client.rxWithConnection(function);
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

  @Singleton
  protected PgPool getClient(Vertx vertx, JsonObject config) {
    return VertxUtil.pool(vertx, config);
  }

  protected UUID getUUID(String str) {
    if (str == null) {
      return null;
    }
    return UUID.fromString(str);
  }

  protected String getUserId(Message<JsonObject> msg) {
    return msg.headers().get(MainVerticle.UID);
  }

  protected <E> Function<E, E> log(String msg) {
    return (E e) -> {
      log.info(">{}", msg);
      return e;
    };
  }
}
