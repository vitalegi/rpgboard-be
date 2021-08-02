package it.vitalegi.rpgboard.be.repository;

import io.reactivex.Single;
import io.vertx.reactivex.pgclient.PgPool;
import it.vitalegi.rpgboard.be.data.Game;
import it.vitalegi.rpgboard.be.reactivex.data.Mappers;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collections;
import java.util.List;
import java.util.UUID;

public class GameRepository {

  private static final String INSERT =
      "INSERT INTO RPG_Game (name, owner_id, is_open) VALUES (#{name}, #{owner_id}, #{is_open}) RETURNING game_id, name, owner_id, is_open;";
  private static final String UPDATE_BY_GAME_ID =
      "UPDATE RPG_Game SET game_id=#{id}, name=#{name}, owner_id=#{owner_id}, is_open=#{is_open} WHERE game_id=#{game_id} RETURNING game_id, name, owner_id, is_open;";
  private static final String DELETE_BY_GAME_ID =
      "DELETE FROM RPG_Game WHERE game_id=#{game_id} RETURNING game_id, name, owner_id, is_open;";
  private static final String FIND_BY_GAME_ID =
      "SELECT game_id, name, owner_id, is_open FROM RPG_Game WHERE game_id =#{game_id};";
  private static final String FIND_ALL = "SELECT game_id, name, owner_id, is_open FROM RPG_Game;";

  Logger log = LoggerFactory.getLogger(this.getClass());

  protected AbstractCrudRepository<Game> crud;
  protected PgPool client;

  public GameRepository(PgPool client) {
    this.client = client;
    crud = new DefaultCrudRepository<Game>(client, Mappers.GAME);
  }

  public Single<Game> add(String name, String ownerId, Boolean open) {
    return crud.add(INSERT, Game.map(null, name, ownerId, open));
  }

  public Single<Game> update(UUID gameId, String name, String ownerId, Boolean open) {
    return crud.update(UPDATE_BY_GAME_ID, Game.map(gameId, name, ownerId, open));
  }

  public Single<Game> delete(UUID gameId) {
    return crud.delete(DELETE_BY_GAME_ID, Game.map(gameId, null, null, null));
  }

  public Single<Game> getById(UUID gameId) {
    return crud.getById(FIND_BY_GAME_ID, Collections.singletonMap("game_id", gameId));
  }

  public Single<List<Game>> getAll() {
    return crud.search(FIND_ALL, Collections.emptyMap());
  }
}
