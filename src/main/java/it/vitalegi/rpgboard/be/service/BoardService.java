package it.vitalegi.rpgboard.be.service;

import io.reactivex.Single;
import io.reactivex.functions.Function;
import io.vertx.core.json.JsonObject;
import io.vertx.reactivex.sqlclient.SqlConnection;
import it.vitalegi.rpgboard.be.data.Board;
import it.vitalegi.rpgboard.be.data.BoardElement;
import it.vitalegi.rpgboard.be.repository.BoardElementRepository;
import it.vitalegi.rpgboard.be.repository.BoardRepository;
import it.vitalegi.rpgboard.be.util.VertxUtil;
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
  @Inject protected GameService gameService;
  @Inject protected BoardElementRepository boardElementRepository;
  @Inject protected GamePlayerRoleServiceLocal gamePlayerRoleServiceLocal;
  @Inject protected EventBusService eventBusService;

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

    return gameService
        .checkGrantGameWrite(conn, gameId, userId)
        .flatMap(
            b -> {
              if (active) {
                return boardRepository.resetActiveBoard(conn, gameId).map(boards -> true);
              }
              return Single.just(true);
            })
        .flatMap(b -> boardRepository.add(conn, board).singleOrError().map(notifyBoard("ADD")));
  }

  public Single<Board> getActiveBoard(SqlConnection conn, UUID gameId, UUID userId) {
    return gameService
        .checkGrantGameRead(conn, gameId, userId)
        .flatMap(hasGrant -> boardRepository.getActiveBoard(conn, gameId).toSingle());
  }

  public Single<List<Board>> getAllBoards(SqlConnection conn, UUID gameId, UUID userId) {
    return gameService
        .checkGrantGameRead(conn, gameId, userId)
        .flatMap(hasGrant -> boardRepository.getAllBoards(conn, gameId));
  }

  public Single<Board> getBoard(SqlConnection conn, UUID boardId, UUID userId) {
    return boardRepository
        .getById(conn, boardId)
        .flatMap(
            board ->
                gameService
                    .checkGrantGameRead(conn, board.getGameId(), userId)
                    .map(hasGrant -> board));
  }

  public Single<Board> updateBoard(
      SqlConnection conn,
      UUID boardId,
      UUID userId,
      String name,
      String visibilityPolicy,
      boolean active) {
    log.info("Update board {}", boardId);

    return boardRepository
        .getById(conn, boardId)
        .flatMap(board -> checkGrantBoardWrite(conn, board, userId).map(hasGrant -> board))
        .map(
            board -> {
              board.setUserId(userId);
              board.setName(name);
              board.setVisibilityPolicy(visibilityPolicy);
              board.setActive(active);
              board.setLastUpdate(OffsetDateTime.now());
              return board;
            })
        .flatMap(board -> boardRepository.update(conn, board).singleOrError())
        .map(notifyBoard("UPDATE"));
  }

  public Single<List<BoardElement>> getBoardElements(
      SqlConnection conn, UUID boardId, UUID userId) {
    return getBoard(conn, boardId, userId)
        .flatMap(board -> gameService.checkGrantGameRead(conn, board.getGameId(), userId))
        .flatMap(hasGrant -> boardElementRepository.getBoardElements(conn, boardId));
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

    return getBoard(conn, boardId, userId)
        .flatMap(board -> gameService.checkGrantGameRead(conn, board.getGameId(), userId))
        .flatMap(hasGrant -> boardElementRepository.add(conn, entry).singleOrError());
  }

  public Single<BoardElement> updateBoardElement(
      SqlConnection conn,
      UUID entryId,
      UUID parentId,
      Long entryPosition,
      JsonObject config,
      String updatePolicy,
      String visibilityPolicy,
      UUID userId) {
    log.info("Update boardElement {}", entryId);

    notNull(entryId, "entryId null");
    notNull(config, "config null");
    notNull(updatePolicy, "updatePolicy null");
    notNull(visibilityPolicy, "visibilityPolicy null");
    notNull(userId, "userId null");
    return boardElementRepository
        .getById(conn, entryId)
        .map(VertxUtil.debug("Entry retrieved"))
       .flatMap(
            entry ->
                getBoard(conn, entry.getBoardId(), userId)
                    .map(VertxUtil.debug("Board retrieved"))
                    .flatMap(
                        board -> gameService.checkGrantGameRead(conn, board.getGameId(), userId))
                        .map(VertxUtil.debug("user has permissions"))
                    .map(hasPermission -> entry))
        .map(
            entry -> {
              log.info("Mapping inputs");
              entry.setParentId(parentId);
              entry.setEntryPosition(entryPosition);
              entry.setConfig(config);
              entry.setUpdatePolicy(updatePolicy);
              entry.setVisibilityPolicy(visibilityPolicy);
              entry.setLastUpdate(OffsetDateTime.now());
              return entry;
            })
        .flatMap(entry -> boardElementRepository.update(conn, entry).singleOrError());
  }

  public Single<BoardElement> deleteBoardElement(SqlConnection conn, UUID entryId, UUID userId) {
    notNull(entryId, "entryId null");
    notNull(userId, "userId null");
    return boardElementRepository
        .getById(conn, entryId)
        .flatMap(entry -> getBoard(conn, entry.getBoardId(), userId))
        .flatMap(board -> gameService.checkGrantGameRead(conn, board.getGameId(), userId))
        .flatMap(hasGrant -> boardElementRepository.delete(conn, entryId).singleOrError());
  }

  protected Single<Boolean> checkGrantBoardWrite(SqlConnection conn, UUID boardId, UUID userId) {
    return boardRepository
        .getById(conn, boardId)
        .flatMap(board -> checkGrantBoardWrite(conn, board, userId));
  }

  protected Single<Boolean> checkGrantBoardWrite(SqlConnection conn, Board board, UUID userId) {
    if (board.getUserId().equals(userId)) {
      // the owner can always edit
      return Single.just(true);
    }
    // the master can always edit
    return gameService.checkGrantGameWrite(conn, board.getGameId(), userId);
  }

  protected Single<Boolean> checkGrantElementBoardWrite(
      SqlConnection conn, UUID entryId, UUID userId) {
    return boardElementRepository
        .getById(conn, entryId)
        .flatMap(
            boardElement -> {
              if (boardElement.getUserId().equals(userId)) {
                // the owner can always edit
                return Single.just(true);
              }
              // TODO apply also update_policy policies
              // board policies applies
              return checkGrantBoardWrite(conn, boardElement.getBoardId(), userId);
            });
  }

  protected void notNull(Object obj, String msg) {
    if (obj == null) {
      throw new IllegalArgumentException(msg);
    }
  }

  protected Function<Board, Board> notifyBoard(String action) {
    return board -> {
      eventBusService.publish(board.getGameId(), "boards", action, board);
      return board;
    };
  }
}
