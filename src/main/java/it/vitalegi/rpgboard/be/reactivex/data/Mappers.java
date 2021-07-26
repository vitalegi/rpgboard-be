package it.vitalegi.rpgboard.be.reactivex.data;

public class Mappers {

  public static final it.vitalegi.rpgboard.be.reactivex.data.AccountRowMapper ACCOUNT =
      it.vitalegi.rpgboard.be.reactivex.data.AccountRowMapper.newInstance(
          it.vitalegi.rpgboard.be.data.AccountRowMapper.INSTANCE);
  public static final it.vitalegi.rpgboard.be.reactivex.data.BoardRowMapper BOARD =
      it.vitalegi.rpgboard.be.reactivex.data.BoardRowMapper.newInstance(
          it.vitalegi.rpgboard.be.data.BoardRowMapper.INSTANCE);
  public static final it.vitalegi.rpgboard.be.reactivex.data.GameRowMapper GAME =
      it.vitalegi.rpgboard.be.reactivex.data.GameRowMapper.newInstance(
          it.vitalegi.rpgboard.be.data.GameRowMapper.INSTANCE);
}
