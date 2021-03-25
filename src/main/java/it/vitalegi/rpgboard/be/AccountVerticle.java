package it.vitalegi.rpgboard.be;

import io.reactivex.Single;
import io.vertx.core.Promise;
import io.vertx.core.json.JsonObject;
import io.vertx.reactivex.core.AbstractVerticle;
import io.vertx.reactivex.core.Vertx;
import io.vertx.reactivex.core.eventbus.EventBus;
import io.vertx.reactivex.core.eventbus.Message;
import io.vertx.reactivex.pgclient.PgPool;
import it.vitalegi.rpgboard.be.data.Account;
import it.vitalegi.rpgboard.be.repository.AccountRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

public class AccountVerticle extends AbstractVerticle {
  Logger log = LoggerFactory.getLogger(AccountVerticle.class);

  private PgPool client;
  private AccountRepository accountRepository;

  @Override
  public void start(Promise<Void> startPromise) throws Exception {
    log.info("start");
    EventBus eventBus = vertx.eventBus();

    client = VertxUtil.pool(vertx);
    accountRepository = new AccountRepository(client);
    eventBus.consumer("account.get", this::getAccount);
    eventBus.consumer("account.getAll", this::getAccounts);
    eventBus.consumer("account.add", this::addAccount);
  }

  private void getAccount(Message<JsonObject> msg) {
    String id = msg.body().getString("id2");
    accountRepository
        .getAccount(id)
        .subscribe(account -> msg.reply(JsonObject.mapFrom(account)), VertxUtil.handleError(msg));
  }

  private void getAccounts(Message<Object> msg) {
    accountRepository
        .getAccounts()
        .subscribe(accounts -> msg.reply(VertxUtil.jsonMap(accounts)), VertxUtil.handleError(msg));
  }

  private void mix(Message<Object> msg) {
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
              obj.put("b", VertxUtil.jsonMap(a2));
              obj.put("id1", JsonObject.mapFrom(i1));
              obj.put("id2", JsonObject.mapFrom(i2));
              return obj;
            })
        .subscribe(obj -> msg.reply(obj.encodePrettily()), VertxUtil.handleError(msg));
  }

  private void addAccount(Message<Object> msg) {
    JsonObject obj = (JsonObject) msg.body();
    String id = obj.getString("id");
    String name = obj.getString("name");
    accountRepository
        .addAccount(id, name)
        .subscribe(account -> msg.reply(JsonObject.mapFrom(account)), VertxUtil.handleError(msg));
  }
}
