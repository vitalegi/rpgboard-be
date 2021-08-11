package it.vitalegi.rpgboard.be.repository;

import it.vitalegi.rpgboard.be.data.GameItem;
import it.vitalegi.rpgboard.be.reactivex.data.Mappers;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Singleton;
import java.util.UUID;

@Singleton
public class GameItemRepository extends AbstractSinglePkCrudRepository<GameItem, UUID> {

  Logger log = LoggerFactory.getLogger(this.getClass());

  public GameItemRepository() {
    super(
        Mappers.GAME_ITEM, GameItem::map, GameItem::mapPK, GameItem.BUILDER);
  }
}
