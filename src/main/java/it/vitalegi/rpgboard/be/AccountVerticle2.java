package it.vitalegi.rpgboard.be;

import io.reactivex.Single;
import io.vertx.core.Promise;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.pgclient.PgConnectOptions;
import io.vertx.reactivex.core.AbstractVerticle;
import io.vertx.reactivex.core.eventbus.EventBus;
import io.vertx.reactivex.core.eventbus.Message;
import io.vertx.reactivex.pgclient.PgPool;
import io.vertx.sqlclient.PoolOptions;
import it.vitalegi.rpgboard.be.data.Account;
import it.vitalegi.rpgboard.be.repository.AccountRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

public class AccountVerticle2 extends AbstractVerticle {
  Logger log = LoggerFactory.getLogger(AccountVerticle2.class);

  private PgPool client;
  private AccountRepository accountRepository;

  @Override
  public void start(Promise<Void> startPromise) throws Exception {
    log.info("start");
    EventBus eventBus = vertx.eventBus();

    client = pool();
    accountRepository = new AccountRepository(client);
    eventBus.consumer("account.get", this::getAccount);
    eventBus.consumer("account.getAll", this::getAccounts);
    eventBus.consumer("account.add", this::addAccount);
  }

  private void getAccount(Message<JsonObject> msg) {
    log.info("getAccount");
    String id = msg.body().getString("id");
    accountRepository.getAccount(id).subscribe(account -> msg.reply(JsonObject.mapFrom(account)), failure -> msg.reply("Fail, "+failure.getMessage()));
  }

  private void getAccounts(Message<Object> msg) {
    log.info("getAccounts");
    accountRepository.getAccounts().subscribe(accounts -> msg.reply(jsonMap(accounts)), failure -> msg.reply("Fail, "+failure.getMessage()));
  }

  private void mix(Message<Object> msg) {
    log.info("getAccounts");
    Single<Account> account = accountRepository.getAccount("a");
    Single<List<Account>> accounts = accountRepository.getAccounts();
    Single<Account> id1 = accountRepository.addAccount("g", "ffff");
    Single<Account> id2 = accountRepository.addAccount("h", "gggggg");

    Single.zip(
            account,
            accounts,
            id1,
            id2,
            (a1, a2, i1, i2) -> {
              JsonObject obj = new JsonObject();
              obj.put("a", JsonObject.mapFrom(a1));
              obj.put("b", jsonMap(a2));
              obj.put("id1", JsonObject.mapFrom(i1));
              obj.put("id2", JsonObject.mapFrom(i2));
              return obj;
            })
        .subscribe(obj -> msg.reply(obj.encodePrettily()), failure -> msg.reply("Fail, "+failure.getMessage()));
  }

  private void addAccount(Message<Object> msg) {
    log.info("addAccount");
    JsonObject obj = (JsonObject) msg.body();
    String id = obj.getString("id");
    String name = obj.getString("name");
    accountRepository
        .addAccount(id, name)
        .subscribe(account -> msg.reply(JsonObject.mapFrom(account)), failure -> msg.reply("Fail, "+failure.getMessage()));
  }

  protected PgPool pool() {
    PgConnectOptions connectOptions = PgConnectOptions.fromUri(System.getenv("JDBC_DATABASE_URL"));

    // Pool options
    PoolOptions poolOptions = new PoolOptions().setMaxSize(5);

    // Create the pooled client
    return PgPool.pool(vertx, connectOptions, poolOptions);
  }

  protected <E> JsonArray jsonMap(List<E> list) {
    JsonArray jsonArr = new JsonArray();
    list.stream().map(JsonObject::mapFrom).forEach(jsonArr::add);
    return jsonArr;
  }
}
