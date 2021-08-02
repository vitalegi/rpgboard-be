package it.vitalegi.rpgboard.be.repository;

import io.reactivex.Single;
import io.vertx.reactivex.pgclient.PgPool;
import it.vitalegi.rpgboard.be.data.Game;
import it.vitalegi.rpgboard.be.reactivex.data.Mappers;
import it.vitalegi.rpgboard.be.util.PreparedStatementBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collections;
import java.util.List;
import java.util.UUID;

public class GameRepository {

  private static final String GAME_ID = "game_id";
  private static final PreparedStatementBuilder BUILDER =
      PreparedStatementBuilder.init()
          .tableName("RPG_Game")
          .fields(GAME_ID, "name", "owner_id", "is_open");

  private static final String INSERT = BUILDER.add(Collections.singletonList(GAME_ID));
  private static final String UPDATE_BY_GAME_ID = BUILDER.updateAllById(GAME_ID);
  private static final String DELETE_BY_GAME_ID = BUILDER.deleteAllById(GAME_ID);
  private static final String FIND_BY_GAME_ID =
      BUILDER.searchEquals(Collections.singletonList(GAME_ID));
  private static final String FIND_ALL = BUILDER.searchEquals(Collections.emptyList());

  Logger log = LoggerFactory.getLogger(this.getClass());

  protected DatabaseProxy<Game> proxy;
  protected PgPool client;

  public GameRepository(PgPool client) {
    this.client = client;
    proxy = new DefaultCrudRepository<Game>(client, Mappers.GAME);
  }

  public Single<Game> add(String name, String ownerId, Boolean open) {
    return proxy.updateSingle(INSERT, Game.map(null, name, ownerId, open));
  }

  public Single<Game> update(UUID gameId, String name, String ownerId, Boolean open) {
    return proxy.updateSingle(UPDATE_BY_GAME_ID, Game.map(gameId, name, ownerId, open));
  }

  public Single<Game> delete(UUID gameId) {
    return proxy.updateSingle(DELETE_BY_GAME_ID, Game.map(gameId, null, null, null));
  }

  public Single<Game> getById(UUID gameId) {
    return proxy.querySingle(FIND_BY_GAME_ID, Collections.singletonMap("game_id", gameId));
  }

  public Single<List<Game>> getAll() {
    return proxy.queryList(FIND_ALL, Collections.emptyMap());
  }
}
