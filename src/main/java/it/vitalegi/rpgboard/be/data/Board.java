package it.vitalegi.rpgboard.be.data;

import io.vertx.codegen.annotations.DataObject;
import io.vertx.codegen.format.SnakeCase;
import io.vertx.core.json.JsonObject;
import io.vertx.sqlclient.templates.annotations.Column;
import io.vertx.sqlclient.templates.annotations.RowMapped;
import it.vitalegi.rpgboard.be.util.PreparedStatementBuilder;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@DataObject(generateConverter = true)
@RowMapped(formatter = SnakeCase.class)
public class Board {
  public static final String BOARD_ID = "board_id";
  public static final String GAME_ID = "game_id";
  public static final String NAME = "name";
  public static final String IS_ACTIVE = "is_active";
  public static final PreparedStatementBuilder BUILDER =
      PreparedStatementBuilder.init()
          .tableName("RPG_Board")
          .fields(BOARD_ID, GAME_ID, NAME, IS_ACTIVE);

  @Column(name = BOARD_ID)
  UUID boardId;

  @Column(name = GAME_ID)
  UUID gameId;

  @Column(name = NAME)
  String name;

  @Column(name = IS_ACTIVE)
  Boolean active;

  public static Map<String, Object> map(UUID boardId, UUID gameId, String name, Boolean active) {
    Map<String, Object> map = new HashMap<>();
    map.put(BOARD_ID, boardId);
    map.put(GAME_ID, gameId);
    map.put(NAME, name);
    map.put(IS_ACTIVE, active);
    return map;
  }

  public JsonObject toJson() {
    JsonObject json = new JsonObject();
    BoardConverter.toJson(this, json);
    return json;
  }

  public UUID getBoardId() {
    return boardId;
  }

  public void setBoardId(UUID boardId) {
    this.boardId = boardId;
  }

  public UUID getGameId() {
    return gameId;
  }

  public void setGameId(UUID gameId) {
    this.gameId = gameId;
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public Boolean getActive() {
    return active;
  }

  public void setActive(Boolean active) {
    this.active = active;
  }

  @Override
  public String toString() {
    return "Board{"
        + "boardId="
        + boardId
        + ", gameId="
        + gameId
        + ", name='"
        + name
        + '\''
        + ", active="
        + active
        + '}';
  }
}
