package it.vitalegi.rpgboard.be.service;

import io.reactivex.Single;
import io.vertx.core.json.JsonObject;
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

  public Single<Game> addGame(
      SqlConnection conn,
      UUID userId,
      String name,
      String type,
      String status,
      String visibilityPolicy) {
    return gameServiceLocal.addGame(conn, userId, name, type, status, visibilityPolicy);
  }

  public Single<Game> getGame(SqlConnection conn, UUID userId, UUID gameId) {
    return checkRole(conn, userId, gameId, GameRole.PLAYER)
        .flatMap(r -> gameServiceLocal.getGame(conn, gameId));
  }

  public Single<Boolean> joinGame(SqlConnection conn, UUID userId, UUID gameId) {
    return gameServiceLocal.joinGame(conn, userId, gameId);
  }

  public Single<Game> updateGame(SqlConnection conn, UUID userId, Game game) {
    return checkRole(conn, userId, game.getGameId(), GameRole.MASTER)
        .flatMap(r -> gameServiceLocal.updateGame(conn, game));
  }

  public Single<Game> deleteGame(SqlConnection conn, UUID userId, UUID gameId) {
    return checkRole(conn, userId, gameId, GameRole.MASTER)
        .flatMap(r -> gameServiceLocal.deleteGame(conn, userId, gameId));
  }

  public Single<List<JsonObject>> getAvailableGames(SqlConnection conn, UUID userId) {
    return gameServiceLocal.getAvailableGames(conn, userId);
  }

  public Single<Boolean> checkGrantGameWrite(SqlConnection conn, UUID gameId, UUID userId) {
    return gameServiceLocal.checkGrantGameWrite(conn, gameId, userId);
  }

  public Single<Boolean> checkGrantGameRead(SqlConnection conn, UUID gameId, UUID userId) {
    return gameServiceLocal.checkGrantGameRead(conn, gameId, userId);
  }

  public Single<Boolean> hasGrantGameRead(SqlConnection conn, UUID gameId, UUID userId) {
    return gameServiceLocal.checkGrantGameRead(conn, gameId, userId);
  }

  protected Single<Boolean> checkRole(SqlConnection conn, UUID userId, UUID gameId, String role) {
    return gamePlayerRoleServiceLocal.checkUserRole(conn, gameId, userId, role);
  }
}
