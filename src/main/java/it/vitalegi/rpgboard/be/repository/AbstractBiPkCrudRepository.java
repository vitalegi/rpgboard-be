package it.vitalegi.rpgboard.be.repository;

import io.reactivex.Observable;
import io.vertx.reactivex.sqlclient.SqlConnection;
import io.vertx.reactivex.sqlclient.templates.RowMapper;
import it.vitalegi.rpgboard.be.repository.querybuilder.pg.PreparedStatementFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;
import java.util.function.BiFunction;
import java.util.function.Function;

public abstract class AbstractBiPkCrudRepository<E, P1, P2> extends AbstractCrudRepository<E> {

  protected BiFunction<P1, P2, Map<String, Object>> primaryKeysMapper;

  Logger log = LoggerFactory.getLogger(this.getClass());

  public AbstractBiPkCrudRepository(
      RowMapper<E> rowMapper,
      Function<E, Map<String, Object>> entryMapper,
      BiFunction<P1, P2, Map<String, Object>> primaryKeysMapper,
      PreparedStatementFactory builder) {
    super(rowMapper, entryMapper, builder);
    this.primaryKeysMapper = primaryKeysMapper;
  }

  public Observable<E> delete(SqlConnection connection, P1 pk1, P2 pk2) {
    return updateSingle(
        connection,
        builder.delete().where().areEqualToPlaceholder(builder.primaryKeys()).end().build(),
        primaryKeysMapper.apply(pk1, pk2));
  }

  public Observable<E> getById(SqlConnection connection, P1 pk1, P2 pk2) {
    return querySingle(
        connection,
        builder.select().where().areEqualToPlaceholder(builder.primaryKeys()).end().build(),
        primaryKeysMapper.apply(pk1, pk2));
  }
}
