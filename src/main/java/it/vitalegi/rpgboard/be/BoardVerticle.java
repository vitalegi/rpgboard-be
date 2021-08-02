package it.vitalegi.rpgboard.be;

import io.vertx.core.Promise;
import io.vertx.core.json.JsonObject;
import io.vertx.pgclient.SslMode;
import io.vertx.reactivex.core.AbstractVerticle;
import io.vertx.reactivex.core.eventbus.EventBus;
import io.vertx.reactivex.core.eventbus.Message;
import io.vertx.reactivex.pgclient.PgPool;
import it.vitalegi.rpgboard.be.repository.BoardRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.UUID;

public class BoardVerticle extends AbstractVerticle {
  Logger log = LoggerFactory.getLogger(BoardVerticle.class);

  private PgPool client;
  private BoardRepository boardRepository;

  @Override
  public void start(Promise<Void> startPromise) throws Exception {
    log.info("start");
    EventBus eventBus = vertx.eventBus();

    log.info("Config: {}", config());
    SslMode sslMode = SslMode.valueOf(config().getJsonObject("database").getString("sslMode"));
    client = VertxUtil.pool(vertx, sslMode);
    boardRepository = new BoardRepository(client);
    eventBus.consumer("board.get", this::getBoard);
    eventBus.consumer("board.getAll", this::getBoards);
    eventBus.consumer("board.add", this::addBoard);
  }

  private void getBoard(Message<JsonObject> msg) {
    log.info("getBoard");
    String id = msg.body().getString("id2");
    boardRepository
        .getBoard(id)
        .subscribe(board -> msg.reply(JsonObject.mapFrom(board)), VertxUtil.handleError(msg));
  }

  private void getBoards(Message<Object> msg) {
    log.info("getBoards");
    boardRepository
        .getBoards()
        .subscribe(boards -> msg.reply(VertxUtil.jsonMap(boards)), VertxUtil.handleError(msg));
  }

  private void addBoard(Message<Object> msg) {
    log.info("addBoard");
    JsonObject obj = (JsonObject) msg.body();
    String id = obj.getString("id");
    String name = obj.getString("name");
    boardRepository
        .addBoard(null, UUID.fromString(id), name, true)
        .subscribe(board -> msg.reply(JsonObject.mapFrom(board)), VertxUtil.handleError(msg));
  }
}
