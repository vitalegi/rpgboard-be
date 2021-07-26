package it.vitalegi.rpgboard.be.repository;

import io.reactivex.Observable;
import io.reactivex.Single;
import io.vertx.reactivex.pgclient.PgPool;
import io.vertx.reactivex.sqlclient.templates.SqlTemplate;
import it.vitalegi.rpgboard.be.data.Board;
import it.vitalegi.rpgboard.be.data.Game;
import it.vitalegi.rpgboard.be.reactivex.data.Mappers;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collections;
import java.util.List;

public class GameRepository {

  private static final String INSERT =
      "INSERT INTO Game (name) VALUES (#{name}) RETURNING game_id, name;";
  private static final String UPDATE =
      "UPDATE Game SET game_id=#{id}, name=${name} WHERE game_id=#{game_id} RETURNING game_id, name;";
  private static final String FIND_BY_GAME_ID =
      "SELECT game_id, name FROM Game WHERE game_id =#{game_id};";
  private static final String FIND_ALL = "SELECT game_id, name FROM Game;";

  Logger log = LoggerFactory.getLogger(GameRepository.class);
  private PgPool client;

  public GameRepository(PgPool client) {
    this.client = client;
  }

  public Single<Game> add(String name) {
    return SqlTemplate.forQuery(client, INSERT)
        .mapTo(Mappers.GAME)
        .rxExecute(Board.map(null, name))
        .flatMapObservable(Observable::fromIterable)
        .singleOrError();
  }

  public Single<Game> update(Long gameId, String name) {
    return SqlTemplate.forQuery(client, UPDATE)
        .mapTo(Mappers.GAME)
        .rxExecute(Game.map(gameId, name))
        .flatMapObservable(Observable::fromIterable)
        .singleOrError();
  }

  public Single<Game> getById(String gameId) {
    return SqlTemplate.forQuery(client, FIND_BY_GAME_ID)
        .mapTo(Mappers.GAME)
        .rxExecute(Collections.singletonMap("id", gameId))
        .flatMapObservable(Observable::fromIterable)
        .singleOrError();
  }

  public Single<List<Game>> getAll() {
    return SqlTemplate.forQuery(client, FIND_ALL)
        .mapTo(Mappers.GAME)
        .rxExecute(Collections.emptyMap())
        .flatMapObservable(Observable::fromIterable)
        .toList();
  }
}
