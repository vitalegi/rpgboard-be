package it.vitalegi.rpgboard.be.repository;

import io.reactivex.Single;
import io.vertx.reactivex.sqlclient.SqlConnection;
import it.vitalegi.rpgboard.be.data.BoardElement;
import it.vitalegi.rpgboard.be.reactivex.data.Mappers;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Singleton;
import java.util.List;
import java.util.UUID;

@Singleton
public class BoardElementRepository extends AbstractSinglePkCrudRepository<BoardElement, UUID> {

  Logger log = LoggerFactory.getLogger(this.getClass());

  public BoardElementRepository() {
    super(Mappers.BOARD_ELEMENT, BoardElement::map, BoardElement::mapPK, BoardElement.BUILDER);
  }

  public Single<List<BoardElement>> getBoardElements(SqlConnection connection, UUID boardId) {
    return getByField(connection, BoardElement.BOARD_ID, boardId);
  }
}
