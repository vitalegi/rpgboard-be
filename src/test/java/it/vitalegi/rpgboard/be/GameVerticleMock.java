package it.vitalegi.rpgboard.be;

import io.reactivex.Maybe;
import io.reactivex.functions.Function;
import io.vertx.reactivex.sqlclient.SqlConnection;

public class GameVerticleMock extends GameVerticle {
  private static final SqlConnection CONN = new SqlConnection(null);

  @Override
  protected <T> Maybe<T> tx(Function<SqlConnection, Maybe<T>> function) {
    try {
      return function.apply(CONN);
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  @Override
  protected <T> Maybe<T> cx(Function<SqlConnection, Maybe<T>> function) {
    try {
      return function.apply(CONN);
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }
}
