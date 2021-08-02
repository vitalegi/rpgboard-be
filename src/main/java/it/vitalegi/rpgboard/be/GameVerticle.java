package it.vitalegi.rpgboard.be;

import io.vertx.core.Promise;
import io.vertx.core.json.JsonObject;
import io.vertx.pgclient.SslMode;
import io.vertx.reactivex.core.AbstractVerticle;
import io.vertx.reactivex.core.eventbus.EventBus;
import io.vertx.reactivex.core.eventbus.Message;
import io.vertx.reactivex.pgclient.PgPool;
import it.vitalegi.rpgboard.be.repository.GameRepository;
import it.vitalegi.rpgboard.be.security.FirebaseJWTAuthProvider;
import it.vitalegi.rpgboard.be.security.FirebaseJWTDeliveryContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.UUID;

public class GameVerticle extends AbstractVerticle {
  Logger log = LoggerFactory.getLogger(GameVerticle.class);

  private PgPool client;
  private GameRepository gameRepository;
  private FirebaseJWTAuthProvider authProvider;

  @Override
  public void start(Promise<Void> startPromise) throws Exception {
    log.info("start");
    EventBus eventBus = vertx.eventBus();
    authProvider = new FirebaseJWTAuthProvider();
    vertx.exceptionHandler(
        e -> {
          log.error("Generic exception456 {}:{}", e.getClass().getName(), e.getMessage(), e);
        });
    SslMode sslMode = SslMode.valueOf(config().getJsonObject("database").getString("sslMode"));
    client = VertxUtil.pool(vertx, sslMode);
    gameRepository = new GameRepository(client);
    eventBus.addInboundInterceptor(new FirebaseJWTDeliveryContext(vertx, authProvider));

    eventBus.consumer("external.incoming.game.add", this::addGame);
    eventBus.consumer("external.incoming.game.getAll", this::getGames);
    eventBus.consumer("external.incoming.game.getById", this::getGame);

    eventBus.consumer("game.add", this::addGame);
    eventBus.consumer("game.get", this::getGame);
    eventBus.consumer("game.getAll", this::getGames);
    eventBus.consumer("game.update", this::updateGame);
    eventBus.consumer("game.delete", this::deleteGame);
  }

  protected void addGame(Message<JsonObject> msg) {
    JsonObject body = msg.body();
    String name = body.getString("name");
    String ownerId = body.getString("ownerId");
    Boolean open = body.getBoolean("open");
    log.info("addGame name={} ownerId={} open={}", name, ownerId, open);

    gameRepository
        .add(name, ownerId, open)
        .subscribe(
            game -> {
              msg.reply(JsonObject.mapFrom(game));
              publishBoards();
            },
            VertxUtil.handleError(msg));
  }

  protected void getGame(Message<JsonObject> msg) {
    UUID gameId = UUID.fromString(msg.body().getString("gameId"));
    log.info("getGame {}", gameId);
    gameRepository
        .getById(gameId)
        .subscribe(game -> msg.reply(JsonObject.mapFrom(game)), VertxUtil.handleError(msg));
  }

  protected void updateGame(Message<JsonObject> msg) {
    log.info("updateGame");
    UUID gameId = UUID.fromString(msg.body().getString("gameId"));
    String name = msg.body().getString("name");
    String ownerId = msg.body().getString("ownerId");
    Boolean open = msg.body().getBoolean("open");

    gameRepository
        .update(gameId, name, ownerId, open)
        .subscribe(game -> msg.reply(JsonObject.mapFrom(game)), VertxUtil.handleError(msg));
  }

  protected void deleteGame(Message<JsonObject> msg) {
    log.info("deleteGame");
    UUID gameId = UUID.fromString(msg.body().getString("gameId"));

    gameRepository
        .delete(gameId)
        .subscribe(game -> msg.reply(JsonObject.mapFrom(game)), VertxUtil.handleError(msg));
  }

  protected void getGames(Message<JsonObject> msg) {
    log.info("getGames");
    gameRepository
        .getAll()
        .subscribe(games -> msg.reply(VertxUtil.jsonMap(games)), VertxUtil.handleError(msg));
  }

  protected void publishBoards() {
    gameRepository
        .getAll()
        .subscribe(
            games -> vertx.eventBus().publish("external.outgoing.games", VertxUtil.jsonMap(games)));
  }
}
