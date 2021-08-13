package it.vitalegi.rpgboard.be.reactivex.data;

public class Mappers {

  public static final it.vitalegi.rpgboard.be.reactivex.data.BoardRowMapper BOARD =
      it.vitalegi.rpgboard.be.reactivex.data.BoardRowMapper.newInstance(
          it.vitalegi.rpgboard.be.data.BoardRowMapper.INSTANCE);

  public static final it.vitalegi.rpgboard.be.reactivex.data.GameRowMapper GAME =
      it.vitalegi.rpgboard.be.reactivex.data.GameRowMapper.newInstance(
          it.vitalegi.rpgboard.be.data.GameRowMapper.INSTANCE);

  public static final it.vitalegi.rpgboard.be.reactivex.data.AssetRowMapper ASSET =
      it.vitalegi.rpgboard.be.reactivex.data.AssetRowMapper.newInstance(
          it.vitalegi.rpgboard.be.data.AssetRowMapper.INSTANCE);

  public static final it.vitalegi.rpgboard.be.reactivex.data.GameItemRowMapper GAME_ITEM =
      it.vitalegi.rpgboard.be.reactivex.data.GameItemRowMapper.newInstance(
          it.vitalegi.rpgboard.be.data.GameItemRowMapper.INSTANCE);

  public static final it.vitalegi.rpgboard.be.reactivex.data.GamePlayerRowMapper GAME_PLAYER =
      it.vitalegi.rpgboard.be.reactivex.data.GamePlayerRowMapper.newInstance(
          it.vitalegi.rpgboard.be.data.GamePlayerRowMapper.INSTANCE);

  public static final it.vitalegi.rpgboard.be.reactivex.data.GamePlayerRoleRowMapper
      GAME_PLAYER_ROLE =
          it.vitalegi.rpgboard.be.reactivex.data.GamePlayerRoleRowMapper.newInstance(
              it.vitalegi.rpgboard.be.data.GamePlayerRoleRowMapper.INSTANCE);

  public static final it.vitalegi.rpgboard.be.reactivex.data.UserRowMapper USER =
          it.vitalegi.rpgboard.be.reactivex.data.UserRowMapper.newInstance(
                  it.vitalegi.rpgboard.be.data.UserRowMapper.INSTANCE);
  public static final it.vitalegi.rpgboard.be.reactivex.data.BoardElementRowMapper BOARD_ELEMENT =
          it.vitalegi.rpgboard.be.reactivex.data.BoardElementRowMapper.newInstance(
                  it.vitalegi.rpgboard.be.data.BoardElementRowMapper.INSTANCE);

}
