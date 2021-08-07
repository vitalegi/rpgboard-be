package it.vitalegi.rpgboard.be.repository;

import io.reactivex.Observable;
import io.reactivex.Single;
import io.vertx.reactivex.sqlclient.SqlConnection;
import io.vertx.reactivex.sqlclient.templates.RowMapper;
import it.vitalegi.rpgboard.be.repository.querybuilder.pg.PreparedStatementFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

public class DefaultCrudRepository<E, P> extends DatabaseProxy<E> {

  protected PreparedStatementFactory builder;
  protected RowMapper<E> rowMapper;
  protected Function<E, Map<String, Object>> entryMapper;
  protected Function<P, Map<String, Object>> primaryKeysMapper;

  Logger log = LoggerFactory.getLogger(this.getClass());

  public DefaultCrudRepository(
      RowMapper<E> rowMapper,
      Function<E, Map<String, Object>> entryMapper,
      Function<P, Map<String, Object>> primaryKeysMapper,
      PreparedStatementFactory builder) {
    this.rowMapper = rowMapper;
    this.builder = builder;
    this.entryMapper = entryMapper;
    this.primaryKeysMapper = primaryKeysMapper;
  }

  public Observable<E> add(SqlConnection connection, E entry) {
    return updateSingle(
        connection,
        builder.insert().values().allExcept(builder.primaryKeys()).build(),
        entryMapper.apply(entry));
  }

  public Observable<E> update(SqlConnection connection, E entry) {
    return updateSingle(
        connection,
        builder
            .update()
            .setValues()
            .allExcept(builder.primaryKeys())
            .where()
            .areEqualToPlaceholder(builder.primaryKeys())
            .end()
            .build(),
        entryMapper.apply(entry));
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

  public Single<List<E>> getAll(SqlConnection connection) {
    return queryList(connection, builder.select().build(), Collections.emptyMap());
  }

  protected RowMapper<E> rowMapper() {
    return rowMapper;
  }
}
