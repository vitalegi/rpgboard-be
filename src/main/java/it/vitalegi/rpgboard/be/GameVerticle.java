package it.vitalegi.rpgboard.be;

import io.reactivex.Maybe;
import io.vertx.core.Promise;
import io.vertx.core.json.JsonObject;
import io.vertx.reactivex.core.AbstractVerticle;
import io.vertx.reactivex.core.eventbus.EventBus;
import io.vertx.reactivex.core.eventbus.Message;
import io.vertx.reactivex.ext.auth.User;
import io.vertx.reactivex.pgclient.PgPool;
import it.vitalegi.rpgboard.be.repository.GameRepository;
import it.vitalegi.rpgboard.be.security.FirebaseJWTAuthProvider;
import it.vitalegi.rpgboard.be.security.FirebaseJWTDeliveryContext;
import it.vitalegi.rpgboard.be.security.UserContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
    client = VertxUtil.pool(vertx);
    gameRepository = new GameRepository(client);
    eventBus.addInboundInterceptor(new FirebaseJWTDeliveryContext(vertx, authProvider));

    eventBus.consumer("external.incoming.game.add", this::addGame);
    eventBus.consumer("external.incoming.game.getAll", this::getGames);
    eventBus.consumer("external.incoming.game.getById", this::getGame);
  }

  protected Maybe<User> getUser(String token) {
    return vertx.rxExecuteBlocking(
        future -> {
          authProvider.getUser(token);
        });
  }

  protected void addGame(Message<JsonObject> msg) {
    log.info("addGame {}", UserContext.getUserId(msg));
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
        .subscribe(
            games -> vertx.eventBus().publish("external.outgoing.games", VertxUtil.jsonMap(games)));
  }
}
