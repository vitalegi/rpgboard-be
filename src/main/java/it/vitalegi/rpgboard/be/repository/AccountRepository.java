package it.vitalegi.rpgboard.be.repository;

import io.reactivex.Observable;
import io.reactivex.Single;
import io.vertx.reactivex.pgclient.PgPool;
import io.vertx.reactivex.sqlclient.templates.SqlTemplate;
import io.vertx.reactivex.sqlclient.templates.TupleMapper;
import it.vitalegi.rpgboard.be.data.Account;
import it.vitalegi.rpgboard.be.reactivex.data.Mappers;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class AccountRepository {
  Logger log = LoggerFactory.getLogger(AccountRepository.class);
  private PgPool client;

  public AccountRepository(PgPool client) {
    this.client = client;
  }

  public Single<Account> addAccount(String accountId, String name) {
    return SqlTemplate.forQuery(
            client,
            "INSERT INTO Account (account_id, name) VALUES (#{account_id}, #{name}) RETURNING account_id, name;")
            .mapTo(Mappers.ACCOUNT)
            .rxExecute(Account.map(accountId, name))
            .flatMapObservable(Observable::fromIterable)
            .singleOrError();
  }

  public Single<Account> updateAccount(String accountId, String name) {
    return SqlTemplate.forQuery(
            client,
            "UPDATE Account SET account_id=#{id}, name=${name} WHERE id=#{account_id} RETURNING account_id, name;")
            .mapTo(Mappers.ACCOUNT)
            .rxExecute(Account.map(accountId, name))
            .flatMapObservable(Observable::fromIterable)
            .singleOrError();
  }

  public Single<Account> getAccount( String accountId) {
    return SqlTemplate.forQuery(
            client, "SELECT account_id, name FROM Account WHERE account_id =#{id};")
        .mapTo(Mappers.ACCOUNT)
        .rxExecute(Collections.singletonMap("id", accountId))
        .flatMapObservable(Observable::fromIterable)
        .singleOrError();
  }

  public Single<List<Account>> getAccounts() {
    return SqlTemplate.forQuery(client, "SELECT account_id, name FROM Account;")
        .mapTo(Mappers.ACCOUNT)
        .rxExecute(Collections.emptyMap())
        .flatMapObservable(Observable::fromIterable)
        .toList();
  }
}
