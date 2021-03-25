package it.vitalegi.rpgboard.be.reactivex.data;

import io.vertx.reactivex.sqlclient.templates.TupleMapper;

public class Mappers {

  public static final it.vitalegi.rpgboard.be.reactivex.data.AccountRowMapper ACCOUNT =
      it.vitalegi.rpgboard.be.reactivex.data.AccountRowMapper.newInstance(
          it.vitalegi.rpgboard.be.data.AccountRowMapper.INSTANCE);
}
