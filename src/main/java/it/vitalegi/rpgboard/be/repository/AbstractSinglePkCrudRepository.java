package it.vitalegi.rpgboard.be.repository;

import io.reactivex.Observable;
import io.reactivex.Single;
import io.vertx.reactivex.sqlclient.SqlConnection;
import io.vertx.reactivex.sqlclient.templates.RowMapper;
import it.vitalegi.rpgboard.be.repository.querybuilder.pg.DeleteFactory;
import it.vitalegi.rpgboard.be.repository.querybuilder.pg.SelectFactory;
import it.vitalegi.rpgboard.be.repository.querybuilder.pg.Table;
import it.vitalegi.rpgboard.be.repository.querybuilder.pg.fields.FieldsPicker;
import it.vitalegi.rpgboard.be.repository.querybuilder.pg.where.EqualsPlaceholder;
import it.vitalegi.rpgboard.be.repository.querybuilder.pg.where.WhereClause;
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
      Table table) {
    super(rowMapper, entryMapper, table);
    this.primaryKeysMapper = primaryKeysMapper;
  }

  public Observable<E> delete(SqlConnection connection, P pk) {
    return executeUpdate(
        connection,
        DeleteFactory.init(table)
            .where(
                WhereClause.and(new EqualsPlaceholder(FieldsPicker.exact(table.getPrimaryKeys()))))
            .build(),
        primaryKeysMapper.apply(pk));
  }

  public Single<E> getById(SqlConnection connection, P pk) {
    return executeQuery(
            connection,
            SelectFactory.init(table)
                .where(
                    WhereClause.and(
                        new EqualsPlaceholder(FieldsPicker.exact(table.getPrimaryKeys()))))
                .build(),
            primaryKeysMapper.apply(pk))
        .singleOrError();
  }
}
