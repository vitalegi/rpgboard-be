package it.vitalegi.rpgboard.be;

import io.reactivex.Observable;
import io.reactivex.Single;
import io.vertx.core.Promise;
import io.vertx.core.json.JsonObject;
import io.vertx.reactivex.core.AbstractVerticle;
import io.vertx.reactivex.core.eventbus.EventBus;
import io.vertx.reactivex.core.eventbus.Message;
import io.vertx.reactivex.pgclient.PgPool;
import io.vertx.reactivex.sqlclient.templates.SqlTemplate;
import it.vitalegi.rpgboard.be.data.Board;
import it.vitalegi.rpgboard.be.data.Game;
import it.vitalegi.rpgboard.be.reactivex.data.Mappers;
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
    vertx.exceptionHandler(
        e -> {
          log.error("Generic exception456 {}:{}", e.getClass().getName(), e.getMessage(), e);
        });
    try {
      client = VertxUtil.pool(vertx);
    } catch (Throwable e) {
      log.error("Failed", e);
      throw e;
    }
    gameRepository = new GameRepository(client);

    eventBus.consumer("external.incoming.game.add", this::addGame);
    eventBus.consumer("test123", this::addGame);
    eventBus.consumer("external.incoming.game.getAll", this::getGames);
    eventBus.consumer("external.incoming.game.getById", this::getGame);
  }

  protected void addGame(Message<JsonObject> msg) {
    log.info("addGame");
    String name = msg.body().getString("name");
    Single<Game> out =
        SqlTemplate.forQuery(
                client, "INSERT INTO Game (name) VALUES (#{name}) RETURNING game_id, name;")
            .mapTo(Mappers.GAME)
            .rxExecute(Board.map(null, name))
            .doOnEvent(
                (rs, e) -> {
                  log.error("Do on event", e);
                })
            .doOnError(
                e -> {
                  log.error("Do on error", e);
                })
            .doOnSuccess(
                rs -> {
                  log.info("Do on success");
                })
            .flatMapObservable(Observable::fromIterable)
            .singleOrError();

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
        .subscribe(
            games -> vertx.eventBus().publish("external.outgoing.games", VertxUtil.jsonMap(games)));
  }
}
