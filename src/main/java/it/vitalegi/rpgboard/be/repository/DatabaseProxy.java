package it.vitalegi.rpgboard.be.repository;

import io.reactivex.Observable;
import io.reactivex.Single;
import io.vertx.reactivex.pgclient.PgPool;
import io.vertx.reactivex.sqlclient.templates.RowMapper;
import io.vertx.reactivex.sqlclient.templates.SqlTemplate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Map;

public abstract class DatabaseProxy<E> {

  Logger log = LoggerFactory.getLogger(DatabaseProxy.class);
  protected PgPool client;

  public DatabaseProxy(PgPool client) {
    this.client = client;
  }

  protected abstract RowMapper<E> rowMapper();

  protected Single<E> updateSingle(String query, Map<String, Object> entry) {
    return executeUpdate(query, entry).singleOrError();
  }

  protected Single<E> querySingle(String query, Map<String, Object> entry) {
    return executeQuery(query, entry).singleOrError();
  }

  protected Single<List<E>> queryList(String query, Map<String, Object> entry) {
    return executeQuery(query, entry).toList();
  }

  private Observable<E> executeQuery(String query, Map<String, Object> entry) {
    log.debug("query='{}', placeholders='{}'", query, entry);
    return SqlTemplate.forQuery(client, query)
        .mapTo(rowMapper())
        .rxExecute(entry)
        .flatMapObservable(Observable::fromIterable);
  }

  private Observable<E> executeUpdate(String query, Map<String, Object> entry) {
    log.debug("query='{}', placeholders='{}'", query, entry);
    return SqlTemplate.forUpdate(client, query)
        .mapTo(rowMapper())
        .rxExecute(entry)
        .flatMapObservable(Observable::fromIterable);
  }
}
