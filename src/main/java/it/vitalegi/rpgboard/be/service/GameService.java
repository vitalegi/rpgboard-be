package it.vitalegi.rpgboard.be.service;

import io.reactivex.Single;
import io.vertx.reactivex.sqlclient.SqlConnection;
import it.vitalegi.rpgboard.be.data.Game;
import it.vitalegi.rpgboard.be.repository.GameRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.List;
import java.util.UUID;

@Singleton
public class GameService {
  @Inject protected GameRepository gameRepository;
  Logger log = LoggerFactory.getLogger(GameService.class);

  public Single<Game> addGame(SqlConnection conn, Game game) {
    log.info("add game");
    return Single.just(game).flatMap(g -> gameRepository.add(conn, g).singleOrError());
  }

  public Single<Game> getGame(SqlConnection conn, UUID gameId) {
    log.info("getGame {}", gameId);
    return Single.just(gameId).flatMap(id -> gameRepository.getById(conn, gameId).singleOrError());
  }

  public Single<Game> updateGame(SqlConnection conn, Game game) {
    log.info("updateGame");
    return Single.just(game).flatMap(g -> gameRepository.update(conn, g).singleOrError());
  }

  public Single<Game> deleteGame(SqlConnection conn, UUID gameId) {
    log.info("deleteGame");
    return gameRepository.delete(conn, gameId).singleOrError();
  }

  public Single<List<Game>> getGames(SqlConnection conn) {
    log.info("getGames");
    return gameRepository.getAll(conn);
  }
}
