package it.vitalegi.rpgboard.be.data;

import io.vertx.codegen.annotations.DataObject;
import io.vertx.codegen.format.SnakeCase;
import io.vertx.core.json.JsonObject;
import io.vertx.sqlclient.templates.annotations.Column;
import io.vertx.sqlclient.templates.annotations.RowMapped;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@DataObject(generateConverter = true)
@RowMapped(formatter = SnakeCase.class)
public class Board {
  @Column(name = "board_id")
  UUID boardId;

  @Column(name = "game_id")
  UUID gameId;

  @Column(name = "name")
  String name;

  @Column(name = "is_active")
  Boolean active;

  public static Map<String, Object> map(UUID boardId, UUID gameId, String name, Boolean active) {
    Map<String, Object> map = new HashMap<>();
    map.put("board_id", boardId);
    map.put("game_id", gameId);
    map.put("name", name);
    map.put("is_active", active);
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
