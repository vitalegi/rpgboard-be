package it.vitalegi.rpgboard.be.repository;

import io.reactivex.Observable;
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

  private static final String INSERT = Game.BUILDER.add(Collections.singletonList(Game.GAME_ID));
  private static final String UPDATE_BY_GAME_ID = Game.BUILDER.updateAllById(Game.GAME_ID);
  private static final String DELETE_BY_GAME_ID = Game.BUILDER.deleteAllById(Game.GAME_ID);
  private static final String FIND_BY_GAME_ID =
      Game.BUILDER.searchEquals(Collections.singletonList(Game.GAME_ID));
  private static final String FIND_ALL = Game.BUILDER.searchEquals(Collections.emptyList());

  Logger log = LoggerFactory.getLogger(this.getClass());

  protected DatabaseProxy<Game> proxy;
  protected PgPool client;

  public GameRepository() {}

  public GameRepository(PgPool client) {
    this.client = client;
    proxy = new DefaultCrudRepository<Game>(client, Mappers.GAME);
  }

  public Observable<Game> add(Game game) {
    return proxy.updateSingle(
        INSERT, Game.map(null, game.getName(), game.getOwnerId(), game.getOpen()));
  }

  public Single<Game> update(UUID gameId, String name, String ownerId, Boolean open) {
    return proxy
        .updateSingle(UPDATE_BY_GAME_ID, Game.map(gameId, name, ownerId, open))
        .singleOrError();
  }

  public Single<Game> delete(UUID gameId) {
    return proxy
        .updateSingle(DELETE_BY_GAME_ID, Game.map(gameId, null, null, null))
        .singleOrError();
  }

  public Single<Game> getById(UUID gameId) {
    return proxy.querySingle(FIND_BY_GAME_ID, Collections.singletonMap(Game.GAME_ID, gameId));
  }

  public Single<List<Game>> getAll() {
    return proxy.queryList(FIND_ALL, Collections.emptyMap());
  }
}
