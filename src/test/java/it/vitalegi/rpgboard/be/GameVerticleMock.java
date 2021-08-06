package it.vitalegi.rpgboard.be;

import io.reactivex.Maybe;
import io.reactivex.functions.Function;
import io.vertx.reactivex.pgclient.PgPool;
import io.vertx.reactivex.sqlclient.SqlConnection;

public class GameVerticleMock extends GameVerticle {
  @Override
  protected PgPool getClient() {
    return null;
  }

  @Override
  protected <T> Maybe<T> tx(Function<SqlConnection, Maybe<T>> function) {
    try {
      return function.apply(null);
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  @Override
  protected <T> Maybe<T> cx(Function<SqlConnection, Maybe<T>> function) {
    try {
      return function.apply(null);
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }
}
