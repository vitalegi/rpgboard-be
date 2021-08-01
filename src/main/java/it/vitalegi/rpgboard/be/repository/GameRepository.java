package it.vitalegi.rpgboard.be.repository;

import io.reactivex.Single;
import io.vertx.reactivex.pgclient.PgPool;
import io.vertx.reactivex.sqlclient.templates.RowMapper;
import it.vitalegi.rpgboard.be.data.Board;
import it.vitalegi.rpgboard.be.data.Game;
import it.vitalegi.rpgboard.be.reactivex.data.Mappers;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collections;
import java.util.List;
import java.util.UUID;

public class GameRepository {

  private static final String INSERT =
      "INSERT INTO Game (name) VALUES (#{name}) RETURNING game_id, name;";
  private static final String UPDATE =
      "UPDATE Game SET game_id=#{id}, name=${name} WHERE game_id=#{game_id} RETURNING game_id, name;";
  private static final String FIND_BY_GAME_ID =
      "SELECT game_id, name FROM Game WHERE game_id =#{game_id};";
  private static final String FIND_ALL = "SELECT game_id, name FROM Game;";

  Logger log = LoggerFactory.getLogger(GameRepository.class);

  protected AbstractCrudRepository<Game, UUID> crud;
  protected PgPool client;

  public GameRepository(PgPool client) {
    this.client = client;
    crud =
        new AbstractCrudRepository<Game, UUID>(client) {
          @Override
          protected RowMapper<Game> rowMapper() {
            return Mappers.GAME;
          }
        };
  }

  public Single<Game> add(String name) {
    return crud.add(INSERT, Board.map(null, name));
  }

  public Single<Game> update(Long gameId, String name) {
    return crud.update(UPDATE, Game.map(gameId, name));
  }

  public Single<Game> getById(UUID gameId) {
    return crud.getById(FIND_BY_GAME_ID, gameId);
  }

  public Single<List<Game>> getAll() {
    return crud.search(FIND_ALL, Collections.emptyMap());
  }
}
