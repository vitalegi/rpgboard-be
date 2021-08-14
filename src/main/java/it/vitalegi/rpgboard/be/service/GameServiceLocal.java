package it.vitalegi.rpgboard.be.service;

import io.reactivex.Single;
import io.vertx.core.json.JsonObject;
import io.vertx.reactivex.sqlclient.SqlConnection;
import it.vitalegi.rpgboard.be.data.Game;
import it.vitalegi.rpgboard.be.mapper.GameMapper;
import it.vitalegi.rpgboard.be.repository.GameRepository;
import it.vitalegi.rpgboard.be.roles.GameRole;
import it.vitalegi.rpgboard.be.util.VertxUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

@Singleton
public class GameServiceLocal {
  @Inject protected GameRepository gameRepository;
  @Inject protected GamePlayerService gamePlayerService;
  @Inject protected GamePlayerRoleServiceLocal gamePlayerRoleServiceLocal;
  @Inject protected UserServiceLocal userServiceLocal;
  @Inject protected EventBusService eventBus;
  Logger log = LoggerFactory.getLogger(GameServiceLocal.class);

  public Single<Game> addGame(
      SqlConnection conn,
      UUID userId,
      String name,
      String type,
      String status,
      String visibilityPolicy) {
    notNull(userId, "userId null");
    notNull(name, "name null");
    notNull(type, "type null");
    notNull(status, "status null");
    Game game = new Game();
    game.setName(name);
    game.setOwnerId(userId);
    game.setStatus(status);
    game.setType(type);
    game.setVisibilityPolicy(visibilityPolicy);
    game.setCreateDate(OffsetDateTime.now());
    game.setLastUpdate(OffsetDateTime.now());

    return Single.just(game)
        .flatMap(g -> gameRepository.add(conn, g).singleOrError())
        .map(VertxUtil.logEntry("game created", Game::getGameId, Game::getName, Game::getOwnerId))
        .flatMap(g -> gamePlayerService.addGamePlayer(conn, g.getGameId(), userId).map(gp -> g))
        .flatMap(
            g ->
                gamePlayerRoleServiceLocal
                    .addUserRole(conn, g.getGameId(), userId, GameRole.MASTER)
                    .map(gpr -> g))
        .flatMap(
            g ->
                gamePlayerRoleServiceLocal
                    .addUserRole(conn, g.getGameId(), userId, GameRole.PLAYER)
                    .map(gpr -> g))
        .map(VertxUtil.debug("creation done"));
  }

  public Single<Boolean> joinGame(SqlConnection conn, UUID userId, UUID gameId) {
    return gamePlayerRoleServiceLocal
        .hasUserRole(conn, gameId, userId, GameRole.PLAYER)
        .flatMap(
            hasRole -> {
              if (hasRole) {
                return Single.just(true);
              }
              return register(conn, userId, gameId);
            })
        .flatMap(
            join -> {
              if (!join) {
                return Single.just(false);
              }
              return GameMapper.mapGamePlayer(
                      userServiceLocal.getUser(conn, userId),
                      gamePlayerRoleServiceLocal.getUserRoles(conn, gameId, userId))
                  .map(
                      user -> {
                        eventBus.publish(gameId, "players", "ADD", user);
                        return true;
                      });
            });
  }

  public Single<Boolean> register(SqlConnection conn, UUID userId, UUID gameId) {
    return Single.just(gameId)
        .flatMap(g -> gamePlayerService.addGamePlayer(conn, gameId, userId))
        .flatMap(g -> gamePlayerRoleServiceLocal.addUserRole(conn, gameId, userId, GameRole.PLAYER))
        .map(VertxUtil.logEntry("user " + userId + " joined " + gameId))
        .map(gpr -> true);
  }

  public Single<Game> getGame(SqlConnection conn, UUID gameId) {
    return Single.just(gameId).flatMap(id -> gameRepository.getById(conn, gameId));
  }

  public Single<Game> updateGame(SqlConnection conn, Game game) {
    return Single.just(game).flatMap(g -> gameRepository.update(conn, g).singleOrError());
  }

  public Single<Game> deleteGame(SqlConnection conn, UUID userId, UUID gameId) {
    log.info("deleteGame gameId={} userId={}", gameId, userId);
    return gameRepository.delete(conn, gameId).singleOrError();
  }

  public Single<List<JsonObject>> getAvailableGames(SqlConnection conn, UUID userId) {
    return gameRepository.getAvailableGames(conn, userId);
  }

  public Single<Boolean> checkGrantGameWrite(SqlConnection conn, UUID gameId, UUID userId) {
    return gameRepository
        .getById(conn, gameId)
        .flatMap(
            game -> {
              if (game.getOwnerId().equals(userId)) {
                // the owner can always edit
                return Single.just(true);
              }
              // the master can always edit
              return gamePlayerRoleServiceLocal.checkUserRole(
                  conn, gameId, userId, GameRole.MASTER);
            });
  }

  public Single<Boolean> checkGrantGameRead(SqlConnection conn, UUID gameId, UUID userId) {
    return hasGrantGameRead(conn, gameId, userId)
        .map(
            hasGrant -> {
              if (hasGrant) {
                return true;
              }
              throw new IllegalAccessException(
                  "User " + userId + " doesn't have READ permissions on game " + gameId);
            });
  }

  public Single<Boolean> hasGrantGameRead(SqlConnection conn, UUID gameId, UUID userId) {
    return gamePlayerRoleServiceLocal.hasUserRole(conn, gameId, userId, GameRole.PLAYER);
  }

  protected void notNull(Object obj, String msg) {
    if (obj == null) {
      throw new IllegalArgumentException(msg);
    }
  }
}
