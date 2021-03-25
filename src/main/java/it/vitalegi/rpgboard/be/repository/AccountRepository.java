package it.vitalegi.rpgboard.be.repository;

import io.reactivex.Observable;
import io.reactivex.Single;
import io.vertx.reactivex.pgclient.PgPool;
import io.vertx.reactivex.sqlclient.templates.SqlTemplate;
import it.vitalegi.rpgboard.be.data.Account;
import it.vitalegi.rpgboard.be.reactivex.data.Mappers;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collections;
import java.util.List;

public class AccountRepository {

  private static final String INSERT =
      "INSERT INTO Account (account_id, name) VALUES (#{account_id}, #{name}) RETURNING account_id, name;";
  private static final String UPDATE =
      "UPDATE Account SET account_id=#{id}, name=${name} WHERE id=#{account_id} RETURNING account_id, name;";
  private static final String FIND_BY_ACCOUNT_ID =
      "SELECT account_id, name FROM Account WHERE account_id =#{id};";
  private static final String FIND_ALL = "SELECT account_id, name FROM Account;";

  Logger log = LoggerFactory.getLogger(AccountRepository.class);
  private PgPool client;

  public AccountRepository(PgPool client) {
    this.client = client;
  }

  public Single<Account> addAccount(String accountId, String name) {
    return SqlTemplate.forQuery(client, INSERT)
        .mapTo(Mappers.ACCOUNT)
        .rxExecute(Account.map(accountId, name))
        .flatMapObservable(Observable::fromIterable)
        .singleOrError();
  }

  public Single<Account> updateAccount(String accountId, String name) {
    return SqlTemplate.forQuery(client, UPDATE)
        .mapTo(Mappers.ACCOUNT)
        .rxExecute(Account.map(accountId, name))
        .flatMapObservable(Observable::fromIterable)
        .singleOrError();
  }

  public Single<Account> getAccount(String accountId) {
    return SqlTemplate.forQuery(client, FIND_BY_ACCOUNT_ID)
        .mapTo(Mappers.ACCOUNT)
        .rxExecute(Collections.singletonMap("id", accountId))
        .flatMapObservable(Observable::fromIterable)
        .singleOrError();
  }

  public Single<List<Account>> getAccounts() {
    return SqlTemplate.forQuery(client, FIND_ALL)
        .mapTo(Mappers.ACCOUNT)
        .rxExecute(Collections.emptyMap())
        .flatMapObservable(Observable::fromIterable)
        .toList();
  }
}
