package it.vitalegi.rpgboard.be;

import io.reactivex.Single;
import io.vertx.core.Promise;
import io.vertx.core.json.JsonObject;
import io.vertx.pgclient.SslMode;
import io.vertx.reactivex.core.AbstractVerticle;
import io.vertx.reactivex.core.eventbus.EventBus;
import io.vertx.reactivex.core.eventbus.Message;
import io.vertx.reactivex.pgclient.PgPool;
import it.vitalegi.rpgboard.be.data.Game;
import it.vitalegi.rpgboard.be.repository.GameRepository;
import it.vitalegi.rpgboard.be.security.FirebaseJWTAuthProvider;
import it.vitalegi.rpgboard.be.security.FirebaseJWTDeliveryContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.UUID;

public class GameVerticle extends AbstractVerticle {
  Logger log = LoggerFactory.getLogger(GameVerticle.class);

  protected GameRepository gameRepository;

  @Override
  public void start(Promise<Void> startPromise) throws Exception {
    log.info("Start");
    EventBus eventBus = vertx.eventBus();
    vertx.exceptionHandler(
        e -> log.error("Unhandled exception {}: {}", e.getClass().getName(), e.getMessage(), e));

    initRepositories();
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

  protected void initRepositories() {
    SslMode sslMode = SslMode.valueOf(config().getJsonObject("database").getString("sslMode"));
    PgPool client = VertxUtil.pool(vertx, sslMode);
    gameRepository = new GameRepository(client);
  }

  protected void addGame(Message<JsonObject> msg) {
    log.info("add game");

    Single.just(msg)
        .map(
            m -> {
              log.info("map");
              JsonObject body = msg.body();
              String name = body.getString("name");
              String ownerId = body.getString("ownerId");
              Boolean open = body.getBoolean("open");
              if (name == null || ownerId == null || open == null) {
                throw new NullPointerException();
              }
              Game game = new Game();
              game.setName(name);
              game.setOwnerId(ownerId);
              game.setOpen(open);
              log.info("map {}", game);
              return game;
            })
        .flatMap(game -> gameRepository.add(game).singleOrError())
        .subscribe(game -> msg.reply(JsonObject.mapFrom(game)), VertxUtil.handleError(msg));
  }

  protected Single<Game> getGame(Message<JsonObject> msg) {
    UUID gameId = getUUID(msg.body().getString("gameId"));
    log.info("getGame {}", gameId);
    return gameRepository
        .getById(gameId)
        .doOnSuccess(
            game -> {
              msg.reply(JsonObject.mapFrom(game));
            })
        .doOnError(VertxUtil.handleError(msg));
  }

  protected Single<Game> updateGame(Message<JsonObject> msg) {
    log.info("updateGame");
    UUID gameId = UUID.fromString(msg.body().getString("gameId"));
    String name = msg.body().getString("name");
    String ownerId = msg.body().getString("ownerId");
    Boolean open = msg.body().getBoolean("open");

    return gameRepository
        .update(gameId, name, ownerId, open)
        .doOnSuccess(
            game -> {
              msg.reply(JsonObject.mapFrom(game));
            })
        .doOnError(VertxUtil.handleError(msg));
  }

  protected Single<Game> deleteGame(Message<JsonObject> msg) {
    log.info("deleteGame");
    UUID gameId = UUID.fromString(msg.body().getString("gameId"));

    return gameRepository
        .delete(gameId)
        .doOnSuccess(
            game -> {
              msg.reply(JsonObject.mapFrom(game));
            })
        .doOnError(VertxUtil.handleError(msg));
  }

  protected Single<List<Game>> getGames(Message<JsonObject> msg) {
    log.info("getGames");
    return gameRepository
        .getAll()
        .doOnSuccess(
            games -> {
              msg.reply(JsonObject.mapFrom(games));
            })
        .doOnError(VertxUtil.handleError(msg));
  }

  protected void publishBoards() {
    gameRepository
        .getAll()
        .subscribe(
            games -> vertx.eventBus().publish("external.outgoing.games", VertxUtil.jsonMap(games)));
  }

  protected UUID getUUID(String str) {
    if (str == null) {
      return null;
    }
    return UUID.fromString(str);
  }
}
