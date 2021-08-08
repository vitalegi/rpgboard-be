package it.vitalegi.rpgboard.be.repository;

import io.reactivex.Observable;
import io.reactivex.Single;
import io.vertx.reactivex.sqlclient.SqlConnection;
import io.vertx.reactivex.sqlclient.templates.RowMapper;
import it.vitalegi.rpgboard.be.repository.querybuilder.pg.PreparedStatementFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.function.Function;

public abstract class AbstractCrudRepository<E> extends DatabaseProxy<E> {

  protected PreparedStatementFactory builder;
  protected RowMapper<E> rowMapper;
  protected Function<E, Map<String, Object>> entryMapper;

  Logger log = LoggerFactory.getLogger(this.getClass());

  public AbstractCrudRepository(
      RowMapper<E> rowMapper,
      Function<E, Map<String, Object>> entryMapper,
      PreparedStatementFactory builder) {
    this.rowMapper = rowMapper;
    this.builder = builder;
    this.entryMapper = entryMapper;
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

  protected Single<List<E>> getByFields(SqlConnection connection, E entry, List<String> fields) {
    return queryList(
        connection,
        builder.select().where().areEqualToPlaceholder(fields).end().build(),
        entryMapper.apply(entry));
  }

  protected Single<List<E>> getByFields(
      SqlConnection connection,
      String fieldName1,
      Object fieldValue1,
      String fieldName2,
      Object fieldValue2) {
    Map<String, Object> in = new HashMap<>();
    in.put(fieldName1, fieldValue1);
    in.put(fieldName2, fieldValue2);
    return queryList(
        connection,
        builder
            .select()
            .where()
            .areEqualToPlaceholder(Arrays.asList(fieldName1, fieldName2))
            .end()
            .build(),
        in);
  }

  protected Single<List<E>> getByField(
      SqlConnection connection, String fieldName, Object fieldValue) {
    Map<String, Object> in = new HashMap<>();
    in.put(fieldName, fieldValue);
    return queryList(
        connection, builder.select().where().isEqualsToPlaceholder(fieldName).end().build(), in);
  }

  public Single<List<E>> getAll(SqlConnection connection) {
    return queryList(connection, builder.select().build(), Collections.emptyMap());
  }

  protected RowMapper<E> rowMapper() {
    return rowMapper;
  }
}
