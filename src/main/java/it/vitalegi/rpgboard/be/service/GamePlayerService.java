package it.vitalegi.rpgboard.be.service;

import io.reactivex.Single;
import io.vertx.reactivex.sqlclient.SqlConnection;
import it.vitalegi.rpgboard.be.data.GamePlayer;
import it.vitalegi.rpgboard.be.repository.GamePlayerRepository;
import it.vitalegi.rpgboard.be.util.VertxUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.List;
import java.util.UUID;

@Singleton
public class GamePlayerService {
  @Inject protected GamePlayerRepository gamePlayerRepository;
  @Inject GamePlayerRoleServiceLocal gamePlayerRoleServiceLocal;

  Logger log = LoggerFactory.getLogger(GamePlayerService.class);

  public Single<GamePlayer> addGamePlayer(SqlConnection conn, UUID gameId, UUID userId) {
    notNull(userId, "userId null");
    notNull(gameId, "gameId null");

    GamePlayer entry = new GamePlayer();
    entry.setGameId(gameId);
    entry.setUserId(userId);
    entry.setUsername("todo");
    return Single.just(entry)
        .flatMap(gp -> gamePlayerRepository.add(conn, gp).singleOrError())
        .map(VertxUtil.debug("game player entry created", GamePlayer::toString));
  }

  public Single<Boolean> isMember(SqlConnection conn, UUID gameId, UUID userId) {
    return gamePlayerRepository.getById(conn, gameId, userId).isEmpty().map(b -> !b);
  }

  public Single<GamePlayer> updateGamePlayer(SqlConnection conn, GamePlayer gamePlayer) {
    return Single.just(gamePlayer)
        .flatMap(gp -> gamePlayerRepository.update(conn, gp).singleOrError());
  }

  public Single<GamePlayer> deleteGamePlayer(SqlConnection conn, UUID gameId, UUID userId) {
    return gamePlayerRepository.delete(conn, gameId, userId).singleOrError();
  }

  public Single<List<GamePlayer>> getGamePlayers(SqlConnection conn, UUID gameId) {
    return gamePlayerRepository.getAllByGameId(conn, gameId);
  }

  protected void notNull(Object obj, String msg) {
    if (obj == null) {
      throw new IllegalArgumentException(msg);
    }
  }
}
