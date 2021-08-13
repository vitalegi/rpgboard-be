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
import it.vitalegi.rpgboard.be.service.BoardService;
import it.vitalegi.rpgboard.be.service.GamePlayerRoleServiceLocal;
import it.vitalegi.rpgboard.be.service.GameService;
import it.vitalegi.rpgboard.be.util.JsonObserver;
import it.vitalegi.rpgboard.be.util.UuidUtil;
import it.vitalegi.rpgboard.be.util.VertxUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.UUID;

public class GameVerticle extends AbstractVerticle {
  static Logger log = LoggerFactory.getLogger(GameVerticle.class);
  protected GameService gameService;
  protected GamePlayerRoleServiceLocal gamePlayerRoleServiceLocal;
  protected BoardService boardService;
  protected PgPool client;

  @Override
  public void start(Promise<Void> startPromise) throws Exception {
    try {
      log.info("Start");
      EventBus eventBus = vertx.eventBus();
      vertx.exceptionHandler(
          e -> log.error("Unhandled exception {}: {}", e.getClass().getName(), e.getMessage(), e));

      BeanContext beanContext = BeanContext.run();
      beanContext.registerSingleton(config());
      beanContext.registerSingleton(vertx);
      beanContext.registerSingleton(vertx.eventBus());

      beanContext.getAllBeanDefinitions().forEach(bean -> log.info("Bean: {}", bean));
      gameService = beanContext.getBean(GameService.class);
      gamePlayerRoleServiceLocal = beanContext.getBean(GamePlayerRoleServiceLocal.class);
      boardService = beanContext.getBean(BoardService.class);

      if (!config().getString("DATABASE_URL", "").equals("")) {
        beanContext.registerSingleton(getClient(vertx, config()));
        client = beanContext.getBean(PgPool.class);
      } else {
        log.info("No database provided, skip configuration.");
      }

      eventBus.consumer("external.incoming.game.getById", this::getGame);
      eventBus.consumer("game.add", this::addGame);
      eventBus.consumer("game.get", this::getGame);
      eventBus.consumer("game.join", this::joinGame);
      eventBus.consumer("game.getAvailableGames", this::getAvailableGames);
      eventBus.consumer("game.update", this::updateGame);
      eventBus.consumer("game.delete", this::deleteGame);
      eventBus.consumer("game.board.add", this::addBoard);
      eventBus.consumer("game.board.getActive", this::getActiveBoard);
      eventBus.consumer("game.board.getAll", this::getAllBoards);
      eventBus.consumer("game.boardelement.add", this::addBoardElement);
      eventBus.consumer("game.boardelement.getAll", this::getBoardElements);
      eventBus.consumer("game.boardelement.delete", this::deleteBoardElement);
      log.info("Start done");
      startPromise.complete();
    } catch (Exception e) {
      log.error("KO", e);
      throw e;
    }
  }

  protected void addGame(Message<JsonObject> msg) {
    JsonObserver observer = JsonObserver.init(msg, "addGame");
    JsonObject body = msg.body();
    tx(conn -> {
          String name = body.getString("name");
          String type = body.getString("type");
          String status = body.getString("status");
          String visibilityPolicy = body.getString("visibilityPolicy");

          return Single.just(msg)
              .flatMap(
                  m ->
                      gameService.addGame(
                          conn, getUserId(msg), name, type, status, visibilityPolicy))
              .toMaybe();
        })
        .subscribe(observer);
  }

  protected void getGame(Message<JsonObject> msg) {
    JsonObserver observer = JsonObserver.init(msg, "getGame");
    UUID gameId = UuidUtil.getUUID(msg.body().getString("gameId"));
    cx(conn -> gameService.getGame(conn, getUserId(msg), gameId).toMaybe()).subscribe(observer);
  }

  protected void joinGame(Message<JsonObject> msg) {
    JsonObserver observer = JsonObserver.init(msg, "joinGame");
    UUID gameId = UuidUtil.getUUID(msg.body().getString("gameId"));
    tx(conn -> gameService.joinGame(conn, getUserId(msg), gameId).toMaybe()).subscribe(observer);
  }

  protected void updateGame(Message<JsonObject> msg) {
    JsonObserver observer = JsonObserver.init(msg, "updateGame");
    tx(conn ->
            Single.just(msg)
                .map(this::mapGameParams)
                .map(notNull(Game::getGameId, "gameId"))
                .map(notNull(Game::getName, "name"))
                .map(notNull(Game::getOwnerId, "ownerId"))
                .map(notNull(Game::getStatus, "status"))
                .flatMap(game -> gameService.updateGame(conn, getUserId(msg), game))
                .toMaybe())
        .subscribe(observer);
  }

