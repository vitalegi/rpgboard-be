package it.vitalegi.rpgboard.be.repository;

import io.reactivex.Observable;
import io.reactivex.Single;
import io.vertx.reactivex.pgclient.PgPool;
import io.vertx.reactivex.sqlclient.templates.RowMapper;
import io.vertx.reactivex.sqlclient.templates.SqlTemplate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collections;
import java.util.List;
import java.util.Map;

public abstract class AbstractCrudRepository<E> {

  Logger log = LoggerFactory.getLogger(AbstractCrudRepository.class);
  protected PgPool client;

  public AbstractCrudRepository(PgPool client) {
    this.client = client;
  }

  protected abstract RowMapper<E> rowMapper();

  protected Single<E> add(String query, Map<String, Object> entry) {
    return SqlTemplate.forQuery(client, query)
        .mapTo(rowMapper())
        .rxExecute(entry)
        .flatMapObservable(Observable::fromIterable)
        .singleOrError();
  }

  protected Single<E> delete(String query, Map<String, Object> entry) {
    return SqlTemplate.forQuery(client, query)
        .mapTo(rowMapper())
        .rxExecute(entry)
        .flatMapObservable(Observable::fromIterable)
        .singleOrError();
  }

  protected Single<E> update(String query, Map<String, Object> entry) {
    return SqlTemplate.forQuery(client, query)
        .mapTo(rowMapper())
        .rxExecute(entry)
        .flatMapObservable(Observable::fromIterable)
        .singleOrError();
  }

  protected Single<E> getById(String query, Map<String, Object> entry) {
    return SqlTemplate.forQuery(client, query)
        .mapTo(rowMapper())
        .rxExecute(entry)
        .flatMapObservable(Observable::fromIterable)
        .singleOrError();
  }

  protected Single<List<E>> search(String query, Map<String, Object> entry) {
    return SqlTemplate.forQuery(client, query)
        .mapTo(rowMapper())
        .rxExecute(entry)
        .flatMapObservable(Observable::fromIterable)
        .toList();
  }
}
