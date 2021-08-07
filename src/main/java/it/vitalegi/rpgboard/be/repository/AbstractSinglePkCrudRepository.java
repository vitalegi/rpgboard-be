package it.vitalegi.rpgboard.be.repository;

import io.reactivex.Observable;
import io.vertx.reactivex.sqlclient.SqlConnection;
import io.vertx.reactivex.sqlclient.templates.RowMapper;
import it.vitalegi.rpgboard.be.repository.querybuilder.pg.PreparedStatementFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;
import java.util.function.Function;

public abstract class AbstractSinglePkCrudRepository<E, P> extends AbstractCrudRepository<E> {

  protected Function<P, Map<String, Object>> primaryKeysMapper;

  Logger log = LoggerFactory.getLogger(this.getClass());

  public AbstractSinglePkCrudRepository(
      RowMapper<E> rowMapper,
      Function<E, Map<String, Object>> entryMapper,
      Function<P, Map<String, Object>> primaryKeysMapper,
      PreparedStatementFactory builder) {
    super(rowMapper, entryMapper, builder);
    this.primaryKeysMapper = primaryKeysMapper;
  }

  public Observable<E> delete(SqlConnection connection, P pk) {
    return updateSingle(
        connection,
        builder.delete().where().areEqualToPlaceholder(builder.primaryKeys()).end().build(),
        primaryKeysMapper.apply(pk));
  }

  public Observable<E> getById(SqlConnection connection, P pk) {
    return querySingle(
        connection,
        builder.select().where().areEqualToPlaceholder(builder.primaryKeys()).end().build(),
        primaryKeysMapper.apply(pk));
  }
}
