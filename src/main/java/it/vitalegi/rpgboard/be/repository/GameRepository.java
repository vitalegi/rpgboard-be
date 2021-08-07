package it.vitalegi.rpgboard.be.repository;

import it.vitalegi.rpgboard.be.data.Game;
import it.vitalegi.rpgboard.be.reactivex.data.Mappers;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Singleton;
import java.util.UUID;

@Singleton
public class GameRepository extends DefaultCrudRepository<Game, UUID> {

  Logger log = LoggerFactory.getLogger(this.getClass());

  public GameRepository() {
    super(Mappers.GAME, Game::map, Game::mapPK, Game.BUILDER);
  }
}
