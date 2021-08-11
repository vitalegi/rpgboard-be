package it.vitalegi.rpgboard.be.repository;

import io.reactivex.Observable;
import io.reactivex.Single;
import io.vertx.reactivex.sqlclient.SqlConnection;
import io.vertx.reactivex.sqlclient.templates.RowMapper;
import it.vitalegi.rpgboard.be.repository.querybuilder.pg.InsertFactory;
import it.vitalegi.rpgboard.be.repository.querybuilder.pg.SelectFactory;
import it.vitalegi.rpgboard.be.repository.querybuilder.pg.Table;
import it.vitalegi.rpgboard.be.repository.querybuilder.pg.UpdateFactory;
import it.vitalegi.rpgboard.be.repository.querybuilder.pg.fields.FieldsPicker;
import it.vitalegi.rpgboard.be.repository.querybuilder.pg.insert.InsertValuesClause;
import it.vitalegi.rpgboard.be.repository.querybuilder.pg.updateset.SetClause;
import it.vitalegi.rpgboard.be.repository.querybuilder.pg.where.EqualsPlaceholder;
import it.vitalegi.rpgboard.be.repository.querybuilder.pg.where.WhereClause;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.function.Function;

public abstract class AbstractCrudRepository<E> extends DatabaseProxy<E> {

  protected Table table;
  protected RowMapper<E> rowMapper;
  protected Function<E, Map<String, Object>> entryMapper;

  Logger log = LoggerFactory.getLogger(this.getClass());

  public AbstractCrudRepository(
      RowMapper<E> rowMapper, Function<E, Map<String, Object>> entryMapper, Table table) {
    this.rowMapper = rowMapper;
    this.table = table;
    this.entryMapper = entryMapper;
  }

  public Observable<E> add(SqlConnection connection, E entry) {
    return updateSingle(
        connection,
        InsertFactory.init(table)
            .values(new InsertValuesClause().allExcept(table.getAutoGenerated()))
            .build(),
        entryMapper.apply(entry));
  }

  public Observable<E> update(SqlConnection connection, E entry) {
    return updateSingle(
        connection,
        UpdateFactory.init(table)
            .set(SetClause.init().except(table.getPrimaryKeys()))
            .where(
                WhereClause.and(new EqualsPlaceholder(FieldsPicker.except(table.getPrimaryKeys()))))
            .build(),
        entryMapper.apply(entry));
  }

  protected Single<List<E>> getByFields(SqlConnection connection, E entry, List<String> fields) {
    return queryList(
        connection,
        SelectFactory.init(table)
            .where(WhereClause.and(new EqualsPlaceholder(FieldsPicker.exact(fields))))
            .build(),
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
        SelectFactory.init(table)
            .where(
                WhereClause.and(
                    new EqualsPlaceholder(
                        FieldsPicker.exact(Arrays.asList(fieldName1, fieldName2)))))
            .build(),
        in);
  }

  protected Single<List<E>> getByField(
      SqlConnection connection, String fieldName, Object fieldValue) {
    Map<String, Object> in = new HashMap<>();
    in.put(fieldName, fieldValue);
    return queryList(
        connection,
        SelectFactory.init(table)
            .where(WhereClause.and(new EqualsPlaceholder(FieldsPicker.exact(fieldName))))
            .build(),
        in);
  }

  protected Single<E> getUniqueByField(
          SqlConnection connection, String fieldName, Object fieldValue) {
    Map<String, Object> in = new HashMap<>();
    in.put(fieldName, fieldValue);
    return querySingle(
            connection,
            SelectFactory.init(table)
                    .where(WhereClause.and(new EqualsPlaceholder(FieldsPicker.exact(fieldName))))
                    .build(),
            in)
            .singleOrError();
  }

  public Single<List<E>> getAll(SqlConnection connection) {
    return queryList(connection, SelectFactory.init(table).build(), Collections.emptyMap());
  }

  protected RowMapper<E> rowMapper() {
    return rowMapper;
  }
}
