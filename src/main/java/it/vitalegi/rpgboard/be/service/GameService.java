package it.vitalegi.rpgboard.be.service;

import io.reactivex.Single;
import io.vertx.reactivex.sqlclient.SqlConnection;
import it.vitalegi.rpgboard.be.data.Game;
import it.vitalegi.rpgboard.be.roles.GameRole;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.List;
import java.util.UUID;

@Singleton
public class GameService {
  @Inject protected GameServiceLocal gameServiceLocal;
  @Inject protected GamePlayerRoleServiceLocal gamePlayerRoleServiceLocal;

  Logger log = LoggerFactory.getLogger(GameService.class);

  public Single<Game> addGame(SqlConnection conn, String userId, String name, Boolean open) {
    return gameServiceLocal.addGame(conn, userId, name, open);
  }

  public Single<Game> getGame(SqlConnection conn, String userId, UUID gameId) {
    return checkRole(conn, userId, gameId, GameRole.PLAYER)
        .flatMap(r -> gameServiceLocal.getGame(conn, gameId));
  }

  public Single<Game> updateGame(SqlConnection conn, String userId, Game game) {
    return checkRole(conn, userId, game.getId(), GameRole.MASTER)
        .flatMap(r -> gameServiceLocal.updateGame(conn, game));
  }

  public Single<Game> deleteGame(SqlConnection conn, String userId, UUID gameId) {
    return checkRole(conn, userId, gameId, GameRole.MASTER)
        .flatMap(r -> gameServiceLocal.deleteGame(conn, gameId));
  }

  public Single<List<Game>> getGames(SqlConnection conn, String userId) {
    return gameServiceLocal.getGames(conn);
  }

  protected Single<Boolean> checkRole(SqlConnection conn, String userId, UUID gameId, String role) {
    return gamePlayerRoleServiceLocal.checkUserRole(conn, gameId, userId, role);
  }
}
