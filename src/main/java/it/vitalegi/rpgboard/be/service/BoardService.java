package it.vitalegi.rpgboard.be.service;

import io.reactivex.Single;
import io.vertx.reactivex.sqlclient.SqlConnection;
import it.vitalegi.rpgboard.be.data.Board;
import it.vitalegi.rpgboard.be.repository.BoardRepository;
import it.vitalegi.rpgboard.be.util.VertxUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.time.OffsetDateTime;
import java.util.UUID;

@Singleton
public class BoardService {
  @Inject protected BoardRepository boardRepository;

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
                return boardRepository.resetBoardsVisibility(conn, gameId).map(boards -> true);
              }
              return Single.just(true);
            })
        .flatMap(b -> boardRepository.add(conn, board).singleOrError());
  }

  public Single<Boolean> setActiveBoard(SqlConnection conn, UUID gameId, UUID boardId) {
    log.info("Set active board, gameId={} boardId={}", gameId, boardId);
    return boardRepository
        .getActiveBoard(conn, gameId)
        .map(VertxUtil.debug("Old board", Board::getBoardId))
        .flatMap(oldBoard -> updateVisibility(conn, oldBoard, false).toMaybe())
        .switchIfEmpty(Single.just(new Board()))
        .flatMap(
            old ->
                boardRepository
                    .getById(conn, boardId)
                    .flatMap(board -> updateVisibility(conn, board, true)))
        .map(b -> true);
  }

  public Single<Board> getBoard(SqlConnection conn, UUID boardId, UUID userId) {
    // TODO add grants check
    return boardRepository.getById(conn, boardId);
  }

  protected Single<Board> updateVisibility(SqlConnection conn, Board board, boolean active) {
    return updateBoard(
        conn,
        board.getBoardId(),
        board.getUserId(),
        board.getName(),
        board.getVisibilityPolicy(),
        active);
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

  protected void notNull(Object obj, String msg) {
    if (obj == null) {
      throw new IllegalArgumentException(msg);
    }
  }
}
