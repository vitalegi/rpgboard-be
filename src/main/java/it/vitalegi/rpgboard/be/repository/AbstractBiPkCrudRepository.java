package it.vitalegi.rpgboard.be.repository;

import io.reactivex.Observable;
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
import java.util.function.BiFunction;
import java.util.function.Function;

public abstract class AbstractBiPkCrudRepository<E, P1, P2> extends AbstractCrudRepository<E> {

  protected BiFunction<P1, P2, Map<String, Object>> primaryKeysMapper;

  Logger log = LoggerFactory.getLogger(this.getClass());

  public AbstractBiPkCrudRepository(
      RowMapper<E> rowMapper,
      Function<E, Map<String, Object>> entryMapper,
      BiFunction<P1, P2, Map<String, Object>> primaryKeysMapper,
      Table table) {
    super(rowMapper, entryMapper, table);
    this.primaryKeysMapper = primaryKeysMapper;
  }

  public Observable<E> delete(SqlConnection connection, P1 pk1, P2 pk2) {
    return updateSingle(
        connection,
        DeleteFactory.init(table)
            .where(
                WhereClause.and(new EqualsPlaceholder(FieldsPicker.exact(table.getPrimaryKeys()))))
            .build(),
        primaryKeysMapper.apply(pk1, pk2));
  }

  public Observable<E> getById(SqlConnection connection, P1 pk1, P2 pk2) {
    return querySingle(
        connection,
        SelectFactory.init(table)
            .where(
                WhereClause.and(new EqualsPlaceholder(FieldsPicker.exact(table.getPrimaryKeys()))))
            .build(),
        primaryKeysMapper.apply(pk1, pk2));
  }
}
