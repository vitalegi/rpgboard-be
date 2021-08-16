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
        .flatMap(
            b -> boardRepository.add(conn, board).singleOrError().map(notifyBoard("ADD", userId)));
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
                    .map(hasGrant -> board))
        .map(VertxUtil.debug("Board retrieved"));
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
        .map(notifyBoard("UPDATE", userId));
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
        .flatMap(
            board ->
                gameService.checkGrantGameRead(conn, board.getGameId(), userId).map(grant -> board))
        .flatMap(board -> addAndNotifyBoardElement(conn, board.getGameId(), userId, entry));
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
                    .flatMap(
                        board ->
                            gameService
                                .checkGrantGameRead(conn, board.getGameId(), userId)
                                .map(VertxUtil.debug("user has permissions"))
                                .flatMap(
                                    grants ->
                                        updateAndNotifyBoardElement(
                                            conn,
                                            board,
                                            userId,
                                            entry,
                                            parentId,
                                            entryPosition,
                                            config,
                                            updatePolicy,
                                            visibilityPolicy))));
  }

  protected Single<BoardElement> updateAndNotifyBoardElement(
      SqlConnection conn,
      Board board,
      UUID userId,
      BoardElement entry,
      UUID parentId,
      Long entryPosition,
      JsonObject config,
      String updatePolicy,
      String visibilityPolicy) {

    return Single.just(entry)
        .map(
            oldValue ->
                mapUpdate(
                    oldValue, parentId, entryPosition, config, updatePolicy, visibilityPolicy))
        .flatMap(newEntry -> boardElementRepository.update(conn, newEntry).singleOrError())
        .flatMap(newEntry -> notifyBoardElement(board.getGameId(), "UPDATE", userId, newEntry));
  }

  protected BoardElement mapUpdate(
      BoardElement entry,
      UUID parentId,
      Long entryPosition,
      JsonObject config,
      String updatePolicy,
      String visibilityPolicy) {
    log.debug("Mapping inputs");
    entry.setParentId(parentId);
    entry.setEntryPosition(entryPosition);
    entry.setConfig(config);
    entry.setUpdatePolicy(updatePolicy);
    entry.setVisibilityPolicy(visibilityPolicy);
    entry.setLastUpdate(OffsetDateTime.now());
    return entry;
  }

  public Single<BoardElement> deleteBoardElement(SqlConnection conn, UUID entryId, UUID userId) {
    notNull(entryId, "entryId null");
    notNull(userId, "userId null");
    return boardElementRepository
        .getById(conn, entryId)
        .flatMap(entry -> getBoard(conn, entry.getBoardId(), userId))
        .flatMap(
            board ->
                gameService
                    .checkGrantGameRead(conn, board.getGameId(), userId)
                    .flatMap(
                        hasGrant -> boardElementRepository.delete(conn, entryId).singleOrError())
                    .flatMap(
                        deletedEntry ->
                            notifyBoardElement(board.getGameId(), "DELETE", userId, deletedEntry)));
  }

  protected Single<BoardElement> addAndNotifyBoardElement(
      SqlConnection conn, UUID gameId, UUID userId, BoardElement entry) {

    return boardElementRepository
        .add(conn, entry)
        .singleOrError()
        .flatMap(element -> notifyBoardElement(gameId, "ADD", userId, element));
  }

  protected Single<BoardElement> notifyBoardElement(
      UUID gameId, String action, UUID userId, BoardElement element) {
    eventBusService.publish(gameId, "boards", action, userId, JsonObject.mapFrom(element));
    return Single.just(element);
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

  protected Function<Board, Board> notifyBoard(String action, UUID userId) {
    return board -> {
      eventBusService.publish(board.getGameId(), "boards", action, userId, board);
      return board;
    };
  }
}
