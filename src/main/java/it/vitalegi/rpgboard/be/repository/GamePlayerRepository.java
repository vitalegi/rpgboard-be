package it.vitalegi.rpgboard.be.repository;

import io.reactivex.Single;
import io.vertx.reactivex.sqlclient.SqlConnection;
import it.vitalegi.rpgboard.be.data.GamePlayer;
import it.vitalegi.rpgboard.be.reactivex.data.Mappers;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Singleton;
import java.util.List;
import java.util.UUID;

@Singleton
public class GamePlayerRepository extends AbstractBiPkCrudRepository<GamePlayer, UUID, String> {

  Logger log = LoggerFactory.getLogger(this.getClass());

  public GamePlayerRepository() {
    super(Mappers.GAME_PLAYER, GamePlayer::map, GamePlayer::mapPK, GamePlayer.BUILDER);
  }

  public Single<List<GamePlayer>> getAllByGameId(SqlConnection connection, UUID gameId) {
    return super.getByField(connection, GamePlayer.GAME_ID, gameId);
  }
}
