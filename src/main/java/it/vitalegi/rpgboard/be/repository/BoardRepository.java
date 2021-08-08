package it.vitalegi.rpgboard.be.repository;

import it.vitalegi.rpgboard.be.data.Board;
import it.vitalegi.rpgboard.be.reactivex.data.Mappers;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.UUID;

public class BoardRepository extends AbstractSinglePkCrudRepository<Board, UUID> {

  Logger log = LoggerFactory.getLogger(this.getClass());

  public BoardRepository() {
    super(Mappers.BOARD, Board::map, Board::mapPK, Board.BUILDER);
  }
}