  protected void deleteGame(Message<JsonObject> msg) {
    JsonObserver observer = JsonObserver.init(msg, "deleteGame");
    UUID gameId = UuidUtil.getUUID(msg.body().getString("gameId"));

    tx(conn -> gameService.deleteGame(conn, getUserId(msg), gameId).toMaybe()).subscribe(observer);
  }

  protected void getAvailableGames(Message<JsonObject> msg) {
    JsonObserver observer = JsonObserver.init(msg, "getGames");
    cx(conn -> gameService.getAvailableGames(conn, getUserId(msg)).toMaybe()).subscribe(observer);
  }

  protected void addBoard(Message<JsonObject> msg) {
    JsonObserver observer = JsonObserver.init(msg, "addBoard");
    JsonObject body = msg.body();
    tx(conn -> {
          String name = body.getString("name");
          UUID gameId = UuidUtil.getUUID(body.getString("gameId"));
          String visibilityPolicy = body.getString("visibilityPolicy");
          Boolean active = body.getBoolean("active");

          return Single.just(msg)
              .flatMap(
                  m ->
                      boardService.addBoard(
                          conn, gameId, getUserId(msg), name, visibilityPolicy, active))
              .toMaybe();
        })
        .subscribe(observer);
  }

  protected void getActiveBoard(Message<JsonObject> msg) {
    JsonObserver observer = JsonObserver.init(msg, "getActiveBoard");
    JsonObject body = msg.body();
    tx(conn -> {
          UUID gameId = UuidUtil.getUUID(body.getString("gameId"));
          return boardService.getActiveBoard(conn, gameId).toMaybe();
        })
        .subscribe(observer);
  }

  protected void getAllBoards(Message<JsonObject> msg) {
    JsonObserver observer = JsonObserver.init(msg, "getAllBoards");
    JsonObject body = msg.body();
    tx(conn -> {
          UUID gameId = UuidUtil.getUUID(body.getString("gameId"));
          return boardService.getAllBoards(conn, gameId).toMaybe();
        })
        .subscribe(observer);
  }

  protected void addBoardElement(Message<JsonObject> msg) {
    JsonObserver observer = JsonObserver.init(msg, "addBoardElement");
    JsonObject body = msg.body();
    tx(conn -> {
          UUID boardId = UuidUtil.getUUID(body.getString("boardId"));
          UUID parentId = UuidUtil.getUUID(body.getString("parentId"));
          JsonObject config = body.getJsonObject("config");
          String updatePolicy = body.getString("updatePolicy");
          String visibilityPolicy = body.getString("visibilityPolicy");
          Long entryPosition = body.getLong("entryPosition", 0L);

          return Single.just(msg)
              .flatMap(
                  m ->
                      boardService.addBoardElement(
                          conn,
                          boardId,
                          parentId,
                          entryPosition,
                          config,
                          updatePolicy,
                          visibilityPolicy,
                          getUserId(msg)))
              .toMaybe();
        })
        .subscribe(observer);
  }

  protected void getBoardElements(Message<JsonObject> msg) {
    JsonObserver observer = JsonObserver.init(msg, "getBoardElements");
    JsonObject body = msg.body();
    tx(conn -> {
          UUID boardId = UuidUtil.getUUID(body.getString("boardId"));
          return Single.just(msg)
              .flatMap(m -> boardService.getBoardElements(conn, boardId))
              .toMaybe();
        })
        .subscribe(observer);
  }

  protected void deleteBoardElement(Message<JsonObject> msg) {
    JsonObserver observer = JsonObserver.init(msg, "deleteBoardElement");
    JsonObject body = msg.body();
    tx(conn -> {
          UUID entryId = UuidUtil.getUUID(body.getString("entryId"));
          return Single.just(msg)
              .flatMap(m -> boardService.deleteBoardElement(conn, entryId, getUserId(msg)))
              .toMaybe();
        })
        .subscribe(observer);
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
    game.setGameId(UuidUtil.getUUID(body.getString("gameId")));
    game.setName(body.getString("name"));
    game.setOwnerId(UuidUtil.getUUID(body.getString("ownerId")));
    game.setStatus(body.getString("status"));
    game.setType(body.getString("type"));
    game.setVisibilityPolicy(body.getString("visibilityPolicy"));
    log.info("map {}", game);
    return game;
  }

  protected PgPool getClient(Vertx vertx, JsonObject config) {
    log.info("getClient");
    return VertxUtil.pool(vertx, config);
  }

  protected UUID getUserId(Message<JsonObject> msg) {
    return UuidUtil.getUUID(msg.headers().get(MainVerticle.UID));
  }

  protected <E> Function<E, E> log(String msg) {
    return (E e) -> {
      log.info(">{}", msg);
      return e;
    };
  }
}
