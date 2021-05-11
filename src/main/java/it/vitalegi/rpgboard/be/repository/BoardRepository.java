package it.vitalegi.rpgboard.be.repository;

import io.reactivex.Observable;
import io.reactivex.Single;
import io.vertx.reactivex.pgclient.PgPool;
import io.vertx.reactivex.sqlclient.templates.SqlTemplate;
import it.vitalegi.rpgboard.be.data.Board;
import it.vitalegi.rpgboard.be.reactivex.data.Mappers;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collections;
import java.util.List;

public class BoardRepository {

  private static final String INSERT =
      "INSERT INTO Board (board_id, name) VALUES (#{board_id}, #{name}) RETURNING board_id, name;";
  private static final String UPDATE =
      "UPDATE Board SET board_id=#{id}, name=${name} WHERE board_id=#{board_id} RETURNING board_id, name;";
  private static final String FIND_BY_BOARD_ID =
      "SELECT board_id, name FROM Board WHERE board_id =#{board_id};";
  private static final String FIND_ALL = "SELECT board_id, name FROM Board;";

  Logger log = LoggerFactory.getLogger(BoardRepository.class);
  private PgPool client;

  public BoardRepository(PgPool client) {
    this.client = client;
  }

  public Single<Board> addBoard(String boardId, String name) {
    return SqlTemplate.forQuery(client, INSERT)
        .mapTo(Mappers.BOARD)
        .rxExecute(Board.map(boardId, name))
        .flatMapObservable(Observable::fromIterable)
        .singleOrError();
  }

  public Single<Board> updateBoard(String boardId, String name) {
    return SqlTemplate.forQuery(client, UPDATE)
        .mapTo(Mappers.BOARD)
        .rxExecute(Board.map(boardId, name))
        .flatMapObservable(Observable::fromIterable)
        .singleOrError();
  }

  public Single<Board> getBoard(String boardId) {
    return SqlTemplate.forQuery(client, FIND_BY_BOARD_ID)
        .mapTo(Mappers.BOARD)
        .rxExecute(Collections.singletonMap("id", boardId))
        .flatMapObservable(Observable::fromIterable)
        .singleOrError();
  }

  public Single<List<Board>> getBoards() {
    return SqlTemplate.forQuery(client, FIND_ALL)
        .mapTo(Mappers.BOARD)
        .rxExecute(Collections.emptyMap())
        .flatMapObservable(Observable::fromIterable)
        .toList();
  }
}
