package it.vitalegi.rpgboard.be.service;

import io.reactivex.Single;
import io.vertx.core.json.JsonObject;
import io.vertx.reactivex.sqlclient.SqlConnection;
import it.vitalegi.rpgboard.be.data.Board;
import it.vitalegi.rpgboard.be.data.BoardElement;
import it.vitalegi.rpgboard.be.repository.BoardElementRepository;
import it.vitalegi.rpgboard.be.repository.BoardRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

@Singleton
public class BoardService {
  @Inject protected BoardRepository boardRepository;
  @Inject protected BoardElementRepository boardElementRepository;

  Logger log = LoggerFactory.getLogger(this.getClass());

  public Single<Board> addBoard(
      SqlConnection conn,
      UUID gameId,
      UUID userId,
      String name,
      String visibilityPolicy,
      boolean active) {
    notNull(userId, "userId null");
    notNull(gameId, "gameId null");
    notNull(name, "name null");

    Board board = new Board();
    board.setGameId(gameId);
    board.setName(name);
    board.setUserId(userId);
    board.setActive(active);
    board.setVisibilityPolicy(visibilityPolicy);
    OffsetDateTime now = OffsetDateTime.now();
    board.setCreateDate(now);
    board.setLastUpdate(now);

    return Single.just(board)
        .flatMap(
            b -> {
              if (active) {
                return boardRepository.resetActiveBoard(conn, gameId).map(boards -> true);
              }
              return Single.just(true);
            })
        .flatMap(b -> boardRepository.add(conn, board).singleOrError());
  }

  public Single<Board> setActiveBoard(SqlConnection conn, UUID gameId, UUID boardId) {
    log.info("Set active board, gameId={} boardId={}", gameId, boardId);
    return Single.just(boardId)
        .flatMap(b -> boardRepository.resetActiveBoard(conn, gameId).map(boards -> true))
        .flatMap(b -> boardRepository.updateActive(conn, boardId, true).singleOrError());
  }

  public Single<Board> getActiveBoard(SqlConnection conn, UUID gameId) {
    return boardRepository.getActiveBoard(conn, gameId).toSingle();
  }

  public Single<List<Board>> getAllBoards(SqlConnection conn, UUID gameId) {
    return boardRepository.getAllBoards(conn, gameId);
  }

  public Single<Board> getBoard(SqlConnection conn, UUID boardId, UUID userId) {
    // TODO add grants check
    return boardRepository.getById(conn, boardId);
  }

  public Single<Board> updateBoard(
      SqlConnection conn,
      UUID boardId,
      UUID userId,
      String name,
      String visibilityPolicy,
      boolean active) {
    log.info("Update board {}", boardId);
    // TODO add grants check
    return boardRepository
        .getById(conn, boardId)
        .map(
            board -> {
              board.setUserId(userId);
              board.setName(name);
              board.setVisibilityPolicy(visibilityPolicy);
              board.setActive(active);
              board.setLastUpdate(OffsetDateTime.now());
              return board;
            })
        .flatMap(board -> boardRepository.update(conn, board).singleOrError());
  }

  public Single<List<BoardElement>> getBoardElements(SqlConnection conn, UUID boardId) {
    return boardElementRepository.getBoardElements(conn, boardId);
  }

  public Single<BoardElement> addBoardElement(
      SqlConnection conn,
      UUID boardId,
      UUID parentId,
      Long entryPosition,
      JsonObject config,
      String updatePolicy,
      String visibilityPolicy,
      UUID userId) {
    notNull(boardId, "boardId null");
    notNull(config, "config null");
    notNull(updatePolicy, "updatePolicy null");
    notNull(visibilityPolicy, "visibilityPolicy null");
    notNull(userId, "userId null");

    BoardElement entry = new BoardElement();
    entry.setBoardId(boardId);
    entry.setParentId(parentId);
    entry.setEntryPosition(entryPosition);
    entry.setConfig(config);
    entry.setUpdatePolicy(updatePolicy);
    entry.setVisibilityPolicy(visibilityPolicy);
    entry.setUserId(userId);
    OffsetDateTime now = OffsetDateTime.now();
    entry.setCreateDate(now);
    entry.setLastUpdate(now);

    return boardElementRepository.add(conn, entry).singleOrError();
  }

  public Single<BoardElement> deleteBoardElement(SqlConnection conn, UUID entryId, UUID userId) {
    notNull(entryId, "entryId null");
    notNull(userId, "userId null");

    return boardElementRepository.delete(conn, entryId).singleOrError();
  }

  protected void notNull(Object obj, String msg) {
    if (obj == null) {
      throw new IllegalArgumentException(msg);
    }
  }
}
