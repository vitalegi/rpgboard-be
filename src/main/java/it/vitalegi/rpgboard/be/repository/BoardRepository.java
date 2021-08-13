package it.vitalegi.rpgboard.be.repository;

import io.reactivex.Maybe;
import io.reactivex.Observable;
import io.reactivex.Single;
import io.vertx.reactivex.sqlclient.SqlConnection;
import it.vitalegi.rpgboard.be.data.Board;
import it.vitalegi.rpgboard.be.reactivex.data.Mappers;
import it.vitalegi.rpgboard.be.repository.querybuilder.pg.UpdateFactory;
import it.vitalegi.rpgboard.be.repository.querybuilder.pg.fields.FieldsPicker;
import it.vitalegi.rpgboard.be.repository.querybuilder.pg.updateset.SetClause;
import it.vitalegi.rpgboard.be.repository.querybuilder.pg.where.EqualsPlaceholder;
import it.vitalegi.rpgboard.be.repository.querybuilder.pg.where.WhereClause;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Singleton;
import java.util.List;
import java.util.UUID;

@Singleton
public class BoardRepository extends AbstractSinglePkCrudRepository<Board, UUID> {

  Logger log = LoggerFactory.getLogger(this.getClass());

  public BoardRepository() {
    super(Mappers.BOARD, Board::map, Board::mapPK, Board.BUILDER);
  }

  public Maybe<Board> getActiveBoard(SqlConnection connection, UUID gameId) {
    return getByFields(connection, Board.GAME_ID, gameId, Board.IS_ACTIVE, true)
        .filter(boards -> !boards.isEmpty())
        .map(boards -> boards.get(0));
  }

  public Single<List<Board>> getAllBoards(SqlConnection connection, UUID gameId) {
    return getByField(connection, Board.GAME_ID, gameId);
  }

  public Single<List<Board>> resetActiveBoard(SqlConnection connection, UUID gameId) {

    Board b = new Board();
    b.setGameId(gameId);
    b.setActive(false);

    return executeUpdate(
            connection,
            UpdateFactory.init(Board.BUILDER)
                .set(new SetClause().exact(null, Board.IS_ACTIVE))
                .where(WhereClause.and(new EqualsPlaceholder(FieldsPicker.exact(Board.GAME_ID))))
                .build(),
            Board.map(b))
        .toList();
  }

  public Observable<Board> updateActive(SqlConnection connection, UUID boardId, boolean active) {
    Board b = new Board();
    b.setBoardId(boardId);
    b.setActive(active);

    return executeUpdate(
        connection,
        UpdateFactory.init(table)
            .set(SetClause.init().exact(Board.IS_ACTIVE))
            .where(
                WhereClause.and(new EqualsPlaceholder(FieldsPicker.exact(table.getPrimaryKeys()))))
            .build(),
        entryMapper.apply(b));
  }
}
