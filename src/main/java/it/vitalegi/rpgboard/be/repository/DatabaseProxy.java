package it.vitalegi.rpgboard.be.repository;

import io.reactivex.Observable;
import io.reactivex.Single;
import io.reactivex.functions.Action;
import io.reactivex.functions.Consumer;
import io.vertx.reactivex.pgclient.PgPool;
import io.vertx.reactivex.sqlclient.templates.RowMapper;
import io.vertx.reactivex.sqlclient.templates.SqlTemplate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public abstract class DatabaseProxy<E> {

  Logger log = LoggerFactory.getLogger(DatabaseProxy.class);
  protected PgPool client;

  public DatabaseProxy(PgPool client) {
    this.client = client;
  }

  protected abstract RowMapper<E> rowMapper();

  protected Observable<E> updateSingle(String query, Map<String, Object> entry) {
    return executeUpdate(query, entry);
  }

  protected Single<E> querySingle(String query, Map<String, Object> entry) {
    return executeQuery(query, entry).singleOrError();
  }

  protected Single<List<E>> queryList(String query, Map<String, Object> entry) {
    return executeQuery(query, entry).toList();
  }

  private Observable<E> executeQuery(String query, Map<String, Object> entry) {
    long start = System.currentTimeMillis();
    return SqlTemplate.forQuery(client, query)
        .mapTo(rowMapper())
        .rxExecute(entry)
        .flatMapObservable(Observable::fromIterable)
        .doOnError(onError(query, entry, start))
        .doOnComplete(onComplete(query, entry, start));
  }

  private Observable<E> executeUpdate(String query, Map<String, Object> entry) {
    long start = System.currentTimeMillis();
    return SqlTemplate.forUpdate(client, query)
        .mapTo(rowMapper())
        .rxExecute(entry)
        .flatMapObservable(Observable::fromIterable)
        .doOnError(onError(query, entry, start))
        .doOnComplete(onComplete(query, entry, start));
  }

  private Consumer<Throwable> onError(String query, Map<String, Object> entry, long start) {
    return (e) -> {
      log.error(
          "DATABASE_STATS time_taken={}, status=KO, query='{}', placeholders='{}', ex={}, ex_description={}",
          System.currentTimeMillis() - start,
          query,
          entriesToString(entry),
          e.getClass().getName(),
          e.getMessage());
    };
  }

  private Action onComplete(String query, Map<String, Object> entry, long start) {
    return () -> {
      log.info(
          "DATABASE_STATS time_taken={}, status=OK, query='{}'",
          System.currentTimeMillis() - start,
          query);
    };
  }

  private String entriesToString(Map<String, Object> entries) {
    return entries.entrySet().stream()
        .map(
            e -> {
              String out = e.getKey() + "=";
              if (e.getValue() == null) {
                return out + "null";
              }
              String value = e.getValue().toString();
              if (value.length() > 50) {
                value = value.substring(0, 50) + "..";
              }
              return out + value;
            })
        .collect(Collectors.joining(", "));
  }
}
