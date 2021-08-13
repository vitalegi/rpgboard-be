package it.vitalegi.rpgboard.be.service;

import io.reactivex.Single;
import io.vertx.reactivex.sqlclient.SqlConnection;
import it.vitalegi.rpgboard.be.data.GamePlayerRole;
import it.vitalegi.rpgboard.be.repository.GamePlayerRoleRepository;
import it.vitalegi.rpgboard.be.util.VertxUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

@Singleton
public class GamePlayerRoleServiceLocal {
  @Inject protected GamePlayerRoleRepository gamePlayerRoleRepository;

  Logger log = LoggerFactory.getLogger(GamePlayerRoleServiceLocal.class);

  public Single<GamePlayerRole> addUserRole(
      SqlConnection conn, UUID gameId, UUID userId, String role) {
    GamePlayerRole gpr = new GamePlayerRole();
    gpr.setGameId(gameId);
    gpr.setUserId(userId);
    gpr.setRole(role);
    OffsetDateTime now = OffsetDateTime.now();
    gpr.setCreateDate(now);
    gpr.setLastUpdate(now);
    return Single.just(gpr)
        .flatMap(entry -> gamePlayerRoleRepository.add(conn, entry).singleOrError())
        .map(VertxUtil.debug("game player role created", GamePlayerRole::toString));
  }

  public Single<List<GamePlayerRole>> getUserRoles(SqlConnection conn, UUID gameId, UUID userId) {
    return gamePlayerRoleRepository.getUserRoles(conn, gameId, userId);
  }
  public Single<Boolean> checkUserRole(SqlConnection conn, UUID gameId, UUID userId, String role) {
    return hasUserRole(conn, gameId, userId, role)
        .map(
            hasRole -> {
              if (!hasRole) {
                throw new IllegalAccessException(
                    "User " + userId + " doesn't have role " + role + " on game " + gameId);
              }
              return true;
            });
  }

  public Single<Boolean> hasUserRole(SqlConnection conn, UUID gameId, UUID userId, String role) {
    return gamePlayerRoleRepository
        .hasUserRole(conn, gameId, userId, role)
        .map(
            hasRole -> {
              log.debug("Does {} have grant {} on game {}?", userId, gameId, role);
              return hasRole;
            });
  }
}
