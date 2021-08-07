package it.vitalegi.rpgboard.be.repository;

import io.reactivex.Single;
import io.vertx.reactivex.sqlclient.SqlConnection;
import it.vitalegi.rpgboard.be.data.GamePlayerRole;
import it.vitalegi.rpgboard.be.reactivex.data.Mappers;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Singleton;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

@Singleton
public class GamePlayerRoleRepository extends AbstractSinglePkCrudRepository<GamePlayerRole, UUID> {

  Logger log = LoggerFactory.getLogger(this.getClass());

  public GamePlayerRoleRepository() {
    super(
        Mappers.GAME_PLAYER_ROLE,
        GamePlayerRole::map,
        GamePlayerRole::mapPK,
        GamePlayerRole.BUILDER);
  }

  public Single<List<GamePlayerRole>> getUserRoles(
      SqlConnection connection, UUID gameId, String userId) {
    return super.getByFields(
        connection, GamePlayerRole.GAME_ID, gameId, GamePlayerRole.USER_ID, userId);
  }

  public Single<Boolean> hasUserRole(
      SqlConnection connection, UUID gameId, String userId, String role) {
    GamePlayerRole gpr = new GamePlayerRole();
    gpr.setGameId(gameId);
    gpr.setUserId(userId);
    gpr.setRole(role);
    return getByFields(
            connection,
            gpr,
            Arrays.asList(GamePlayerRole.GAME_ID, GamePlayerRole.USER_ID, GamePlayerRole.ROLE))
        .map(roles -> !roles.isEmpty());
  }
}
