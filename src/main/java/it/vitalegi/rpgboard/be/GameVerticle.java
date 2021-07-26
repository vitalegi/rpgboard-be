package it.vitalegi.rpgboard.be;

import io.vertx.core.Promise;
import io.vertx.core.json.JsonObject;
import io.vertx.reactivex.core.AbstractVerticle;
import io.vertx.reactivex.core.eventbus.EventBus;
import io.vertx.reactivex.core.eventbus.Message;
import io.vertx.reactivex.pgclient.PgPool;
import it.vitalegi.rpgboard.be.repository.GameRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class GameVerticle extends AbstractVerticle {
  Logger log = LoggerFactory.getLogger(GameVerticle.class);

  private PgPool client;
  private GameRepository gameRepository;

  @Override
  public void start(Promise<Void> startPromise) throws Exception {
    log.info("start");
    EventBus eventBus = vertx.eventBus();

    client = VertxUtil.pool(vertx);
    gameRepository = new GameRepository(client);

    eventBus.consumer("external.game.add", this::addGame);
    eventBus.consumer("external.game.getAll", this::getGames);
    eventBus.consumer("external.game.getById", this::getGame);
  }

  protected void addGame(Message<JsonObject> msg) {
    log.info("addGame");
    String name = msg.body().getString("name");
    gameRepository
        .add(name)
        .subscribe(
            game -> {
              msg.reply(JsonObject.mapFrom(game));
              publishBoards();
            },
            VertxUtil.handleError(msg));
  }

  protected void getGame(Message<JsonObject> msg) {
    log.info("getGame");
    String id = msg.body().getString("id");
    gameRepository
        .getById(id)
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
        .subscribe(games -> vertx.eventBus().publish("external.games", VertxUtil.jsonMap(games)));
  }
}
