package it.vitalegi.rpgboard.be.repository;

import io.reactivex.Observable;
import io.reactivex.Single;
import io.vertx.reactivex.sqlclient.SqlConnection;
import it.vitalegi.rpgboard.be.data.Game;
import it.vitalegi.rpgboard.be.reactivex.data.Mappers;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collections;
import java.util.List;
import java.util.UUID;

public class GameRepository {

  private static final String INSERT = Game.BUILDER.add(Collections.singletonList(Game.GAME_ID));
  private static final String UPDATE_BY_GAME_ID = Game.BUILDER.updateAllById(Game.GAME_ID);
  private static final String DELETE_BY_GAME_ID = Game.BUILDER.deleteAllById(Game.GAME_ID);
  private static final String FIND_BY_GAME_ID =
      Game.BUILDER.searchEquals(Collections.singletonList(Game.GAME_ID));
  private static final String FIND_ALL = Game.BUILDER.searchEquals(Collections.emptyList());
  protected DatabaseProxy<Game> proxy;
  Logger log = LoggerFactory.getLogger(this.getClass());

  public GameRepository() {
    proxy = new DefaultCrudRepository<Game>(Mappers.GAME);
  }

  public Observable<Game> add(SqlConnection connection, Game game) {
    return proxy.updateSingle(connection, INSERT, Game.map(game));
  }

  public Observable<Game> update(SqlConnection connection, Game game) {
    return proxy.updateSingle(connection, UPDATE_BY_GAME_ID, Game.map(game));
  }

  public Observable<Game> delete(SqlConnection connection, UUID gameId) {
    return proxy.updateSingle(connection, DELETE_BY_GAME_ID, Game.map(gameId, null, null, null));
  }

  public Observable<Game> getById(SqlConnection connection, UUID gameId) {
    return proxy.querySingle(
        connection, FIND_BY_GAME_ID, Collections.singletonMap(Game.GAME_ID, gameId));
  }

  public Single<List<Game>> getAll(SqlConnection connection) {
    return proxy.queryList(connection, FIND_ALL, Collections.emptyMap());
  }
}
