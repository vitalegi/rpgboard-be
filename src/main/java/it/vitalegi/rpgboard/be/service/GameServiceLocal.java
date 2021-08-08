package it.vitalegi.rpgboard.be.service;

import io.reactivex.Single;
import io.vertx.reactivex.sqlclient.SqlConnection;
import it.vitalegi.rpgboard.be.data.Game;
import it.vitalegi.rpgboard.be.data.GamePlayerRole;
import it.vitalegi.rpgboard.be.repository.GameRepository;
import it.vitalegi.rpgboard.be.roles.GameRole;
import it.vitalegi.rpgboard.be.util.VertxUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.List;
import java.util.UUID;

@Singleton
public class GameServiceLocal {
  @Inject protected GameRepository gameRepository;
  @Inject protected GamePlayerService gamePlayerService;
  @Inject protected GamePlayerRoleServiceLocal gamePlayerRoleServiceLocal;

  Logger log = LoggerFactory.getLogger(GameServiceLocal.class);

  public Single<Game> addGame(
      SqlConnection conn, String userId, String name, String type, Boolean open) {
    notNull(userId, "userId null");
    notNull(name, "name null");
    notNull(type, "type null");
    notNull(open, "open null");
    Game game = new Game();
    game.setOpen(open);
    game.setName(name);
    game.setType(type);
    game.setOwnerId(userId);
    return Single.just(game)
        .flatMap(g -> gameRepository.add(conn, g).singleOrError())
        .map(VertxUtil.logEntry("game created", Game::getId, Game::getName, Game::getOwnerId))
        .flatMap(g -> gamePlayerService.addGamePlayer(conn, g.getId(), userId).map(gp -> g))
        .flatMap(
            g ->
                gamePlayerRoleServiceLocal
                    .addUserRole(conn, g.getId(), userId, GameRole.MASTER)
                    .map(gpr -> g))
        .flatMap(
            g ->
                gamePlayerRoleServiceLocal
                    .addUserRole(conn, g.getId(), userId, GameRole.PLAYER)
                    .map(gpr -> g))
        .map(VertxUtil.debug("creation done"));
  }

  public Single<GamePlayerRole> joinGame(SqlConnection conn, String userId, UUID gameId) {
    return Single.just(gameId)
        .flatMap(g -> gamePlayerService.addGamePlayer(conn, gameId, userId))
        .flatMap(g -> gamePlayerRoleServiceLocal.addUserRole(conn, gameId, userId, GameRole.PLAYER))
        .map(VertxUtil.logEntry("user " + userId + " joined " + gameId));
  }

  public Single<Game> getGame(SqlConnection conn, UUID gameId) {
    return Single.just(gameId).flatMap(id -> gameRepository.getById(conn, gameId).singleOrError());
  }

  public Single<Game> updateGame(SqlConnection conn, Game game) {
    return Single.just(game).flatMap(g -> gameRepository.update(conn, g).singleOrError());
  }

  public Single<Game> deleteGame(SqlConnection conn, String userId, UUID gameId) {
    log.info("deleteGame gameId={} userId={}", gameId, userId);
    return gameRepository.delete(conn, gameId).singleOrError();
  }

  public Single<List<Game>> getGames(SqlConnection conn) {
    log.info("getGames");
    return gameRepository.getAll(conn);
  }

  protected void notNull(Object obj, String msg) {
    if (obj == null) {
      throw new IllegalArgumentException(msg);
    }
  }
}
