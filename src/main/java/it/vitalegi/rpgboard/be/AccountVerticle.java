package it.vitalegi.rpgboard.be;

import io.reactivex.Maybe;
import io.reactivex.Observable;
import io.reactivex.Single;
import io.reactivex.functions.Function;
import io.vertx.core.Promise;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.pgclient.PgConnectOptions;
import io.vertx.reactivex.core.AbstractVerticle;
import io.vertx.reactivex.core.eventbus.EventBus;
import io.vertx.reactivex.core.eventbus.Message;
import io.vertx.reactivex.core.eventbus.MessageConsumer;
import io.vertx.reactivex.pgclient.PgPool;
import io.vertx.reactivex.sqlclient.Row;
import io.vertx.reactivex.sqlclient.RowSet;
import io.vertx.reactivex.sqlclient.SqlConnection;
import io.vertx.reactivex.sqlclient.Tuple;
import io.vertx.reactivex.sqlclient.templates.SqlTemplate;
import io.vertx.sqlclient.PoolOptions;
import it.vitalegi.rpgboard.be.data.Account;
import it.vitalegi.rpgboard.be.reactivex.data.Mappers;
import it.vitalegi.rpgboard.be.repository.AccountRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class AccountVerticle {}/*extends AbstractVerticle {
  Logger log = LoggerFactory.getLogger(AccountVerticle.class);

  @Override
  public void start(Promise<Void> startPromise) throws Exception {
    log.info("start");

    EventBus eventBus = vertx.eventBus();

    eventBus
        .<String>consumer("account.getAll") //
        .toObservable() //
        .subscribe(
            msg -> {
              Single<RowSet<Row>> single =
                  pool().query("SELECT account_id, name FROM Account;").rxExecute();
              single.subscribe(
                  result -> {
                    log.info("Got {} results", result.size());
                    msg.reply("ok");
                  },
                  err -> {
                    log.error("Failed to get accounts", err);
                    msg.reply(err);
                  });
            });

    eventBus
        .<JsonObject>consumer("account.add") //
        .toObservable() //
        .subscribe(
            msg -> {
              String id = msg.body().getString("id");
              String name = msg.body().getString("name");

              Maybe<JsonObject> observable =
                  pool()
                      .rxWithTransaction( //
                          // query 1
                          (Function<SqlConnection, Maybe<JsonObject>>)
                              conn ->
                                  getAccounts(conn) //
                                      .flatMapMaybe(
                                          accounts -> {
                                            JsonObject obj = new JsonObject();
                                            obj.put("obj1", mapAccounts(accounts));
                                            // query 2
                                            return addAccount(conn, id, name) //
                                                .flatMapMaybe(
                                                    rs ->
                                                        addAccount(conn, "a", name)
                                                            .flatMapMaybe(
                                                                rs2 ->
                                                                    Maybe.<JsonObject>just(obj)));
                                          }));
              observable.subscribe(
                  obj -> {
                    log.info("Retrieved {} accounts", obj);
                    msg.reply(obj);
                  },
                  error -> {
                    log.error("Failed to retrieve accounts", error);
                    msg.reply(error.toString());
                  },
                  () -> {
                    log.info("Operation completed");
                  });
            });

    eventBus
        .<JsonObject>consumer("account.add2") //
        .toObservable() //
        .subscribe(
            msg -> {
              Maybe<String> subs = pool().rxWithTransaction(this::doProcess2);
              subs.subscribe(obj -> msg.reply(obj), err -> msg.reply(err.getMessage()));
            });

    MessageConsumer<Object> consumer = eventBus.consumer("account.add3", this::handle3);
  }

  private void handle3(Message<Object> msg) {
    log.info("start handle3");
    PgPool client = pool();
    Single<List<Account>> accounts =
        SqlTemplate.forQuery(
                client,
                "SELECT account_id, name FROM Account WHERE account_id =#{id};")
            .mapTo(Mappers.ACCOUNT)
            .rxExecute(Collections.singletonMap("id", "a"))
            .flatMapObservable(Observable::fromIterable)
            .toList();

    Single<List<Account>> account2 =
        SqlTemplate.forQuery(
                client, "SELECT account_id, name FROM Account WHERE account_id =#{id};")
            .mapTo(Mappers.ACCOUNT)
            .rxExecute(Collections.singletonMap("id", "b"))
            .flatMapObservable(Observable::fromIterable)
            .toList();
    Single.zip(
            accounts,
            account2,
            (a1, a2) -> {
              a1.forEach(a -> log.info("> 1: {}", a));
              a2.forEach(a -> log.info("> 2: {}", a));
              return "";
            })
        .subscribe(a -> msg.reply("OK"));
  }

  protected Maybe<String> doProcess2(SqlConnection conn) {
    return getAccounts(conn) //
        .flatMapMaybe(
            accounts ->
                getAccounts(conn)
                    .flatMapMaybe(
                        accounts2 -> {
                          JsonObject json = new JsonObject();
                          json.put("k1", mapAccounts(accounts));
                          json.put("k2", mapAccounts(accounts2));
                          return Maybe.just(json.encodePrettily());
                        }));
  }

  protected Single<List<Account>> getAccounts(SqlConnection conn) {

    return conn.query(accountRepository().getAll())
        .rxExecute()
        .flatMap(
            rs -> {
              return Single.just(this.mapAccounts(rs));
            });
  }

  protected Single<RowSet<Row>> addAccount(SqlConnection conn, String id, String name) {
    return conn.preparedQuery(accountRepository().add()).rxExecute(Tuple.of(id, name));
  }

  protected AccountRepository accountRepository() {
    return new AccountRepository();
  }

  protected PgPool pool() {
    PgConnectOptions connectOptions = PgConnectOptions.fromUri(System.getenv("JDBC_DATABASE_URL"));

    // Pool options
    PoolOptions poolOptions = new PoolOptions().setMaxSize(5);

    // Create the pooled client
    return PgPool.pool(vertx, connectOptions, poolOptions);
  }

  protected Account mapAccount(Row row) {
    Account account = new Account();
    account.setId(row.getString(0));
    account.setName(row.getString(1));
    return account;
  }

  protected <E> JsonArray mapAccounts(List<E> list) {
    JsonArray jsonArr = new JsonArray();
    list.stream().map(JsonObject::mapFrom).forEach(jsonArr::add);
    return jsonArr;
  }

  protected List<Account> mapAccounts(RowSet<Row> list) {
    List<Account> accounts = new ArrayList<Account>();
    list.iterator().forEachRemaining(next -> accounts.add(mapAccount(next)));
    return accounts;
  }

  protected void handleException(String description, Message<?> msg, Throwable throwable) {
    log.error("Operation failed - {}", description, throwable);
    msg.fail(0, throwable.getMessage());
  }
}
*/